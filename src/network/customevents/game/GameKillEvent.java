package network.customevents.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameKillEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Player killed = null;
    private String message = null;
    
    public GameKillEvent(Player player, Player killed, String message) {
    	this.player = player;
    	this.killed = killed;
    	this.message = message;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Player getKilled() {
    	return this.killed;
    }
    
    public String getMessage() {
    	return this.message;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
