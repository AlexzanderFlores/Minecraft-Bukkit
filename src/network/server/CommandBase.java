package network.server;

import network.Network;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.tasks.DelayedTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandBase implements CommandExecutor {
	private List<String> delayedPlayers = null;
	private Ranks requiredRank = Ranks.PLAYER;
	private int delay = 0;
	private int minArguments = 0;
	private int maxArguments = 0;
	private boolean playerOnly = false;
	
	public CommandBase(String command) {
		this(command, 0);
	}
	
	public CommandBase(String command, boolean playerOnly) {
		this(command, 0, playerOnly);
	}
	
	public CommandBase(String command, int requiredArguments) {
		this(command, requiredArguments, requiredArguments);
	}
	
	public CommandBase(String command, int minArguments, int maxArguments) {
		this(command, minArguments, maxArguments, false);
	}
	
	public CommandBase(String command, int requiredArguments, boolean playerOnly) {
		this(command, requiredArguments, requiredArguments, playerOnly);
	}
	
	public CommandBase(String command, int minArguments, int maxArguments, boolean playerOnly) {
		this.minArguments = minArguments;
		this.maxArguments = maxArguments;
		this.playerOnly = playerOnly;
		try {
			Network.getInstance().getCommand(command).setExecutor(this);
		} catch(Exception e) {
			Bukkit.getLogger().info("");
			Bukkit.getLogger().info("\t\"" + command + "\" command is not registered");
			Bukkit.getLogger().info("");
		}
	}
	
	public CommandBase enableDelay(int delay) {
		this.delay = delay;
		this.delayedPlayers = new ArrayList<String>();
		return this;
	}
	
	public CommandBase removeFromDelay(Player player) {
		if(this.delayedPlayers != null) {
			this.delayedPlayers.remove(player.getName());
		}
		return this;
	}
	
	public CommandBase setRequiredRank(Ranks requiredRank) {
		this.requiredRank = requiredRank;
		return this;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] arguments) {
		if(arguments.length < minArguments || (arguments.length > maxArguments && maxArguments != -1)) {
			return false;
		} else {
			if(playerOnly && !(sender instanceof Player)) {
				MessageHandler.sendPlayersOnly(sender);
			} else {
				if(AccountHandler.getRank(sender).isAboveRank(requiredRank)) {
					if(delayedPlayers != null && sender instanceof Player) {
						final Player player = (Player) sender;
						if(delayedPlayers.contains(player.getName())) {
							if(delay == 1) {
								MessageHandler.sendMessage(player, "&cThis command has a delay of &e" + delay + "&c second");
							} else {
								MessageHandler.sendMessage(player, "&cThis command has a delay of &e" + delay + "&c seconds");
							}
						} else {
							delayedPlayers.add(player.getName());
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									delayedPlayers.remove(player.getName());
								}
							}, 20 * delay);
							return execute(sender, arguments);
						}
					} else {
						return execute(sender, arguments);
					}
				} else {
					MessageHandler.sendMessage(sender, "&cThis command requires " + requiredRank.getPrefix());
				}
			}
			return true;
		}
	}
	
	public abstract boolean execute(CommandSender sender, String [] arguments);
}
