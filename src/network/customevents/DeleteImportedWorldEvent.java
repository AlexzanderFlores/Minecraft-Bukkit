package network.customevents;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DeleteImportedWorldEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private World world = null;
    private boolean cancelled = false;
    
    public DeleteImportedWorldEvent(World world) {
    	this.world = world;
    }
    
    public World getWorld() {
    	return this.world;
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
