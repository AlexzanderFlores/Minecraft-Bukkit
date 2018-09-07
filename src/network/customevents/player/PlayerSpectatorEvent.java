package network.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSpectatorEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private boolean cancelled = false;
    private SpectatorState state = null;
    public enum SpectatorState {STARTING, ADDED, END}
 
    public PlayerSpectatorEvent(Player player, SpectatorState state) {
        this.player = player;
        this.state = state;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public SpectatorState getState() {
    	return state;
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