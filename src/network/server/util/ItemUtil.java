package network.server.util;

import java.util.ArrayList;
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

	public static void displayGameGlass(Inventory inventory) {
		List<Integer> slots = new ArrayList<>();

		for(int a = 0; a < inventory.getSize(); ++a) {
			double row = Math.floor(a / 9);
			if(a <= 8 || a % 9 == 0 || a >= inventory.getSize() - 9 || a == (8 * (row + 1) + row)) {
				slots.add(a);
			}
		}

		// 00 01 02 03 04 05 06 07 08
		// 09 10 11 12 13 14 15 16 17
		// 18 19 20 21 22 23 24 25 26
		// 27 28 29 30 31 32 33 34 35
		// 36 37 38 39 40 41 42 43 44
		// 45 46 47 48 49 50 51 52 53

		displayGameGlass(inventory, slots);
	}

	public static void displayGameGlass(Inventory inventory, List<Integer> slots) {
		for(int slot : slots) {
			try {
				ItemStack itemStack = inventory.getItem(slot);
				Material material = itemStack == null ? null : itemStack.getType();
				if(itemStack == null || material == null || material == Material.AIR) {
					inventory.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, (byte) 13).setGlow(true).setName(" ").getItemStack());
				}
			} catch(IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
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
