package network.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.util.EventUtil;

public class RankAds implements Listener {
	private static String [] alerts = null;
	private int counter = 0;
	
	public RankAds() {
		alerts = new String [] {
			"Ranks give you &cx2 &xcoins &b/buy",
			"Ranks allow you to fly in hubs &b/buy",
			"Ranks bypass all rank ads &b/buy",
			"Ranks allow you to join full servers &b/buy",
			"Ranks display your skin on all hubs &b/buy",
			"Ranks get &cx2 &xvotes &b/buy"
		};
		EventUtil.register(this);
	}
	
	public static String [] getAlerts() {
		return alerts;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.VIP.hasRank(player)) {
					MessageHandler.sendMessage(player, "&a[TIP] &x" + alerts[counter]);
				}
			}
			if(++counter >= alerts.length) {
				counter = 0;
			}
		}
	}
}
