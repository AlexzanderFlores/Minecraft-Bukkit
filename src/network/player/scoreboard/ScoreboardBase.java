package network.player.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardBase {
	private Scoreboard scoreboard = null;
	private Objective objective = null;
	
	public ScoreboardBase() {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	}
	
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}
	
	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}
	
	public Objective getObjective() {
		return this.objective;
	}
	
	public void setObjective(Objective objective) {
		this.objective = objective;
	}
}
