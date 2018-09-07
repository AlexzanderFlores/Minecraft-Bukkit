package network.server;

import network.Network;
import network.customevents.TimeEvent;
import network.player.account.AccountHandler.Ranks;
import network.server.DB.Databases;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandDispatcher implements Listener {
	public CommandDispatcher() {
		new CommandBase("hubAlert", 1, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String alert = "";
				for(String argument : arguments) {
					alert += argument + " ";
				}
				sendToGame("hub", "say " + alert);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("hubCommand", 1, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String command = "";
				for(String argument : arguments) {
					command += argument + " ";
				}
				sendToGame("hub", command);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("globalAlert", -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String alert = "";
				for(String arg : arguments) {
					alert += arg + " ";
				}
				sendToAll("say " + alert);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static void sendToAll(final String command) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getLogger().info("Sending \"" + command + "\" to all servers");
				List<String> servers = new ArrayList<String>();
				ResultSet resultSet = null;
				try {
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT game_name, server_number FROM server_status").executeQuery();
					while(resultSet.next()) {
						servers.add(resultSet.getString("game_name") + resultSet.getInt("server_number"));
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					if(resultSet != null) {
						try {						
							resultSet.close();
						} catch(SQLException e) {
							e.printStackTrace();
						}
					}
				}
				for(String server : servers) {
//					DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + server + "', '" + command + "'");
				}
				servers.clear();
				servers = null;
			}
		});
	}
	
	public static void sendToGame(final String game, final String command) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getLogger().info("Sending \"" + command + "\" to all \"" + game + "\" servers");
//				for(String serverNumber : DB.NETWORK_SERVER_STATUS.getAllStrings("server_number", "game_name", game)) {
//					DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + game + serverNumber + "', '" + command + "'");
//				}
			}
		});
	}
	
	public static void sendToServer(final String server, final String command) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getLogger().info("Sending \"" + command + "\" to \"" + server + "\"");
//				DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + server + "', '" + command + "'");
			}
		});
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String server = Network.getServerName();
					try {
//						for(String command : DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.getAllStrings("command", "server", server)) {
//							Bukkit.getLogger().info("Command Dispatcher: Running \"" + command + "\" for \"" + server + "\"");
//							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
//						}
					} catch(Exception e) {
						e.printStackTrace();
					}
//					DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.delete("server", server);
				}
			});
		}
	}
}
