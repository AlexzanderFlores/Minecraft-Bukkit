package network.customevents.player;

import network.server.effects.images.DisplayImage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerItemFrameInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private DisplayImage.ImageID id = null;
    private String name = null;
    
    public PlayerItemFrameInteractEvent(Player player, DisplayImage.ImageID id, String name) {
    	this.player = player;
    	this.id = id;
    	this.name = name;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public DisplayImage.ImageID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
