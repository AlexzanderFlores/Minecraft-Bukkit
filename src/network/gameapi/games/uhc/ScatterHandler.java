package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import network.customevents.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scoreboard.Team;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.uhc.border.BorderHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.util.EventUtil;
import network.server.util.StringUtil;

public class ScatterHandler implements Listener {
	private ScatterHandler instance = null;
	private Map<String, Location> locations = null;
	private Map<Team, Location> teams = null;
	private List<String> scattered = null;
	private static List<LivingEntity> savedEntities = null;
	private int toScatter = 0;
	private boolean scatter = false;
	private Random random = null;
	
	public ScatterHandler() {
		instance = this;
		locations = new HashMap<String, Location>();
		teams = new HashMap<Team, Location>();
		scattered = new ArrayList<String>();
		savedEntities = new ArrayList<LivingEntity>();
		random = new Random();
		EventUtil.register(instance);
		String command = "spreadPlayers 0 0 100 " + (BorderHandler.getOverworldBorder().getRadius() / 2 + (BorderHandler.getOverworldBorder().getRadius() / 3) - 250) + " false ";
		for(Player player : ProPlugin.getPlayers()) {
			command += player.getName() + " ";
			++toScatter;
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		new CommandBase("lateScatter", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(HostHandler.isHost(player.getUniqueId())) {
					Player target = ProPlugin.getPlayer(arguments[0]);
					if(target == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
					} else {
						target.setNoDamageTicks(20 * 10);
						SpectatorHandler.getInstance().remove(target);
						target.teleport(WorldHandler.getWorld().getSpawnLocation());
						String command = "spreadPlayers 0 0 100 " + (BorderHandler.getOverworldBorder().getRadius() / 2 + (BorderHandler.getOverworldBorder().getRadius() / 3) - 250) + " false " + target.getName();
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
					}
				} else {
					MessageHandler.sendUnknownCommand(player);
				}
				return true;
			}
		};
		new CommandBase("forceStart") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Player main = HostHandler.getMainHost();
					if(main == null || main.getUniqueId() != player.getUniqueId()) {
						MessageHandler.sendMessage(player, "&cYou must be the main UHC host to use this command");
						return true;
					}
				}
				String msg = "";
				int counter = 0;
				for(String name : locations.keySet()) {
					msg += name + ", ";
					++counter;
				}
				if(msg.equals("")) {
					msg = "&cNone, ";
				}
				MessageHandler.sendMessage(sender, "Players in the locations list: (&e" + counter + "&a) " + msg.substring(0, msg.length() - 2));
				msg = "";
				counter = 0;
				for(String name : scattered) {
					msg += name + ", ";
					++counter;
				}
				if(msg.equals("")) {
					msg = "&cNone, ";
				}
				MessageHandler.sendMessage(sender, "Players in the scattered list: (&e" + counter + "&a) " + msg.substring(0, msg.length() - 2));
				HandlerList.unregisterAll(instance);
				if(teams != null) {
					teams.clear();
					teams = null;
				}
				if(locations != null) {
					locations.clear();
					locations = null;
				}
				scatter = false;
				if(scattered != null) {
					scattered.clear();
					scattered = null;
				}
				Events.start();
				return true;
			}
		};
	}
	
	public static boolean isSaved(LivingEntity livingEntity) {
		return savedEntities != null && savedEntities.contains(livingEntity);
	}
	
	public static void doneSaving() {
		if(savedEntities != null) {
			savedEntities.clear();
			savedEntities = null;
		}
	}
	
	private void scatter(Player player, int counter, int size, Location location) {
		player.setNoDamageTicks(20 * 15);
		if(!scattered.contains(player.getName())) {
			MessageHandler.alert("&eScattering " + AccountHandler.getPrefix(player) + " &e[&a" + (counter + 1) + "&7/&a" + size + "&e]");
		}
		player.teleport(location);
	}
	
	@EventHandler
	public void onTenTickTask(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 10 && scatter && locations != null && !locations.isEmpty()) {
			List<Player> players = ProPlugin.getPlayers();
			for(int b = 0; b < players.size(); ++b) {
				Player player = players.get(b);
				if(locations != null && player != null && locations.containsKey(player.getName())) {
					Location location = locations.get(player.getName());
					Team team = TeamHandler.getTeam(player);
					if(team != null) {
						if(teams.containsKey(team)) {
							location = teams.get(team);
						} else {
							teams.put(team, location);
						}
					}
					scatter(player, b, players.size(), location);
					locations.remove(player.getName());
					if(!scattered.contains(player.getName())) {
						scattered.add(player.getName());
					}
					if(scattered.size() >= players.size()) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "forceStart");
					}
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		String name = event.getPlayer().getName();
		if(locations.containsKey(name)) {
			World world = WorldHandler.getWorld();
			Location to = event.getTo();
			Location from = event.getFrom();
			if(to.getWorld().getName().equals(world.getName()) && from.getWorld().getName().equals(world.getName()) && to.getY() < 200 && from.getY() >= 200) {
				locations.remove(name);
			}
		} else {
			Location to = WorldHandler.getGround(event.getTo()).add(0, 15, 0);
			EntityType type = random.nextBoolean() ? EntityType.PIG : EntityType.SHEEP;
			for(int a = 0; a < 5; ++a) {
				int x = random.nextInt(5);
				int z = random.nextInt(5);
				if(random.nextBoolean()) {
					x *= -1;
				}
				if(random.nextBoolean()) {
					z *= -1;
				}
				LivingEntity livingEntity = (LivingEntity) to.getWorld().spawnEntity(to.clone().add(x, -14, z), type);
				if(type == EntityType.SHEEP) {
					livingEntity.setCustomName(StringUtil.color("&bSheep Drop Food"));
				}
				savedEntities.add(livingEntity);
			}
			locations.put(name, to);
			to.getChunk().load(true);
			event.setCancelled(true);
			if(locations.size() >= toScatter) {
				scatter = true;
			}
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		locations.remove(event.getPlayer().getName());
	}
}
