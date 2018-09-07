package network.gameapi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import network.Network;
import network.ProPlugin;
import network.Network.Plugins;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;

public class AutoJoinHandler implements Listener {
	public AutoJoinHandler() {
		new CommandBase("autoJoin", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				send(player);
				return true;
			}
		}.enableDelay(1);
	}
	
	public static String getBestServer(Plugins plugin) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DB table = DB.NETWORK_SERVER_STATUS;
		try {
			int max = Bukkit.getMaxPlayers();
			statement = table.getConnection().prepareStatement("SELECT server_number FROM " + table.getName() + " WHERE game_name = '" + plugin.toString() + "' AND players < " + max + " ORDER BY listed_priority, players DESC, server_number LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next() && resultSet.getInt(1) > 0) {
				return plugin.getServer() + resultSet.getInt("server_number");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(statement, resultSet);
		}
		return "hub";
	}
	
	public static void send(Player player) {
		send(player, Network.getPlugin());
	}
	
	public static void send(final Player player, final Plugins plugin) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ProPlugin.sendPlayerToServer(player, getBestServer(plugin));
			}
		});
	}
}
