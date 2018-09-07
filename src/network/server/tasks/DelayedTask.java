package network.server.tasks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import network.Network;

public class DelayedTask implements Listener {
	private int id = -1;
	
	public DelayedTask(Runnable runnable) {
		this(runnable, 1);
	}
	
	public DelayedTask(Runnable runnable, long delay) {
		Network instance = Network.getInstance();
		if(instance.isEnabled()) {
			id = Bukkit.getScheduler().scheduleSyncDelayedTask(instance, runnable, delay);
		} else {
			runnable.run();
		}
	}
	
	public int getId() {
		return id;
	}
}
