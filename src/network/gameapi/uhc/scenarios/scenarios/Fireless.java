package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import network.gameapi.uhc.scenarios.Scenario;

public class Fireless extends Scenario {
    private static Fireless instance = null;

    public Fireless() {
        super("Fireless", "FL", Material.FLINT_AND_STEEL);
        instance = this;
        setInfo("Fire and lava cannot do any damage");
        setPrimary(false);
    }

    public static Fireless getInstance() {
        if(instance == null) {
            new Fireless();
        }
        return instance;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
    	if(event.getEntity() instanceof Player) {
    		DamageCause cause = event.getCause();
    		if(cause == DamageCause.LAVA || cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) {
    			event.setCancelled(true);
    		}
    	}
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
    	if(event.getEntity() instanceof Player) {
    		event.setCancelled(true);
    	}
    }
}
