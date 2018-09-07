package network.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.server.util.EventUtil;

//TODO: Use this in all places that it is useful: Staff mode, spectating
public class Vanisher implements Listener {
	private static List<String> vanished = null;
	private static boolean enabled = false;
	
	public Vanisher() {
		if(!enabled) {
			enabled = true;
			vanished = new ArrayList<String>();
			EventUtil.register(this);
		}
	}
	
	public static boolean isVanished(Player player) {
		if(!enabled) {
			new Vanisher();
		}
		return vanished != null && vanished.contains(player.getName());
	}
	
	public static void toggleVanished(Player player) {
		if(!enabled) {
			new Vanisher();
		}
		if(isVanished(player)) {
			remove(player);
		} else {
			add(player);
		}
	}
	
	public static void add(Player player) {
		if(!enabled) {
			new Vanisher();
		}
		remove(player);
		vanished.add(player.getName());
		for(Player online : Bukkit.getOnlinePlayers()) {
			online.hidePlayer(player);
		}
	}
	
	public static void remove(Player player) {
		if(!enabled) {
			new Vanisher();
		}
		vanished.remove(player.getName());
		for(Player online : Bukkit.getOnlinePlayers()) {
			online.showPlayer(player);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for(String name : vanished) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				event.getPlayer().hidePlayer(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		vanished.remove(event.getPlayer().getName());
	}
}
