package network.staff.ban;

import network.ProPlugin;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.TimeUtil;
import network.staff.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.UUID;

public class BanHandler extends Punishment implements Listener {
	public enum Violations {CHEATING, CHARGING_BACK}
	
	public BanHandler() {
		super("Banned");
		// Command syntax: /ban <name> <reason> <proof>
		new CommandBase("ban", 2, -1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						// See if they are not attaching proof to the command, if they aren't and they aren't an owner don't run the command
						if(arguments.length == 2 && AccountHandler.getRank(sender) != Ranks.OWNER && AccountHandler.getRank(sender) != Ranks.SENIOR_STAFF) {
							return;
						}
						// Use a try/catch to view if the given reason is valid
						Violations reason = Violations.CHEATING;
						try {
							reason = Violations.valueOf(arguments[1].toUpperCase());
						} catch(IllegalArgumentException e) {
							if(sender instanceof Player) {
								// Display all the valid options
								MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown violation, use one of the following:");
								String reasons = "";
								for(Violations reasonList : Violations.values()) {
									reasons += "&a" + reasonList + "&e, ";
								}
								MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
								return;
							}
						}
						// Detect if the command should be activated
						PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
						if(result.isValid()) {
							UUID uuid = result.getUUID();
							// See if the player is already banned
							String [] keys = new String [] {"uuid", "active"};
							String [] values = new String [] {uuid.toString(), "1"};
							if(DB.STAFF_BAN.isKeySet(keys, values)) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already " + getName());
								return;
							}
							// Get the staff data
							String staff = "CONSOLE";
							String staffUUID = staff;
							if(sender instanceof Player) {
								Player player = (Player) sender;
								staff = player.getName();
								staffUUID = player.getUniqueId().toString();
							}
							// Compile the message and proof strings
							String address = AccountHandler.getAddress(uuid);
							final String message = getReason(AccountHandler.getRank(sender), arguments, reason.toString(), result);
							String time = TimeUtil.getTime();
							String date = time.substring(0, 7);
							int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
							DB.STAFF_BAN.insert("'" + uuid.toString() + "', '" + address + "', 'null', '" + staffUUID + "', 'null',  '" + arguments[1] + "', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
							int id = DB.STAFF_BAN.getInt(keys, values, "id");
							String proof = (arguments.length == 2 ? "none" : arguments[2]);
							DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
							// Perform any final execution instructions
							MessageHandler.alert(message);
							// Ban other accounts attached to the IP
							int counter = 0;
							for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", address)) {
								if(!uuidString.equals(uuid.toString())) {
									final Player player = Bukkit.getPlayer(UUID.fromString(uuidString));
									if(player != null) {
										new DelayedTask(new Runnable() {
											@Override
											public void run() {
												player.kickPlayer(ChatColor.RED + "You have been banned due to sharing the IP of " + arguments[0]);
											}
										});
									}
									keys = new String [] {"uuid", "active"};
									values = new String [] {uuidString, "1"};
									if(!DB.STAFF_BAN.isKeySet(keys, values)) {
										DB.STAFF_BAN.insert("'" + uuidString + "', '" + address + "', '" + uuid.toString() + "', '" + uuidString + "', '" + staffUUID + "', 'null' '" + arguments[1] + "', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
									}
									id = DB.STAFF_BAN.getInt(keys, values, "id");
									DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
									++counter;
								}
							}
							if(counter > 0) {
								MessageHandler.alert("&cBanned &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + arguments[0]);
							}
							// Execute the ban if the player is online
							final Player player = ProPlugin.getPlayer(arguments[0]);
							if(player != null) {
								new DelayedTask(new Runnable() {
									@Override
									public void run() {
										player.kickPlayer(message.replace("&x", "").replace("&c", ""));
									}
								});
							}
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.STAFF);
		new CommandBase("banData", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        UUID uuid = AccountHandler.getUUID(arguments[0]);
                        if(uuid == null) {
                            MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
                        } else {
                            String [] keys = new String [] {"uuid", "active"};
                            String [] values = new String [] {uuid.toString(), "1"};
                            if(DB.STAFF_BAN.isKeySet(keys, values)) {
                                Player player = (Player) sender;
                                display(uuid, player);
                            } else {
                                MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not banned");
                            }
                        }
                    }
                });
				return true;
			}
		}.enableDelay(1);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(checkForBanned(event.getPlayer(), event.getAddress().getHostAddress())) {
			event.setKickMessage(ChatColor.RED + "You are banned");
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	public static boolean checkForBanned(Player player, String address) {
		return DB.STAFF_BAN.isKeySet(new String [] {"uuid", "active"}, new String [] {player.getUniqueId().toString(), "1"}) || 
			   DB.STAFF_BAN.isKeySet(new String [] {"address", "active"}, new String [] {address, "1"});
	}
	
	private static void display(final UUID uuid, final Player viewer) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ResultSet resultSet = null;
				try {
					DB table = DB.STAFF_BAN;
					resultSet = table.getConnection().prepareStatement("SELECT id,reason,time,staff_uuid,attached_uuid, day FROM " + table.getName() + " WHERE uuid = '" + uuid.toString() + "' AND active = 1").executeQuery();
					if(!resultSet.wasNull()) {
						MessageHandler.sendLine(viewer);
						while(resultSet.next()) {
							String id = resultSet.getString("id");
							String reason = resultSet.getString("reason").replace("_", " ");
							String time = resultSet.getString("time");
							MessageHandler.sendMessage(viewer, "This account has been &cBANNED &xfor " + (reason.equals("null") ? "&cN/A" : "&b" + reason) + " &xon &b" + time);
							int counter = 0;
							for(String proof : DB.STAFF_BAN_PROOF.getAllStrings("proof", "ban_id", id)) {
								MessageHandler.sendMessage(viewer, "Proof #" + (++counter) + " &b" + proof);
							}
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&eTo appeal your ban");
							String staff = resultSet.getString("staff_uuid");
							if(staff.equals("CONSOLE") || reason.equals("XRAY")) {
								int loggedDay = resultSet.getInt("day");
								int day = loggedDay - Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
								Bukkit.getLogger().info(loggedDay + " vs " + Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
								if(day > 30) {
									ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&eThis player &bMAY &eappeal at this time");
								} else {
									MessageHandler.sendMessage(viewer, "This player &cMAY NOT &xappeal at this time");
									MessageHandler.sendMessage(viewer, "They must wait &b" + (30 - day) + " &xmore day" + (day == 1 ? "" : "s"));
								}
							}
							if(!staff.equals("CONSOLE")) {
								staff = AccountHandler.getName(UUID.fromString(staff));
							}
							if(Ranks.STAFF.hasRank(viewer)) {
								MessageHandler.sendMessage(viewer, "&c&lThis is ONLY shown to Staff and above");
								MessageHandler.sendMessage(viewer, "Banned by &b" + staff);
							}
						}
						MessageHandler.sendLine(viewer);
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(resultSet);
				}
			}
		});
	}
}
