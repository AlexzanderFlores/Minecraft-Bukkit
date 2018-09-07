package network.server;

import npc.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class Campfire implements Listener {
	private List<ArmorStand> stands = null;
	
	public Campfire(Location location) {
		stands = new ArrayList<ArmorStand>();
		World world = location.getWorld();
		float counter = 0.0f;
		for(int a = 0; a < 4; ++a) {
			ArmorStand armorStand = (ArmorStand) world.spawnEntity(new Location(world, location.getX(), location.getY() - 1.3, location.getZ(), counter, 0.0f), EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			armorStand.setFireTicks(20 * 60 * 60 * 24);
			counter += 45.0f;
			stands.add(armorStand);
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof ArmorStand && stands.contains(event.getEntity())) {
			event.setCancelled(true);
		}
	}
}
