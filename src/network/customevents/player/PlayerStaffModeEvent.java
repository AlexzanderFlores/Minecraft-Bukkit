package network.customevents.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStaffModeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private StaffModeEventType type = null;
    private Location target = null;
    private boolean cancelled = false;
    
    public static enum StaffModeEventType {
    	ENABLE, DISABLE, TELEPORT
    }
 
    public PlayerStaffModeEvent(Player player, StaffModeEventType type) {
        this(player, type, null);
    }
    
    public PlayerStaffModeEvent(Player player, StaffModeEventType type, Location target) {
    	this.player = player;
    	this.type = type;
    	this.target = target;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public StaffModeEventType getType() {
    	return type;
    }
    
    public Location getTarget() {
    	return target;
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