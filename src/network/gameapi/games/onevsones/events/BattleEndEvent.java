package network.gameapi.games.onevsones.events;

import network.gameapi.games.onevsones.Battle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Battle battle = null;

    public BattleEndEvent(Battle battle) {
        this.battle = battle;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    public Battle getBattle() {
    	return this.battle;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}