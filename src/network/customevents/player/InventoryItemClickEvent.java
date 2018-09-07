package network.customevents.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.server.util.EventUtil;

public class InventoryItemClickEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Player player = null;
    private ItemStack item = null;
    private String itemTitle = null;
    private String title = null;
    private ClickType clickType = null;
    private int slot = 0;
    private Inventory inv = null;
    private SlotType slotType = null;
    
    public InventoryItemClickEvent() {
    	EventUtil.register(this);
    }
    
    public InventoryItemClickEvent(Player player, ItemStack item, String title, ClickType clickType, int slot, Inventory inv, SlotType slotType) {
    	this.player = player;
    	this.item = item;
    	if(item != null) {
    		this.itemTitle = item.getItemMeta().getDisplayName();
    	}
    	this.title = title;
    	this.clickType = clickType;
    	this.slot = slot;
    	this.inv = inv;
    	this.slotType = slotType;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public ItemStack getItem() {
    	return this.item;
    }
    
    public String getItemTitle() {
    	return this.itemTitle;
    }
    
    public String getTitle() {
    	return this.title;
    }
    
    public ClickType getClickType() {
    	return this.clickType;
    }
    
    public int getSlot() {
    	return this.slot;
    }
    
    public Inventory getInventory() {
    	return inv;
    }
    
    public SlotType getSlotType() {
    	return slotType;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    	if(event.getWhoClicked() instanceof Player && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getInventory().getTitle() != null) {
        	Player player = (Player) event.getWhoClicked();
    		InventoryItemClickEvent itemClickEvent = new InventoryItemClickEvent(player, event.getCurrentItem(), event.getInventory().getTitle(), event.getClick(), event.getSlot(), event.getInventory(), event.getSlotType());
    		Bukkit.getPluginManager().callEvent(itemClickEvent);
    		if(itemClickEvent.isCancelled()) {
    			event.setCancelled(true);
    		} else {
    			event.setCancelled(false);
    		}
    	}
    }
}
