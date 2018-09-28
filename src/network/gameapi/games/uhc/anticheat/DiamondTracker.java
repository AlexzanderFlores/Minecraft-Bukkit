package network.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.customevents.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.games.uhc.HostHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class DiamondTracker implements Listener {
	private Map<String, Integer> mined;
	private List<String> delayed;
	
	public DiamondTracker() {
		mined = new HashMap<>();
		delayed = new ArrayList<>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.DIAMOND_ORE) {
			Player player = event.getPlayer();
			int times = 0;
			if(mined.containsKey(player.getName())) {
				times = mined.get(player.getName());
			}

			if(++times >= 10 && !delayed.contains(player.getName())) {
				String name = player.getName();
				delayed.add(name);
				new DelayedTask(() -> delayed.remove(name), 20 * 5);

				for(Player online: Bukkit.getOnlinePlayers()) {
					if(HostHandler.isHost(online.getUniqueId())) {
						MessageHandler.sendMessage(online, AccountHandler.getPrefix(player) + " &cHAS MINED &e&l" + times + " &cDIAMONDS WITHIN THE LAST 5 MINUTES");
					}
				}
			}
			mined.put(player.getName(), times);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 20 * 60 * 5) {
			mined.clear();
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		mined.remove(event.getPlayer().getName());
	}
}
