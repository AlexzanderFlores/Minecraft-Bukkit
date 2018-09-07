package network.server.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import network.customevents.TimeEvent;
import network.customevents.player.PlayerAFKEvent;

public abstract class CircleUtil implements Listener {
	private Entity entity = null;
	private Location location = null;
	private double radius = 0;
	private int iterations = 12;
	private int counter = 0;
	
	public CircleUtil(Entity entity, double radius, int iterations) {
		this(entity.getLocation(), radius, iterations);
		this.entity = entity;
	}
	
	public CircleUtil(Location location, double radius, int iterations) {
		this.location = location;
		this.radius = radius;
		this.iterations = iterations;
		EventUtil.register(this);
	}
	
	public abstract void run(Vector vector, Location location);
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			if(entity instanceof Player) {
				Player player = (Player) entity;
				if(!player.isOnline()) {
					delete();
					return;
				} else if(PlayerAFKEvent.isAFK(player)) {
					return;
				}
			}
			if(counter >= 360) {
				counter = 0;
			} else {
				Location location = this.location.clone();
				if(entity != null) {
					location = entity.getLocation();
				}
				counter += iterations;
				double angle = counter * Math.PI / 180;
				double x = (double) (location.getX() + radius * Math.cos(angle));
				double z = (double) (location.getZ() + radius * Math.sin(angle));
				Location newLoc = new Location(location.getWorld(), x, location.getY(), z);
				double directionX = newLoc.getX() - location.getX();
				double directionY = newLoc.getY() - location.getY();
				double directionZ = newLoc.getZ() - location.getZ();
				double factor = Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2) + Math.pow(directionZ, 2));
				double velocityX = directionX * factor;
				double velocityY = directionY * factor;
				double velocityZ = directionZ * factor;
				Vector vector = new Vector(velocityX, velocityY, velocityZ);
				run(vector, location.add(vector));
			}
		}
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public void delete() {
		HandlerList.unregisterAll(this);
		entity = null;
		location = null;
	}
}
