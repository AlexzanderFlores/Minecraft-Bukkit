package network.gameapi.games.onevsones;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.TextLine;
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
import network.server.effects.images.DisplayImage;
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
import java.util.Random;

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
		new DailyRewards(new Location(world, -7.5, 12, -13.5), new Location(world, 0.5, 12, -30.5));
		new StatsNPC(new Location(world, 8.5, 12, -13.5), new Location(world, 0.5, 12, -30.5));

        new DelayedTask(() -> {
			List<Location> locations = Arrays.asList(
				new Location(world, 13.5, 15, -25),
				new Location(world, 13.5, 15, -31),
				new Location(world, 13.5, 15, -37)
			);
			new StatDisplayer(locations);

			for(int a = 0; a < 3; ++a) {
				Hologram hologram = new Hologram("elo_ranks_" + a, new Location(world, -14.5, 15, -38.5));
				hologram.addLine(new TextLine(hologram, StringUtil.color("&eElo ranks are based off of your percentile")));
				hologram.addLine(new TextLine(hologram, StringUtil.color("&eof elo compared to other players.&b /elo")));
				hologram.addLine(new TextLine(hologram, EloRank.DIAMOND.getPrefix() + " &aTop " + EloRank.DIAMOND.getPercentRange() + " of players"));
				hologram.addLine(new TextLine(hologram, EloRank.PLATINUM.getPrefix() + " &aTop " + EloRank.PLATINUM.getPercentRange() + " of players"));
				hologram.addLine(new TextLine(hologram, EloRank.GOLD.getPrefix() + " &aTop " + EloRank.GOLD.getPercentRange() + " of players"));
				hologram.addLine(new TextLine(hologram, EloRank.SILVER.getPrefix() + " &aTop " + EloRank.SILVER.getPercentRange() + " of players"));
				hologram.addLine(new TextLine(hologram, EloRank.BRONZE.getPrefix() + " &aTop " + EloRank.BRONZE.getPercentRange() + " of players"));
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
					"&aOpal Gaming",
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

		new DisplayImage(
				DisplayImage.ImageID.ONEVSONE_ELO,
				"elo").display();
	}

	public static List<Integer> getTeamSizes() {
		return teamSizes;
	}
}
