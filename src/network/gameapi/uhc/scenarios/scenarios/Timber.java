package network.gameapi.uhc.scenarios.scenarios;

import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class Timber implements Listener {
	public Timber() {
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Material below = block.getRelative(0, -1, 0).getType();
		if(below == Material.DIRT || below == Material.SAND) {
			Location location = block.getLocation().clone();
			int id = block.getTypeId();
			while(id == 162 || id == 17) {
				EffectUtil.displayParticles(block.getType(), block.getLocation());
				for(ItemStack itemStack : block.getDrops()) {
					block.getWorld().dropItem(location, itemStack);
				}
				block.setType(Material.AIR);
				block = block.getRelative(0, 1, 0);
				id = block.getTypeId();
			}
		}
	}
}
