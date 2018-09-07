package network.player.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import network.Network;

public class BelowNameScoreboardUtil extends ScoreboardBase {
	public BelowNameScoreboardUtil(String name, String type) {
		this(name, type, name);
	}
	
	public BelowNameScoreboardUtil(Scoreboard scoreboard, String name, String type) {
		this(scoreboard, name, type, name);
	}
	
	public BelowNameScoreboardUtil(String name, String type, String display) {
		setObjective(getScoreboard().registerNewObjective(ChatColor.translateAlternateColorCodes('&', name), type));
		getObjective().setDisplaySlot(DisplaySlot.BELOW_NAME);
		getObjective().setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
		Network.setBelowName(this);
	}
	
	public BelowNameScoreboardUtil(Scoreboard scoreboard, String name, String type, String display) {
		setScoreboard(scoreboard);
		setObjective(getScoreboard().registerNewObjective(ChatColor.translateAlternateColorCodes('&', name), type));
		getObjective().setDisplaySlot(DisplaySlot.BELOW_NAME);
		getObjective().setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
		Network.setBelowName(this);
	}
	
	public int getScore(String text) {
		return getObjective().getScore(text).getScore();
	}
	
	public void setScore(String text, int score) {
		getObjective().getScore(text).setScore(score);
	}
}
