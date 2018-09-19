package network.customevents.player;

import network.server.effects.images.DisplayImage;
import network.server.tasks.DelayedTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class PlayerItemFrameInteractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private static List<String> sent = new ArrayList<String>();
    private Player player = null;
    private DisplayImage.ImageID id = null;
    private String name = null;
    
    public PlayerItemFrameInteractEvent(Player player, DisplayImage.ImageID id, String name) {
    	this.player = player;
    	this.id = id;
    	this.name = name;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public DisplayImage.ImageID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDelayed() {
        return sent.contains(player.getName());
    }

    public void setSent() {
        if(!sent.contains(player.getName())) {
            sent.add(player.getName());
            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    sent.remove(player.getName());
                }
            }, 20 * 3);
        }
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
