package network.gameapi.games.onevsones.events;

import network.gameapi.games.onevsones.kits.OneVsOneKit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QueueEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public enum QueueAction {
        ADD,
        REMOVE
    }

    private Player player;
    private OneVsOneKit kit;
    private QueueAction queueAction;
    private boolean ranked;

    public QueueEvent(Player player, OneVsOneKit kit, QueueAction queueAction) {
        this.player = player;
        this.kit = kit;
        this.queueAction = queueAction;
    }

    public Player getPlayer() {
        return this.player;
    }

    public OneVsOneKit getKit() {
        return this.kit;
    }

    public QueueAction getAction() {
        return this.queueAction;
    }

    public void setRanked(boolean ranked) {
        this.ranked = ranked;
    }

    public boolean isRanked() {
        return this.ranked;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}