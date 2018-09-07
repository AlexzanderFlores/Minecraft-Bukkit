package network.gameapi.games.uhcskywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import network.Network.Plugins;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.SkyWarsShop;
import network.server.servers.hub.items.Features.Rarity;
import network.server.util.ItemCreator;
import network.server.util.UnicodeUtil;

public class Archer extends KitBase {
	private static final int amount = 6;
	
	public Archer() {
		super(Plugins.UHCSW, new ItemCreator(Material.BOW).setName("Archer").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aBow",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Arrows",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1, 19);
	}
	
	public static Rarity getRarity() {
		return Rarity.COMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemStack(Material.BOW));
			player.getInventory().addItem(new ItemStack(Material.ARROW, amount));
		}
	}
	
	@Override
	public void execute(Player player) {
	}
}