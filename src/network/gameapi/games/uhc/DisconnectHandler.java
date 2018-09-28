package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import network.customevents.TimeEvent;
import network.customevents.player.PlayerSpectatorEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.customevents.game.GameDeathEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PostPlayerJoinEvent;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.uhc.events.PlayerTimeOutEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.EventUtil;

public class DisconnectHandler implements Listener {
	private static Map<UUID, Integer> times = null;
	private static List<String> cannotRelog = null;
	
	public DisconnectHandler() {
		times = new HashMap<>();
		cannotRelog = new ArrayList<>();
		EventUtil.register(this);
	}
	
	public static boolean isDisconnected(Player player) {
		return times.containsKey(player.getUniqueId());
	}
	
	public static boolean cannotRelog(Player player) {
		return cannotRelog.contains(player.getName());
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 20) {
			Iterator<UUID> iterator = times.keySet().iterator();
			while(iterator.hasNext()) {
				UUID uuid = iterator.next();
				times.put(uuid, times.get(uuid) + 1);
				if(times.get(uuid) >= (60 * 5)) {
					Bukkit.getPluginManager().callEvent(new PlayerTimeOutEvent(uuid));
					iterator.remove();
					String name = AccountHandler.getName(uuid);
					cannotRelog.remove(name);
					WhitelistHandler.unWhitelist(uuid);
					MessageHandler.alert(name + " &ctook too long to come back!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		times.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		cannotRelog.add(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerSpectator(PlayerSpectatorEvent event) {
		if(event.getState() == PlayerSpectatorEvent.SpectatorState.ADDED && times.containsKey(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(Network.getMiniGame().getGameState() == GameStates.STARTED && !SpectatorHandler.contains(player) && !cannotRelog.contains(player.getName())) {
			times.put(player.getUniqueId(), 0);
		}
	}
}
