package network.gameapi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class TimeOfDay implements Listener {
	private String name = null;
	private ItemStack item = null;
	
	public TimeOfDay() {
		name = "Time of Day";
		item = new ItemCreator(Material.WATCH).setName("&a" + name).getItemStack();
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(Network.getMiniGame().getJoiningPreGame()) {
			event.getPlayer().getInventory().setItem(7, item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item != null && item.equals(this.item)) {
			if(Ranks.PLAYER.hasRank(player)) {
				Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
				inventory.setItem(11, new ItemCreator(Material.WOOL, 1).setName("&bMorning").setLores(new String [] {"", "&7Ticks: &a23250", ""}).getItemStack());
				inventory.setItem(13, new ItemCreator(Material.WOOL, 4).setName("&bDay").setLores(new String [] {"", "&7Ticks: &a6000", ""}).getItemStack());
				inventory.setItem(15, new ItemCreator(Material.WOOL, 15).setName("&bNight").setLores(new String [] {"", "&7Ticks: &a18000", ""}).getItemStack());
				player.openInventory(inventory);
			} else {
				MessageHandler.sendMessage(player, Ranks.VIP.getNoPermission());
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			int slot = event.getSlot();
			if(slot == 12) {
				player.setPlayerTime(23250, false);
			} else if(slot == 14) {
				player.setPlayerTime(6000, false);
			} else if(slot == 16) {
				player.setPlayerTime(18000, false);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
