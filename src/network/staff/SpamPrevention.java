package network.staff;

import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class SpamPrevention implements Listener {
	private Map<String, String> lastMessages = null;
	
	public SpamPrevention() {
		lastMessages = new HashMap<String, String>();
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		if(lastMessages.containsKey(player.getName()) && lastMessages.get(player.getName()).equals(msg)) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(!player.getName().equals(online.getName())) {
					event.getRecipients().remove(online);
				}
			}
		} else if(msg.length() >= 2) {
			for(int a = 3; a < msg.length(); ++a) {
				if(msg.charAt(a - 3) == msg.charAt(a) && msg.charAt(a - 2)  == msg.charAt(a) && msg.charAt(a - 1) == msg.charAt(a)) {
					String spam = "";
					for(int b = 0; b < 4; ++b) {
						spam += msg.charAt(a);
					}
					MessageHandler.sendMessage(player, "&cSpamming is against our rules &7(&c\"" + spam + "\"&7)");
					event.setCancelled(true);
					return;
				}
			}
		}
		lastMessages.put(player.getName(), event.getMessage());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		lastMessages.remove(event.getPlayer().getName());
	}
}
