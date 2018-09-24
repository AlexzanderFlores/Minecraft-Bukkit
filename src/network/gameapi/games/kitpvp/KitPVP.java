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
import network.server.CPSDetector;
import network.server.DB;
import network.server.DailyRewards;
import network.server.ServerLogger;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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
		new SpectatorHandler().createNPC(new Location(world, -13.5, 79, -9.5), world.getSpawnLocation());
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY, DB.PLAYERS_STATS_KIT_PVP_WEEKLY);
		new Events();
		new TemporaryFireUtil(20 * 5);
		new BelowNameHealthScoreboardUtil();
		new Shop(-1.5, 79, -9.5);
		new SpawnHandler();
		new KillstreakHandler(6, 78, -4);
		new AutoRegenHandler(-1.5, 79, 2.5);
		new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData());
		new CPSDetector(new Location(world, -13.5, 79, 2.5), world.getSpawnLocation());
		CoinsHandler.setKillCoins(5);
		world.setGameRuleValue("keepInventory", "true");

		for(Location location : new Location [] {
				new Location(world, -7.5, 77, -24.5),
				new Location(world, 13.5, 77, -3.5),
				new Location(world, -7.5, 77, 17.5),
				new Location(world, -28, 77, -3.5),
		}) {
			new DailyRewards(location, world.getSpawnLocation());
		}

		Location location = new Location(world, 107.5, 33, -8.5, -305.0f, -1.2f);
		new NPCEntity(EntityType.SKELETON, "&eMiner John &7(Click)", location, Material.DIAMOND_PICKAXE) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, AccountHandler.Ranks.PRO_PLUS.getPrefix() + "MinerJohn: Back in my day this whole cave was filled with emeralds. We sure got all of them over the last few years.");
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
