package network.gameapi.games.onevsones;

import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.customevents.player.PlayerStaffModeEvent;
import network.customevents.player.PlayerStaffModeEvent.StaffModeEventType;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.events.QueueEvent;
import network.gameapi.games.onevsones.events.QuitCommandEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.player.TitleDisplayer;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class QueueHandler implements Listener {
    private static List<String> inQueue = null;

    public QueueHandler() {
        inQueue = new ArrayList<String>();

        new CommandBase("viewQueue") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                MessageHandler.sendLine(sender);
                MessageHandler.sendMessage(sender, "&bQueue:");
                for(OneVsOneKit kit : OneVsOneKit.getKits()) {
                    for(int teamSize : OnevsOnes.getTeamSizes()) {
                        List<String> queue = kit.getQueue(teamSize);
                        if(!queue.isEmpty()) {
                            MessageHandler.sendMessage(sender, "   &b" + kit.getName() + ":");
                            String message = "";
                            for(String name : queue) {
                                message += name + ", ";
                            }
                            if(message.equalsIgnoreCase("")) {
                                MessageHandler.sendMessage(sender, "      &cNone");
                            } else {
                                MessageHandler.sendMessage(sender, "      " + message.substring(0, message.length() - 2));
                            }
                        }
                    }
                }
                MessageHandler.sendLine(sender);
                return true;
            }
        };

        EventUtil.register(this);
    }

    public static void add(Player player, OneVsOneKit kit, int teamSize) {
        QueueEvent event = new QueueEvent(player, kit, QueueEvent.QueueAction.ADD, teamSize);
        Bukkit.getPluginManager().callEvent(event);

        if(!inQueue.contains(player.getName())) {
            inQueue.add(player.getName());
        }

        String ranked = event.isRanked() ? "&cRanked Queue" : "&cUnranked Queue";
        new TitleDisplayer(player, "&e" + kit.getName(), ranked).display();
        MessageHandler.sendMessage(player, "&e" + kit.getName() + " " + ranked);

        if(Bukkit.getOnlinePlayers().size() == 1 && Ranks.OWNER.hasRank(player)) {
            remove(player);
            Team team = new Team(DyeColor.RED, kit, player);
            new MapProvider(false, false, team);
        }
    }

    public static void remove(Player player) {
        QueueEvent event = new QueueEvent(player, null, QueueEvent.QueueAction.REMOVE, -1);
        Bukkit.getPluginManager().callEvent(event);

        if(inQueue.contains(player.getName())) {
            inQueue.remove(player.getName());
            MessageHandler.sendMessage(player, "&cRemoved from the queue");
        }
    }

    public static boolean isInQueue(Player player) {
        return inQueue.contains(player.getName());
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();

        if(ticks == 20) {
            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    // Process the queue
                    for(OneVsOneKit kit : OneVsOneKit.getKits()) {
                        for(int teamSize : OnevsOnes.getTeamSizes()) {
                            List<String> queue = kit.getQueue(teamSize);

                            // If there are enough players in this queue
                            if(queue.size() >= teamSize * 2) {
                                Player playerOne = null;
                                Player playerTwo = null;

                                for(String name : queue) {
                                    Player player = ProPlugin.getPlayer(name);

                                    if(player == null) {
                                        // Player is offline
                                        kit.removeFromQueue(name);
                                    } else if(playerOne == null) {
                                        playerOne = player;
                                    } else {
                                        playerTwo = player;
                                        break;
                                    }
                                }

                                if(playerOne != null && playerTwo != null) {
                                    for(Player player : new Player [] { playerOne, playerTwo }) {
                                        queue.remove(player.getName());
                                        MessageHandler.sendMessage(player, "Match found!");
                                        inQueue.remove(player.getName());
                                    }

                                    Team teamOne = new Team(DyeColor.RED, kit, playerOne);
                                    Team teamTwo = new Team(DyeColor.BLUE, kit, playerTwo);

                                    new MapProvider(false, true, teamOne, teamTwo);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    public void onQuitCommand(QuitCommandEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerStaffMode(PlayerStaffModeEvent event) {
        Player player = event.getPlayer();
        if(event.getType() == StaffModeEventType.ENABLE && isInQueue(player)) {
            MessageHandler.sendMessage(player, "&cStaff Mode auto-vanish cancelled: You're in a queue");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
        if(event.getState() == SpectatorState.ADDED) {
            remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
        for(Player player : event.getBattle().getPlayers()) {
            remove(player);
        }
    }
}
