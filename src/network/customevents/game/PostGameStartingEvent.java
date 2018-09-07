package network.customevents.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class PostGameStartingEvent extends Event implements Listener {
	private static final HandlerList handlers = new HandlerList();
	
	public PostGameStartingEvent(boolean registerEvents) {
    	EventUtil.register(this);
    }
	
	public PostGameStartingEvent() {
		
	}
	
	@Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @EventHandler
    public void onGameStarting(GameStartingEvent event) {
    	new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getPluginManager().callEvent(new PostGameStartingEvent());
			}
		}, 5);
    }
}
