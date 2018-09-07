package network.player.scoreboard;

import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidebarScoreboardUtil extends ScoreboardBase {
	private boolean enabled = true;
	private Map<String, Integer> scores = null;
	
	public SidebarScoreboardUtil(String name) {
		scores = new HashMap<String, Integer>();
		if(getScoreboard().getObjective(" ") == null) {
			setObjective(getScoreboard().registerNewObjective(" ", "dummy"));
		} else {
			setObjective(getScoreboard().getObjective(" "));
		}
		getObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
		setName(name);
	}
	
	public SidebarScoreboardUtil(Scoreboard scoreboard, String name) {
		scores = new HashMap<String, Integer>();
		setScoreboard(scoreboard);
		if(getScoreboard().getObjective(" ") == null) {
			setObjective(getScoreboard().registerNewObjective(" ", "dummy"));
		} else {
			setObjective(getScoreboard().getObjective(" "));
		}
		getObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
		setName(name);
	}
	
	public void setText(String text, int score) {
		text = StringUtil.color(text);
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		getObjective().getScore(text).setScore(score);
		scores.put(text, score);
	}
	
	public void setText(String [] list) {
		setText(list, list.length);
	}
	
	public void setText(String [] list, int startingScore) {
		for(String text : list) {
			setText(text, startingScore--);
		}
	}
	
	public String getText(int score) {
		for(String text : scores.keySet()) {
			if(scores.get(text) == score) {
				return text;
			}
		}
		return null;
	}
	
	public void removeText(String text) {
		getScoreboard().resetScores(text);
		scores.remove(text);
	}
	
	public void removeScore(int score) {
		List<String> toRemove = new ArrayList<String>();
		for(String entry : scores.keySet()) {
			if(scores.get(entry) == score) {
				toRemove.add(entry);
			}
		}
		for(String remove : toRemove) {
			removeText(remove);
		}
		scores.remove(getText(score));
	}
	
	public void removeScoresBelow(int score) {
		List<String> toRemove = new ArrayList<String>();
		for(String entry : scores.keySet()) {
			if(scores.get(entry) < score) {
				toRemove.add(entry);
			}
		}
		for(String remove : toRemove) {
			removeText(remove);
		}
		scores.remove(getText(score));
	}
	
	public void removeScoresAbove(int score) {
		List<String> toRem = new ArrayList<String>();
		for(String entry : scores.keySet()) {
			if(scores.get(entry) > score) {
				toRem.add(entry);
			}
		}
		for(String rem : toRem) {
			removeText(rem);
		}
		scores.remove(getText(score));
	}
	
	public void clear() {
		for(String score : scores.keySet()) {
			getScoreboard().resetScores(score);
		}
	}
	
	public boolean hasScore(int score) {
		for(String entry : scores.keySet()) {
			if(scores.get(entry) == score) {
				return true;
			}
		}
		return false;
	}
	
	public void setName(String name) {
		getObjective().setDisplayName(StringUtil.color(name));
	}
	
	public void update(String name) {
		if(enabled) {
			setName(name);
			update();
		}
	}
	
	public void update(Player player) {
		if(enabled) {
			player.setScoreboard(getScoreboard());
		}
	}
	
	public void update() {
		if(enabled) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				player.setScoreboard(getScoreboard());
			}
		}
	}
	
	public void toggleEnabled() {
		enabled = !enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public Map<String, Integer> getScores() {
		return scores;
	}
	
	public void remove() {
		if(scores != null) {
			for(String score : scores.keySet()) {
				getScoreboard().resetScores(score);
			}
			scores.clear();
			scores = null;
		}
		getObjective().unregister();
	}
}
