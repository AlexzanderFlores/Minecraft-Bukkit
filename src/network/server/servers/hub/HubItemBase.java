package network.server.servers.hub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public abstract class HubItemBase implements Listener {
	private static List<HubItemBase> items = null;
	public ItemCreator item = null;
	public int slot = -1;
	
	public HubItemBase(ItemCreator item, int slot) {
		this.item = item;
		this.slot = slot;
		EventUtil.register(this);
		if(items == null) {
			items = new ArrayList<HubItemBase>();
		}
		items.add(this);
	}
	
	public void giveItem(Player player) {
		player.getInventory().setItem(slot, item.getItemStack());
	}
	
	public ItemStack getItem() {
		return item.getItemStack();
	}
	
	public void setItem(ItemStack itemStack) {
		this.item.setItemStack(itemStack);
	}
	
	public String getName() {
		return item.getName();
	}
	
	public void setName(String name) {
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public boolean isItem(Player player) {
		ItemStack item = player.getItemInHand();
		return item.getType() != Material.AIR && item.getItemMeta().getDisplayName() != null && getName().startsWith(item.getItemMeta().getDisplayName());
	}
	
	public abstract void onPlayerJoin(PlayerJoinEvent event);
	public abstract void onMouseClick(MouseClickEvent event);
	public abstract void onInventoryItemClick(InventoryItemClickEvent event);
	
	public static void giveItems(Player player) {
		for(HubItemBase item : items) {
			item.giveItem(player);
		}
	}
}
