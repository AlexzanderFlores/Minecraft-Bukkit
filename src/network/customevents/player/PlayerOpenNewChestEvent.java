package network.customevents.player;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerOpenNewChestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Chest chest = null;
    
    public PlayerOpenNewChestEvent(Player player, Chest chest) {
    	this.player = player;
    	this.chest = chest;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Chest getChest() {
    	return this.chest;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
