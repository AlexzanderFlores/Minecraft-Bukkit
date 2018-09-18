package network.server.servers.hub;

import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerRankChangeEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.DB;
import network.server.effects.images.DisplaySkin;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Events implements Listener {
	private static Random random = null;
	private static Map<String, SidebarScoreboardUtil> sidebars = null;
	private static int players = 0;
	private static int oldPlayers = players;
	
	public Events() {
		random = new Random();
		Bukkit.getWorlds().get(0).setSpawnLocation(1684, 6, -1280);
		sidebars = new HashMap<String, SidebarScoreboardUtil>();
		updateSkins();
		EventUtil.register(this);
	}
	
	public static Location getSpawn() {
		int range = 6;
		Location location = Bukkit.getWorlds().get(0).getSpawnLocation();
		location.setYaw(-180.0f);
		location.setPitch(0.0f);
		location.setX(location.getX() + (random.nextBoolean() ? random.nextInt(range) : random.nextInt(range) * -1));
		location.setZ(location.getZ() + (random.nextBoolean() ? random.nextInt(range) : random.nextInt(range) * -1));
		return location;
	}
	
	public static void giveSidebar(Player player) {
		SidebarScoreboardUtil sidebar = new SidebarScoreboardUtil(" &6IP TBA ") {
			@Override
			public void update(Player player) {
				if(oldPlayers != players) {
					removeScore(8);
				}
				Ranks rank = AccountHandler.getRank(player);
				String rankString = rank == Ranks.PLAYER ? "&7None &b/buy" : rank.getPrefix();
				String current = getText(5);
				if(current != null && !ChatColor.stripColor(rankString).equals(ChatColor.stripColor(current))) {
					removeScore(5);
				}
				setText(new String [] {
					" ",
					"&eFollow us!",
					"&b/socialMedia",
					"  ",
					"&ePlayers",
					"&b" + players,
					"   ",
					"&eRank",
					rankString,
					"    ",
					"&eServer",
					"&bHUB" +  + HubBase.getHubNumber(),
					"     ",
				});
				super.update(player);
			}
		};
		sidebar.update(player);
		sidebars.put(player.getName(), sidebar);
	}
	
	public static void removeSidebar(Player player) {
		String name = player.getName();
		if(sidebars.containsKey(name)) {
			sidebars.get(name).remove();
			sidebars.remove(name);
		}
	}

	private void updateSkins() {
		World world = Bukkit.getWorlds().get(0);

		Map<String, Location []> locations = new HashMap<String, Location []>();

		locations.put("RecentCustomer", new Location [] {
				new Location(world, 1689, 5, -1260),
				new Location(world, 1687, 8, -1260)
		});

		locations.put("RecentVoter", new Location [] {
				new Location(world, 1685, 5, -1260),
				new Location(world, 1683, 8, -1260)
		});

		locations.put("RecentlyJoinedDiscord", new Location [] {
				new Location(world, 1681, 5, -1260),
				new Location(world, 1679, 8, -1260),
		});

		List<String> recentCustomer = DB.PLAYERS_CUSTOMERS.getOrdered("id", "uuid", 1, true);
		List<String> recentVoter = DB.PLAYERS_RECENT_VOTER.getOrdered("id", "uuid", 1, true);
		List<String> recentDiscord = DB.PLAYERS_DISCORD.getOrdered("id", "uuid", 1, true);

		if(recentCustomer != null && !recentCustomer.isEmpty()) {
			new DisplaySkin(
					"RecentCustomer",
					locations,
					UUID.fromString(recentCustomer.get(0)),
					new Color(0x312117)
			) {
				@Override
				public void interact(Player player) {
					player.chat("/buy");
				}
			}.display();
		}

		if(recentVoter != null && !recentVoter.isEmpty()) {
			new DisplaySkin(
					"RecentVoter",
					locations,
					UUID.fromString(recentVoter.get(0)),
					new Color(0x312117)
			) {
				@Override
				public void interact(Player player) {
					player.chat("/vote");
				}
			}.display();
		}

		if(recentDiscord != null && !recentDiscord.isEmpty()) {
			new DisplaySkin(
					"RecentlyJoinedDiscord",
					locations,
					UUID.fromString(recentDiscord.get(0)),
					new Color(0x312117)
			) {
				@Override
				public void interact(Player player) {
					player.chat("/discord");
				}
			}.display();
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if(Ranks.PRO.hasRank(player)) {
			player.setAllowFlight(true);
		}
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack [] {});
		player.getInventory().setHeldItemSlot(0);
		player.teleport(getSpawn());
		giveSidebar(player);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		removeSidebar(event.getPlayer());
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					int players = 0;
					for(String server : DB.NETWORK_POPULATIONS.getAllStrings("server")) {
						players += DB.NETWORK_POPULATIONS.getInt("server", server, "population");
					}
					oldPlayers = Events.players;
					Events.players = players;
				}
			});
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(sidebars.containsKey(player.getName())) {
					sidebars.get(player.getName()).update(player);
				}
			}
		} else if(ticks == 20 * 60 * 5) {
			updateSkins();
		}
	}
	
	@EventHandler
	public void onPlayerRankChange(final PlayerRankChangeEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();
				if(sidebars.containsKey(player.getName())) {
					sidebars.get(player.getName()).update(player);
					if(event.getRank() == Ranks.PLAYER && player.getAllowFlight()) {
						player.setFlying(false);
						player.setAllowFlight(false);
					} else if(event.getRank() != Ranks.PLAYER && !player.getAllowFlight()) {
						player.setAllowFlight(true);
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID) {
			event.getEntity().teleport(getSpawn());
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}
}
