package network.player;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import network.customevents.TimeEvent;
import network.gameapi.SpectatorHandler;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;

public class LevelGiver implements Listener {
	private final Player player;
	private boolean demo = false;
	
	public LevelGiver(Player player) {
		this(player, false);
	}
	
	public LevelGiver(Player player, boolean demo) {
		this.player = player;
		this.demo = demo;
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			if(player.isOnline() && !SpectatorHandler.contains(player)) {
				player.setExp(player.getExp() + 0.025f);
				if(player.getExp() > 1.0f) {
					if(demo) {
						player.setExp(0.0f);
						HandlerList.unregisterAll(this);
						return;
					}
					player.setExp(0.0f);
					player.setLevel(player.getLevel() + 1);
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				} else {
					return;
				}
			}
			HandlerList.unregisterAll(this);
		}
	}
}
