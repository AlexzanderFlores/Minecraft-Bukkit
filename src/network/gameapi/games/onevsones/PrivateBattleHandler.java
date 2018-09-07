package network.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                Player clicker = (Player) sender;
                Player clicked = ProPlugin.getPlayer(arguments[0]);
                if(clicked == null) {
                    MessageHandler.sendMessage(clicker, "&c" + arguments[0] + " is not online");
                } else if(clicker.getInventory().contains(Material.MAGMA_CREAM)) {
                    MessageHandler.sendMessage(clicker, "&cCannot send request: You have your battle requests disabled");
                } else if(clicked.getInventory().contains(Material.MAGMA_CREAM)) {
                    MessageHandler.sendMessage(clicker, AccountHandler.getPrefix(clicked) + " &chas battle requests disabled");
                } else if(SpectatorHandler.contains(clicker)) {
                    MessageHandler.sendMessage(clicker, "&cCannot send request while spectating");
                } else if(SpectatorHandler.contains(clicked)) {
                    MessageHandler.sendMessage(clicked, "&cCannot send request to a spectator");
                } else if(clicker.getName().equals(clicked.getName())) {
                    MessageHandler.sendMessage(sender, "&cYou can't battle yourself");
                } else if(!LobbyHandler.isInLobby(clicker)) {
                    MessageHandler.sendMessage(clicker, "&cYou are already in a battle");
                } else if(!LobbyHandler.isInLobby(clicked)) {
                    MessageHandler.sendMessage(clicker, AccountHandler.getPrefix(clicked) + " &cis already in a battle");
                } else if(QueueHandler.isWaitingForMap(clicker)) {
                    MessageHandler.sendMessage(clicker, "&cYou are currently waiting for a map, cannot send another request");
                } else if(LobbyHandler.isInLobby(clicked) && !QueueHandler.isWaitingForMap(clicked)) {
                    if(battleRequests.containsKey(clicker.getName())) {
                        if(hasChallengedPlayer(clicker, clicked)) {
                            MessageHandler.sendMessage(clicked, AccountHandler.getPrefix(clicker) + " &6has accepted your battle request");
                            MessageHandler.sendMessage(clicker, "&aYou have accepted " + AccountHandler.getPrefix(clicked) + "&6's battle request");

                            QueueHandler.remove(clicker, true);
                            QueueHandler.remove(clicked, true);

                            ProPlugin.resetPlayer(clicked);
                            ProPlugin.resetPlayer(clicker);

                            getInvite(clicked, clicker).getKit().give(clicked);
                            getInvite(clicked, clicker).getKit().give(clicker);

                            removeAllInvitesFromPlayer(clicker);
                            removeAllInvitesFromPlayer(clicked);

                            battleRequests.remove(clicked.getName());
                            battleRequests.remove(clicker.getName());

                            String clickedName = clicked.getName();
                            String clickerName = clicker.getName();

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

                    QueueHandler.remove(clicker, true);
                    LobbyHandler.openKitSelection(clicker);
                    choosingMatchType.add(clicker.getName());
                    sendingTo.put(clicker.getName(), clicked.getName());
                } else {
                    MessageHandler.sendMessage(clicker, "&cThis player is currently in a match please wait");
                }
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
