package network.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import network.customevents.game.GameDeathEvent;
import network.customevents.player.PlayerAssistEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.util.EventUtil;

public class AssistTracker implements Listener {
	public Map<String, List<String>> allDamagers = null;
	
	public AssistTracker() {
		allDamagers = new HashMap<String, List<String>>();
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player) {
			Player damager = null;
			if(event.getDamager() instanceof Player) {
				damager = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					damager = (Player) projectile.getShooter();
				}
			}
			if(damager != null) {
				Player player = (Player) event.getEntity();
				List<String> damagers = allDamagers.get(player.getName());
				if(damagers == null) {
					damagers = new ArrayList<String>();
				}
				damagers.add(damager.getName());
				allDamagers.put(player.getName(), damagers);
			}
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
		if(allDamagers.containsKey(player.getName())) {
			List<String> damagers = allDamagers.get(player.getName());
			if(damagers != null) {
				for(Player damager : Bukkit.getOnlinePlayers()) {
					if(damagers.contains(damager.getName())) {
						Bukkit.getPluginManager().callEvent(new PlayerAssistEvent(damager, player));
					}
				}
				damagers.clear();
				damagers = null;
			}
			allDamagers.remove(player.getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(allDamagers.containsKey(player.getName())) {
			List<String> damagers = allDamagers.get(player.getName());
			if(damagers != null) {
				damagers.clear();
				damagers = null;
			}
			allDamagers.remove(player.getName());
		}
	}
}
