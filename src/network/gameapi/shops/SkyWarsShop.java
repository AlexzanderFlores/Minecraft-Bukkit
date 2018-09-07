package network.gameapi.shops;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import network.Network.Plugins;
import network.customevents.player.InventoryItemClickEvent;
import network.gameapi.crates.SkyWarsCrate;
import network.gameapi.games.uhcskywars.cages.Cage;
import network.gameapi.games.uhcskywars.kits.Archer;
import network.gameapi.games.uhcskywars.kits.Bomber;
import network.gameapi.games.uhcskywars.kits.Builder;
import network.gameapi.games.uhcskywars.kits.CowSlayer;
import network.gameapi.games.uhcskywars.kits.Enchanter;
import network.gameapi.games.uhcskywars.kits.Enderman;
import network.gameapi.games.uhcskywars.kits.Fisherman;
import network.gameapi.games.uhcskywars.kits.Looter;
import network.gameapi.games.uhcskywars.kits.Medic;
import network.gameapi.games.uhcskywars.kits.Miner;
import network.gameapi.games.uhcskywars.kits.Ninja;
import network.gameapi.games.uhcskywars.kits.Pyro;
import network.gameapi.games.uhcskywars.kits.Spiderman;
import network.gameapi.kit.KitBase;
import network.server.DB;
import network.server.util.EffectUtil;

public class SkyWarsShop extends ShopBase {
	private static SkyWarsShop instance = null;
	
	public SkyWarsShop() {
		super("Shop - UHC Sky Wars", "kit.uhc_sky_wars.", DB.PLAYERS_COINS_SKY_WARS, Plugins.UHCSW, 3, 50);
		instance = this;
		new SkyWarsCrate();
		Cage.createCages();
		new Archer();
		new Builder();
		new Looter();
		new Enchanter();
		new Bomber();
		new Ninja();
		new Medic();
		new CowSlayer();
		new Enderman();
		new Fisherman();
		new Spiderman();
		new Pyro();
		new Miner();
	}
	
	public static SkyWarsShop getInstance() {
		if(instance == null) {
			new SkyWarsShop();
		}
		return instance;
	}
	
	@Override
	public void openShop(Player player, int page) {
		InventoryView view = player.getOpenInventory();
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
		pages.put(player.getName(), page);
		if(hasCrate(player, view)) {
			inventory.setItem(1, view.getItem(1));
			inventory.setItem(4, view.getItem(4));
			inventory.setItem(7, view.getItem(7));
		} else {
			SkyWarsCrate.addItem(player, inventory);
		}
		updateItems(player, inventory);
		String type = "";
		String subType = "";
		if(page == 1) {
			type = "kit";
		} else if(page == 2) {
			type = "cage";
			subType = "small_cage";
		} else if(page == 3) {
			type = "cage";
			subType = "big_cage";
		}
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getPluginData().equals(Plugins.UHCSW.getData()) && type.equals(kit.getKitType()) && subType.equals(kit.getKitSubType())) {
				inventory.setItem(kit.getSlot(), kit.getIcon(player));
			}
		}
		player.openInventory(inventory);
	}

	@Override
	public void updateInfoItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			updateInfoItem(player, player.getOpenInventory().getTopInventory());
		}
	}

	@Override
	public void updateInfoItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page == 1) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Kits Owned", "kit").getItem());
		} else if(page == 2) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Small Cages Owned", "cage", "small_cage").getItem());
		} else if(page == 3) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Big Cages Owned", "cage", "big_cage").getItem());
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.ARROW) {
				InventoryView inv = player.getOpenInventory();
				int size = inv.getTopInventory().getSize();
				if(event.getSlot() == size - 8) {
					openShop(player, getPage(player) - 1);
					return;
				} else if(event.getSlot() == size - 2) {
					openShop(player, getPage(player) + 1);
					return;
				}
			}
			for(KitBase kit : KitBase.getKits()) {
				String name = ChatColor.stripColor(event.getItemTitle());
				if(kit.getPluginData().equals(Plugins.UHCSW.getData()) && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
					if(!kit.use(player)) {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
					return;
				}
			}
		}
	}
}