package network.gameapi.games.onevsones.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillstreakReachedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private int streak = 0;

    public KillstreakReachedEvent(Player player, int streak) {
        this.player = player;
        this.streak = streak;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getStreak() {
        return this.streak;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
