package network.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PrivateMessageEvent;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.DelayedTask;
import network.staff.mute.MuteHandler;

public class PrivateMessaging implements Listener {
	private Map<String, String> lastMessaged = null;
	private List<String> checkedForDisabled = null;
	private List<String> disabled = null;
	private List<String> toldAboutDisabling = null;
	
	public PrivateMessaging() {
		lastMessaged = new HashMap<String, String>();
		checkedForDisabled = new ArrayList<String>();
		disabled = new ArrayList<String>();
		toldAboutDisabling = new ArrayList<String>();
		new CommandBase("msg", 2, -1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						if(MuteHandler.checkMute(player)) {
							MuteHandler.display(player);
						} else {
							Player target = ProPlugin.getPlayer(arguments[0]);
							if(target == null) {
								MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
							} else if(isDisabled(player)) {
								MessageHandler.sendMessage(player, "&cYour messages are disabled, run &a/togglePMs");
							} else if(isDisabled(target) && !Ranks.isStaff(player)) {
								MessageHandler.sendMessage(player, "&c" + target.getName() + "'s messages are disabled");
							} else {
								PrivateMessageEvent event = new PrivateMessageEvent(player);
								Bukkit.getPluginManager().callEvent(event);
								if(!event.isCancelled()) {
									String message = "";
									for(int a = 1; a < arguments.length; ++a) {
										message += arguments[a] + " ";
									}
									MessageHandler.sendMessage(player, "&3Me -> " + target.getName() + ": &f" + message);
									MessageHandler.sendMessage(target, "&3" + player.getName() + " -> Me: &f" + message);
									lastMessaged.put(player.getName(), target.getName());
									lastMessaged.put(target.getName(), player.getName());
								}
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("r", 1, -1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						boolean ableToReply = true;
						if(MuteHandler.checkMute(player)) {
							MuteHandler.display(player);
						} else if(lastMessaged.containsKey(player.getName())){
							Player target = ProPlugin.getPlayer(lastMessaged.get(player.getName()));
							if(target == null) {
								lastMessaged.remove(player.getName());
								ableToReply = false;
							} else if(isDisabled(player)) {
								MessageHandler.sendMessage(player, "&cYour messages are disabled, do &a/togglePMs");
							} else if(isDisabled(target) && !Ranks.isStaff(player)) {
								MessageHandler.sendMessage(player, "&c" + target.getName() + "'s messages are disabled");
							} else {
								PrivateMessageEvent event = new PrivateMessageEvent(player);
								Bukkit.getPluginManager().callEvent(event);
								if(!event.isCancelled()) {
									String message = "";
									for(String argument : arguments) {
										message += argument + " ";
									}
									MessageHandler.sendMessage(player, "&3Me -> " + target.getName() + ": &f" + message);
									MessageHandler.sendMessage(target, "&3" + player.getName() + " -> Me: &f" + message);
									lastMessaged.put(player.getName(), target.getName());
									lastMessaged.put(target.getName(), player.getName());
								}
							}
						} else {
							ableToReply = false;
						}
						if(!ableToReply) {
							MessageHandler.sendMessage(player, "&cNo one to reply to");
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
	}
	
	private boolean isDisabled(final Player player) {
		if(!checkedForDisabled.contains(player.getName())) {
			checkedForDisabled.add(player.getName());
			if(DB.PLAYERS_DISABLED_MESSAGES.isUUIDSet(player.getUniqueId())) {
				disabled.add(player.getName());
			}
		}
		return disabled.contains(player.getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		lastMessaged.remove(event.getPlayer().getName());
		checkedForDisabled.remove(event.getPlayer().getName());
		disabled.remove(event.getPlayer().getName());
		toldAboutDisabling.remove(event.getPlayer().getName());
	}
}
