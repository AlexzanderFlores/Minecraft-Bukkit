package network.gameapi;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class TemporaryFireUtil implements Listener {
	private int ticks = 0;
	
	public TemporaryFireUtil(int ticks) {
		this.ticks = ticks;
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.FIRE) {
			final Block block = event.getBlock();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					block.setType(Material.AIR);
				}
			}, ticks);
			event.setCancelled(false);
		}
	}
}
