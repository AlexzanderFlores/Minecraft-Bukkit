package network.server.effects.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class HollowCylinderUtil {
	private List<Block> blocks = null;
	
	public HollowCylinderUtil(String world, int centerX, int centerY, int centerZ, int radius, int height, Material type) {
		this(Bukkit.getWorld(world), centerX, centerY, centerZ, radius, height, type, (byte) 0);
	}
	
	public HollowCylinderUtil(String world, int centerX, int centerY, int centerZ, int radius, int height, Material type, byte data) {
		this(Bukkit.getWorld(world), centerX, centerY, centerZ, radius, height, type, data);
	}
	
	public HollowCylinderUtil(World world, int centerX, int centerY, int centerZ, int radius, int height, Material type) {
		this(world, centerX, centerY, centerZ, radius, height, type, (byte) 0);
	}
	
	public HollowCylinderUtil(World world, int centerX, int centerY, int centerZ, int radius, int height, Material type, byte data) {
		blocks = new ArrayList<Block>();
		for(Block block : new CylinderUtil(world, centerX, centerY, centerZ, radius, height, type, data).getBlocks()) {
			blocks.add(block);
		}
		new CylinderUtil(world, centerX, centerY, centerZ, radius - 1, height, Material.AIR, (byte) 0);
	}
	
	public List<Block> getBlocks() {
		return blocks;
	}
}
