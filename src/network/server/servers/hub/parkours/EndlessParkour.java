package network.server.servers.hub.parkours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.ProPlugin;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.CommandBase;
import network.server.DB;
import network.server.servers.hub.Events;
import network.server.servers.hub.ParkourNPC;
import network.server.servers.hub.parkours.ParkourStartEvent.ParkourTypes;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.StringUtil;
import npc.events.NPCClickedEvent;

@SuppressWarnings("deprecation")
public class EndlessParkour implements Listener {
	private Map<String, Block> blocks = null;
	private Map<String, SidebarScoreboardUtil> scoreboards = null;
	private Map<String, Integer> scores = null;
	private Map<String, Block> lastScoredOn = null;
	private Map<String, Integer> taskIds = null;
	private Map<String, Integer> storedScores = null;
	private int counter = 0;
	private Random random = null;
	private String topPlayer = null;
	private String url = null;
	private int topScore = 0;
	
	public EndlessParkour() {
		blocks = new HashMap<String, Block>();
		scoreboards = new HashMap<String, SidebarScoreboardUtil>();
		scores = new HashMap<String, Integer>();
		lastScoredOn = new HashMap<String, Block>();
		taskIds = new HashMap<String, Integer>();
		storedScores = new HashMap<String, Integer>();
		random = new Random();
		loadTopData();
		url = "1v1s.org/EPK";
		World world = Bukkit.getWorlds().get(0);
		Hologram hologram = HologramAPI.createHologram(new Location(world, 1592.5, 6.5, -1262.5), StringUtil.color("&eWalk Forward"));
		hologram.spawn();
		EventUtil.register(this);
		//TODO: Add 1 respawn into the database when this command is dispatched on the slave server
		new CommandBase("endlessParkourRespawn", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						final Player player = (Player) sender;
						final String name = player.getName();
						if(scores.containsKey(name)) {
							MessageHandler.sendMessage(player, "&cYou cannot run this command while playing");
							return true;
						}
						if(!storedScores.containsKey(name)) {
							MessageHandler.sendMessage(player, "&cNot scores saved. Scores only save for &e2:30");
							return true;
						}
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								UUID uuid = player.getUniqueId();
								int amount = DB.HUB_PARKOUR_ENDLESS_RESPAWNS.getInt("uuid", uuid.toString(), "amount");
								if(amount > 0) {
									MessageHandler.sendMessage(player, "You now have &e" + (--amount) + " &xrespawn" + (amount == 1 ? "" : "s"));
									DB.HUB_PARKOUR_ENDLESS_RESPAWNS.updateInt("amount", amount, "uuid", uuid.toString());
									scores.put(name, storedScores.get(name));
									storedScores.remove(name);
								} else {
									MessageHandler.sendMessage(player, "&cYou do not have any Endless Parkour respawns, get some here: &e" + url);
								}
							}
						});
					} else {
						return false;
					}
				} else if(arguments.length == 1) {
					if(Ranks.OWNER.hasRank(sender)) {
						Player player = ProPlugin.getPlayer(arguments[0]);
						if(player == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
						} else {
							cancelRemove(player.getName());
							MessageHandler.sendMessage(player, "Your Endless Parkour respawn has been loaded. To activate it run &e/endlessParkourRespawn &xand you will be teleported into the game. Be ready!");
						}
					} else {
						MessageHandler.sendMessage(sender, Ranks.OWNER.getNoPermission());
					}
				}
				return true;
			}
		}.enableDelay(2);
	}
	
	public void cancelRemove(String name) {
		if(taskIds.containsKey(name)) {
			Bukkit.getScheduler().cancelTask(taskIds.get(name));
			taskIds.remove(name);
		}
	}
	
	private boolean start(Player player) {
		if(counter <= 0) {
			Bukkit.getPluginManager().callEvent(new ParkourStartEvent(player, ParkourTypes.ENDLESS));
			counter = 5;
			if(player.getAllowFlight()) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
			Block block = Bukkit.getWorlds().get(0).getBlockAt(1586, 4, -1263);
			final String name = player.getName();
			lastScoredOn.put(name, block);
			blocks.put(name, block);
			place(name);
			Location location = block.getLocation().clone().add(1.5, 1, 0.5);
			location.setYaw(-270.0f);
			location.setPitch(25.0f);
			player.teleport(location);
			scores.put(name, 1);
			final int personalBest = DB.HUB_PARKOUR_ENDLESS_SCORES.getInt("uuid", player.getUniqueId().toString(), "best_score");
			Events.removeSidebar(player);
			SidebarScoreboardUtil sidebar = new SidebarScoreboardUtil(" &aEndless Parkour ") {
				@Override
				public void update(Player player) {
					removeScore(11);
					setText(new String [] {
						" ",
						"&eCurrent Score",
						"&b" + scores.get(name) + " ",
						"  ",
						"&ePersonal Best",
						"&b" + personalBest + "  ",
						"   ",
						"&eTop Player",
						"&b" + topPlayer,
						"    ",
						"&eTop Score",
						"&b" + topScore + "   ",
						"     ",
					});
				}
			};
			scoreboards.put(name, sidebar);
			player.setScoreboard(sidebar.getScoreboard());
			sidebar.update(player);
			return true;
		}
		return false;
	}
	
	private void place(String name) {
		final Block oldBlock = blocks.get(name);
		int offsetX = -5;
		int offsetZ = random.nextBoolean() ? random.nextInt(1) + 1 : (random.nextInt(1) + 1) * -1;
		int index = random.nextInt(3);
		int offsetY = index == 0 ? 0 : index == 1 ? 1 : -1;
		if(oldBlock.getY() < 5 && offsetY < 0) {
			offsetY = 1;
		} else if(oldBlock.getY() > 10 && offsetY > 0) {
			offsetY = -1;
			--offsetX;
		} else if(random.nextInt(100) <= 15) {
			--offsetX;
			if(offsetY > 0) {
				offsetY = 0;
			}
		}
		Block newBlock = scores.containsKey(name) ? oldBlock.getRelative(offsetX, offsetY, offsetZ) : oldBlock;
		newBlock.getChunk().load(true);
		newBlock.setType(Material.STAINED_GLASS);
		newBlock.setData((byte) random.nextInt(15));
		for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0)}) {
			Block near = newBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
			near.setType(Material.STAINED_GLASS);
			near.setData((byte) random.nextInt(15));
		}
		blocks.put(name, newBlock);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(int a = 0; a < 2; ++a) {
					oldBlock.setType(Material.AIR);
					oldBlock.setData((byte) 0);
					for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0)}) {
						Block near = oldBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
						near.setType(Material.AIR);
						near.setData((byte) 0);
					}
				}
			}
		}, 20 * 2);
	}
	
	private void remove(final Player player, boolean teleport) {
		final String name = player.getName();
		if(blocks.containsKey(name)) {
			final Block block = blocks.get(name);
			blocks.remove(name);
			remove(block);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					remove(block);
				}
			}, 20);
			if(Ranks.VIP.hasRank(player)) {
				player.setAllowFlight(true);
			}
			if(teleport) {
				player.teleport(ParkourNPC.getEndlessLocation());
			}
		}
		if(scores.containsKey(name)) {
			final int score = scores.get(name);
			if(score >= 50) {
				cancelRemove(name);
				MessageHandler.sendMessage(player, "Want to return to where you were? &e" + url);
				MessageHandler.sendMessage(player, "Your score of &e" + score + " &xis saved for only &e2:30 &xso be quick!");
				taskIds.put(name, new DelayedTask(new Runnable() {
					@Override
					public void run() {
						if(player.isOnline()) {
							MessageHandler.sendMessage(player, "&cYour previous score of &e" + score + " &cwas removed");
						}
						storedScores.remove(name);
					}
				}, 20 * 60 * 2 + 30).getId());
				storedScores.put(name, score);
			}
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					UUID uuid = player.getUniqueId();
					int bestScore = DB.HUB_PARKOUR_ENDLESS_SCORES.getInt("uuid", uuid.toString(), "best_score");
					if(score > bestScore) {
						if(DB.HUB_PARKOUR_ENDLESS_SCORES.isUUIDSet(uuid)) {
							DB.HUB_PARKOUR_ENDLESS_SCORES.updateInt("best_score", score, "uuid", uuid.toString());
						} else {
							DB.HUB_PARKOUR_ENDLESS_SCORES.insert("'" + uuid.toString() + "', '" + score + "'");
						}
						MessageHandler.sendMessage(player, "&6New Personal Best: &e" + score);
						List<String> top = DB.HUB_PARKOUR_ENDLESS_SCORES.getOrdered("best_score", "uuid", 1, true);
						if(!top.isEmpty() && top.get(0).equals(uuid.toString())) {
							MessageHandler.sendMessage(player, "&4&k|||&6 New Top Score: &e" + score + " &4&k|||");
						}
					}
					scores.remove(name);
				}
			});
		}
		if(scoreboards.containsKey(name)) {
			scoreboards.get(name).remove();
			scoreboards.remove(name);
			Events.giveSidebar(player);
		}
		lastScoredOn.remove(name);
	}
	
	private void remove(Block block) {
		if(block != null) {
			block.setType(Material.AIR);
			block.setData((byte) 0);
			for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0)}) {
				Block near = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
				near.setType(Material.AIR);
				near.setData((byte) 0);
			}
		}
	}
	
	private void loadTopData() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<String> top = DB.HUB_PARKOUR_ENDLESS_SCORES.getOrdered("best_score", "uuid", 1, true);
				if(top.isEmpty()) {
					topPlayer = "None";
				} else {
					UUID topPlayerUUID = UUID.fromString(top.get(0));
					topPlayer = AccountHandler.getName(topPlayerUUID);
					topScore = DB.HUB_PARKOUR_ENDLESS_SCORES.getInt("uuid", topPlayerUUID.toString(), "best_score");
				}
			}
		});
	}
	
	@EventHandler
	public void onParkourStart(ParkourStartEvent event) {
		if(event.getType() == ParkourTypes.COURSE) {
			remove(event.getPlayer(), false);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 17) {
			for(String name : blocks.keySet()) {
				place(name);
			}
		} else if(ticks == 20) {
			--counter;
		} else if(ticks == (20 * 60)) {
			loadTopData();
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		String name = player.getName();
		if(blocks.containsKey(name)) {
			if(to.getY() < 0) {
				remove(player, true);
				return;
			}
			Block below = player.getLocation().getBlock().getRelative(0, -1, 0);
			if(below.getType() == Material.STAINED_GLASS && below.getRelative(0, -1, 0).getType() == Material.STAINED_GLASS && !lastScoredOn.get(name).equals(below)) {
				lastScoredOn.put(name, below);
				scores.put(name, scores.get(name) + 1);
				scoreboards.get(name).update(player);
			}
		}
		int x = to.getBlockX();
		if(x == 1589) {
			int y = to.getBlockY();
			if(y == 5 || y == 6) {
				int z = to.getBlockZ();
				if(z >= -1264 && z <= -1262) {
					if(!start(player)) {
						player.teleport(ParkourNPC.getEndlessLocation());
						MessageHandler.sendMessage(player, "&cPlease wait &e" + counter + " &csecond" + (counter == 1 ? "" : "s"));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onNPCClicked(NPCClickedEvent event) {
		Player player = event.getPlayer();
		if(blocks.containsKey(player.getName())) {
			MessageHandler.sendMessage(player, "&cYou cannot interact with NPCs while playing Endless Parkour");
			MessageHandler.sendMessage(player, "&cPlease leave parkour then interact with the NPC again");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		cancelRemove(event.getPlayer().getName());
		storedScores.remove(event.getPlayer().getName());
		remove(event.getPlayer(), false);
	}
}