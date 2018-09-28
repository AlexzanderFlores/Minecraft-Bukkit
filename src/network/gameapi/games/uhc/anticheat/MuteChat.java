package network.gameapi.games.uhc.anticheat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import network.gameapi.games.uhc.HostHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.util.EventUtil;

public class MuteChat implements Listener {
	private static boolean muted = false;
	private static boolean hasBeenManuallyMuted = false;
	
	public MuteChat() {
		new CommandBase("muteChat") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!Ranks.STAFF.hasRank(player) && !HostHandler.isHost(player.getUniqueId())) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				muted = !muted;
				MessageHandler.alert("Chat is now " + (muted ? "&cMUTED" : "&eUNMUTED"));
				hasBeenManuallyMuted = true;
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static boolean isMuted() {
		return muted;
	}
	
	public static boolean hasBeenManuallyMuted() {
		return hasBeenManuallyMuted;
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(isMuted() && !Ranks.TRIAL.hasRank(event.getPlayer()) && !HostHandler.isHost(event.getPlayer().getUniqueId())) {
			MessageHandler.sendMessage(event.getPlayer(), "&cThe chat is currently muted");
			MessageHandler.sendMessage(event.getPlayer(), "Need help? &b/helpop <Text>");
			event.setCancelled(true);
		}
	}
}
