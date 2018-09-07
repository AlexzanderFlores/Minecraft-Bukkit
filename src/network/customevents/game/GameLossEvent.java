package network.customevents.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;

public class GameLossEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Team team = null;
    
    public GameLossEvent(Player player) {
    	this.player = player;
    }
    
    public GameLossEvent(Team team) {
    	this.team = team;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Team getTeam() {
    	return team;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}