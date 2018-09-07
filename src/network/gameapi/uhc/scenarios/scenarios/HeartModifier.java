package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import network.ProPlugin;
import network.customevents.game.PostGameStartEvent;
import network.gameapi.uhc.scenarios.Scenario;

public class HeartModifier extends Scenario {
    private static HeartModifier instance = null;
    private static double amount = 0;

    public HeartModifier(String name, double amount) {
        super(name, "HM", Material.RED_ROSE);
        instance = this;
        setInfo("Players will have " + amount + " hearts of health");
        setPrimary(false);
        enable(false);
        setAmount(amount);
    }
    
    public static void setAmount(double amount) {
    	HeartModifier.amount = amount;
    }

    public static HeartModifier getInstance(String name, double amount) {
        if(instance == null) {
            new HeartModifier(name, amount);
        }
        return instance;
    }
    
    @EventHandler
    public void onPostGameStart(PostGameStartEvent event) {
    	for(Player player : ProPlugin.getPlayers()) {
    		player.setMaxHealth(amount);
    		player.setHealth(amount);
    	}
    }
}
