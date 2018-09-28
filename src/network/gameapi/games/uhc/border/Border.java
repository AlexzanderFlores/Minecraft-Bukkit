package network.gameapi.games.uhc.border;

import network.customevents.TimeEvent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import network.server.util.EventUtil;

public class Border implements Listener {
	private int startingRadius = 0;
	private int radius = 0;
	private int y = 0;
	private int yLimit = 0;
	private World world = null;
	private boolean placeBorder = false;
	private boolean pregen = false;
	
	public Border(int radius, World world) {
		this.startingRadius = radius;
		this.radius = radius;
		this.world = world;
		yLimit = world.getEnvironment() == Environment.NETHER ? 150 : 250;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 5 && placeBorder) {
			for(int x = -radius; x <= radius; ++x) {
				for(int z = -radius; z <= radius; ++z) {
					int distance = (int) Math.sqrt((0 - x) * (0 - x) + (0 - z) * (0 - z));
					if(distance == radius) {
						Block block = world.getBlockAt(x, y, z);
						block.setType(Material.BEDROCK);
					}
				}
			}

			if(++y >= yLimit) {
				placeBorder = false;
				y = 0;
				for(Entity entity : world.getEntities()) {
					if(BorderHandler.getDistance(entity.getLocation()) > radius) {
						entity.remove();
					}
				}
				TimeEvent.getHandlerList().unregister(this);
			}
		}
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getEntity().getWorld().getName().equalsIgnoreCase(world.getName()) && BorderHandler.getDistance(event.getLocation()) > startingRadius) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if(event.getEntity().getWorld().getName().equalsIgnoreCase(world.getName()) && BorderHandler.getDistance(event.getLocation()) > startingRadius) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		if(event.getBlock().getWorld().getName().equalsIgnoreCase(world.getName()) && BorderHandler.getDistance(event.getBlock().getLocation()) > startingRadius) {
			event.setCancelled(true);
		}
	}
	
	public void pregenSettings() {
		if(!pregen) {
			pregen = true;
			setBorder(radius);
			EventUtil.register(this);
			for(Entity entity : world.getEntities()) {
				if(BorderHandler.getDistance(entity.getLocation()) > startingRadius) {
					entity.remove();
				}
			}
		}
	}
	
	public void setBorder(int radius) {
		this.radius = radius;
		y = 0;
		placeBorder = true;
		BorderHandler.updateScoreboard();
	}
	
	public boolean getPregen() {
		return pregen;
	}
	
	public boolean isShrinking() {
		return startingRadius > radius;
	}
	
	public World getWorld() {
		return world;
	}
	
	public int getStartingRadius() {
		return startingRadius;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public boolean getPlaceBorder() {
		return placeBorder;
	}
	
	public int getY() {
		return y;
	}
}