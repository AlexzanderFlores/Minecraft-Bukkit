package network.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.server.util.EventUtil;

public class Alerts implements Listener {
	private static String [] alerts = null;
	private int counter = 0;
	
	public Alerts() {
		alerts = new String [] {
			"Follow us! &b/socialMedia",
			"Vote every day for advantages: &b/vote",
			"Join our discord: &b/discord",
			"Visit our store: &b/buy"
		};
		EventUtil.register(this);
	}
	
	public static String [] getAlerts() {
		return alerts;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60 * 5) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				MessageHandler.sendMessage(player, "&a&l[TIP] &x" + alerts[counter]);
			}
			if(++counter >= alerts.length) {
				counter = 0;
			}
		}
	}
}