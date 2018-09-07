package network.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerRankChangeEvent;
import network.customevents.player.PostPlayerJoinEvent;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.util.EventUtil;

public class TeamScoreboardHandler implements Listener {
	public TeamScoreboardHandler() {
		EventUtil.register(this);
	}
	
	private void set(Player player) {
		remove(player);
		Ranks pRank = AccountHandler.getRank(player);
		for(Player online : Bukkit.getOnlinePlayers()) {
			Ranks oRank = AccountHandler.getRank(online);
			try {
				online.getScoreboard().getTeam(getName(pRank)).addPlayer(player);
			} catch(NullPointerException e) {
				
			}
			try {
				player.getScoreboard().getTeam(getName(oRank)).addPlayer(online);
			} catch(NullPointerException e) {
				
			}
		}
	}
	
	private void remove(Player player) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			Scoreboard scoreboard = online.getScoreboard();
			for(Team team : scoreboard.getTeams()) {
				team.removePlayer(player);
			}
		}
	}
	
	private String getName(Ranks rank) {
		String name = rank.getPrefix().replace(" ", "");
		name = name.substring(0, name.length() - 2);
		return name;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		Scoreboard scoreboard = player.getScoreboard();
		for(Ranks rank : Ranks.values()) {
			if(scoreboard.getTeam(getName(rank)) == null) {
				Team team = scoreboard.registerNewTeam(getName(rank));
				team.setPrefix(rank.getColor() + "");
			}
		}
		set(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerRankChange(PlayerRankChangeEvent event) {
		set(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
