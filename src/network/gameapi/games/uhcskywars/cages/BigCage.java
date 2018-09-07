package network.gameapi.games.uhcskywars.cages;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import network.gameapi.shops.SkyWarsShop;
import network.server.servers.hub.items.Features.Rarity;

@SuppressWarnings("deprecation")
public class BigCage extends Cage {
	public BigCage(ItemStack icon) {
		this(icon, -1);
	}
	
	public BigCage(ItemStack icon, int slot) {
		super(icon, Rarity.UNCOMMON, slot);
		setMaterial(icon.getType(), icon.getData().getData());
		setKitType("cage");
		setKitSubType("big_cage");
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}
	
	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		if(player != null) {
			Bukkit.getLogger().info(player.getName() + " used " + getName());
			Location location = player.getLocation();
			placeBlock(location.clone().add(0, -1, 0));
			Block min = location.getBlock().getRelative(-3, -1, -3);
			Block max = location.getBlock().getRelative(3, 2, 3);
			World world = player.getWorld();
			int x1 = min.getX();
			int y1 = min.getY();
			int z1 = min.getZ();
			int x2 = max.getX();
			int y2 = max.getY();
			int z2 = max.getZ();
			for(int x = x1; x <= x2; ++x) {
				for(int y = y1; y <= y2; ++y) {
					for(int z = z1; z <= z2; ++z) {
						Block block = world.getBlockAt(x, y, z);
						if(block.getType() != Material.AIR) {
							block.setType(Material.AIR);
						}
						if(x == x1 || x == x2 || y == y1 || z == z1 || z == z2) {
							placeBlock(block);
						}
					}
				}
			}
			teleport(player);
		}
	}
}