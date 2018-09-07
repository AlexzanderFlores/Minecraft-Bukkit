package network.customevents.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;

public class GameWinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private boolean endServer = true;
    private Team team = null;
    
    public GameWinEvent(Player player) {
    	this.player = player;
    }
    
    public GameWinEvent(Player player, boolean endServer) {
    	this.player = player;
    	this.endServer = endServer;
    }
    
    public GameWinEvent(Team team) {
    	this.team = team;
    }
    
    public GameWinEvent(Team team, boolean endServer) {
    	this.team = team;
    	this.endServer = endServer;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public boolean getEndServer() {
    	return this.endServer;
    }
    
    public Team getTeam() {
    	return team;
    }
    
    public void setEndServer(boolean endServer) {
    	this.endServer = endServer;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
