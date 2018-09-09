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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class QueueHandler implements Listener {
    private static List<String> inQueue = null;

//    private static List<_QueueData> _queueData = null;
//    private static List<String> _waitingForMap = null;

    public QueueHandler() {
        inQueue = new ArrayList<String>();

//        _queueData = new ArrayList<_QueueData>();
//        _waitingForMap = new ArrayList<String>();

        new CommandBase("viewQueue", 1) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                MessageHandler.sendLine(sender);
                if(arguments[0].equalsIgnoreCase("new")) {
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
                                    MessageHandler.sendMessage(sender, "&cNone");
                                } else {
                                    MessageHandler.sendMessage(sender, message.substring(0, message.length() - 2));
                                }
                            }
                        }
                    }
                } else {
//                    MessageHandler.sendMessage(sender, "&bQueue Data:");
//                    for(_QueueData data : _queueData) {
//                        MessageHandler.sendMessage(sender, "   Player One: " + data.getPlayerOne());
//                        MessageHandler.sendMessage(sender, "   Player Two: " + data.getForcedPlayer());
//                        MessageHandler.sendMessage(sender, "   Kit: " + data.getKit().getName());
//                        MessageHandler.sendMessage(sender, "   Counter: " + data.getCounter());
//                        MessageHandler.sendMessage(sender, "");
//                    }
//                    MessageHandler.sendMessage(sender, "&bWaiting for map:");
//                    String message = "";
//                    for(String name : _waitingForMap) {
//                        message += name + ", ";
//                    }
//                    if(!message.equalsIgnoreCase("")) {
//                        MessageHandler.sendMessage(sender, message.substring(0, message.length() - 2));
//                        MessageHandler.sendMessage(sender, "");
//                    }
//                    MessageHandler.sendMessage(sender, "&bIn Battle:");
//                    for(Battle battle : BattleHandler.getBattles()) {
//                        MessageHandler.sendMessage(sender, "   Players:");
//                        for(Player player : battle.getPlayers()) {
//                            MessageHandler.sendMessage(sender, "      " + player.getName());
//                        }
//                        MessageHandler.sendMessage(sender, "   Timer: " + battle.getTimer());
//                        Location loc = battle.getTargetLocation();
//                        MessageHandler.sendMessage(sender, "   Target location: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
//                        MessageHandler.sendMessage(sender, "Kit: " + battle.getKit().getName());
//                        MessageHandler.sendMessage(sender, "Blocks placed: " + battle.getPlacedBlocks().size());
//                        MessageHandler.sendMessage(sender, "");
//                    }
//                    MessageHandler.sendLine(sender);
                }
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

        String ranked = event.isRanked() ? "&cRanked Queue" : "&cUnranked Queue &b/vote";
        new TitleDisplayer(player, "&e" + kit.getName(), ranked).display();
        MessageHandler.sendMessage(player, "&e" + kit.getName() + ranked);
    }

    public static void remove(Player player) {
        QueueEvent event = new QueueEvent(player, null, QueueEvent.QueueAction.REMOVE, -1);
        Bukkit.getPluginManager().callEvent(event);

        inQueue.remove(player.getName());

        MessageHandler.sendMessage(player, "&cRemoved from the queue");
    }

    public static boolean isInQueue(Player player) {
        return inQueue.contains(player.getName());
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();

        // New
        if(ticks == 20) {
            new AsyncDelayedTask(new Runnable() {
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
                                    }

                                    new MapProvider(playerOne, playerTwo, playerOne.getWorld(), false, true);
                                }
                            }
                        }
                    }
                }
            });
        }

        // Old
