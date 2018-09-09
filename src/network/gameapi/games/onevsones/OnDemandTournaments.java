package network.gameapi.games.onevsones;

import network.ProPlugin;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.CountDownUtil;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.List;

public class OnDemandTournaments extends CountDownUtil implements Listener {
    private String name = null;
    private List<String> players = null;
    private int min = 8;
    private int max = 14;

    public OnDemandTournaments(Location location) {
        name = "On Demand Tournaments";
        players = new ArrayList<>();

        new NPCEntity(EntityType.ZOMBIE, "&e&n" + name, location, Material.GOLDEN_APPLE) {
            @Override
            public void onInteract(Player player) {
                Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);

                inventory.setItem(38, new ItemCreator(Material.WOOL, DyeColor.LIME.getData())
                        .setName("&eJoin On Demand Tournament")
                        .addLore("")
                        .addLore("&bPlayers: &e" + min + " - " + max)
                        .addLore("&bStarts with " + min + "+ people")
                        .addLore("")
                        .addLore("&b1st Place:")
                        .addLore("   &e30 Ranked Matches")
                        .addLore("   &e2 Key Fragments")
                        .addLore("   &e1 Voting Key")
                        .addLore("")
                        .addLore("&b2nd Place:")
                        .addLore("   &e15 Ranked Matches")
                        .addLore("   &e1 Key Fragment")
                        .addLore("").getItemStack());

                ItemCreator itemCreator = new ItemCreator(Material.WATCH)
                        .addLore("")
                        .addLore("&cYou will be removed from")
                        .addLore("&cany current games as soon")
                        .addLore("&cas the tournament starts.")
                        .addLore("");
                if(players.size() >= min) {
                    itemCreator.setName("&eStarting in " + getCounterAsString());
                } else {
                    itemCreator.setName("&eWaiting for " + (min - players.size()) + " Players");
                }
                inventory.setItem(40, itemCreator.getItemStack());

                inventory.setItem(42, new ItemCreator(Material.WOOL, DyeColor.RED.getData())
                        .setName("&cLeave Tournament")
                        .addLore("")
                        .addLore("&bThis will _remove you from the")
                        .addLore("&bcurrent tournament queue")
                        .addLore("").getItemStack());

                player.openInventory(inventory);

                setPlayerHeads(player);
            }
        };

        EventUtil.register(this);
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

                    // Is the tournament full?
                    if(players.size() >= max) {
                        if(AccountHandler.Ranks.VIP.hasRank(player)) {
                            boolean kickedPlayer = false;
                            for(String name : players) {
                                Player tournamentPlayer = ProPlugin.getPlayer(name);
                                if(tournamentPlayer == null || !AccountHandler.Ranks.VIP.hasRank(tournamentPlayer)) {
                                    kickedPlayer = true;
                                    if(removePlayer(name) && tournamentPlayer != null) {
                                        MessageHandler.sendMessage(tournamentPlayer, "A " + AccountHandler.Ranks.VIP.getPrefix() + "&xplayer has joined a full tournament.");
                                        MessageHandler.sendMessage(tournamentPlayer, "Don't get kicked again by getting " + AccountHandler.Ranks.VIP.getPrefix() + "&b/buy");
                                    }
                                    break;
                                }
                            }
                            if(!kickedPlayer) {
                                MessageHandler.sendMessage(player, "&cThere are no default players to kick. Cannot join tournament.");
                                event.setCancelled(true);
                                return;
                            }
                        } else {
                            MessageHandler.sendMessage(player, "&cThis tournament is full. Join full tournaments with " + AccountHandler.Ranks.VIP.getPrefix() + "&b/buy");
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if(players.contains(player.getName())) {
                        MessageHandler.sendMessage(player, "&cYou are already in this tournament.");
                    } else {
                        players.add(player.getName().toLowerCase());
                        setPlayerHeads(player);
                    }
                } else if(data == DyeColor.RED.getData()) {
                    if(removePlayer(player.getName())) {
                        MessageHandler.sendMessage(player, "You have left this on demand tournament.");
                    }
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
