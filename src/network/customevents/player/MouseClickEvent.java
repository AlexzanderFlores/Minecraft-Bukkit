package network.customevents.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import network.server.util.EventUtil;

public class MouseClickEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Player player = null;
    private ClickType clickType = null;
    
    public static enum ClickType {
    	LEFT_CLICK, RIGHT_CLICK
    }
    
    public MouseClickEvent() {
    	EventUtil.register(this);
    }
    
    public MouseClickEvent(Player player, Action action) {
    	this.player = player;
    	if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
    		this.clickType = ClickType.LEFT_CLICK;
    	} else if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
    		this.clickType = ClickType.RIGHT_CLICK;
    	}
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public ClickType getClickType() {
    	return this.clickType;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if(event.getAction() != Action.PHYSICAL) {
    		MouseClickEvent playerClickEvent = new MouseClickEvent(event.getPlayer(), event.getAction());
    		if(playerClickEvent != null) {
    			Bukkit.getPluginManager().callEvent(playerClickEvent);
        		if(playerClickEvent.isCancelled()) {
        			event.setCancelled(true);
        		}
    		}
    	}
    }
}
