package network.staff;

import me.konsolas.aac.api.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.util.EventUtil;
import network.server.util.StringUtil;

public class StaffChat implements Listener {
	public StaffChat() {
		new CommandBase("s", 1, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String staff = "CONSOLE";
				Ranks rank = Ranks.OWNER;
				if(sender instanceof Player) {
					Player player = (Player) sender;
					staff = player.getName();
					rank = AccountHandler.getRank(player);
				}

				String message = "";
				for(String argument : arguments) {
					message += argument + " ";
				}

				send(rank.getColor() + staff + ": &f" + StringUtil.color(message.substring(0, message.length() - 1)));
				return true;
			}
		}.setRequiredRank(Ranks.TRIAL);

		EventUtil.register(this);
	}

	private void send(String message) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(Ranks.isStaff(player)) {
				MessageHandler.sendMessage(player, "&b[Staff] " + message);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(Ranks.isStaff(event.getPlayer())) {
			send(AccountHandler.getPrefix(event.getPlayer()) + " has joined the server");
		}
	}

//	@EventHandler
//	public void onPlayerViolation(PlayerViolationEvent event) {
//		if(event.getViolations() >= 5) {
//			send("&6[AAC] &f" + event.getMessage());
//		}
//	}
}