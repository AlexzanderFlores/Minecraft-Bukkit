package network.server.util;

import org.bukkit.ChatColor;

public class StringUtil {
	public static String getFirstLetterCap(String string) {
		string = string.toLowerCase();
		return string.substring(0, 1).toUpperCase() + string.substring(1, string.length()).replace("_", " ");
	}
	
	public static String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}
}
