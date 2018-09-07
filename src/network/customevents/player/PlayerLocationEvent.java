package network.customevents.player;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLocationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private String server = null;
    
    public PlayerLocationEvent(UUID uuid, String server) {
    	this.uuid = uuid;
    	this.server = server;
    }
    
    public UUID getUUID() {
    	return this.uuid;
    }
    
    public String getServer() {
    	return this.server;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
