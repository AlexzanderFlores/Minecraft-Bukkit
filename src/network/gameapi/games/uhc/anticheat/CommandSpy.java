package network.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import network.ProPlugin;
import network.gameapi.games.uhc.HostHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.util.EventUtil;

public class CommandSpy implements Listener {
	private List<String> enabled = null;
	
	public CommandSpy() {
		new CommandBase("commandSpy", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(Ranks.STAFF.hasRank(player) || HostHandler.isHost(player.getUniqueId())) {
					if(enabled.contains(player.getName())) {
						enabled.remove(player.getName());
						MessageHandler.sendMessage(player, "Command Spy is now &cOFF");
					} else {
						enabled.add(player.getName());
						MessageHandler.sendMessage(player, "Command Spy is now &eON");
					}
				} else {
					MessageHandler.sendUnknownCommand(player);
				}
				return true;
			}
		};
		enabled = new ArrayList<>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(Ranks.OWNER.hasRank(event.getPlayer())) {
			return;
		}
		for(String name : enabled) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				MessageHandler.sendMessage(player, "&6&lCS: " + AccountHandler.getPrefix(event.getPlayer()) + ": " + event.getMessage());
			}
		}
	}
}
