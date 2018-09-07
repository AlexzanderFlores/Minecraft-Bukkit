package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;

import network.gameapi.uhc.scenarios.Scenario;

public class Vanilla extends Scenario {
    private static Vanilla instance = null;

    public Vanilla() {
        super("Vanilla", "Vanilla", Material.DIAMOND_PICKAXE);
        instance = this;
        setInfo("DefaultKit Minecraft setting, no extra changes");
        setPrimary(true);
    }

    public static Vanilla getInstance() {
        if(instance == null) {
            new Vanilla();
        }
        return instance;
    }
}