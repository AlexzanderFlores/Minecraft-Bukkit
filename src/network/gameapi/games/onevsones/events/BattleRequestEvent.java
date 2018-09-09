package network.gameapi.games.onevsones.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleRequestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Player playerOne;
    private Player playerTwo;
    private boolean cancelled = false;

    public BattleRequestEvent(Player playerOne, Player playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
    }

    public Player getPlayerOne() {
        return this.playerOne;
    }

    public Player getPlayerTwo() {
        return this.playerTwo;
    }

    public Player [] getPlayers() {
        return new Player [] { getPlayerOne(), getPlayerTwo() };
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}