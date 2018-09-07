package network.player;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import network.gameapi.SpectatorHandler;

public class PlayerMove implements Listener {
	private static PlayerMove instance = null;
	private boolean preventY;
	
	public PlayerMove(boolean preventY) {
		instance = this;
		this.preventY = preventY;
	}
	
	public static PlayerMove getInstance() {
		return instance;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer())) {
			Location to = event.getTo();
			Location from = event.getFrom();
			boolean cannotMove = to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ();
			if(preventY) {
				cannotMove |= to.getBlockY() != from.getBlockY();
			}
			if(cannotMove) {
				from.setYaw(event.getPlayer().getLocation().getYaw());
				from.setPitch(event.getPlayer().getLocation().getPitch());
				event.setTo(from);
			}
		}
	}
}
