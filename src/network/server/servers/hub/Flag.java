package network.server.servers.hub;

import network.customevents.TimeEvent;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Flag implements Listener {
	private World world = null;
	private Location location = null;
	private Map<ArmorStand, Integer> flags = null;
	private Map<Integer, Double> movement = null;
	private byte color = 11;
	private enum Direction {
		MIN(-1),
		CENTER_GOING_MIN(0),
		CENTER_GOING_MAX(0),
		MAX(1);
		
		private int counter = 0;
		
		private Direction(int counter) {
			this.counter = counter;
		}
		
		public int getCounter() {
			return this.counter;
		}
		
		public Direction getNext() {
			return this == MIN ? CENTER_GOING_MAX : this == CENTER_GOING_MIN ? MIN : this == CENTER_GOING_MAX ? MAX : CENTER_GOING_MIN;
		}
	}
	private Direction direction = null;
	
	public Flag() {
		world = Bukkit.getWorlds().get(0);
		location = new Location(world, 1673.5, 3.75, -1296.5);
		flags = new HashMap<ArmorStand,Integer>();
		movement = new HashMap<Integer, Double>();
		movement.put(1, 0.025);
		movement.put(2, 0.050);
		movement.put(3, 0.075);
		movement.put(4, 0.100);
		direction = Direction.CENTER_GOING_MIN;
		float counter = 0.0f;
		for(int a = 0; a < 4; ++a) {
			Location armorStandLocation = new Location(world, location.getX(), location.getY(), location.getZ(), counter, 0.0f);
			ArmorStand armorStand = (ArmorStand) world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			counter += 45.0f;
		}
		double [] heights = new double [] {9.65, 9.05, 8.45};
		for(double y : heights) {
			Location newLoc = location.clone();
			newLoc.setY(y);
			ArmorStand armorStand = (ArmorStand) world.spawnEntity(newLoc, EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			armorStand.setVisible(false);
			armorStand.setHelmet(new ItemStack(Material.WOOL, 1, color));
		}
		ArmorStand armorStand = (ArmorStand) world.spawnEntity(location.clone().add(0, 6.85, 0), EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setSmall(true);
		armorStand.setHelmet(new ItemStack(Material.WOOD));
		for(int a = 0; a < 3; ++a) {
			for(int b = 1; b < 5; ++b) {
				double x = 0.60 * b;
				for(double y : heights) {
					double move = a == 0 ? movement.get(b) * -1 : a == 1 ? 0 : movement.get(b);
					Location newLoc = location.clone().add(x, 0, move);
					newLoc.setY(y);
					armorStand = (ArmorStand) world.spawnEntity(newLoc, EntityType.ARMOR_STAND);
					armorStand.setGravity(false);
					armorStand.setVisible(false);
					double diff = location.getZ() - newLoc.getZ();
					int index = diff < 0 ? Direction.MIN.getCounter() : diff == 0 ? Direction.CENTER_GOING_MAX.getCounter() : Direction.MAX.getCounter();
					flags.put(armorStand, index);
				}
			}
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 3) {
			for(ArmorStand armorStand : flags.keySet()) {
				if(flags.get(armorStand) == direction.getCounter()) {
					armorStand.setHelmet(new ItemStack(Material.WOOL, 1, color));
				}
			}
			for(ArmorStand armorStand : flags.keySet()) {
				if(flags.get(armorStand) != direction.getCounter()) {
					armorStand.setHelmet(new ItemStack(Material.AIR));
				}
			}
			direction = direction.getNext();
		}
	}
}
