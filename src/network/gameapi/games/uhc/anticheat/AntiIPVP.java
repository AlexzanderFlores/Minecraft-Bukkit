package network.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.List;

import network.customevents.game.GracePeriodEndEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class AntiIPVP implements Listener {
	private static List<Location> blockedFire = null;
	private static List<String> noFireDamage = null;
	
	public AntiIPVP() {
		blockedFire = new ArrayList<>();
		noFireDamage = new ArrayList<>();
		EventUtil.register(this);
	}
	
	private static boolean isInBlockedFire(World world, int x, int y, int z) {
		for(Location location : blockedFire) {
			if(location.getWorld().getName().equalsIgnoreCase(world.getName()) && location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z) {
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		if(item != null && item.getType() == Material.FLINT_AND_STEEL && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() != Material.OBSIDIAN) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.FIRE) {
			if(event.getBlockAgainst().getType() == Material.OBSIDIAN) {
				Location location = event.getBlock().getLocation();
				new DelayedTask(() -> {
					if(event.getBlock().getType() == Material.FIRE) {
						event.getBlock().setType(Material.AIR);
					}
				}, 2);

				if(!isInBlockedFire(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
					Location blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
					blockedFire.add(blockLocation);
					new DelayedTask(() -> blockedFire.remove(blockLocation), 10L);
				}
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			DamageCause damageCause = event.getCause();
			final Player player = (Player) event.getEntity();
			if((damageCause == DamageCause.FIRE || damageCause == DamageCause.FIRE_TICK) && !noFireDamage.contains(player.getName())) {
				Location location = player.getLocation();
				if(isInBlockedFire(player.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
					event.setCancelled(true);
					noFireDamage.add(player.getName());
					new DelayedTask(() -> noFireDamage.remove(player.getName()), 20);
				}
			} else if((damageCause == DamageCause.FIRE || damageCause == DamageCause.FIRE_TICK) && noFireDamage.contains(player.getName())) {
				event.setCancelled(true);
				if(player.getFireTicks() > 0) {
					player.setFireTicks(0);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(event.getBucket() == Material.LAVA_BUCKET) {
			event.setCancelled(true);
		}
	}
		
	@EventHandler
	public void onGracePeriodEnding(GracePeriodEndEvent event) {
		blockedFire.clear();
		blockedFire = null;
		noFireDamage.clear();
		noFireDamage = null;
		HandlerList.unregisterAll(this);
	}
}