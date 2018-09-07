package network.customevents.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import network.server.util.EventUtil;


public class PlayerHeadshotEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private static Map<String, Integer> numberOfHeadshots = null;
    private Player player = null;
    private double damage = -1;
    private boolean cancelled = false;
    
    public PlayerHeadshotEvent(Player player, double damage) {
    	this.player = player;
    	this.damage = damage;
    	if(numberOfHeadshots == null) {
    		numberOfHeadshots = new HashMap<String, Integer>();
    		EventUtil.register(this);
    	}
    	int counter = numberOfHeadshots.containsKey(player.getName()) ? numberOfHeadshots.get(player.getName()) : 0;
    	numberOfHeadshots.put(player.getName(), counter + 1);
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public void setDamage(double damage) {
    	this.damage = damage;
    }
    
    public double getDamage() {
    	return damage;
    }
 
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public int getNumberOfHeadshots() {
    	return numberOfHeadshots == null || !numberOfHeadshots.containsKey(player.getName()) ? 0 : numberOfHeadshots.get(player.getName());
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
    	if(numberOfHeadshots != null) {
    		numberOfHeadshots.remove(event.getPlayer().getName());
    	}
    }
}
