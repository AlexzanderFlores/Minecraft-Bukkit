package network.gameapi.games.onevsones;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.Network;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.gameapi.competitive.EloHandler;
import network.gameapi.competitive.EloRanking;
import network.gameapi.competitive.EloRanking.EloRank;
import network.gameapi.competitive.StatDisplayer;
import network.gameapi.games.onevsones.kits.*;
import network.gameapi.uhc.GoldenHead;
import network.player.MessageHandler;
import network.player.TeamScoreboardHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.CPSDetector;
import network.server.CommandBase;
import network.server.DB;
import network.server.ServerLogger;
import network.server.tasks.DelayedTask;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnevsOnes extends ProPlugin {
	private static String oldPlayerCount = null;
	
	public OnevsOnes() {
		super("1v1");
		setAllowEntityDamage(true);
        setAllowEntityDamageByEntities(true);
        setAllowPlayerInteraction(true);
        setAllowBowShooting(true);
        setAllowInventoryClicking(true);
        setFlintAndSteelUses(2);
        setAllowEntityCombusting(true);
        setAllowInventoryClicking(true);
        setAllowItemSpawning(true);
        setAutoVanishStaff(true);
		resetWorld();
        World world = Bukkit.getWorlds().get(0);
        Location target = new Location(world, 0.5, 12, -30.5);
		new ServerLogger();
//		new StatsHandler(DB.PLAYERS_STATS_ONE_VS_ONE, DB.PLAYERS_STATS_ONE_VS_ONE_MONTHLY, DB.PLAYERS_STATS_ONE_VS_ONE_WEEKLY);
		new LobbyHandler();
        new QueueHandler();
        new BattleHandler();
        new MapProvider(world);
        new SpectatorHandler().createNPC(new Location(world, 8.5, 13, -43.5), target);
        new PrivateBattleHandler();
        new HotbarEditor();
        new EloHandler(DB.PLAYERS_ONE_VS_ONE_ELO, 1400);
        new ServerLogger();
        //Arrays.asList(ImageMap.getItemFrame(world, -16, 10, -34))
        new EloRanking(new ArrayList<ItemFrame>(), DB.PLAYERS_ONE_VS_ONE_ELO, DB.PLAYERS_ONE_VS_ONE_RANKED);
        new CPSDetector(new Location(world, -20.5, 13, -24.5), target);
        new GoldenHead();
        new RankedHandler();
        new TeamMatchHandler(new Location(world, 3.5, 13, -44.5));
        new OnDemandTournaments(new Location(world, -2.5, 13, -44.5));
        new MonthlyTournaments(new Location(world, -7.5, 13, -43.5));
        new DelayedTask(new Runnable() {
			@Override
			public void run() {
				List<Location> locations = Arrays.asList(
					new Location(world, -5.5, 18.5, -47),
					new Location(world, 0.5, 18.5, -49),
					new Location(world, 6.5, 18.5, -47)
				);
				new StatDisplayer(locations);
				List<Hologram> holograms = new ArrayList<Hologram>();
				Location [] hologramLocation = new Location [] {
						new Location(world, -14.5, 16, -38.5),
						new Location(world, -14.5, 15.5, -38.5),
						new Location(world, -14.5, 15, -38.5),
						new Location(world, -14.5, 14.5, -38.5),
						new Location(world, -14.5, 14, -38.5)
				};
				for(int a = 0; a < 3; ++a) {
					holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 17.25, -38.5), StringUtil.color("&e&nElo ranks are based off of your percentile")));
					holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 16.75, -38.5), StringUtil.color("&e&nof elo compared to other players.&b /elo")));
					holograms.add(HologramAPI.createHologram(hologramLocation[0], StringUtil.color(EloRank.DIAMOND.getPrefix() + " &aTop " + EloRank.DIAMOND.getPercentRange() + " of players")));
					holograms.add(HologramAPI.createHologram(hologramLocation[1], StringUtil.color(EloRank.PLATINUM.getPrefix() + " &aTop " + EloRank.PLATINUM.getPercentRange() + " of players")));
					holograms.add(HologramAPI.createHologram(hologramLocation[2], StringUtil.color(EloRank.GOLD.getPrefix() + " &aTop " + EloRank.GOLD.getPercentRange() + " of players")));
					holograms.add(HologramAPI.createHologram(hologramLocation[3], StringUtil.color(EloRank.SILVER.getPrefix() + " &aTop " + EloRank.SILVER.getPercentRange() + " of players")));
					holograms.add(HologramAPI.createHologram(hologramLocation[4], StringUtil.color(EloRank.BRONZE.getPrefix() + " &aTop " + EloRank.BRONZE.getPercentRange() + " of players")));
				}
				for(Hologram hologram : holograms) {
					hologram.spawn();
				}
			}
		}, 20);

        oldPlayerCount = "";

        Network.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
        	@Override
        	public void update(Player player) {
				int size = ProPlugin.getPlayers().size();
        		String playerCount = "&b" + size + " &7/&b " + Network.getMaxPlayers();
        		if(!oldPlayerCount.equals(playerCount)) {
        			oldPlayerCount = playerCount;
        			removeScore(8);
        		}

				setText(new String [] {
					" ",
					"&ePlaying",
					playerCount,
					"  ",
					"&eQueue Times",
					Ranks.PLAYER.getColor() + "Default: &b5s",
					Ranks.VIP.getColor() + "VIP: &b1s /buy",
					"     ",
					"&aIP TBA",
					"      ",
				});
				super.update(player);
        	}
        });
        new BelowNameHealthScoreboardUtil();
        new TeamScoreboardHandler();
        // Kits
        new SurvivalGames();
        new Archer();
        new BuildUHC();
        new NoDebuff();
        new Skywars();
        new SpeedUHC();

        new CommandBase("testDist", 4,true) {
        	@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				int x1 = Integer.valueOf(arguments[0]);
				int y1 = 12;
				int z1 = Integer.valueOf(arguments[1]);

				int x2 = Integer.valueOf(arguments[2]);
				int y2 = 12;
				int z2 = Integer.valueOf(arguments[3]);

				Player player = (Player) sender;
				World world = player.getWorld();

				Location loc1 = new Location(world, x1, y1, z1);
				Location loc2 = new Location(world, x2, y2, z2);

				MessageHandler.sendMessage(player, "Distance = " + loc1.distance(loc2));
        		return true;
			}
		};
	}
	
	@Override
    public void disable() {
        super.disable();
//        String container = "/root/" + Network.getServerName().toLowerCase() + "/";
//        Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
//        File newWorld = new File("/root/resources/maps/1v1s");
//        if(newWorld.exists() && newWorld.isDirectory()) {
//            FileHandler.delete(new File(container + "/1v1s"));
//            FileHandler.copyFolder(newWorld, new File(container + "/1v1s"));
//        }
    }
}
