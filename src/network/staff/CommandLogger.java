package network.staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import network.customevents.player.AsyncPlayerLeaveEvent;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.util.EventUtil;
import network.server.util.TimeUtil;

public class CommandLogger implements Listener {
	private Map<String, List<String>> playerCommands = null;
	
	public CommandLogger() {
		playerCommands = new HashMap<String, List<String>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if(Ranks.isStaff(player) && !event.getMessage().toLowerCase().contains("killaura")) {
			List<String> commands = new ArrayList<String>();
			if(playerCommands.containsKey(player.getName())) {
				commands = playerCommands.get(player.getName());
			}
			commands.add(event.getMessage().replace("\'", "\""));
			playerCommands.put(player.getName(), commands);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		String name = event.getName();
		if(playerCommands.containsKey(name)) {
			String time = TimeUtil.getTime();
			for(String command : playerCommands.get(name)) {
				DB.STAFF_COMMANDS.insert("'" + uuid.toString() + "', '" +  time + "', '" + command + "'");
			}
			playerCommands.get(name).clear();
			playerCommands.remove(name);
		}
	}
}
