package network.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import network.server.util.StringUtil;

public class MessageHandler {
	public static void sendMessage(CommandSender sender, String message) {
		message = message.replace("&x", "&e");
		sender.sendMessage(StringUtil.color("&e" + message));
	}
	
	public static void sendLine(CommandSender sender) {
		sendLine(sender, "&4");
	}
	
	public static void sendLine(CommandSender sender, String color) {
		sendMessage(sender, color + "&m-----------------------------------------------------");
	}
	
	public static void sendUnknownCommand(CommandSender sender) {
		sendMessage(sender, "&fUnknown command. Type \"/help\" for help.");
	}
	
	public static void sendPlayersOnly(CommandSender sender) {
		sendMessage(sender, "Only players can use this command");
	}
	
	public static void alert(String message) {
		Bukkit.getLogger().info(message);
		for(Player player : Bukkit.getOnlinePlayers()) {
			sendMessage(player, message);
		}
	}
	
	public static void alertLine() {
		alertLine("&4");
	}
	
	public static void alertLine(String color) {
		alert(color + "&m-----------------------------------------------------");
	}
}
