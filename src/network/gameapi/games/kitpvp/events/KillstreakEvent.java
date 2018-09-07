package network.gameapi.games.kitpvp.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import network.gameapi.games.kitpvp.killstreaks.Killstreak;

public class KillstreakEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Killstreak killstreak = null;
    
    public KillstreakEvent(Player player, Killstreak killstreak) {
    	this.player = player;
    	this.killstreak = killstreak;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Killstreak getKillstreak() {
    	return this.killstreak;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}