//        if(ticks == 20) {
//            for(_QueueData data : _queueData) {
//                data.incrementCounter();
//            }
//            _processQueue(true);
//        } else if(ticks == 20 * 5) {
//            _processQueue(false);
//        }
    }

    @EventHandler
    public void onQuitCommand(QuitCommandEvent event) {
        Player player = event.getPlayer();

        remove(player);
        ProPlugin.resetPlayer(player);
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

//    public static void _add(Player player, OneVsOneKit kit) {
//        _remove(player);
//        PrivateBattleHandler.removeAllInvitesFromPlayer(player);
//        boolean ranked = RankedHandler.getMatches(player) > 0;
//        new TitleDisplayer(player, "&e" + kit.getName(), ranked ? "&cRanked Queue" : "&cUnranked Queue &b/vote").display();
//        MessageHandler.sendMessage(player, "&e" + kit.getName() + (ranked ? " &cRanked Queue" : " &cUnranked Queue &b/vote"));
//        OneVsOneKit.givePlayersKit(player, kit);
//        if(Ranks.VIP.hasRank(player)) {
//            new _QueueData(player, null, true, kit);
//        } else {
//            MessageHandler.sendMessage(player, "&a&l[TIP] " + Ranks.VIP.getPrefix() + "&cPerk: &e5x faster queuing time &b/buy");
//            new DelayedTask(new Runnable() {
//                @Override
//                public void run() {
//                    new _QueueData(player, null, ranked, kit);
//                }
//            }, 20 * 5);
//        }
//    }
//
//    public static void _remove(Player player) {
//        _remove(player, false);
//    }
//
//    public static void _remove(Player player, boolean message) {
//        try {
//        	Iterator<_QueueData> iterator = _queueData.iterator();
//            while(iterator.hasNext()) {
//                if(iterator.next().getPlayerOne().equals(player.getName())) {
//                    iterator.remove();
//                    if(message) {
//                        MessageHandler.sendMessage(player, "&cYou were removed from the match queue");
//                    }
//                    break;
//                }
//            }
//        } catch(Exception e) {
//        	e.printStackTrace();
//        }
//        _waitingForMap.remove(player.getName());
//    }
//
//    public static boolean _isInQueue(Player player) {
//        for(_QueueData data : _queueData) {
//            if(data.isPlaying(player)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean _isWaitingForMap(Player player) {
//        return _waitingForMap.contains(player.getName());
//    }
//
//    public static void _gotMap(Player player) {
//        _waitingForMap.remove(player.getName());
//    }
//
//    private void _processQueue(boolean priority) {
//        new AsyncDelayedTask(new Runnable() {
//            @Override
//            public void run() {
//            	for(_QueueData data : _queueData) {
//        			for(_QueueData comparingData : _queueData) {
//                        if((data.isPrioirty() == priority || comparingData.isPrioirty() == priority) && data.canJoin(comparingData) && data.isRanked() == comparingData.isRanked()) {
//                        	Player playerOne;
//                            Player playerTwo;
//
//                            if(data.getForcedPlayer() != null && comparingData.getForcedPlayer() != null) {
//                                playerOne = ProPlugin.getPlayerOne(data.getPlayerOne());
//                                playerTwo = ProPlugin.getPlayerOne(comparingData.getForcedPlayer());
//                                ProPlugin.resetPlayer(playerOne);
//                                ProPlugin.resetPlayer(playerTwo);
//                            } else {
//                                playerOne = ProPlugin.getPlayerOne(data.getPlayerOne());
//                                playerTwo = ProPlugin.getPlayerOne(comparingData.getPlayerOne());
//                            }
//
//                            _remove(playerOne);
//                            _remove(playerTwo);
//
//                            _waitingForMap.add(playerOne.getName());
//                            _waitingForMap.add(playerTwo.getName());
//
//                            new MapProvider(playerOne, playerTwo, playerOne.getWorld(), false, true);
//
//                            return;
//                        }
//            		}
//        		}
//            }
//        });
//    }

//    public static class _QueueData {
//        private boolean priority = false;
//        private String player = null;
//        private String forcedPlayer = null;
//        private boolean ranked = false;
//        private OneVsOneKit kit = null;
//        private int counter = 0;
//
//        public _QueueData(Player player, Player playerTwo, boolean ranked, OneVsOneKit kit) {
//            if(Ranks.VIP.hasRank(player) || Ranks.VIP.hasRank(playerTwo)) {
//                priority = true;
//            }
//            this.player = player.getName();
//            if(playerTwo != null) {
//                this.forcedPlayer = playerTwo.getName();
//            }
//            this.ranked = ranked;
//            this.kit = kit;
//            _queueData.add(this);
//
//            if(ProPlugin.getPlayers().size() == 1 && Ranks.OWNER.hasRank(player)) {
//                _remove(player);
//                new MapProvider(player, null, player.getWorld(), false, true);
//            }
//        }
//
//        public boolean canJoin(_QueueData data) {
//            Player playerOne = ProPlugin.getPlayerOne(player);
//            Player playerTwo = ProPlugin.getPlayerOne(data.getPlayerOne());
//            if(playerOne == null || playerTwo == null) {
//            	return false;
//            }
//            if(playerOne.getAddress().getAddress().getHostAddress().equals(playerTwo.getAddress().getAddress().getHostAddress())) {
//            	if(AccountHandler.getRank(playerOne) != Ranks.OWNER && AccountHandler.getRank(playerTwo) != Ranks.OWNER) {
//            		return false;
//            	}
//            }
//            if(forcedPlayer != null && data.getForcedPlayer() != null) {
//                return data.getForcedPlayer().equals(forcedPlayer);
//            } else {
//                return !this.getPlayerOne().equals(data.getPlayerOne()) && this.getKit() == data.getKit();
//            }
//        }
//
//        public boolean isPrioirty() {
//            return this.priority;
//        }
//
//        public String getPlayerOne() {
//            return this.player;
//        }
//
//        public String getForcedPlayer() {
//            return this.forcedPlayer;
//        }
//
//        public boolean isRanked() {
//        	return this.ranked;
//        }
//
//        public OneVsOneKit getKit() {
//            return this.kit;
//        }
//
//        public int getCounter() {
//            return this.counter;
//        }
//
//        public int incrementCounter() {
//            return ++this.counter;
//        }
//
//        public boolean isPlaying(Player player) {
//            return getPlayerOne().equals(player.getName()) || (getForcedPlayer() != null && getForcedPlayer().equals(player.getName()));
//        }
//    }
}
