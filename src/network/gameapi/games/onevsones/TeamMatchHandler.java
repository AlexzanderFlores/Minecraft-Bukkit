package network.gameapi.games.onevsones;

import network.customevents.player.InventoryItemClickEvent;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class TeamMatchHandler implements Listener {
    private String name = null;

    public TeamMatchHandler(Location location) {
        name = "Team Matches";

        new NPCEntity(EntityType.ZOMBIE, "&e" + name, location, Material.GOLDEN_APPLE) {
            @Override
            public void onInteract(Player player) {
                Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);

                inventory.setItem(11, new ItemCreator(Material.GOLD_SWORD).setName("&e2v2").getItemStack());
                inventory.setItem(13, new ItemCreator(Material.IRON_SWORD).setName("&e3v3").getItemStack());
                inventory.setItem(15, new ItemCreator(Material.DIAMOND_SWORD).setName("&e4v4").getItemStack());

                player.openInventory(inventory);
            }
        };

        EventUtil.register(this);
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getInventory().getTitle().equals(name)) {
            event.setCancelled(true);
        }
    }
}
