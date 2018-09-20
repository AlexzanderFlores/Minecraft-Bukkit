package network.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.gameapi.games.onevsones.events.BattleRequestEvent;
import network.gameapi.games.onevsones.events.QuitCommandEvent;
import network.server.util.EffectUtil;
import org.bukkit.*;
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
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.inventory.Inventory;

public class PrivateBattleHandler implements Listener {
    private static Map<String, List<PrivateBattle>> battleRequests = null;
    private static Map<String, String> sendingTo = null;
    private static String name = null;

    public PrivateBattleHandler() {
        battleRequests = new HashMap<>();
        sendingTo = new HashMap<>();
        name = "Kit Selector";

        new CommandBase("battle", 1, true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player playerOne = (Player) sender;
                Player playerTwo = ProPlugin.getPlayer(arguments[0]);

                if(playerTwo == null) {
                    MessageHandler.sendMessage(playerOne, "&c" + arguments[0] + " is not online");
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

                            OneVsOneKit kit = OneVsOneKit.getPlayersKit(playerOne);
                            if(kit == null) {
                                kit = OneVsOneKit.getPlayersKit(playerTwo);
                            }
                            Team teamOne = new Team(DyeColor.RED, kit, playerOne);
                            Team teamTwo = new Team(DyeColor.BLUE, kit, playerTwo);

                            for(Player player : new Player [] { playerOne, playerTwo }) {
                                QueueHandler.remove(player);
                                ProPlugin.resetPlayer(player);
                                kit.give(player);
                                removeAllInvitesFromPlayer(player);
                                battleRequests.remove(player.getName());
                            }

                            new DelayedTask(new Runnable() {
                                @Override
                                public void run() {
                                    new MapProvider(false, false, teamOne, teamTwo);
                                }
                            }, 20 * 2);
                            return true;
                        }
                    }

                    QueueHandler.remove(playerOne);
                    Inventory inventory = LobbyHandler.getKitSelectorInventory(playerOne, name, false);
                    playerOne.openInventory(inventory);
                    sendingTo.put(playerOne.getName(), playerTwo.getName());
                }

//                if(QueueHandler._isWaitingForMap(playerOne)) {
//                    MessageHandler.sendMessage(playerOne, "&cYou are currently waiting for a map, cannot send another request");
//                } else if(LobbyHandler.isInLobby(playerTwo) && !QueueHandler._isWaitingForMap(playerTwo)) {
//                    if(battleRequests.containsKey(playerOne.getDisplay())) {
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
//                            battleRequests.remove(playerTwo.getDisplay());
//                            battleRequests.remove(playerOne.getDisplay());
//
//                            String clickedName = playerTwo.getDisplay();
//                            String clickerName = playerOne.getDisplay();
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
//                    choosingMatchType.add(playerOne.getDisplay());
//                    sendingTo.put(playerOne.getDisplay(), playerTwo.getDisplay());
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
        List<String> names = new ArrayList<>();
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

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if(LobbyHandler.isInLobby(player) && player.getItemInHand().getType() == Material.SLIME_BALL) {
            battleRequests.remove(event.getPlayer().getName());
            sendingTo.remove(event.getPlayer().getName());
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
            if(event.getInventory().getTitle().equals(name)) {
                sendingTo.remove(name);
            }
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        Player challenger = event.getPlayer();
        if(event.getTitle().equals(name)) {
            Player challenged = ProPlugin.getPlayer(sendingTo.get(challenger.getName()));
            if(!hasChallengedPlayer(challenged, challenger)) {
                if(!battleRequests.containsKey(challenged.getName())) {
                    battleRequests.put(challenged.getName(), new ArrayList<>());
                }
                String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                PrivateBattle battle = new PrivateBattle(challenger, challenged, OneVsOneKit.getKit(name));
                if(battle == null || battle.getKit() == null || battle.getKit().getName() == null) {
                    MessageHandler.sendMessage(challenger, "&cAn error occured when sending request, please try again");
                } else {
                    OneVsOneKit kit = OneVsOneKit.getKit(event.getItem());
                    kit.give(challenger);
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
    public void onQuit(QuitCommandEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        remove(event.getPlayer());
    }

    private void remove(Player player) {
        String name = player.getName();
        battleRequests.remove(name);
        sendingTo.remove(name);
        removeAllInvitesFromPlayer(player);
    }
}
