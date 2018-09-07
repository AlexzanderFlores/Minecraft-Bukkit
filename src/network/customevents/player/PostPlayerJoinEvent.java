package network.customevents.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import network.customevents.TimeEvent;
import network.server.util.EventUtil;


public class PostPlayerJoinEvent extends Event implements Listener {
	private static List<String> players = null;
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    
    public PostPlayerJoinEvent() {
    	players = new ArrayList<String>();
    	EventUtil.register(this);
    }
 
    public PostPlayerJoinEvent(Player player) {
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
    
    @EventHandler
    public void onTime(TimeEvent event) {
    	long ticks = event.getTicks();
    	if(ticks == 1) {
    		for(Player player : Bukkit.getOnlinePlayers()) {
        		if(!players.contains(player.getName()) && player.getTicksLived() >= 20) {
        			players.add(player.getName());
        			Bukkit.getPluginManager().callEvent(new PostPlayerJoinEvent(player));
        		}
        	}
    	}
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
    	players.remove(event.getPlayer().getName());
    }
}
