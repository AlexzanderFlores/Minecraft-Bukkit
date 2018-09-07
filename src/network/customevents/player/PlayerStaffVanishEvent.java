package network.customevents.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStaffVanishEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private StaffVanishType type = null;
    private Location target = null;
    private boolean cancelled = false;
    
    public static enum StaffVanishType {
    	ENABLE, DISABLE, TELEPORT
    }
 
    public PlayerStaffVanishEvent(Player player, StaffVanishType type) {
        this(player, type, null);
    }
    
    public PlayerStaffVanishEvent(Player player, StaffVanishType type, Location target) {
    	this.player = player;
    	this.type = type;
    	this.target = target;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public StaffVanishType getType() {
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