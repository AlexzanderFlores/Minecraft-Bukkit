package network.gameapi.games.uhcskywars.cages;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import network.gameapi.shops.SkyWarsShop;
import network.server.servers.hub.items.Features.Rarity;

@SuppressWarnings("deprecation")
public class SmallCage extends Cage {
	public SmallCage(ItemStack icon) {
		this(icon, -1);
	}
	
	public SmallCage(ItemStack icon, int slot) {
		super(icon, Rarity.COMMON, slot);
		setMaterial(icon.getType(), icon.getData().getData());
		setKitType("cage");
		setKitSubType("small_cage");
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
			Location location = player.getLocation();
			placeBlock(location.clone().add(0, -1, 0));
			for(int [] a : new int [] [] {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}) {
				int x = a[0];
				int z = a[1];
				for(int y = 0; y < 3; ++y) {
					placeBlock(location.clone().add(x, y, z));
				}
			}
			teleport(player);
		}
	}
}