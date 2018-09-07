package network.customevents.player;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerItemFrameInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private ItemFrame itemFrame = null;
    
    public PlayerItemFrameInteractEvent(Player player, ItemFrame itemFrame) {
    	this.player = player;
    	this.itemFrame = itemFrame;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public ItemFrame getItemFrame() {
    	return this.itemFrame;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
