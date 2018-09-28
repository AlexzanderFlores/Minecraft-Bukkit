package network.gameapi.games.uhc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;

import network.gameapi.SpectatorHandler;
import network.gameapi.games.uhc.border.BorderHandler;
import network.server.util.EventUtil;

public class PortalHandler implements Listener {
	private static World world = null;
	private static World nether = null;
	private static World end = null;
	
	public PortalHandler() {
		world = WorldHandler.getWorld();
		nether = WorldHandler.getNether();
		end = WorldHandler.getEnd();
		if(OptionsHandler.getEnd()) {
			end.spawnEntity(new Location(end, 0, 125, 0),  EntityType.ENDER_DRAGON);
		}
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPortalCreate(PortalCreateEvent event) {
		event.setCancelled(false);
	}
	
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if(SpectatorHandler.contains(player)) {
			event.setCancelled(true);
		} else {
			TravelAgent agent = event.getPortalTravelAgent();
			Location fromLocation = event.getFrom();
			String fromWorldName = fromLocation.getWorld().getName();
			World toWorld = null;
			TeleportCause tpCause = event.getCause();
			if(fromWorldName.equalsIgnoreCase(nether.getName())) {
				toWorld = world;
			} else if(fromWorldName.equalsIgnoreCase(world.getName()) && tpCause == TeleportCause.NETHER_PORTAL) {
				toWorld = nether;
			} else if(fromWorldName.equalsIgnoreCase(world.getName()) && tpCause == TeleportCause.END_PORTAL && OptionsHandler.getEnd()) {
				toWorld = end;
			} else if(fromWorldName.equalsIgnoreCase(end.getName())) {
				toWorld = world;
			}
			if(toWorld != null) {
				Location toLocation = getPortalLocation(agent, fromLocation, toWorld);
				if(toLocation != null) {
					event.setTo(toLocation);
				}
			}
		}
	}
	
	public static Location getPortalLocation(TravelAgent agent, Location fromLocation, World toWorld) {
		Location location = null;
		Location search = null;
		agent.setCanCreatePortal(true);
		agent.setCreationRadius(10);
		agent.setSearchRadius(50);
		Environment fromEnv = fromLocation.getWorld().getEnvironment();
		Environment toEnv = toWorld.getEnvironment();
		if(fromEnv == Environment.NETHER) {
			search = new Location(toWorld, fromLocation.getX() * 8, fromLocation.getY(), fromLocation.getZ() * 8);
			location = findValidPortalLocation(search, agent, true);
		} else if(fromEnv == Environment.NORMAL && toEnv == Environment.NETHER) {
			search = new Location(toWorld, fromLocation.getX() / 8D, fromLocation.getY(), fromLocation.getZ() / 8D);
			location = findValidPortalLocation(search, agent, false);
		} else if(fromEnv == Environment.NORMAL && toEnv == Environment.THE_END) {
			// create platform underneath player
			int y = 48;
			for(int a = 0; a <= 2; a++) {
				for(int x = 98; x <= 102; x++) {
					for(int z = -2; z <= 2; z++) {
						toWorld.getBlockAt(x, y, z).setType(y == 48 ? Material.OBSIDIAN : Material.AIR);
					}
				}
				y++;
			}
			// player will always be teleported to these coordinates when going to the end
			location = new Location(toWorld, 100, 49, 0);
		} else if(fromEnv == Environment.THE_END) {
			location = toWorld.getSpawnLocation();
			location.setY(255);
			boolean con = true;
			while(con) {
				if(!location.getBlock().getType().isSolid()) {
					location.add(0, -1, 0);
				} else {
					location.add(0, 2, 0);
					con = false;
				}
			}
		}
		return location;
	}
	
	public static Location findValidPortalLocation(Location search, TravelAgent agent, boolean teleportingToOverworld) {
		Location portalLocation = null;
		int radius = teleportingToOverworld ? BorderHandler.getOverworldBorder().getRadius() : BorderHandler.getNetherBorder().getRadius();
		boolean con = true;
		while(con) {
			portalLocation = agent.findOrCreate(search);
			if(portalLocation == search || BorderHandler.getDistance(portalLocation) > radius || portalLocation.getBlock().getType() != Material.PORTAL) {
				search = getNextSearchLocation(search);
			} else {
				con = false;
			}
		}
		return portalLocation;
	}
	
	public static Location getNextSearchLocation(Location originalSearch) {
		Location newSearch = originalSearch;
		double distance = BorderHandler.getDistance(originalSearch) - 10;
		double angle = getAngleFromCenter(originalSearch.getX(), originalSearch.getZ());
		angle = getReferenceAngle(angle);
		angle = Math.toRadians(angle);
		double newX = distance * Math.cos(angle);
		double newZ = distance * Math.sin(angle);
		newSearch.setX(newX);
		newSearch.setZ(newZ);
		return newSearch;
	}
	
	public static double getReferenceAngle(double angle) {
		while(angle < 0) {
			angle += 360;
		}
		while(angle > 360) {
			angle -= 360;
		}
		if(angle > 90 && angle <= 180) {
			angle = 180 - angle;
		} else if(angle > 180 && angle <= 270) {
			angle = angle - 180;
		} else if(angle > 270 && angle <= 360) {
			angle = 360 - angle;
		}
		return angle;
	}
	
	public static double getAngleFromCenter(double x, double z) {
		double angle = Math.toDegrees(Math.atan2(z, x));
		if(angle < 0) {
			angle += 360;
		}
		return angle;
	}
	
}