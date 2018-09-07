package network.server;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.ProPlugin;
import network.customevents.ServerLoggerEvent;
import network.customevents.ServerRestartEvent;
import network.customevents.TimeEvent;
import network.gameapi.MiniGame;
import network.gameapi.SpectatorHandler;
import network.gameapi.MiniGame.GameStates;
import network.server.util.EventUtil;

public class ServerLogger implements Listener {
	private static int players = -1;
	private static int max = -1;
	private static GameStates state = null;
	private static boolean shuttingDown = false;
	
	public ServerLogger() {
		EventUtil.register(this);
		updateStatus(false);
	}
	
	public static boolean updatePlayerCount() {
		int size = ProPlugin.getPlayers().size();
		if(players != size) {
			return true;
		}
		return false;
	}
	
	public static void updateStatus(boolean delete) {
		String game = Network.getPlugin().toString();
		String number = Network.getServerName().replaceAll("[^\\d.]", "");
		String [] keys = {"game_name", "server_number"};
		String [] values = {game, number};
		if(delete) {
			DB.NETWORK_SERVER_STATUS.delete(keys, values);
		} else {
			ServerLoggerEvent event = new ServerLoggerEvent();
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				int current = ProPlugin.getPlayers().size() + (SpectatorHandler.isEnabled() ? SpectatorHandler.getNumberOf() : 0);
				GameStates gameState = null;
				MiniGame miniGame = Network.getMiniGame();
				if(miniGame != null) {
					gameState = miniGame.getGameState();
				}
				int serverMax = Network.getMaxPlayers();
				if(current != players || gameState != state || serverMax != max) {
					players = current;
					max = serverMax;
					state = gameState;
					int priority = 2;
					if(miniGame != null) {
						if(ProPlugin.isServerFull()) {
							if(miniGame.getJoiningPreGame()) {
								priority = 1;
							} else {
								priority = 3;
							}
						} else if(!miniGame.getJoiningPreGame()) {
							priority = 3;
						}
					}
					String lore = gameState == null ? "null" : gameState.toString();
					if(DB.NETWORK_SERVER_STATUS.isKeySet(keys, values)) {
						DB.NETWORK_SERVER_STATUS.updateInt("listed_priority", priority, keys, values);
						DB.NETWORK_SERVER_STATUS.updateString("lore", lore, keys, values);
						DB.NETWORK_SERVER_STATUS.updateInt("players", players, keys, values);
						DB.NETWORK_SERVER_STATUS.updateInt("max_players", Network.getMaxPlayers(), keys, values);
					} else {
						DB.NETWORK_SERVER_STATUS.insert("'" + game + "', '" + number + "', '" + priority + "', '" + lore + "', '0', '" + Network.getMaxPlayers() + "'");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && !shuttingDown) {
			updateStatus(false);
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		shuttingDown = true;
		updateStatus(true);
	}
}
