package network.gameapi.games.kitpvp;

import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.gameapi.SpectatorHandler;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.KitPVPShop;
import network.player.MessageHandler;
import network.server.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class SpawnHandler implements Listener {
	public static int spawnY = 77;
	private static int radius = 40;
	private static Location spawn;
	
	public SpawnHandler() {
		EventUtil.register(this);
	}
	
	public static Location spawn(Player player) {
		Random random = new Random();
		int range = 3;
		spawn = player.getWorld().getSpawnLocation();
		spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setY(spawn.getY() + 2.5d);
		spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setYaw(-90.0f);
		player.teleport(spawn);
		return spawn;
	}
	
	public static boolean isAtSpawn(Entity entity) {
		return isAtSpawn(entity.getLocation());
	}

	public static boolean isAtSpawn(Location location) {
		return location.getY() >= spawnY;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		spawn(event.getPlayer());
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isAtSpawn(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if(isAtSpawn(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(isAtSpawn(player) && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerSpectateEnd(PlayerSpectatorEvent event) {
		if(event.getState() == SpectatorState.END) {
			spawn(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(spawn);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(event.getTo().getBlockY() == spawnY - 2 && !SpectatorHandler.contains(player)) {
			boolean hasKit = false;
			if(KitBase.getKits() != null) {
				for(KitBase kit : KitBase.getKits()) {
					if(kit.has(player)) {
						hasKit = true;
						break;
					}
				}
			}

			if(!hasKit) {
				MessageHandler.sendMessage(player, "&cYou must have a kit before jumping down");
				spawn(player);
				KitPVPShop.getInstance().openShop(player);
				return;
			}

			Vector spawn = player.getWorld().getSpawnLocation().toVector();
			Vector location = event.getTo().toVector();
			if(location.isInSphere(spawn, radius)) {
				MessageHandler.sendMessage(player, "&cYou can't jump down here, jump down the far edge");
				spawn(player);
			}
		}
	}
}