package network.gameapi.games.uhc;

import java.util.HashMap;
import java.util.Map;

import network.Network;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import network.Network;
import network.ProPlugin;
import network.customevents.game.GameDeathEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.account.AccountHandler;
import network.server.util.EventUtil;

public class KillLogger implements Listener {
	private Map<String, Integer> kills;
	
	public KillLogger() {
		kills = new HashMap<>();
		Network.getSidebar().setText(new String [] {
				" ",
				"&cNo Deaths",
				"  "
		});
		EventUtil.register(this);
	}
	
	private static String getText(Player player) {
		String text = AccountHandler.getRank(player).getColor() + player.getName();
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		return text;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(kills.containsKey(player.getName())) {
			Network.getSidebar().setText(getText(player), kills.get(player.getName()));
		}
		Network.getSidebar().setText("Playing", ProPlugin.getPlayers().size());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		if(kills.isEmpty()) {
			Network.getSidebar().removeScoresBelow(0);
		}
		Player player = event.getPlayer();
		kills.remove(player.getName());
		Network.getSidebar().removeText(getText(player));
		Player killer = event.getKiller();
		if(killer == null) {
			String name = "PVE";
			if(kills.containsKey(name)) {
				kills.put(name, kills.get(name) + 1);
			} else {
				kills.put(name, 1);
			}
			Network.getSidebar().setText("&6" + name, kills.get(name));
		} else {
			if(kills.containsKey(killer.getName())) {
				kills.put(killer.getName(), kills.get(killer.getName()) + 1);
			} else {
				kills.put(killer.getName(), 1);
			}
			Network.getSidebar().setText(getText(killer), kills.get(killer.getName()));
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		Network.getSidebar().removeText(getText(player));
		Network.getSidebar().setText("Playing", ProPlugin.getPlayers().size());
	}
}
