package network.gameapi.games.onevsones;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.Network;
import network.ProPlugin;
import network.gameapi.competitive.*;
import network.gameapi.competitive.EloRanking.EloRank;
import network.gameapi.games.onevsones.kits.*;
import network.gameapi.uhc.GoldenHead;
import network.player.TeamScoreboardHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.*;
import network.server.tasks.DelayedTask;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnevsOnes extends ProPlugin {
	private static String oldPlayerCount = null;
	private static List<Integer> teamSizes = null;
	
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

        teamSizes = Arrays.asList(1, 2, 3, 4);

		resetWorld();
        World world = Bukkit.getWorlds().get(0);
        Location target = new Location(world, 0.5, 12, -30.5);
		new ServerLogger();
		new StatsHandler(DB.PLAYERS_STATS_ONE_VS_ONE, DB.PLAYERS_STATS_ONE_VS_ONE_MONTHLY, DB.PLAYERS_STATS_ONE_VS_ONE_WEEKLY);
		new LobbyHandler();
        new QueueHandler();
        new BattleHandler();
        new MapProvider(world);
        new SpectatorHandler1v1s();
        new PrivateBattleHandler();
        new EloHandler(DB.PLAYERS_ONE_VS_ONE_ELO, 1400);
        new ServerLogger();
        //Arrays.asList(ImageMap.getItemFrame(world, -16, 10, -34))
        new EloRanking(new ArrayList<ItemFrame>(), DB.PLAYERS_ONE_VS_ONE_ELO, DB.PLAYERS_ONE_VS_ONE_RANKED);
        new CPSDetector(new Location(world, -27.5, 13, -24.5), target);
        new GoldenHead();
        new RankedHandler();
        new HotBarEditor();
//        new TeamMatchHandler(new Location(world, 3.5, 13, -44.5));
//        new OnDemandTournaments(new Location(world, -2.5, 13, -44.5));
//        new MonthlyTournaments(new Location(world, -7.5, 13, -43.5));
		new DailyRewards(new Location(world, -7.5, 12, -13.5));
		new StatsNPC(new Location(world, 8.5, 12, -13.5));

        new DelayedTask(() -> {
			List<Location> locations = Arrays.asList(
				new Location(world, 13.5, 18.5, -25),
				new Location(world, 13.5, 18.5, -31),
				new Location(world, 13.5, 18.5, -37)
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
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 17.25, -38.5), StringUtil.color("&eElo ranks are based off of your percentile")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 16.75, -38.5), StringUtil.color("&eof elo compared to other players.&b /elo")));
				holograms.add(HologramAPI.createHologram(hologramLocation[0], StringUtil.color(EloRank.DIAMOND.getPrefix() + " &aTop " + EloRank.DIAMOND.getPercentRange() + " of players")));
				holograms.add(HologramAPI.createHologram(hologramLocation[1], StringUtil.color(EloRank.PLATINUM.getPrefix() + " &aTop " + EloRank.PLATINUM.getPercentRange() + " of players")));
				holograms.add(HologramAPI.createHologram(hologramLocation[2], StringUtil.color(EloRank.GOLD.getPrefix() + " &aTop " + EloRank.GOLD.getPercentRange() + " of players")));
				holograms.add(HologramAPI.createHologram(hologramLocation[3], StringUtil.color(EloRank.SILVER.getPrefix() + " &aTop " + EloRank.SILVER.getPercentRange() + " of players")));
				holograms.add(HologramAPI.createHologram(hologramLocation[4], StringUtil.color(EloRank.BRONZE.getPrefix() + " &aTop " + EloRank.BRONZE.getPercentRange() + " of players")));
			}
			for(Hologram hologram : holograms) {
				hologram.spawn();
			}
		}, 20);

        oldPlayerCount = "";

        Network.setSidebar(new SidebarScoreboardUtil(" &a" + getDisplayName() + " ") {
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
					Ranks.PRO.getColor() + "PRO: &b1s /buy",
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
	}

	public static List<Integer> getTeamSizes() {
		return teamSizes;
	}
}
