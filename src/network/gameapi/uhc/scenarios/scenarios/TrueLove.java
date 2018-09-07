package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;

import network.gameapi.uhc.scenarios.Scenario;

public class TrueLove extends Scenario {
    private static TrueLove instance = null;

    public TrueLove() {
        super("TrueLove", "TL", Material.RED_ROSE);
        instance = this;
        setInfo("Game is set up like an FFA but you may team with up to 1 person at a time. It is up to you to find that person. You may kill your team mate. If your team mate dies you can select a new team mate if they do not already have a team mate.");
        setPrimary(false);
    }

    public static TrueLove getInstance() {
        if(instance == null) {
            new TrueLove();
        }
        return instance;
    }
}
