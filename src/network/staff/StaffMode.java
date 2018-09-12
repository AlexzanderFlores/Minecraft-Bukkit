package network.staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import network.Network;
import network.ProPlugin;
import network.Network.Plugins;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerAFKEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerStaffModeEvent;
import network.customevents.player.PlayerStaffModeEvent.StaffModeEventType;
import network.gameapi.SpectatorHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.PerformanceHandler;
import network.server.util.EventUtil;
import npc.util.DelayedTask;

public class StaffMode implements Listener {
	private static List<String> vanished = null;
	private static Map<String, String> watching = null;
	
	public StaffMode() {
		new CommandBase("sm", 0, 3, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					toggle(player);
					return true;
				} else if(arguments.length == 1 && arguments[0].equalsIgnoreCase("watch")) {
					if(watching != null && watching.containsKey(player.getName())) {
						MessageHandler.sendMessage(player, "You are no longer watching " + watching.get(player.getName()));
						watching.remove(player.getName());
					} else {
						MessageHandler.sendMessage(player, "&cYou are currently not watching anyone");
					}
					return true;
				} else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("watch")) {
					if(contains(player)) {
						Player target = ProPlugin.getPlayer(arguments[1]);
						if(target == null) {
							MessageHandler.sendMessage(player, "&c" + arguments[1] + " is not online");
						} else {
							player.teleport(target);
							if(watching == null) {
								watching = new HashMap<String, String>();
							}
							watching.put(player.getName(), target.getName());
							MessageHandler.sendMessage(player, "You are now watching " + target.getName());
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou must be in staff mode for this");
					}
					return true;
				}
				player.sendMessage("/sm");
				player.sendMessage("/sm watch [name]");
				return true;
			}
		}.enableDelay(2).setRequiredRank(Ranks.TRIAL);
		EventUtil.register(this);
	}
	
	public static void toggle(final Player player) {
		String prefix = AccountHandler.getPrefix(player);
		if(contains(player)) {
			PlayerStaffModeEvent staffModeEvent = new PlayerStaffModeEvent(player, StaffModeEventType.DISABLE);
			Bukkit.getPluginManager().callEvent(staffModeEvent);
			if(!staffModeEvent.isCancelled()) {
				if(Network.getMiniGame() == null && SpectatorHandler.isEnabled() && SpectatorHandler.contains(player)) {
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							SpectatorHandler.getInstance().remove(player);
						}
					});
				}
				vanished.remove(player.getName());
				if(watching != null) {
					watching.remove(player.getName());
				}
				Plugins plugin = Network.getPlugin();
				if(plugin == Plugins.HUB) {
					for(Player online : Bukkit.getOnlinePlayers()) {
						online.showPlayer(player);
					}
				}
				String location = Network.getServerName();
				if(DB.STAFF_ONLINE.isUUIDSet(player.getUniqueId())) {
					DB.STAFF_ONLINE.updateString("prefix", prefix, "uuid", player.getUniqueId().toString());
					DB.STAFF_ONLINE.updateString("server", location, "uuid", player.getUniqueId().toString());
				} else {
					DB.STAFF_ONLINE.insert("'" + player.getUniqueId().toString() + "', '" + prefix + "', '" + location + "'");
				}
				MessageHandler.sendMessage(player, "Staff mode disabled");
				player.teleport(player.getWorld().getSpawnLocation());
			}
		} else {
			PlayerStaffModeEvent staffModeEvent = new PlayerStaffModeEvent(player, StaffModeEventType.ENABLE);
			Bukkit.getPluginManager().callEvent(staffModeEvent);
			if(!staffModeEvent.isCancelled()) {
				if(SpectatorHandler.isEnabled() && !SpectatorHandler.contains(player)) {
					if(Network.getMiniGame() == null) {
						SpectatorHandler.getInstance().add(player);
					} else {
						MessageHandler.sendMessage(player, "&cYou must be a spectator to do this");
						return;
					}
				}
				if(vanished == null) {
					vanished = new ArrayList<String>();
				}
				vanished.add(player.getName());
				Plugins plugin = Network.getPlugin();
				if(plugin == Plugins.HUB) {
					for(Player online : Bukkit.getOnlinePlayers()) {
						online.hidePlayer(player);
					}
				}
				if(DB.STAFF_ONLINE.isUUIDSet(player.getUniqueId())) {
					DB.STAFF_ONLINE.updateString("prefix", prefix, "uuid", player.getUniqueId().toString());
					DB.STAFF_ONLINE.updateString("server", "VANISHED", "uuid", player.getUniqueId().toString());
				} else {
					DB.STAFF_ONLINE.insert("'" + player.getUniqueId() + "', '" + prefix + "', 'VANISHED'");
				}
				MessageHandler.sendMessage(player, "Staff mode enabled");
			}
		}
	}
	
	public static boolean contains(Player player) {
		return vanished != null && vanished.contains(player.getName());
	}
	
	@EventHandler
	public void onPlayerAFK(PlayerAFKEvent event) {
		Player player = event.getPlayer();
		if(Network.getProPlugin().getAutoVanishStaff() && Ranks.isStaff(player)) {
			if(event.getAFK() && !contains(player) && !Ranks.OWNER.hasRank(player)) {
				toggle(player);
				if(Network.getPlugin() == Plugins.HUB && event.getAFK()) {
					MessageHandler.alert(AccountHandler.getPrefix(player) + " &ehas been vanished due to being AFK");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(vanished != null) {
			for(String name : vanished) {
				event.getPlayer().hidePlayer(ProPlugin.getPlayer(name));
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(contains(event.getPlayer())) {
			vanished.remove(event.getPlayer().getName());
			if(watching != null) {
				watching.remove(event.getPlayer().getName());
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			if(watching != null) {
				for(String staffName : watching.keySet()) {
					Player staff = ProPlugin.getPlayer(staffName);
					Player player = ProPlugin.getPlayer(watching.get(staffName));
					if(staff != null && player != null) {
						MessageHandler.sendLine(staff, "&9");
						MessageHandler.sendMessage(staff, "&cStaff Mode - Server Data Displayer");
						MessageHandler.sendMessage(staff, staff.getName() + "&a's ping: &e" + PerformanceHandler.getPing(staff));
						MessageHandler.sendMessage(staff, player.getName() + "&a's ping: &e" + PerformanceHandler.getPing(player));
						MessageHandler.sendMessage(staff, "Server TPS: &e" + PerformanceHandler.getTicksPerSecond());
						MessageHandler.sendMessage(staff, "Used Memory: &e" + PerformanceHandler.getMemory() + "%");
						MessageHandler.sendLine(staff, "&9");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
		if(event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(vanished != null && vanished.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(vanished != null && vanished.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(vanished != null && vanished.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(event.getCause() == TeleportCause.PLUGIN && contains(player)) {
			PlayerStaffModeEvent staffModeEvent = new PlayerStaffModeEvent(player, StaffModeEventType.TELEPORT, event.getTo());
			Bukkit.getPluginManager().callEvent(staffModeEvent);
			if(staffModeEvent.isCancelled()) {
				event.setCancelled(true);
			}
		}
	}
}
