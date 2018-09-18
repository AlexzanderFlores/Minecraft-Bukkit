package network.server.servers.building;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

import network.customevents.DeleteImportedWorldEvent;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerItemFrameInteractEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		event.getWorld().setGameRuleValue("doDaylightCycle", "false");
		event.getWorld().setTime(6000);
	}
	
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
		if(event.getBlock().getType() == Material.FIRE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(event.getBlock().getType() != Material.NETHERRACK) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60) {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "save-all");
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(ChatColor.YELLOW + event.getPlayer().getName() + " joined the server");
		if(event.getPlayer().isOp()) {
			event.getPlayer().setGameMode(GameMode.CREATIVE);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		event.setLeaveMessage(ChatColor.YELLOW + event.getPlayer().getName() + " left the server");
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(false);
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		if(event.getMessage().toLowerCase().contains("/mv import ")) {
			String worldName = event.getMessage().toLowerCase().replace("/mv import ", "");
			worldName = worldName.replace(" normal", "");
			worldName = worldName.replace(" nether", "");
			worldName = worldName.replace(" end", "");
			final String world = worldName;
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					event.getPlayer().chat("/mvtp " + world);
				}
			}, 5);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().clear();
	}
	
	@EventHandler
	public void onBlockGrow(BlockSpreadEvent event) {
		if(event.getNewState().getType() == Material.VINE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDeleteImportedWorld(DeleteImportedWorldEvent event) {
		event.setCancelled(true);
	}
}
