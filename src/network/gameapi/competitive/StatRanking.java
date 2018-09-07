package network.gameapi.competitive;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.customevents.game.GameDeathEvent;
import network.customevents.game.GameWinEvent;
import network.gameapi.MiniGame;
import network.player.MessageHandler;
import network.server.ChatClickHandler;
import network.server.DB;
import network.server.DB.Databases;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;

public class StatRanking implements Listener {
	public StatRanking() {
		if(Network.getMiniGame() != null && !Network.getMiniGame().getPlayersHaveOneLife()) {
			return;
		}
		EventUtil.register(this);
	}
	
	private void display(final Player player) {
		MiniGame miniGame = Network.getMiniGame();
		if(player == null || miniGame == null || !miniGame.getPlayersHaveOneLife()) {
			return;
		}
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				StatsHandler.save(player);
				int rank = -1;
				int nextRank = -1;
				int kills = 0;
				DB db = DB.PLAYERS_STATS_SKY_WARS;
				ResultSet resultSet = null;
				try {
					Connection connection = Databases.PLAYERS.getConnection();
					connection.prepareStatement("SET @uuid = '" + player.getUniqueId().toString() + "'").executeQuery();
					connection.prepareStatement("SET @kills = (SELECT kills FROM " + db.getName() + " WHERE uuid = @uuid)").executeQuery();
					resultSet = connection.prepareStatement("SELECT kills,(SELECT COUNT(*) FROM " + db.getName() + " WHERE kills >= @kills) AS rank, (SELECT COUNT(*) FROM " + db.getName() + " WHERE kills >= (@kills + 1)) AS next FROM " + db.getName() + " WHERE uuid = @uuid").executeQuery();
					while(resultSet.next()) {
						kills = resultSet.getInt("kills");
						rank = resultSet.getInt("rank");
						nextRank = resultSet.getInt("next");
					}
					connection.prepareStatement("SET @uuid = NULL, @kills = NULL").executeQuery();
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(resultSet);
				}
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "&a&lRanking: &xYou now have &b" + kills + " &xkill" + (kills == 1 ? "" : "s") + " bringing you to rank &b#" + rank);
				MessageHandler.sendMessage(player, "&a&lRanking: &xGetting &b1 &xmore kill will bring you to rank &b#" + nextRank);
				ChatClickHandler.sendMessageToRunCommand(player, " &a&lClick Here", "Click to Play Again", "/autoJoin", "&ePlay again to try and get to rank &b#" + nextRank);
			}
		}, 20);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		display(event.getPlayer());
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		display(event.getPlayer());
	}
}
