package network.customevents.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class PostGameStartEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    
	public PostGameStartEvent(boolean registerEvents) {
    	EventUtil.register(this);
    }
    
    public PostGameStartEvent() {
    	
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @EventHandler
    public void onGameStart(GameStartEvent event) {
    	new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getPluginManager().callEvent(new PostGameStartEvent());
			}
		}, 5);
    }
}
