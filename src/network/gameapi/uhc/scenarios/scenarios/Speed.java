package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.ProPlugin;
import network.customevents.game.PostGameStartEvent;
import network.gameapi.uhc.scenarios.Scenario;

public class Speed extends Scenario {
    private static Speed instance = null;

    public Speed() {
        super("Speed", "S", Material.ARROW);
        instance = this;
        setInfo("Players get speed II, haste II, fire resistance, night vision, no fall damage and timber");
        setPrimary(false);
        new Timber();
    }

    public static Speed getInstance() {
        if(instance == null) {
            new Speed();
        }
        return instance;
    }
    
    @EventHandler
    public void onPostGameStart(PostGameStartEvent event) {
    	for(Player player : ProPlugin.getPlayers()) {
    		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
    		player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 999999999, 2));
    		player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999999, 1));
    	}
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
    	if(event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player) {
    		event.setCancelled(true);
    	}
    }
}
