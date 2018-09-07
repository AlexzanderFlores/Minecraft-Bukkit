package network.gameapi.games.kitpvp.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InventoryViewClickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private String title = null;
    private int slot = 0;
    private int viewSlot = 0;
    
    public InventoryViewClickEvent(Player player, String title, int slot, int viewSlot) {
    	this.player = player;
    	this.title = title;
    	this.slot = slot;
    	this.viewSlot = viewSlot;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public String getTitle() {
    	return this.title;
    }
    
    public int getSlot() {
    	return this.slot;
    }
    
    public int getViewSlot() {
    	return this.viewSlot;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
