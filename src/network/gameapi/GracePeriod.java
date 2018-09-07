package network.gameapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import network.Network;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.game.GracePeriodEndEvent;
import network.player.TitleDisplayer;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.util.CountDownUtil;
import network.server.util.EventUtil;

public class GracePeriod extends CountDownUtil implements Listener {
	private static GracePeriod instance = null;
	private static boolean isRunning = false;
	
	public GracePeriod(int seconds) {
		super(seconds);
		instance = this;
		isRunning = true;
		Network.getProPlugin().setAllowEntityDamage(false);
		Network.getProPlugin().setAllowEntityDamageByEntities(false);
		EventUtil.register(instance);
		new CommandBase("grace", 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				try {
					setCounter(Integer.valueOf(arguments[0]));
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(getCounter() <= 0) {
				isRunning = false;
				HandlerList.unregisterAll(instance);
				Network.getProPlugin().setAllowEntityDamage(true);
				Network.getProPlugin().setAllowEntityDamageByEntities(true);
				Network.getProPlugin().setAllowBowShooting(true);
				for(Player player : ProPlugin.getPlayers()) {
					new TitleDisplayer(player, "&cPVP Enabled").setFadeIn(5).setStay(30).setFadeOut(5).display();
				}
				Bukkit.getPluginManager().callEvent(new GracePeriodEndEvent());
			} else {
				if(getCounter() <= 3) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						new TitleDisplayer(player, "&cPVP Enabled in", "&e0:0" + getCounter()).setFadeIn(5).setStay(15).setFadeOut(5).display();
					}
				}
			}
			decrementCounter();
		}
	}
	
	public static boolean isRunning() {
		return isRunning;
	}
	
	public static String getGraceCounterString() {
		return instance.getCounterAsString();
	}
	
	public static String getGraceCounterString(ChatColor color) {
		return instance.getCounterAsString(color);
	}
	
	public static int getGraceCounter() {
		return instance.getCounter();
	}
}
