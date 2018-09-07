package network.gameapi.games.onevsones;

import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.customevents.player.PlayerStaffModeEvent;
import network.customevents.player.PlayerStaffModeEvent.StaffModeEventType;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.player.TitleDisplayer;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueueHandler implements Listener {
    private static List<QueueData> queueData = null;
    private static List<String> waitingForMap = null;

    public QueueHandler() {
        queueData = new ArrayList<QueueData>();
        waitingForMap = new ArrayList<String>();
        EventUtil.register(this);
    }

    public static void add(Player player, OneVsOneKit kit) {
        remove(player);
        PrivateBattleHandler.removeAllInvitesFromPlayer(player);
        boolean ranked = RankedHandler.getMatches(player) > 0;
        new TitleDisplayer(player, "&e" + kit.getName(), ranked ? "&cRanked Queue" : "&cUnranked Queue &b/vote").display();
        MessageHandler.sendMessage(player, "&e" + kit.getName() + (ranked ? " &cRanked Queue" : " &cUnranked Queue &b/vote"));
        OneVsOneKit.givePlayersKit(player, kit);
        if(Ranks.VIP.hasRank(player)) {
        	new QueueData(player, null, true, kit);
        } else {
        	MessageHandler.sendMessage(player, "&a&l[TIP] " + Ranks.VIP.getPrefix() + "&cPerk: &e5x faster queuing time &b/buy");
        	new DelayedTask(new Runnable() {
				@Override
				public void run() {
					new QueueData(player, null, ranked, kit);
				}
			}, 20 * 5);
        }
    }

    public static void remove(Player player) {
        remove(player, false);
    }

    public static void remove(Player player, boolean message) {
        try {
        	Iterator<QueueData> iterator = queueData.iterator();
            while(iterator.hasNext()) {
                if(iterator.next().getPlayer().equals(player.getName())) {
                    iterator.remove();
                    if(message) {
                        MessageHandler.sendMessage(player, "&cYou were removed from the match queue");
                    }
                    break;
                }
            }
        } catch(Exception e) {
        	e.printStackTrace();
        }
        waitingForMap.remove(player.getName());
    }

    public static boolean isInQueue(Player player) {
        for(QueueData data : queueData) {
            if(data.isPlaying(player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWaitingForMap(Player player) {
        return waitingForMap.contains(player.getName());
    }

    public static void gotMap(Player player) {
        waitingForMap.remove(player.getName());
    }

    private void processQueue(boolean priority) {
        new DelayedTask(new Runnable() {
            @Override
            public void run() {
            	for(QueueData data : queueData) {
                    Bukkit.getLogger().info("data = " + data.getPlayer());
        			for(QueueData comparingData : queueData) {
                        Bukkit.getLogger().info("compare data = " + data.getForcedPlayer());
                        if((data.isPrioirty() == priority || comparingData.isPrioirty() == priority) && data.canJoin(comparingData) && data.isRanked() == comparingData.isRanked()) {
                        	Player playerOne;
                            Player playerTwo;

                            if(data.getForcedPlayer() != null && comparingData.getForcedPlayer() != null) {
                                playerOne = ProPlugin.getPlayer(data.getPlayer());
                                playerTwo = ProPlugin.getPlayer(comparingData.getForcedPlayer());
                                ProPlugin.resetPlayer(playerOne);
                                ProPlugin.resetPlayer(playerTwo);
                            } else {
                                playerOne = ProPlugin.getPlayer(data.getPlayer());
                                playerTwo = ProPlugin.getPlayer(comparingData.getPlayer());
                            }

                            remove(playerOne);
                            remove(playerTwo);

                            waitingForMap.add(playerOne.getName());
                            waitingForMap.add(playerTwo.getName());

                            new MapProvider(playerOne, playerTwo, playerOne.getWorld(), false, true);

                            return;
                        }
            		}	
        		}
            }
        });
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            for(QueueData data : queueData) {
                data.incrementCounter();
            }
            processQueue(true);
        } else if(ticks == 20 * 5) {
            processQueue(false);
        }
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
    		remove(event.getPlayer(), true);
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

    public static class QueueData {
        private boolean priority = false;
        private String player = null;
        private String forcedPlayer = null;
        private boolean ranked = false;
        private OneVsOneKit kit = null;
        private int counter = 0;

        public QueueData(Player player, Player playerTwo, boolean ranked, OneVsOneKit kit) {
            Bukkit.broadcastMessage(player.getName() + " has joined the queue for " + kit.getName() + " (Size = " + queueData.size() + ")");
            if(Ranks.VIP.hasRank(player) || Ranks.VIP.hasRank(playerTwo)) {
                priority = true;
            }
            this.player = player.getName();
            if(playerTwo != null) {
                this.forcedPlayer = playerTwo.getName();
            }
            this.ranked = ranked;
            this.kit = kit;
            queueData.add(this);
//            remove(player);
//            new MapProvider(player, null, player.getWorld(), false, true);
        }

        public boolean canJoin(QueueData data) {
            Player playerOne = ProPlugin.getPlayer(player);
            Player playerTwo = ProPlugin.getPlayer(data.getPlayer());
            if(playerOne == null || playerTwo == null) {
            	return false;
            }
            if(playerOne.getAddress().getAddress().getHostAddress().equals(playerTwo.getAddress().getAddress().getHostAddress())) {
            	if(AccountHandler.getRank(playerOne) != Ranks.OWNER && AccountHandler.getRank(playerTwo) != Ranks.OWNER) {
            		return false;
            	}
            }
            if(forcedPlayer != null && data.getForcedPlayer() != null) {
                return data.getForcedPlayer().equals(forcedPlayer);
            } else {
                return !this.getPlayer().equals(data.getPlayer()) && this.getKit() == data.getKit();
            }
        }

        public boolean isPrioirty() {
            return this.priority;
        }

        public String getPlayer() {
            return this.player;
        }

        public String getForcedPlayer() {
            return this.forcedPlayer;
        }

        public boolean isRanked() {
        	return this.ranked;
        }
        
        public OneVsOneKit getKit() {
            return this.kit;
        }

        public int getCounter() {
            return this.counter;
        }

        public int incrementCounter() {
            return ++this.counter;
        }

        public boolean isPlaying(Player player) {
            return getPlayer().equals(player.getName()) || (getForcedPlayer() != null && getForcedPlayer().equals(player.getName()));
        }
    }
}
