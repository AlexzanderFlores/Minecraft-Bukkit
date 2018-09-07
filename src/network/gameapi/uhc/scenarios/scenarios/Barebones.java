package network.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import network.customevents.game.GameDeathEvent;
import network.gameapi.uhc.scenarios.Scenario;
import network.player.MessageHandler;

public class Barebones extends Scenario {
    private static Barebones instance = null;

    public Barebones() {
        super("Barebones", "BB", Material.BONE);
        instance = this;
        setInfo("Emeralds, Diamonds & Gold all become Iron. A player dying will drop 1 Diamond, 1 Golden Apple, 32 Arrows & 2 String. You cannot craft an Enchantment Table, Anvil or Golden Apple.");
        setPrimary(true);
    }

    public static Barebones getInstance() {
        if(instance == null) {
            new Barebones();
        }
        return instance;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack item = event.getCurrentItem();
        Material type = item.getType();
        if(event.getWhoClicked() instanceof Player && (type == Material.ENCHANTMENT_TABLE || type == Material.ANVIL || type == Material.GOLDEN_APPLE)) {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            MessageHandler.sendMessage(player, "&b&l" + getName() + "&e: &cYou may not craft that item");
            event.setCurrentItem(new ItemStack(Material.AIR));
            event.setResult(Result.DENY);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameDeath(GameDeathEvent event) {
        Player player = event.getPlayer();
        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.DIAMOND));
        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLDEN_APPLE));
        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.ARROW, 32));
        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.STRING, 2));
        Player killer = event.getKiller();
        if(killer != null) {
            MessageHandler.sendMessage(killer, "&b&l" + getName() + "&e: Dropping 1 Diamond, 1 Golden Apple, 32 Arrows & 2 String");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if(type == Material.DIAMOND_ORE || type == Material.EMERALD_ORE || type == Material.GOLD_ORE) {
            event.getBlock().setType(Material.IRON_ORE);
            MessageHandler.sendMessage(event.getPlayer(), "&b&l" + getName() + "&e: Dropping Iron instead");
        }
    }
}