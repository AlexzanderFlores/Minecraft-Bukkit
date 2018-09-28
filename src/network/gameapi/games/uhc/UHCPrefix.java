package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import network.customevents.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import network.Network;
import network.Network.Plugins;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;

public class UHCPrefix implements Listener {
	private List<UUID> hosts;
	private List<UUID> queue;
	
	public UHCPrefix() {
		hosts = new ArrayList<>();
		queue = new ArrayList<>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		queue.add(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 10 && !queue.isEmpty()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					UUID uuid = queue.get(0);
					Player player = Bukkit.getPlayer(uuid);
					if(player != null && DB.NETWORK_UHC_HOSTS.isUUIDSet(uuid)) {
						hosts.add(uuid);
					}
					queue.remove(0);
				}
			});
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		if(hosts.contains(uuid)) {
			if(Network.getPlugin() == Plugins.UHC) {
				Player mainHost = HostHandler.getMainHost();
				if(mainHost != null && mainHost.getUniqueId() == uuid) {
					return;
				}
			}
			event.setFormat(ChatColor.YELLOW + "[UHC] " + event.getFormat());
		}
	}
}
