package network.server.util;

import org.bukkit.ChatColor;

public class CountDownUtil {
	private int counter = 0;
	
	public CountDownUtil() {
		
	}
	
	public CountDownUtil(int counter) {
		setCounter(counter);
	}
	
	public int getCounter() {
		return this.counter;
	}
	
	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public void incrementCounter() {
		setCounter(getCounter() + 1);
	}
	
	public void decrementCounter() {
		setCounter(getCounter() - 1);
	}
	
	public String getCounterAsString() {
		return getCounterAsString(ChatColor.YELLOW);
	}
	
	public String getCounterAsString(ChatColor color) {
		String seconds = String.valueOf(counter % 60 < 10 ? "0" + counter % 60 : counter % 60);
		ChatColor chatColor = counter == 1 ? ChatColor.DARK_RED : counter == 2 ? ChatColor.GOLD : color;
		return chatColor + (counter / 60 >= 1 ? counter / 60 + ":" + seconds : "0:" + seconds);
	}
	
	public static String getCounterAsString(int counter) {
		return getCounterAsString(counter, ChatColor.YELLOW);
	}
	
	public static String getCounterAsString(int counter, ChatColor color) {
		String seconds = String.valueOf(counter % 60 < 10 ? "0" + counter % 60 : counter % 60);
		ChatColor chatColor = counter == 1 ? ChatColor.DARK_RED : counter == 2 ? ChatColor.GOLD : color;
		return chatColor + (counter / 60 >= 1 ? counter / 60 + ":" + seconds : "0:" + seconds);
	}
	
	public boolean canDisplay() {
		return (getCounter() % (5 * 60) == 0 || getCounter() % (2 * 60) == 0 || (getCounter() <= 60 && getCounter() % 10 == 0) || getCounter() <= 5) && getCounter() > 0;
	}
}
