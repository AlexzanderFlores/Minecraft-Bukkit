package network.gameapi.games.uhcskywars.cages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.ProPlugin;
import network.Network.Plugins;
import network.customevents.game.GameStartingEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.gameapi.kit.KitBase;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class CageSelector implements Listener {
	private String name = null;
	private ItemStack item = null;
	private Map<String, Integer> pages = null;
	
	public CageSelector() {
		name = "Cage Selector";
		item = new ItemCreator(Material.IRON_FENCE).setName("&b" + name).getItemStack();
		pages = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private int getPage(Player player) {
		if(!pages.containsKey(player.getName())) {
			pages.put(player.getName(), 1);
		}
		return pages.get(player.getName());
	}
	
	private void open(Player player) {
		open(player, getPage(player));
	}
	
	private void open(Player player, int page) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		pages.put(player.getName(), page);
		String type = "";
		String subType = "";
		if(page == 1) {
			type = "cage";
			subType = "small_cage";
		} else if(page == 2) {
			type = "cage";
			subType = "big_cage";
		}
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getPluginData().equals(Plugins.UHCSW.getData()) && type.equals(kit.getKitType()) && subType.equals(kit.getKitSubType())) {
				inventory.setItem(kit.getSlot() - 18, kit.getIcon(player));
			}
		}
		if(page > 1) {
			inventory.setItem(18, new ItemCreator(Material.ARROW).setName("&bPage #" + (page - 1)).getItemStack());
		}
		if(page < 2) {
			inventory.setItem(26, new ItemCreator(Material.ARROW).setName("&bPage #" + (page + 1)).getItemStack());
		}
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(Network.getMiniGame().getJoiningPreGame()) {
			event.getPlayer().getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item.equals(this.item)) {
			event.setCancelled(true);
			open(player);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.ARROW) {
				if(event.getSlot() == 18) {
					open(player, getPage(player) - 1);
					return;
				} else if(event.getSlot() == 26) {
					open(player, getPage(player) + 1);
					return;
				}
			}
			for(KitBase kit : KitBase.getKits()) {
				String name = ChatColor.stripColor(event.getItemTitle());
				if(kit.getPluginData().equals(Plugins.UHCSW.getData()) && name.startsWith(kit.getName()) && kit.getSlot() - 18 == event.getSlot()) {
					if(kit.use(player)) {
						player.closeInventory();
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameStarting(GameStartingEvent event) {
		HandlerList.unregisterAll(this);
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().remove(Material.IRON_FENCE);
		}
		name = null;
		item = null;
		pages.clear();
		pages = null;
	}
}