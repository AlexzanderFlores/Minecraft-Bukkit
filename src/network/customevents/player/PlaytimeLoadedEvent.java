package network.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlaytimeLoadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    
    public PlaytimeLoadedEvent(Player player) {
    	this.player = player;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
