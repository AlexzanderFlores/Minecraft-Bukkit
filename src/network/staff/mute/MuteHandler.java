package network.staff.mute;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.TimeUtil;
import network.staff.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MuteHandler extends Punishment {
	private static Map<String, MuteData> muteData = new HashMap<String, MuteData>();
	private static List<String> checkedForMuted = new ArrayList<String>();
	
	public static class MuteData {
		private String player = null;
		private String time = null;
		private String expires = null;
		private String staffName = null;
		private String reason = null;
		private List<String> proof = null;
		
		public MuteData(Player player) {
			this.player = player.getName();
			DB table = DB.STAFF_MUTES;
			ResultSet resultSet = null;
			try {
				String query = "SELECT * FROM " + table.getName() + " WHERE uuid = '" + player.getUniqueId().toString() + "' AND active = '1' LIMIT 1";
				resultSet = table.getConnection().prepareStatement(query).executeQuery();
				while(resultSet.next()) {
					this.time = resultSet.getString("time");
					this.expires = resultSet.getString("expires");
					if(!hasExpired(player)) {
						String uuid = resultSet.getString("staff_uuid");
						if(uuid.equals("CONSOLE")) {
							this.staffName = uuid;
						} else {
							this.staffName = AccountHandler.getName(UUID.fromString(uuid));
						}
						this.reason = resultSet.getString("reason");
					}
				}
				proof = DB.STAFF_MUTE_PROOF.getAllStrings("proof", new String [] {"uuid", "active"}, new String [] {player.getUniqueId().toString(), "1"});
				muteData.put(this.player, this);
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				DB.close(resultSet);
			}
		}
		
		public MuteData(Player player, String time, String expires, String staff, String reason, String proof) {
			this.player = player.getName();
			this.time = time;
			this.expires = expires;
			this.staffName = staff;
			this.reason = reason;
			this.proof = new ArrayList<String>();
			muteData.put(this.player, this);
		}
		
		public void display(Player player) {
			if(this.player.equals(player.getName())) {
				String proofs = "";
				for(String proofString : proof) {
					proofs += proofString + " ";
				}
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, (this.player.equals(player.getName()) ? "You have" : this.player + " has") + " been muted by &e" + staffName + " &xfor &e" + reason.replace("_", " ") + " " + proofs);
				MessageHandler.sendMessage(player, "Muted at &e" + time);
				MessageHandler.sendMessage(player, "Expires on &e" + expires);
				MessageHandler.sendMessage(player, "Appeal your mute &e" + appeal);
//				MessageHandler.sendMessage(player, "Purchase unmute pass &ehttp://store.1v1s.org/category/771555");
				MessageHandler.sendLine(player);
			}
		}
		
		public boolean hasExpired(Player player) {
			if(!expires.equals("NEVER")) {
				long timeCheck = Long.valueOf(TimeUtil.getTime().split(" ")[0].replace("/", "").replace(":", ""));
				long expiresCheck = Long.valueOf(expires.split(" ")[0].replace("/", "").replace(":", ""));
				if(expiresCheck <= timeCheck) {
					unMute("CONSOLE", player.getUniqueId(), true);
					return true;
				}
			}
			return false;
		}
	}
	
	public MuteHandler() {
		super("MUTED");
		// Command syntax: /mute <player name> <reason> <proof>
		new CommandBase("mute", 3) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					public void run() {
						// Use a try/catch to view if the given reason is valid
						try {
							ChatViolations reason = ChatViolations.valueOf(arguments[1].toUpperCase());
							// Detect if the command should be activated
							PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
							if(result.isValid()) {
								UUID uuid = result.getUUID();
								// See if the player is already muted
								String [] keys = new String [] {"uuid", "active"};
								String [] values = new String [] {uuid.toString(), "1"};
								if(DB.STAFF_MUTES.isKeySet(keys, values)) {
									MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already " + getName());
									return;
								}
								// Get the staff data
								String staffUUID = "CONSOLE";
								Ranks rank = Ranks.OWNER;
								if(sender instanceof Player) {
									Player player = (Player) sender;
									staffUUID = player.getUniqueId().toString();
									rank = AccountHandler.getRank(player);
								}
								// Compile the message and proof strings
								String message = getReason(rank, arguments, reason.toString(), result);
								// Update the database
								String time = TimeUtil.getTime();
								String date = time.substring(0, 7);
								// Set times for temporary mutes, note that being muted twice for any reason(s) will result in a lifetime mute
								String expires = "NEVER";
								int days = reason.getDays();
								int hours = reason.getHours();
								if(days > 0 || hours > 0) {
									int previousMutes = DB.STAFF_MUTES.getSize(keys, new String [] {uuid.toString(), "0"});
									if(previousMutes > 0) {
										days *= previousMutes;
										hours *= previousMutes;
									}
									expires = TimeUtil.addDate(days, hours);
								}
								DB.STAFF_MUTES.insert("'" + uuid.toString() + "', 'null', '" + staffUUID + "', 'null', '" + reason.toString() + "', '" + date + "', '" + time + "', 'null', 'null', '" + expires + "', '1'");
								int id = DB.STAFF_MUTES.getInt(keys, values, "id");
								String proof = (arguments.length == 2 ? "none" : arguments[2]);
								DB.STAFF_MUTE_PROOF.insert("'" + id + "', '" + proof + "'");
								// Perform any final execution instructions
								MessageHandler.alert(message);
								// Mute other accounts attached to the IP
								int counter = 0;
								for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", AccountHandler.getAddress(uuid))) {
									if(!uuidString.equals(uuid.toString())) {
										Player player = Bukkit.getPlayer(UUID.fromString(uuidString));
										if(player != null) {
											player.kickPlayer(ChatColor.RED + "You have been muted due to sharing the IP of " + arguments[0]);
										}
										DB.STAFF_MUTES.insert("'" + uuid.toString() + "', '" + uuidString + "', '" + staffUUID + "', 'null', '" + reason.toString() + "', '" + arguments[2] + "', '" + date + "', '" + time + "', 'null', 'null', '" + expires + "', '1'");
										values = new String [] {uuidString, "1"};
										id = DB.STAFF_MUTES.getInt(keys, values, "id");
										DB.STAFF_MUTE_PROOF.insert("'" + id + "', '" + proof + "'");
										++counter;
									}
								}
								if(counter > 0) {
									MessageHandler.alert("&cMuted &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + arguments[0]);
								}
								// Execute the mute if the player is online
								Player player = ProPlugin.getPlayer(arguments[0]);
								if(player != null) {
									new MuteData(player, time, expires, sender.getName(), reason.toString(), arguments[2]);
									muteData.get(player.getName()).display(player);
								}
							}
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
							// Display all the valid options
							MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown chat violatoin, use one of the following:");
							String reasons = "";
							for(ChatViolations reason : ChatViolations.values()) {
								reasons += "&a" + reason + "&e, ";
							}
							MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	public static boolean checkMute(Player player) {
		if(Ranks.isStaff(player)) {
			return false;
		} else {
			if(!checkedForMuted.contains(player.getName())) {
				checkedForMuted.add(player.getName());
				if(DB.STAFF_MUTES.isUUIDSet(player.getUniqueId())) {
					new MuteData(player);
				}
			}
			return muteData.containsKey(player.getName());
		}
	}
	
	public static void remove(Player player) {
		checkedForMuted.remove(player.getName());
		muteData.remove(player.getName());
	}
	
	public static void unMute(String staff, UUID uuid, boolean editDatabase) {
		Player player = Bukkit.getPlayer(uuid);
		if(player != null) {
			MessageHandler.sendLine(player);
			MessageHandler.sendMessage(player, "&eYour mute has expired! Be sure to follow all rules please! &b/rules");
			MessageHandler.sendLine(player);
			remove(player);
		}
		if(editDatabase) {
			String [] keys = new String [] {"uuid", "active"};
			String [] values = new String [] {uuid.toString(), "1"};
			String time = TimeUtil.getTime();
			String date = time.substring(0, 7);
			DB.STAFF_MUTES.updateString("who_unmuted", staff, keys, values);
			DB.STAFF_MUTES.updateString("unmute_date", date, keys, values);
			DB.STAFF_MUTES.updateString("unmute_time", time, keys, values);
			DB.STAFF_MUTES.updateString("active", "0", keys, values);
		}
	}
	
	public static void display(Player player) {
		if(checkMute(player)) {
			muteData.get(player.getName()).display(player);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(checkMute(player) && !muteData.get(player.getName()).hasExpired(player)) {
			muteData.get(player.getName()).display(player);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
