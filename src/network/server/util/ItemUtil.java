package network.server.util;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import network.ProPlugin;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;

public class ItemUtil {
	public static ItemStack getSkull(String name) {
		return getSkull(name, new ItemStack(Material.SKULL_ITEM, 1, (byte) 3));
	}
	
	public static ItemStack getSkull(String name, ItemStack itemStack) {
		ItemStack item = itemStack;
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(name);
		item.setItemMeta(meta);
		return itemStack;
	}
	
	public static boolean isItem(ItemStack one, ItemStack two) {
		try {
			return one.getType() == two.getType() && one.getItemMeta().getDisplayName().equals(two.getItemMeta().getDisplayName());
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public static Inventory getPlayerSelector() {
		return null;
	}
	
	public static int getInventorySize(int size) {
		if(size < 9) {
			size = 9;
		}
		while(size % 9 != 0) {
			++size;
		}
		return size;
	}
	
	public static void addEnchantGlassPaneIncrement(Inventory inventory) {
		byte data = 0;
		for(int a = 0; a < inventory.getSize(); ++a) {
			ItemStack itemStack = inventory.getItem(a);
			if(itemStack == null || itemStack.getType() == Material.AIR) {
				itemStack = new ItemCreator(Material.STAINED_GLASS_PANE, data++).setName(" ").setGlow(true).getItemStack();
				if(data > 15) {
					data = 0;
				}
				inventory.setItem(a, itemStack);
			}
		}
	}
	
	public static void addEnchantGlassPane(Inventory inventory, byte data) {
		for(int a = 0; a < inventory.getSize(); ++a) {
			ItemStack itemStack = inventory.getItem(a);
			if(itemStack == null || itemStack.getType() == Material.AIR) {
				itemStack = new ItemCreator(Material.STAINED_GLASS_PANE, data).setName(" ").setGlow(true).getItemStack();
				inventory.setItem(a, itemStack);
			}
		}
	}
	
	public static Inventory getPlayerSelector(Player player, String name) {
		return getPlayerSelector(player, name, false);
	}
	
	public static Inventory getPlayerSelector(Player player, String name, boolean removeStaff) {
		List<Player> players = ProPlugin.getPlayers();
		int inventorySize = ItemUtil.getInventorySize(players.size());
		Inventory inventory = Bukkit.createInventory(player, inventorySize, name);
		boolean hasItem = false;
		for(Player online : players) {
			if(removeStaff && Ranks.isStaff(online)) {
				continue;
			}
			inventory.addItem(new ItemCreator(ItemUtil.getSkull(online.getName())).setName(online.getName()).getItemStack());
			hasItem = true;
		}
		if(hasItem) {
			return inventory;
		} else {
			MessageHandler.sendMessage(player, "&cNo one to teleport to");
			return null;
		}
	}
	
	public static ItemStack colorArmor(ItemStack item, int r, int g, int b) {
		return colorArmor(item, Color.fromRGB(r, g, b));
	}
	
	public static ItemStack colorArmor(ItemStack item, Color color) {
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(color);
		item.setItemMeta(meta);
		return item;
	}
}
