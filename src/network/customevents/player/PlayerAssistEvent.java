package network.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAssistEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player attacker = null;
    private Player damaged = null;
    
    public PlayerAssistEvent(Player attacker, Player damaged) {
    	this.attacker = attacker;
    	this.damaged = damaged;
    }
    
    public Player getAttacker() {
    	return this.attacker;
    }
    
    public Player getDamaged() {
    	return this.damaged;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
