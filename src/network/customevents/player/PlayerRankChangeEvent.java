package network.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import network.player.account.AccountHandler.Ranks;

public class PlayerRankChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Ranks rank = null;
 
    public PlayerRankChangeEvent(Player player, Ranks rank) {
    	this.player = player;
    	this.rank = rank;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Ranks getRank() {
    	return this.rank;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
