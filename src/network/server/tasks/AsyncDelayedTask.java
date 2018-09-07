package network.server.tasks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import network.Network;

public class AsyncDelayedTask implements Listener {
	private int id = -1;
	
	public AsyncDelayedTask(Runnable runnable) {
		this(runnable, 1);
	}
	
	public AsyncDelayedTask(Runnable runnable, long delay) {
		Network instance = Network.getInstance();
		if(instance.isEnabled()) {
			id = Bukkit.getScheduler().runTaskLaterAsynchronously(instance, runnable, delay).getTaskId();
		} else {
			runnable.run();
		}
	}
	
	public int getId() {
		return id;
	}
}
