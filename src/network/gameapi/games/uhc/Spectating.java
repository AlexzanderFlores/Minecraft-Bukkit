package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;

import network.customevents.player.PlayerSpectatorEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;

import network.Network;
import network.ProPlugin;
import network.customevents.player.PlayerSpectateCommandEvent;
import network.gameapi.SpectatorHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class Spectating implements Listener {
	private List<String> delayed = null;
	private int delay = 2;
	
	public Spectating() {
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static Inventory getInventory(Player player, String name, Inventory inventory) {
		if(HostHandler.isHost(player.getUniqueId())) {
			return inventory;
		}
		MessageHandler.sendMessage(player, "&cCannot teleport to a specific player in " + Network.getServerName());
		MessageHandler.sendMessage(player, "Sending you to 0, 0");
		return null;
	}
	
	private boolean isAwayFromSpawn(Location to) {
		return !to.toVector().isInSphere(to.getWorld().getSpawnLocation().toVector(), 150);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(SpectatorHandler.contains(event.getPlayer()) && !HostHandler.isHost(event.getPlayer().getUniqueId()) && !Ranks.STAFF.hasRank(event.getPlayer()) && !delayed.contains(event.getPlayer().getName())) {
			Location to = event.getTo();
			Location from = event.getFrom();
			if(to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ()) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				if(world.getName().equals(WorldHandler.getWorld().getName())) {
					if(isAwayFromSpawn(event.getTo())) {
						MessageHandler.sendMessage(player, "&cYou cannot move that far away from spawn. Spectating is limited to prevent leaking player's information");
						event.setTo(event.getFrom());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectateCommand(PlayerSpectateCommandEvent event) {
		if(!HostHandler.isHost(event.getPlayer().getUniqueId()) && !Ranks.STAFF.hasRank(event.getPlayer())) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot use this command in " + Network.getServerName());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.PLUGIN && SpectatorHandler.contains(event.getPlayer())) {
			final String name = event.getPlayer().getName();
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * delay);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(SpectatorHandler.contains(event.getPlayer()) && !HostHandler.isHost(event.getPlayer().getUniqueId()) && !Ranks.isStaff(event.getPlayer())) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.TRIAL.hasRank(player) && !HostHandler.isHost(player.getUniqueId()) && !SpectatorHandler.contains(player)) {
					event.getRecipients().remove(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(!Ranks.PRO_PLUS.hasRank(event.getPlayer()) && SpectatorHandler.wouldSpectate() && !DisconnectHandler.isDisconnected(event.getPlayer())) {
			event.setKickMessage("To spectate you must have " + Ranks.PRO_PLUS.getPrefix());
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectatorEvent event) {
		if(event.getState() == PlayerSpectatorEvent.SpectatorState.ADDED && !DisconnectHandler.isDisconnected(event.getPlayer())) {
			if((Ranks.PRO_PLUS.hasRank(event.getPlayer()) && HostHandler.isHost(event.getPlayer().getUniqueId()))) {
				event.getPlayer().teleport(WorldHandler.getWorld().getSpawnLocation());
			} else {
				MessageHandler.sendMessage(event.getPlayer(), "&cTo spectate you must have " + Ranks.PRO_PLUS.getPrefix());
				ProPlugin.sendPlayerToServer(event.getPlayer(), "hub");
				event.setCancelled(true);
			}
		}
	}
}
