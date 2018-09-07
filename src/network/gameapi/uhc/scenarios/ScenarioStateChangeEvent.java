package network.gameapi.uhc.scenarios;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScenarioStateChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Scenario scenario = null;
    private boolean enabling = false;

    public ScenarioStateChangeEvent(Scenario scenario, boolean enabling) {
        this.scenario = scenario;
        this.enabling = enabling;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Scenario getScenario() {
        return this.scenario;
    }

    public boolean isEnabling() {
        return this.enabling;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}