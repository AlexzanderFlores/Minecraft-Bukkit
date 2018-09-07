package network.customevents.player;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private CommandSender staff = null;
    
    public PlayerBanEvent(UUID uuid, CommandSender staff) {
    	this.uuid = uuid;
    	this.staff = staff;
    }
    
    public UUID getUUID() {
    	return this.uuid;
    }
    
    public CommandSender getStaff() {
    	return this.staff;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
