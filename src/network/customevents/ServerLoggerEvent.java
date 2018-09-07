package network.customevents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class ServerLoggerEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
 
    public ServerLoggerEvent() {
    	
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
