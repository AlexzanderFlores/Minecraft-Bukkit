package network.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.ProPlugin;
import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import npc.NPCEntity;

public class CPSDetector implements Listener {
	private Map<String, Integer> clicks = null;
	private List<String> delayed = null;
	
	public CPSDetector(Location location) {
		this(location, location.getWorld().getSpawnLocation());
	}
	
	public CPSDetector(Location location, Location target) {
		clicks = new HashMap<String, Integer>();
		delayed = new ArrayList<String>();
		new NPCEntity(EntityType.SKELETON, "&e&nCPS Detector", location, target) {
			@Override
			public void onInteract(Player player) {
				int click = 0;
				if(clicks.containsKey(player.getName())) {
					click = clicks.get(player.getName());
				}
				clicks.put(player.getName(), ++click);
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(final String name : clicks.keySet()) {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					MessageHandler.sendMessage(player, "&aCPS registered: &c" + clicks.get(name));
					if(!delayed.contains(name)) {
						delayed.add(name);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								delayed.remove(name);
							}
						}, 20 * 10);
						MessageHandler.sendMessage(player, "&aNote: &eThis is what the server registers. It may not be your true CPS");
					}
				}
			}
			clicks.clear();
		}
	}
}