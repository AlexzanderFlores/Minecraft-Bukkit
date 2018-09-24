package network.player;

import network.Network;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.timed.PlayerHourOfPlaytimeEvent;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.CommandDispatcher;
import network.server.DB;
import network.server.servers.hub.crate.Beacon;
import network.server.servers.hub.crate.CrateTypes;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static network.server.DB.close;

public class VoteHandler implements Listener {
    private static String inventoryName;
    private static int coins = 20;
    private static int rankedMatches = 10;
    private static List<String> delay;

    public VoteHandler() {
        inventoryName = "Voting Shop";
        delay = new ArrayList<>();

        new CommandBase("vote", true) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                Player player = (Player) sender;
                EffectUtil.playSound(player, Sound.LEVEL_UP);
                open(player);
                return true;
            }
        };

        EventUtil.register(this);
    }

    private static int getPasses(Player player) {
        return DB.PLAYERS_VOTE_PASSES.getInt("uuid", player.getUniqueId().toString(), "vote_passes");
    }

    public static void open(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory inventory = Bukkit.createInventory(player, 9 * 6, inventoryName);
        ItemCreator itemCreator;
        int cost;

        inventory.setItem(11, new ItemCreator(Material.DIAMOND).setName("&bGet Vote Links").getItemStack());

        int passes = getPasses(player);
        itemCreator = new ItemCreator(Material.NAME_TAG).setName("&bVoting Passes");
        itemCreator.addLore("");
        itemCreator.addLore("&aVoting Passes are used to buy");
        itemCreator.addLore("&aperks from this shop.");
        itemCreator.addLore("");
        itemCreator.addLore("&aVoting gives you &b5 &aVoting Passes.");
        itemCreator.addLore("");
        itemCreator.addLore("&eYou currently have &b" + passes);
        itemCreator.addLore("");
        inventory.setItem(13, itemCreator.getItemStack());

        int streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid.toString(), "streak");
        itemCreator = new ItemCreator(ItemUtil.getSkull(player.getName())).setName("&bYour Voting Streak");
        itemCreator.addLore("");
        itemCreator.addLore("&aCurrent Streak: &b" + streak + " &adays in a row");
        itemCreator.addLore("&aStreak of 1 - 10: &b1x Multiplier");
        itemCreator.addLore("&aStreak of 11 - 20: &b2x Multiplier");
        itemCreator.addLore("&aStreak of 21+: &b3x Multiplier");
        itemCreator.addLore("");
        itemCreator.addLore("&aYour streak multiplier applies to");
        itemCreator.addLore("&aVoting Passes only, not shop items.");
        itemCreator.addLore("");
        inventory.setItem(15, itemCreator.getItemStack());

        itemCreator = new ItemCreator(ItemUtil.getSkull(player.getName())).setName("&bSkin Shown in Hubs");
        itemCreator.addLore("");
        itemCreator.addLore("&aYour skin will be shown in hubs");
        itemCreator.addLore("&auntil someone else votes");
        itemCreator.addLore("");
        itemCreator.addLore("&eReceived upon each vote");
        itemCreator.addLore("");
        inventory.setItem(28, itemCreator.getItemStack());

        cost = 3;
        itemCreator = new ItemCreator(Material.TRIPWIRE_HOOK).setName("&b+1 Voting Crate Key");
        itemCreator.addLore("");
        itemCreator.addLore("&eCost: &b" + cost);
        if(passes < cost) {
            itemCreator.addLore("&cYou need more Voting Passes");
        }
        itemCreator.addLore("");
        inventory.setItem(30, itemCreator.getItemStack());

        itemCreator = new ItemCreator(Material.PRISMARINE_SHARD).setName("&b+1 Key Fragment");
        itemCreator.addLore("");
        itemCreator.addLore("&aCollect &b3 &afor a Voting Crate Key");
        itemCreator.addLore("");
        itemCreator.addLore("&eReceived upon each vote");
        itemCreator.addLore("");
        inventory.setItem(32, itemCreator.getItemStack());

        cost = 3;
        itemCreator = new ItemCreator(Material.DOUBLE_PLANT).setName("&b+" + coins + " Coins in all games");
        itemCreator.addLore("");
        itemCreator.addLore("&eCost: &b" + cost);
        if(passes < cost) {
            itemCreator.addLore("&cYou need more Voting Passes");
        }
        itemCreator.addLore("");
        inventory.setItem(34, itemCreator.getItemStack());

        cost = 2;
        itemCreator = new ItemCreator(Material.GOLDEN_APPLE).setName("&b+20 Kit PVP Auto Regen Passes");
        itemCreator.addLore("");
        itemCreator.addLore("&aWhen in use you'll get a regeneration");
        itemCreator.addLore("&aeffect after each kill.");
        itemCreator.addLore("");
        itemCreator.addLore("&eCost: &b" + cost);
        if(passes < cost) {
            itemCreator.addLore("&cYou need more Voting Passes");
        }
        itemCreator.addLore("");
        inventory.setItem(38, itemCreator.getItemStack());

        cost = 2;
        itemCreator = new ItemCreator(Material.DIAMOND_SWORD).setName("&b+" + rankedMatches + " Ranked 1v1 Matches");
        itemCreator.addLore("");
        itemCreator.addLore("&eCost: &b" + cost);
        if(passes < cost) {
            itemCreator.addLore("&cYou need more Voting Passes");
        }
        itemCreator.addLore("");
        inventory.setItem(40, itemCreator.getItemStack());

        cost = 1;
        itemCreator = new ItemCreator(Material.DIAMOND_BOOTS).setName("&b+15 Hub Parkour Checkpoints");
        itemCreator.addLore("");
        itemCreator.addLore("&aCannot be used in Endless Parkour");
        itemCreator.addLore("");
        itemCreator.addLore("&eCost: &b" + cost);
        if(passes < cost) {
            itemCreator.addLore("&cYou need more Voting Passes");
        }
        itemCreator.addLore("");
        inventory.setItem(42, itemCreator.getItemStack());

        ItemUtil.displayGameGlass(inventory);

        player.openInventory(inventory);
    }

    private static void sendLink(CommandSender sender) {
        if(!delay.contains(sender.getName())) {
            delay.add(sender.getName());

            MessageHandler.sendLine(sender, "&6");
            MessageHandler.sendMessage(sender, "https://minecraftservers.org/server/514731");
            MessageHandler.sendLine(sender, "&6");

            new DelayedTask(() -> delay.remove(sender.getName()), 20 * 3);
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getTitle().equalsIgnoreCase(inventoryName)) {
            Player player = event.getPlayer();
            Material type = event.getItem().getType();

            int cost = 0;
            Runnable runnable = null;

            if(type == Material.DIAMOND) {
                sendLink(player);
                player.closeInventory();
            } else if(type == Material.TRIPWIRE_HOOK) {
                cost = 3;
                runnable = () -> Beacon.giveKey(player.getUniqueId(), 1, CrateTypes.VOTING);
            } else if(type == Material.DOUBLE_PLANT) {
                cost = 3;
                runnable = () -> {
                    for(String server : new String [] {
                            "KITPVP"
                    }) {
                        CommandDispatcher.sendToGame(server, "addCoins " + player.getName() + " KITPVP " + coins);
                    }
                };
            } else if(type == Material.GOLDEN_APPLE) {
//                cost = 2;
//                runnable = () -> {
//                };
                MessageHandler.sendMessage(player, "&cComing Soon");
            } else if(type == Material.DIAMOND_SWORD) {
                cost = 2;
                runnable = () -> CommandDispatcher.sendToGame("ONEVSONE", "addRankedMatches " + player.getName() + " " + rankedMatches);
            } else if(type == Material.DIAMOND_BOOTS) {
//                cost = 1;
//                runnable = () -> {};
                MessageHandler.sendMessage(player, "&cComing Soon");
            }

            if(runnable != null) {
                int passes = getPasses(player);
                if(passes >= cost) {
                    passes -= cost;
                    DB.PLAYERS_VOTE_PASSES.updateInt("vote_passes", passes - cost, "uuid", player.getUniqueId().toString());
                    runnable.run();
                    open(player);
                    MessageHandler.sendMessage(player, "Purchased " + event.getItemTitle());
                } else {
                    MessageHandler.sendMessage(player, "&cYou do not have enough Voting Passes");
                }
            }

            event.setCancelled(true);
        }
//        if(event.getTitle().equals(inventoryName)) {
//            int slot = event.getSlot();
//            if(slot == 12) {
//                if(event.getClickType() == ClickType.LEFT) {
//                    player.closeInventory();
//                    player.performCommand("vote");
//                    EffectUtil.playSound(player, Sound.LEVEL_UP);
//                } else if(event.getClickType() == ClickType.RIGHT) {
//                    Inventory inventory = Bukkit.createInventory(player, 9 * 6, rewardsName);
//                    // Coins
//                    inventory.setItem(10, new ItemCreator(Material.GOLD_INGOT).setName("&bUHC Sky Wars Coins").setLores(new String [] {
//                            "",
//                            "&e+" + coins + " &aUHC Sky Wars Coins",
//                            ""
//                    }).getItemStack());
//                    inventory.setItem(12, new ItemCreator(Material.GOLD_INGOT).setName("&bKit PVP Coins").setLores(new String [] {
//                            "",
//                            "&e+" + coins + " &aKit PVP Coins",
//                            ""
//                    }).getItemStack());
//
//                    // Other
//                    inventory.setItem(14, new ItemCreator(Material.CHEST).setName("&bUHC Sky Wars Looter Passes").setLores(new String [] {
//                            "",
//                            "&e+3 &aUHC Sky Wars Looter Passes",
//                            "",
//                            "&7Break a chest to restock its contents",
//                            "&7Does not load if the Looter kit is selected",
//                            "&7Max of 1 use per game",
//                            ""
//                    }).getItemStack());
//                    inventory.setItem(16, new ItemCreator(Material.EXP_BOTTLE).setName("&bNetwork Experience").setLores(new String [] {
//                            "",
//                            "&e+250 &aNetwork Experience",
//                            "&c(Coming soon)",
//                            ""
//                    }).getItemStack());
//
//                    String voting = CrateTypes.VOTING.getDisplay();
//
//                    // Crate keys
//                    inventory.setItem(20, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&b" + voting + " Crate Key").setLores(new String [] {
//                            "",
//                            "&e+1 &aKey to the " + voting + " crate",
//                            ""
//                    }).getItemStack()));
//                    inventory.setItem(22, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&b" + voting + " Crate Key Fragment").setLores(new String [] {
//                            "",
//                            "&e+1 &aKey Fragment to the " + voting + " crate",
//                            "",
//                            "&7Collect 3 of these for a full Key",
//                            "&7Click the Villager NPC near the crates",
//                            ""
//                    }).getItemStack()));
//                    inventory.setItem(24, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bUHC Sky Wars Crate Key").setLores(new String [] {
//                            "",
//                            "&e+1 &aKey to the UHC Sky Wars crate",
//                            "",
//                            "&7Open in the UHC Sky Wars shop",
//                            ""
//                    }).getItemStack()));
//
//                    // Other
//                    inventory.setItem(30, new ItemCreator(Material.LEATHER_BOOTS).setName("&bHub Parkour Checkpoints").setLores(new String [] {
//                            "",
//                            "&e+10 &aHub Parkour Checkpoints",
//                            ""
//                    }).getItemStack());
//                    inventory.setItem(32, new ItemCreator(Material.GOLDEN_APPLE).setName("&bUHC Rescatter Passes").setLores(new String [] {
//                            "",
//                            "&e+1 &aRescatter Pass",
//                            "",
//                            "&7Rescatter yourself with /rescatter",
//                            "&7Only works for the first 20 seconds",
//                            "",
//                            "&7Works for Speed UHC & Twitter UHCs",
//                            ""
//                    }).getItemStack());
//                    inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
//                    player.openInventory(inventory);
//                }
//            } else if(slot == 14) {
//                Inventory inventory = Bukkit.createInventory(player, 9 * 4, streakName);
//                player.openInventory(inventory);
//                UUID uuid = player.getUniqueId();
//                String name = player.getName();
//                new AsyncDelayedTask(() -> {
//                    int streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid.toString(), "streak");
//                    inventory.setItem(10, new ItemCreator(Material.NAME_TAG).setName("&bx1 Multiplier").setLores(new String [] {
//                            "",
//                            "&eVoting 1 - 10 days in a row",
//                            "",
//                            "&7All voting perk quantities",
//                            "&7are multiplied by 1",
//                            ""
//                    }).setGlow(streak >= 1 && streak <= 10).getItemStack());
//
//                    inventory.setItem(12, new ItemCreator(Material.NAME_TAG).setName("&bx2 Multiplier").setAmount(2).setLores(new String [] {
//                            "",
//                            "&eVoting 11 - 20 days in a row",
//                            "",
//                            "&7All voting perk quantities",
//                            "&7are multiplied by 2",
//                            ""
//                    }).setGlow(streak >= 11 && streak <= 20).getItemStack());
//
//                    inventory.setItem(14, new ItemCreator(Material.NAME_TAG).setName("&bx3 Multiplier").setAmount(3).setLores(new String [] {
//                            "",
//                            "&eVoting 21+ days in a row",
//                            "",
//                            "&7All voting perk quantities",
//                            "&7are multiplied by 3",
//                            ""
//                    }).setGlow(streak >= 21).getItemStack());
//
//                    inventory.setItem(16, new ItemCreator(ItemUtil.getSkull(name)).setName("&bCurrent Streak: &a" + streak).getItemStack());
//                    inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
//                });
//            }
//            event.setCancelled(true);
//        } else if(event.getTitle().equals(rewardsName)) {
//            open(player);
//        } else if(event.getTitle().equals(streakName)) {
//            open(player);
//        }
    }

    @EventHandler
    public void onPlayerHourOfPlaytime(PlayerHourOfPlaytimeEvent event) {
        if(DB.Databases.PLAYERS.isEnabled()) {
            new AsyncDelayedTask(() -> {
                Player player = event.getPlayer();
                UUID uuid = player.getUniqueId();

                boolean remind = false;

                if(DB.PLAYERS_RECENT_VOTER.isUUIDSet(uuid)) {
                    PreparedStatement statement = null;
                    ResultSet resultSet = null;
                    try {
                        String query = "SELECT COUNT(id) FROM " + DB.PLAYERS_RECENT_VOTER.getName() + " WHERE uuid = '" + uuid + "' AND date < NOW() - INTERVAL 1 DAY LIMIT 1;";
                        statement = DB.Databases.PLAYERS.getConnection().prepareStatement(query);
                        resultSet = statement.executeQuery();
                        if(resultSet.next()) {
                            remind = resultSet.getInt("COUNT(id)") == 0;
                        }
                    } catch(SQLException e) {
                        e.printStackTrace();
                    } finally {
                        close(statement, resultSet);
                    }
                } else {
                    remind = true;
                }

                Bukkit.getLogger().info("");
                if(remind) {
                    Bukkit.getLogger().info("Reminding " + player.getName() + " to vote");
                    MessageHandler.sendLine(player);
                    MessageHandler.sendMessage(player, "Hey " + player.getName() + "!");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendMessage(player, "You haven't &bvoted&x in the last 24 hours");
                    MessageHandler.sendMessage(player, "Voting gives &bin game advantages&x & only takes a few seconds");
                    MessageHandler.sendMessage(player, "");
                    ChatClickHandler.sendMessageToRunCommand(player, " &c[CLICK HERE]", "Click", "/vote", "&eRun &b/vote&e or");
                    MessageHandler.sendLine(player);
                    EffectUtil.playSound(player, Sound.LEVEL_UP);
                } else {
                    Bukkit.getLogger().info(player.getName() + " has already voted, not reminding");
                }
                Bukkit.getLogger().info("");
            });
        }
    }
}
