package network.gameapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.customevents.game.GameDeathEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.util.EventUtil;

public class KillLogger implements Listener {
	private static Map<UUID, Integer> kills = null;
	
	public KillLogger() {
		kills = new HashMap<UUID, Integer>();
		EventUtil.register(this);
	}
	
	public static int getKills(Player player) {
		return kills != null && kills.containsKey(player.getUniqueId()) ? kills.get(player.getUniqueId()) : 0;
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player killer = event.getKiller();
		if(killer != null) {
			int kill = 0;
			if(kills.containsKey(killer.getUniqueId())) {
				kill = kills.get(killer.getUniqueId());
			}
			kills.put(killer.getUniqueId(), ++kill);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(Network.getMiniGame() == null) {
			kills.remove(event.getPlayer().getUniqueId());
		}
	}
}
