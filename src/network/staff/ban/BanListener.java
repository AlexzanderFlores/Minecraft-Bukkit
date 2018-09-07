package network.staff.ban;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.anticheat.events.PlayerBanEvent;
import network.server.util.EventUtil;

public class BanListener implements Listener {
	public BanListener() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerBan(PlayerBanEvent event) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + event.getName() + " cheating");
	}
}
