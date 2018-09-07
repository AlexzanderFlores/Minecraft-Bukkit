package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import network.gameapi.uhc.scenarios.Scenario;
import network.player.MessageHandler;

public class Switcheroo extends Scenario {
    private static Switcheroo instance = null;

    public Switcheroo() {
        super("Switcheroo", "SR", Material.ARROW);
        instance = this;
        setInfo("Shooting another player will an arrow will switch your player's locations");
        setPrimary(false);
    }

    public static Switcheroo getInstance() {
        if(instance == null) {
            new Switcheroo();
        }
        return instance;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    	if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow && !event.isCancelled()) {
    		Arrow arrow = (Arrow) event.getDamager();
    		if(arrow.getShooter() instanceof Player) {
    			Player player = (Player) event.getEntity();
    			Player shooter = (Player) arrow.getShooter();
    			if(!player.getName().equals(shooter.getName())) {
    				Location playerLocation = player.getLocation();
        			Location shooterLocation = shooter.getLocation();
        			player.teleport(shooterLocation);
        			shooter.teleport(playerLocation);
        			MessageHandler.sendMessage(player, getName() + ": Switching your locations with " + shooter.getName());
        			MessageHandler.sendMessage(shooter, getName() + ": Switching your locations with " + player.getName());
    			}
    		}
    	}
    }
}
