package network.gameapi.games.onevsones;

import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.competitive.EloRanking;
import network.player.MessageHandler;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;
import npc.NPCEntity;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.List;

public class MonthlyTournaments implements Listener {
    private String name = null;
    private List<String> players = null;

    public MonthlyTournaments(Location location) {
        name = "Monthly Tournaments";
        players = new ArrayList<>();

        new NPCEntity(EntityType.SQUID, "&e" + name + "&c (Coming Soon)", location, Material.DIAMOND) {
            @Override
            public void onInteract(Player player) {
                MessageHandler.sendMessage(player, "&cThese will be released once we have a larger player base.");
//                Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
//
//                inventory.setItem(38, new ItemCreator(Material.WOOL, DyeColor.LIME.getData())
//                        .setName("&eJoin Monthly Tournament")
//                        .addLore("")
//                        .addLore("&bDifferent ways to enter:")
//                        .addLore("   &e- Diamond Rank (Top " + EloRanking.EloRank.DIAMOND.getPercentRange() + " players)")
//                        .addLore("   &e- Tournament Ticket &b/buy")
//                        .addLore("")
//                        .addLore("&b1st Place:")
//                        .addLore("   &e$20 via PayPal")
//                        .addLore("   &e$20 Store Credit")
//                        .addLore("   &e100 Ranked Matches")
//                        .addLore("").getItemStack());
//
//                inventory.setItem(40, new ItemCreator(Material.WATCH)
//                        .setName("&eQueuing")
//                        .addLore("")
//                        .addLore("&eMonthly Tournaments are ran on the")
//                        .addLore("&e1st Saturday of each month at 3PM PST")
//                        .addLore("").getItemStack());
//
//                inventory.setItem(42, new ItemCreator(Material.WOOL, DyeColor.RED.getData())
//                        .setName("&cLeave Tournament")
//                        .addLore("")
//                        .addLore("&bThis will _remove you from the")
//                        .addLore("&bcurrent tournament queue")
//                        .addLore("").getItemStack());
//
//                player.openInventory(inventory);
//
//                setPlayerHeads(player);
            }
        };

        EventUtil.register(this);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntityType() == EntityType.SQUID) {
            event.setCancelled(true);
        }
    }

    private void setPlayerHeads(Player player) {
        InventoryView view = player.getOpenInventory();
        int slot = 10;
        for(String name : players) {
            view.getTopInventory().setItem(slot++, new ItemCreator(ItemUtil.getSkull(name)).setName(name).getItemStack());
            if(slot == 17) {
                slot += 2;
            }
        }
    }

    private boolean removePlayer(String name) {
        name = name.toLowerCase();
        if(players.contains(name)) {
            players.remove(name);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryItemClickEvent event) {
        if(event.getInventory().getTitle().equals(name)) {
            if(event.getItem().getType() == Material.WOOL) {
                Player player = event.getPlayer();

                byte data = event.getItem().getData().getData();
                if(data == DyeColor.LIME.getData()) {

                    // Are they in Diamond?
                    if(EloRanking.getRank(player) != EloRanking.EloRank.DIAMOND) {
                        MessageHandler.sendMessage(player, "&cYou must be in " + EloRanking.EloRank.DIAMOND.getPrefix() + "&xto join.");
                        MessageHandler.sendMessage(player, "Or you can purchase an entry ticket &b/buy");
                        player.closeInventory();
                        event.setCancelled(true);
                        return;
                    }

                    if(players.contains(player.getName())) {
                        MessageHandler.sendMessage(player, "&cYou are already in this tournament.");
                        player.closeInventory();
                    } else {
                        players.add(player.getName().toLowerCase());
                        setPlayerHeads(player);
                    }
                } else if(data == DyeColor.RED.getData()) {
                    if(removePlayer(player.getName())) {
                        MessageHandler.sendMessage(player, "You have left this on demand tournament.");
                    }
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        players.remove(event.getPlayer().getName());
    }
}
