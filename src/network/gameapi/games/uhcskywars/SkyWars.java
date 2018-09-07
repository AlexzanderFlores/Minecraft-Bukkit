package network.gameapi.games.uhcskywars;

import org.bukkit.ChatColor;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.gameapi.MiniGame;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.shops.SkyWarsShop;
import network.gameapi.uhc.GoldenHead;
import network.gameapi.uhc.SkullPikeUtil;
import network.gameapi.uhc.scenarios.scenarios.CutClean;
import network.player.CoinsHandler;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.DB;
import network.server.ServerLogger;
import network.server.util.CountDownUtil;

public class SkyWars extends MiniGame {
	private String oldCountDownLine = "";
	
	public SkyWars() {
		super("UHC Sky Wars");
		setVotingCounter(45);
		setStartingCounter(10);
		setFlintAndSteelUses(4);
		setCanJoinWhileStarting(false);
		new CoinsHandler(DB.PLAYERS_COINS_SKY_WARS, Plugins.UHCSW.getData());
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(10);
		new StatsHandler(DB.PLAYERS_STATS_SKY_WARS, DB.PLAYERS_STATS_SKY_WARS_MONTHLY, DB.PLAYERS_STATS_SKY_WARS_WEEKLY);
		new BelowNameHealthScoreboardUtil();
		new Events();
		new ChestHandler();
		new SkyWarsShop();
		new LootPassHandler();
		new SkullPikeUtil();
		new GoldenHead();
		new ScenarioHandler();
		new CutClean().enable(false);
		Network.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
			@Override
			public void update() {
				int size = ProPlugin.getPlayers().size();
				String countDownLine = getGameState() == GameStates.WAITING ? "&b" + size + " &7/&b " + getRequiredPlayers() : CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA);
				if(!oldCountDownLine.equals(countDownLine)) {
					oldCountDownLine = countDownLine;
					removeScore(7);
				}
				if(ServerLogger.updatePlayerCount()) {
					removeScore(10);
				}
				if(getGameState() != getOldGameState()) {
					setOldGameState(getGameState());
					removeScore(8);
				}
				setText(new String [] {
					" ",
					"&e&lPlaying",
					"&b" + size + " &7/&b " + Network.getMaxPlayers(),
					"  ",
					"&e&l" + getGameState().getDisplay(),
					countDownLine,
					"   ",
					"&a&l1v1s.org",
					"    ",
					"&e&lServer",
					"&b&l" + Network.getPlugin().getServer().toUpperCase() + Network.getServerName().replaceAll("[^\\d.]", ""),
					"     "
				});
				super.update();
			}
		});
	}
}