package network.anticheat.detections.movement;

import network.anticheat.AntiCheatBase;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.PerformanceHandler;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class FlyFix extends AntiCheatBase {
	private Map<String, Integer> delay = null;
	private Map<String, Integer> floating = null;
	private Map<String, Integer> floatingViolations = null;
	private Map<String, Integer> flying = null;
	private List<Material> ignores = null;

	public FlyFix() {
		super("Fly");
		delay = new HashMap<String, Integer>();
		floating = new HashMap<String, Integer>();
		floatingViolations = new HashMap<String, Integer>();
		flying = new HashMap<String, Integer>();
		ignores = new ArrayList<Material>();
		ignores.add(Material.LADDER);
		ignores.add(Material.WATER);
		ignores.add(Material.STATIONARY_WATER);
		ignores.add(Material.LAVA);
		ignores.add(Material.STATIONARY_LAVA);
		EventUtil.register(this);
	}

	private boolean checkForFly(Player player) {
		if(PerformanceHandler.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 3 && !player.isFlying() && player.getVehicle() == null) {
			if(notIgnored(player) && player.getTicksLived() >= 20 * 3 && player.getWalkSpeed() == 0.2f) {
				return true;
			}
		}
		return false;
	}

	private boolean isOnIgnored(Player player) {
		if(ignores.contains(player.getLocation().getBlock().getType())) {
			return true;
		}
		return false;
	}

	private boolean onEdgeOfBlock(Player player, boolean checkBelow) {
		if(isOnIgnored(player)) {
			return true;
		}
		for(int a = checkBelow ? -1 : 0; a <= 0; ++a) {
			Block block = player.getLocation().getBlock().getRelative(0, a, 0);
			for(int x = -1; x <= 1; ++x) {
				for(int z = -1; z <= 1; ++z) {
					if(block.getRelative(x, 0, z).getType() != Material.AIR) {
						delay.put(player.getName(), 30);
						return true;
					}
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1 && isEnabled()) {
			try {
				Iterator<String> iterator = delay.keySet().iterator();
				while(iterator.hasNext()) {
					String name = iterator.next();
					int counter = delay.get(name);
					if(--counter <= 0) {
						iterator.remove();
					} else {
						delay.put(name, counter);
					}
				}
			} catch (ConcurrentModificationException e) {

			}
		} else if(ticks == 20 && isEnabled()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(!delay.containsKey(player.getName()) && checkForFly(player) && !onEdgeOfBlock(player, true)) {
							int counter = 0;
							if(floating.containsKey(player.getName())) {
								counter = floating.get(player.getName());
							}
							if(++counter >= 2) {
								Location location = player.getLocation();
								while(location.getBlock().getType() == Material.AIR) {
									location.setY(location.getBlockY() - 1);
								}
								player.teleport(location.add(0, 1, 0));
								int violation = 0;
								if(floatingViolations.containsKey(player.getName())) {
									violation = floatingViolations.get(player.getName());
								}
								floatingViolations.put(player.getName(), ++violation);
								if(violation >= 2) {
//									UUID uuid = player.getUniqueId();
//									if(DB.NETWORK_ANTI_CHEAT_FLOATING_KICKS.isUUIDSet(uuid)) {
//										int amount = DB.NETWORK_ANTI_CHEAT_FLOATING_KICKS.getInt("uuid", uuid.toString(), "kicks") + 1;
//										DB.NETWORK_ANTI_CHEAT_FLOATING_KICKS.updateInt("kicks", amount, "uuid", uuid.toString());
//									} else {
//										DB.NETWORK_ANTI_CHEAT_FLOATING_KICKS.insert("'" + uuid.toString() + "', '1'");
//									}
									new DelayedTask(new Runnable() {
										@Override
										public void run() {
											player.kickPlayer(ChatColor.RED + "Kicked for floating");
										}
									});
								}
							} else {
								floating.put(player.getName(), counter);
							}
						} else {
							floating.put(player.getName(), -1);
						}
					}
				}
			});
		}
	}

	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			Vector vel = player.getVelocity();
			double x = vel.getX() < 0 ? vel.getX() * -1 : vel.getX();
			double y = vel.getY() < 0 ? vel.getY() * -1 : vel.getY();
			double z = vel.getZ() < 0 ? vel.getZ() * -1 : vel.getZ();
			double value = x + y + z;
			delay.put(player.getName(), ((int) value * 10));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			delay.put(player.getName(), 40);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			delay.put(player.getName(), 40);
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(isEnabled()) {
			delay.put(event.getPlayer().getName(), 20);
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			delay.put(player.getName(), 20);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			double y = player.getVelocity().getY();
			if(y % 1 != 0) {
				String vel = y + "";
				vel = vel.substring(3);
				if(vel.startsWith("199999")) {
					delay.put(player.getName(), 20);
					return;
				}
			}
			y = player.getLocation().getY();
			Block block = player.getLocation().getBlock();
			if((y % 1 == 0 || y % .5 == 0) && (block.getType() != Material.AIR || block.getRelative(0, -1, 0).getType() != Material.AIR)) {
				delay.put(player.getName(), 20);
				return;
			}
			Location to = event.getTo();
			Location from = event.getFrom();
			if(to.getY() < from.getY()) {
				floating.put(player.getName(), -3);
				delay.put(player.getName(), 20);
				return;
			}
			if(!delay.containsKey(player.getName()) && checkForFly(player) && !onEdgeOfBlock(player, true)) {
				int counter = 0;
				if(flying.containsKey(player.getName())) {
					counter = flying.get(player.getName());
				}
				if(++counter >= 10) {
//					UUID uuid = player.getUniqueId();
//					if(DB.NETWORK_ANTI_CHEAT_FLY_KICKS.isUUIDSet(uuid)) {
//						int amount = DB.NETWORK_ANTI_CHEAT_FLY_KICKS.getInt("uuid", uuid.toString(), "kicks") + 1;
//						DB.NETWORK_ANTI_CHEAT_FLY_KICKS.updateInt("kicks", amount, "uuid", uuid.toString());
//					} else {
//						DB.NETWORK_ANTI_CHEAT_FLY_KICKS.insert("'" + uuid.toString() + "', '1'");
//					}
					player.kickPlayer(org.bukkit.ChatColor.RED + "Kicked for flying");
					//ban(player);
				} else {
					flying.put(player.getName(), counter);
				}
				return;
			}
			if(flying.containsKey(player.getName())) {
				int counter = flying.get(player.getName()) - 1;
				if(counter <= 0) {
					flying.remove(player.getName());
				} else {
					flying.put(player.getName(), counter);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerleave(PlayerLeaveEvent event) {
		floatingViolations.remove(event.getPlayer().getName());
	}
}
