package network.customevents.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Player killer = null;
    
    public GameDeathEvent(Player player) {
    	this.player = player;
    }
    
    public GameDeathEvent(Player player, Player killer) {
    	this.player = player;
    	this.killer = killer;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Player getKiller() {
    	return this.killer;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
