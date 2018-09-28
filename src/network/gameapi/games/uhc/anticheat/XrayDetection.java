package network.gameapi.games.uhc.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;

import network.Network;
import network.gameapi.games.uhc.WorldHandler;
import network.gameapi.games.uhc.border.BorderHandler;
import network.server.util.EventUtil;

public class XrayDetection implements Listener {
	private int taskID = 0;
	private int x = 0;
	private int z = 0;
	
	public XrayDetection() {
		World world = WorldHandler.getWorld();
		int radius = BorderHandler.getOverworldBorder().getRadius();
		x = -radius;
		z = -radius;

		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Network.getInstance(), () -> {
			double distance = Math.sqrt((0 - x) * (0 - x) + (0 - z) * (0 - z));
			Bukkit.getLogger().info("Distance: " + distance);
			Bukkit.getLogger().info(x + ", " + z);

			for(int y = 30; y > 6; --y) {
				Block block = world.getBlockAt(x, y, z);
				if(block.getType() == Material.STONE) {
					boolean aroundStone = true;
					for(BlockFace face : new BlockFace [] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
						Block relative = block.getRelative(face);
						if(relative.getType() != Material.STONE && relative.getType() != Material.DIAMOND_ORE) {
							aroundStone = false;
							break;
						}
					}
					if(aroundStone) {
						block.setType(Material.DIAMOND_ORE);
					}
				}
			}
			if(z < 1500) {
				++z;
			} else if(x < 1500) {
				++x;
			} else {
				Bukkit.getScheduler().cancelTask(taskID);
				Bukkit.getLogger().info("Cancelled task!");
			}
		}, 1, 1);

		EventUtil.register(this);
	}
}
