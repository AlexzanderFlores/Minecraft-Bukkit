package network.server.servers.hub.parkours;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private ParkourTypes type = null;
    public enum ParkourTypes {COURSE, ENDLESS}
    
    public ParkourStartEvent(Player player, ParkourTypes type) {
    	this.player = player;
    	this.type = type;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public ParkourTypes getType() {
    	return this.type;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}