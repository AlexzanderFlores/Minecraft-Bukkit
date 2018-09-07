package network.server.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import network.Network;

public class EventUtil {
	public static void register(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, Network.getInstance());
	}
}
