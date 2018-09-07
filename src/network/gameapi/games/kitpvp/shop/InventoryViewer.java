package network.gameapi.games.kitpvp.shop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.Network.Plugins;
import network.customevents.player.InventoryItemClickEvent;
import network.gameapi.games.kitpvp.events.InventoryViewClickEvent;
import network.player.CoinsHandler;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class InventoryViewer implements Listener {
    private Map<String, Map<Integer, Integer>> slots = null;
    protected String name = null;
    protected CoinsHandler coinsHandler = null;

    public InventoryViewer(String name) {
        slots = new HashMap<String, Map<Integer, Integer>>();
        this.name = name;
        coinsHandler = CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData());
        EventUtil.register(this);
    }

    public void open(Player player) {
        slots.put(player.getName(), new HashMap<Integer, Integer>());
        ItemStack dye = new ItemCreator(Material.INK_SACK, 8).setName(" ").getItemStack();
        Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
        // Place armor
        int [] slots = new int [] {7, 5, 3, 1};
        for(int a = 0; a < slots.length; ++a) {
            ItemStack armor = player.getInventory().getArmorContents()[a];
            if(armor == null || armor.getType() == Material.AIR) {
                armor = dye;
            } else {
                this.slots.get(player.getName()).put(slots[a], 36 + a);
            }
            inventory.setItem(slots[a], armor);
        }
        // Place the rest of the inventory
        for(int a = 0; a < 9 * 4; ++a) {
            ItemStack item = player.getInventory().getItem(a);
            if(item == null || item.getType() == Material.AIR) {
                item = dye;
            } else {
                this.slots.get(player.getName()).put(a + 18, a);
            }
            inventory.setItem(a + 18, item);
        }
        // Open inventory and register events
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(slots != null && event.getTitle().equals(name)) {
            Player player = event.getPlayer();
            int slot = event.getSlot();
            if(slots.get(player.getName()).containsKey(slot)) {
                Bukkit.getPluginManager().callEvent(new InventoryViewClickEvent(player, event.getTitle(), slots.get(player.getName()).get(slot), slot));
            } else {
                ItemStack item = event.getItem();
                if(item.getType() == Material.INK_SACK) {
                    EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
                }
            }
            event.setCancelled(true);
        }
    }
}