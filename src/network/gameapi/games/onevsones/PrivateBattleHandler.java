package network.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.gameapi.games.onevsones.events.BattleRequestEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import network.ProPlugin;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class PrivateBattleHandler implements Listener {
    private static Map<String, List<PrivateBattle>> battleRequests = null;
    private static Map<String, String> sendingTo = null;
    private static List<String> choosingMatchType = null;

    public PrivateBattleHandler() {
        battleRequests = new HashMap<String, List<PrivateBattle>>();
        sendingTo = new HashMap<String, String>();
        choosingMatchType = new ArrayList<String>();

        new CommandBase("battle", 1, true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player playerOne = (Player) sender;
                Player playerTwo = ProPlugin.getPlayer(arguments[0]);

                if(playerTwo == null) {
                    MessageHandler.sendMessage(playerOne, "&c" + arguments[0] + " is not online");
                    return true;
                } else if(playerOne.getInventory().contains(Material.MAGMA_CREAM)) {
                    MessageHandler.sendMessage(playerOne, "&cCannot send request: You have your battle requests disabled");
                    return true;
                } else if(playerTwo.getInventory().contains(Material.MAGMA_CREAM)) {
                    MessageHandler.sendMessage(playerOne, AccountHandler.getPrefix(playerTwo) + " &chas battle requests disabled");
                    return true;
                } else if(playerOne.getName().equals(playerTwo.getName())) {
                    MessageHandler.sendMessage(sender, "&cYou can't battle yourself");
                    return true;
                }

                BattleRequestEvent event = new BattleRequestEvent(playerOne, playerTwo);
                Bukkit.getPluginManager().callEvent(event);

                if(!event.isCancelled()) {
                    if(battleRequests.containsKey(playerOne.getName())) {
                        if(hasChallengedPlayer(playerOne, playerTwo)) {
                            MessageHandler.sendMessage(playerTwo, AccountHandler.getPrefix(playerOne) + " &6has accepted your battle request");
                            MessageHandler.sendMessage(playerOne, "&aYou have accepted " + AccountHandler.getPrefix(playerTwo) + "&6's battle request");

                            QueueHandler.remove(playerOne);
                            QueueHandler.remove(playerTwo);

                            ProPlugin.resetPlayer(playerTwo);
                            ProPlugin.resetPlayer(playerOne);

                            getInvite(playerTwo, playerOne).getKit().give(playerTwo);
                            getInvite(playerTwo, playerOne).getKit().give(playerOne);

                            removeAllInvitesFromPlayer(playerOne);
                            removeAllInvitesFromPlayer(playerTwo);

                            battleRequests.remove(playerTwo.getName());
                            battleRequests.remove(playerOne.getName());

                            String clickedName = playerTwo.getName();
                            String clickerName = playerOne.getName();

                            new DelayedTask(new Runnable() {
                                @Override
                                public void run() {
                                    Player clicked = ProPlugin.getPlayer(clickedName);
                                    if(clicked != null) {
                                        Player clicker = ProPlugin.getPlayer(clickerName);
                                        if(clicker != null) {
                                            new MapProvider(clicked, clicker, clicked.getWorld(), false, false);
                                        }
                                    }
                                }
                            }, 20 * 2);
                            return true;
                        }
                    }

                    QueueHandler.remove(playerOne);
                    LobbyHandler.openKitSelection(playerOne);
                    sendingTo.put(playerOne.getName(), playerTwo.getName());
                }

//                if(QueueHandler._isWaitingForMap(playerOne)) {
//                    MessageHandler.sendMessage(playerOne, "&cYou are currently waiting for a map, cannot send another request");
//                } else if(LobbyHandler.isInLobby(playerTwo) && !QueueHandler._isWaitingForMap(playerTwo)) {
//                    if(battleRequests.containsKey(playerOne.getName())) {
//                        if(hasChallengedPlayer(playerOne, playerTwo)) {
//                            MessageHandler.sendMessage(playerTwo, AccountHandler.getPrefix(playerOne) + " &6has accepted your battle request");
//                            MessageHandler.sendMessage(playerOne, "&aYou have accepted " + AccountHandler.getPrefix(playerTwo) + "&6's battle request");
//
//                            QueueHandler._remove(playerOne, true);
//                            QueueHandler._remove(playerTwo, true);
//
//                            ProPlugin.resetPlayer(playerTwo);
//                            ProPlugin.resetPlayer(playerOne);
//
//                            getInvite(playerTwo, playerOne).getKit().give(playerTwo);
//                            getInvite(playerTwo, playerOne).getKit().give(playerOne);
//
//                            removeAllInvitesFromPlayer(playerOne);
//                            removeAllInvitesFromPlayer(playerTwo);
//
//                            battleRequests.remove(playerTwo.getName());
//                            battleRequests.remove(playerOne.getName());
//
//                            String clickedName = playerTwo.getName();
//                            String clickerName = playerOne.getName();
//
//                            new DelayedTask(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Player clicked = ProPlugin.getPlayer(clickedName);
//                                    if(clicked != null) {
//                                        Player clicker = ProPlugin.getPlayer(clickerName);
//                                        if(clicker != null) {
//                                            new MapProvider(clicked, clicker, clicked.getWorld(), false, false);
//                                        }
//                                    }
//                                }
//                            }, 20 * 2);
//                            return true;
//                        }
//                    }
//
//                    QueueHandler._remove(playerOne, true);
//                    LobbyHandler.openKitSelection(playerOne);
//                    choosingMatchType.add(playerOne.getName());
//                    sendingTo.put(playerOne.getName(), playerTwo.getName());
//                } else {
//                    MessageHandler.sendMessage(playerOne, "&cThis player is currently in a match please wait");
//                }
                return true;
            }
        };
        EventUtil.register(this);
    }

    private static boolean hasChallengedPlayer(Player challenged, Player challenger) {
        if(challenged == null) {
            return true;
        }

        if(battleRequests.containsKey(challenged.getName())) {
            for(PrivateBattle request : battleRequests.get(challenged.getName())) {
                if(request.getChallenger().getName().equals(challenger.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static PrivateBattle getInvite(Player challenger, Player challenged) {
        if(battleRequests.containsKey(challenged.getName())) {
            for(PrivateBattle request : battleRequests.get(challenged.getName())) {
                if(request.getChallenger().getName().equals(challenger.getName())) {
                    return request;
                }
            }
        }
        return null;
    }

    public static void removeAllInvitesFromPlayer(Player toRemove) {
        List<String> names = new ArrayList<String>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(battleRequests.containsKey(player.getName())) {
                for(PrivateBattle request : battleRequests.get(player.getName())) {
                    if(request.getChallenger().getName().equals(toRemove.getName())) {
                        names.add(player.getName());
                    }
                }
            }
        }

        for(String name : names) {
            battleRequests.get(name).remove(getInvite(toRemove, ProPlugin.getPlayer(name)));
        }
    }

    public static boolean choosingMapType(Player player) {
        return choosingMatchType != null && choosingMatchType.contains(player.getName());
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if(LobbyHandler.isInLobby(player) && player.getItemInHand().getType() == Material.SLIME_BALL) {
            battleRequests.remove(event.getPlayer().getName());
            sendingTo.remove(event.getPlayer().getName());
            choosingMatchType.remove(event.getPlayer().getName());
            removeAllInvitesFromPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            if(LobbyHandler.isInLobby(player)) {
                Player clicked = (Player) event.getRightClicked();
                if(LobbyHandler.isInLobby(clicked)) {
                    player.chat("/battle " + clicked.getName());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String name = player.getName();
            if(event.getInventory().getTitle().equals("Kit Selection") && choosingMatchType.contains(name)) {
                sendingTo.remove(name);
                choosingMatchType.remove(name);
            }
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        Player challenger = event.getPlayer();
        if(event.getTitle().equals("Kit Selection") && choosingMatchType.contains(challenger.getName())) {
            Player challenged = ProPlugin.getPlayer(sendingTo.get(challenger.getName()));
            if(!hasChallengedPlayer(challenged, challenger)) {
                if(!battleRequests.containsKey(challenged.getName())) {
                    battleRequests.put(challenged.getName(), new ArrayList<PrivateBattle>());
                }
                String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                PrivateBattle battle = new PrivateBattle(challenger, challenged, OneVsOneKit.getKit(name));
                if(battle == null || battle.getKit() == null || battle.getKit().getName() == null) {
                    MessageHandler.sendMessage(challenger, "&cAn error occured when sending request, please try again");
                } else {
                    battleRequests.get(challenged.getName()).add(battle);
                    MessageHandler.sendMessage(challenger, "Request to " + AccountHandler.getPrefix(challenged) + " &asent");
                    MessageHandler.sendLine(challenged, "&b");
                    MessageHandler.sendMessage(challenged, "Battle request from " + AccountHandler.getPrefix(challenger));
                    MessageHandler.sendMessage(challenged, "Kit selected: &6" + name);
                    ChatClickHandler.sendMessageToRunCommand(challenged, "&6Click to accept", "Click to accept", "/battle " + challenger.getName());
                    MessageHandler.sendMessage(challenged, "&cDo not want battle requests? Click the &aSlime Ball &citem");
                    MessageHandler.sendLine(challenged, "&b");
                }
            } else if(challenger != null) {
                MessageHandler.sendMessage(challenger, "&c" + challenged.getName() + " already has a request from you!");
            }
            challenger.closeInventory();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        String name = event.getPlayer().getName();
        battleRequests.remove(name);
        sendingTo.remove(name);
        choosingMatchType.remove(name);
        removeAllInvitesFromPlayer(event.getPlayer());
    }
}
