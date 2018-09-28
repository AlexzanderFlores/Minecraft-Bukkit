package network.gameapi.games.uhc.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTimeOutEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    
    public PlayerTimeOutEvent(UUID uuid) {
    	this.uuid = uuid;
    }
    
    public UUID getPlayer() {
    	return this.uuid;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
