package network.gameapi.games.kitpvp;

import network.Network.Plugins;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.gameapi.TemporaryFireUtil;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.games.kitpvp.shop.Shop;
import network.player.CoinsHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.server.DB;
import network.server.ServerLogger;
import network.server.servers.hub.RecentSupporters;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KitPVP extends ProPlugin {
	public KitPVP() {
		super("KitPVP");
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowPlayerInteraction(true);
		setAllowBowShooting(true);
		setAllowInventoryClicking(true);
		setFlintAndSteelUses(2);
		setAllowItemSpawning(true);
		setAllowItemBreaking(false);
		resetWorld();
		World world = Bukkit.getWorlds().get(0);
		new ServerLogger();
		new SpectatorHandler().createNPC(new Location(world, -13.5, 79, -9.5));
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY, DB.PLAYERS_STATS_KIT_PVP_WEEKLY);
		new Events();
		new TemporaryFireUtil(20 * 5);
		new BelowNameHealthScoreboardUtil();
		new Shop(-1.5, 79, -9.5);
		new SpawnHandler();
		new KillstreakHandler(6, 78, -4);
		new AutoRegenHandler(-1.5, 79, 2.5);
		new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData());
		CoinsHandler.setKillCoins(5);
		world.setGameRuleValue("keepInventory", "true");

		Location location = new Location(world, 107.5, 33, -8.5, -305.0f, -1.2f);
		new NPCEntity(EntityType.SKELETON, "&eMiner John &7(Click)", location, Material.DIAMOND_PICKAXE) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, AccountHandler.Ranks.PRO_PLUS.getPrefix() + "MinerJohn: Back in my day this whole cave was filled with emeralds. We sure got all of them over the last few years.");
			}
		};

		Location [] locations = new Location [] {
				new Location(world, 32, 87, -11),
				new Location(world, 31, 87, -5),
				new Location(world, 32, 87, 1)
		};

		Vector[] nameDistances = new Vector [] {
				new Vector(-1.5, -6, 1),
				new Vector(-1.5, -6, 1),
				new Vector(-1.5, -6, 1),

				new Vector(-1.5, -6.3, 1),
				new Vector(-1.5, -6.3, 1),
				new Vector(-1.5, -6.3, 1)
		};

		new RecentSupporters(locations, nameDistances, new String [] {
				"Rank #1 - Monthly",
				"Rank #1 - Lifetime",
				"Rank #1 - Weekly"
		}, new Color(0x312117), 5) {
			@Override
			public List<UUID> getUUIDs() {
				List<UUID> uuids = new ArrayList<UUID>();

				try {
					String monthly = DB.PLAYERS_STATS_KIT_PVP_MONTHLY.getOrdered("kills", "uuid", 1, true).get(0);
					String lifetime = DB.PLAYERS_STATS_KIT_PVP.getOrdered("kills", "uuid", 1, true).get(0);
					String weekly = DB.PLAYERS_STATS_KIT_PVP_WEEKLY.getOrdered("kills", "uuid", 1, true).get(0);

					uuids.add(UUID.fromString(monthly));
					uuids.add(UUID.fromString(lifetime));
					uuids.add(UUID.fromString(weekly));
				} catch(Exception e) {
					e.printStackTrace();
					uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
					uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
					uuids.add(UUID.fromString("ec286bfe-04ef-40d5-ab4c-e8d50148a499"));
				}

				return uuids;
			}
		};
	}
	
	@Override
	public void disable() {
//		String container = "/root/" + Network.getServerName().toLowerCase() + "/";
//		String name = "kitpvp";
//		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
//		FileHandler.delete(new File(container + "/" + name));
//		FileHandler.copyFolder(new File("/root/resources/maps/" + name), new File(container + "/" + name));
	}
}
