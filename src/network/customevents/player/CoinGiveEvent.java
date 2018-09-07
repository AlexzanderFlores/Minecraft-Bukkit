package network.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CoinGiveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private int amount = 0;
    
    public CoinGiveEvent(Player player, int amount) {
    	this.player = player;
    	this.amount = amount;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public int getAmount() {
    	return this.amount;
    }
    
    public void setAmount(int amount) {
    	this.amount = amount;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
