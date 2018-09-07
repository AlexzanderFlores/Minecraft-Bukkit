package network.server;

import network.Network;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PerformanceHandler implements Listener {
	private int counter = 0;
	private static double ticksPerSecond = 0;
	private long seconds = 0;
	private long currentSecond = 0;
	private int tickCounter = 0;
	private static int uptimeCounter = 0;
	
	public PerformanceHandler() {
		new CommandBase("lag") {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					String perm = "bukkit.command.tps";
					PermissionAttachment permission = player.addAttachment(Network.getInstance());
					permission.setPermission(perm, true);
					player.chat("/tps");
					permission.unsetPermission(perm);
					permission.remove();
					permission = null;
					MessageHandler.sendMessage(sender, "&bPing: &c" + getPing(player));
				} else {
					Bukkit.dispatchCommand(sender, "tps");
				}
				int averagePing = 0;
				if(Bukkit.getOnlinePlayers().size() > 0) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						averagePing += getPing(player);
					}
					averagePing /= Bukkit.getOnlinePlayers().size();
				}
				//MessageHandler.sendMessage(sender, "&bTicks per second: &c" + ticksPerSecond);
				MessageHandler.sendMessage(sender, "&bAverage ping: &c" + averagePing);
				MessageHandler.sendMessage(sender, "&bConnected clients: &c" + Bukkit.getOnlinePlayers().size());
				MessageHandler.sendMessage(sender, "&bUsed memory: &c" + getMemory(!Ranks.OWNER.hasRank(sender)) + "%");
				MessageHandler.sendMessage(sender, "&bUptime: &c" + getUptimeString());
				MessageHandler.sendMessage(sender, "&eFor more server performance info run /networkPerformance");
				return true;
			}
		};
		new CommandBase("ping", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(arguments.length == 0 || !Ranks.isStaff(sender)) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						MessageHandler.sendMessage(player, "Your ping is " + getPing(player));
					} else {
						MessageHandler.sendPlayersOnly(sender);
					}
				} else if(arguments.length == 1) {
					Player player = ProPlugin.getPlayer(arguments[0]);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
					} else {
						MessageHandler.sendMessage(sender, player.getName() + "'s ping is " + getPing(player));
					}
				}
				return true;
			}
		};
		final List<Integer> counters = new ArrayList<Integer>();
		for(int a = 1; a <= 20; ++a) {
			counters.add(a);
		}
		counters.add(20 * 2);
		counters.add(20 * 5);
		counters.add(20 * 10);
		counters.add(20 * 60);
		counters.add(20 * 60 * 5);
		counters.add(20 * 60 * 10);
		Bukkit.getScheduler().runTaskTimer(Network.getInstance(), new Runnable() {
			@Override
			public void run() {
				++counter;
				for(int a : counters) {
					if(counter % a == 0) {
						Bukkit.getPluginManager().callEvent(new TimeEvent(a));
					}
				}
			}
		}, 1, 1);
		EventUtil.register(this);
	}
	
	public static int getPing(Player player) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		return craftPlayer.getHandle().ping / 2;
	}
	
	public static double getTicksPerSecond() {
		return ticksPerSecond;
	}
	
	public static double getMemory() {
		return getMemory(true);
	}
	
	public static double getMemory(boolean round) {
		double total = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		double allocated = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		return (int) (total * 100.0d / allocated + 0.5);
	}
	
	public static String getUptimeString() {
		String uptime = null;
		if(uptimeCounter < 60) {
			uptime = uptimeCounter + " second(s)";
		} else if(uptimeCounter < (60 * 60)) {
			int minutes = getAbsoluteValue((uptimeCounter / 60));
			int seconds = getAbsoluteValue((uptimeCounter % 60));
			uptime = minutes + " minute(s) and " + seconds + " second(s)";
		} else {
			int hours = getAbsoluteValue((uptimeCounter / 60 / 60));
			int minutes = getAbsoluteValue((hours * 60) - (uptimeCounter / 60));
			int seconds = getAbsoluteValue((uptimeCounter % 60));
			uptime = hours + " hour(s) and " + minutes + " minute(s) and " + seconds + " second(s)";
		}
		return uptime;
	}
	
	public static int getUptime() {
		return uptimeCounter;
	}
	
	private static int getAbsoluteValue(int value) {
		if(value < 0) {
			value *= -1;
		}
		return value;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			seconds = (System.currentTimeMillis() / 1000);
			if(currentSecond == seconds) {
				++tickCounter;
			} else {
				currentSecond = seconds;
				ticksPerSecond = (ticksPerSecond == 0 ? tickCounter : ((ticksPerSecond + tickCounter) / 2));
				if(ticksPerSecond < 19.0d) {
					++ticksPerSecond;
				}
				if(ticksPerSecond > 20.0d) {
					ticksPerSecond = 20.0d;
				}
				ticksPerSecond = new BigDecimal(ticksPerSecond).setScale(2, RoundingMode.HALF_UP).doubleValue();
				tickCounter = 0;
			}
		} else if(ticks == 20) {
			++uptimeCounter;
		}
	}
}
