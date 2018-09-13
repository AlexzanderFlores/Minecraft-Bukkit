package network.player.account;

import network.Network;
import network.customevents.TimeEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.customevents.player.AsyncPostPlayerJoinEvent;
import network.gameapi.SpectatorHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import network.staff.StaffMode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerTracker implements Listener {
	private List<String> queue = null;
	
	public PlayerTracker() {
		queue = new ArrayList<String>();
		/*new CommandBase("seen", 1) {
            @Override
            public boolean execute(final CommandSender sender, final String[] arguments) {
                new AsyncDelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        UUID uuid = AccountHandler.getUUID(arguments[0]);
                        if(uuid == null) {
                            MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
                        } else if(DB.PLAYERS_LOCATIONS.isUUIDSet(uuid)) {
                        	String prefix = DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "prefix");
                            String location = DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "location");
                            if(sender instanceof Player) {
                                Player player = (Player) sender;
                                ChatClickHandler.sendMessageToRunCommand(player, " &bCLICK TO JOIN", "Click to join " + location, "/join " + location, prefix + " &eis on " + location);
                            } else {
                                MessageHandler.sendMessage(sender, location);
                            }
                        } else {
                        	MessageHandler.sendMessage(sender, "&cCould not find " + arguments[0]);
                        }
                    }
                });
                return true;
            }
        }.enableDelay(1);
        new CommandBase("staff") {
            @Override
            public boolean execute(final CommandSender sender, String[] arguments) {
                new AsyncDelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        List<String> staffUUIDs = DB.STAFF_ONLINE.getAllStrings("uuid");
                        if(staffUUIDs.isEmpty()) {
                            MessageHandler.sendMessage(sender, "&cThere are currently no staff available!");
                        } else {
                            MessageHandler.sendMessage(sender, "&aOnline Staff (&b" + staffUUIDs.size() + "&a)");
                            for(String uuid : staffUUIDs) {
                            	String prefix = DB.STAFF_ONLINE.getString("uuid", uuid.toString(), "prefix");
                                String server = DB.STAFF_ONLINE.getString("uuid", uuid, "server");
                                if(server.equalsIgnoreCase("VANISHED")) {
                                	MessageHandler.sendMessage(sender, prefix + " &eis &cVANISHED");
                                } else {
                                	if(sender instanceof Player) {
                                        Player player = (Player) sender;
                                        ChatClickHandler.sendMessageToRunCommand(player, " &bCLICK TO JOIN", "Click to join " + server, "/join " + server, prefix + " &eis on " + server);
                                    } else {
                                        MessageHandler.sendMessage(sender, server);
                                    }
                                }
                            }
                        }
                    }
                });
                return true;
            }
        }.enableDelay(1);*/
        new CommandBase("staffHere") {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                List<String> onlineStaff = new ArrayList<String>();
                for(Player online : Bukkit.getOnlinePlayers()) {
                    if(Ranks.isStaff(online)) {
                        onlineStaff.add(AccountHandler.getPrefix(online, true, true) + (StaffMode.getInstance().contains(online) ? " &a(SM)" : ""));
                    }
                }
                if(!onlineStaff.isEmpty()) {
                    MessageHandler.sendMessage(sender, "");
                    String message = "Staff on this server: (&b" + onlineStaff.size() + "&x) ";
                    for(String staff : onlineStaff) {
                        message += staff + "&f, ";
                    }
                    MessageHandler.sendMessage(sender, message.substring(0, message.length() - 2));
                    MessageHandler.sendMessage(sender, "");
                    MessageHandler.sendMessage(sender, "View this any time: &c/staffHere");
                    MessageHandler.sendMessage(sender, "");
                    onlineStaff.clear();
                }
                onlineStaff = null;
                return true;
            }
        }.setRequiredRank(Ranks.TRIAL);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 15) {
			if(!queue.isEmpty()) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						/*String name = queue.get(0);
						Player player = ProPlugin.getPlayerOne(name);
						if(player != null) {
							UUID uuid = player.getUniqueId();
							DB.PLAYERS_LOCATIONS.insert("'" + uuid.toString() + "', '" + AccountHandler.getPrefix(player) + "', '" + Network.getServerName() + "'");
						}*/
						queue.remove(0);
					}
				});
			}
		}
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.isStaff(player)) {
			String location = null;
			if(SpectatorHandler.contains(player)) {
				location = "VANISHED";
			} else {
				location = Network.getServerName();
			}
			//DB.STAFF_ONLINE.insert("'" + player.getUniqueId().toString() + "', '" + AccountHandler.getPrefix(player) + "', '" + location + "'");
		} else {
			queue.add(player.getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		//OSTB.getClient().sendMessageToServer(new Instruction(new String [] {Inst.SERVER_PLAYER_DISCONNECT.toString(), uuid.toString()}));
//		DB.STAFF_ONLINE.deleteUUID(uuid);
//		DB.PLAYERS_LOCATIONS.deleteUUID(uuid);
	}
}
