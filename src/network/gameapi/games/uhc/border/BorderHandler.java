package network.gameapi.games.uhc.border;

import java.util.ArrayList;
import java.util.List;

import network.customevents.TimeEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.ProPlugin;
import network.gameapi.games.uhc.Events;
import network.gameapi.games.uhc.HostHandler;
import network.gameapi.games.uhc.WorldHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

public class BorderHandler implements Listener {
	private static BorderHandler instance = null;
	private static Border overworldBorder = null;
	private static Border netherBorder = null;
	private static List<String> delayed = null;
	private static final int delay = 15;
	
	public BorderHandler() {
		if(instance == null) {
			instance = this;
			delayed = new ArrayList<>();
		}
	}
	
	public static boolean isEnabled() {
		return instance != null;
	}
	
	public static Border getOverworldBorder() {
		return overworldBorder;
	}
	
	public static Border getNetherBorder() {
		return netherBorder;
	}
	
	public static void setOverworldBorder() {
		if(overworldBorder == null) {
			overworldBorder = new Border(1500, WorldHandler.getWorld());
		}
	}
	
	public static void setNetherBorder() {
		if(netherBorder == null) {
			netherBorder = new Border(overworldBorder.getStartingRadius() / 8, WorldHandler.getNether());
		}
	}
	
	public static void registerCommands() {
		new CommandBase("setBorder", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 2) {
					if(arguments[0].equalsIgnoreCase("overworld") || arguments[0].equalsIgnoreCase("nether")) {
						int radius = 0;
						try {
							radius = Integer.valueOf(arguments[1]);
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(sender, "&cSecond argument must be a positive integer");
							return true;
						}
						if(radius < 0) {
							MessageHandler.sendMessage(sender, "&cThe radius must be a positive");
						} else {
							Border border = null;
							if(arguments[0].equalsIgnoreCase("overworld")) {
								border = BorderHandler.overworldBorder;
							} else if(arguments[0].equalsIgnoreCase("nether")) {
								border = BorderHandler.netherBorder;
							}
							if(border != null) {
								border.setBorder(radius);
								MessageHandler.sendMessage(sender, "The border has been changed to &b" + radius);
							} else {
								MessageHandler.sendMessage(sender, "&cThe " + arguments[0] + " doesn't have a border");
							}
						}
					} else {
						MessageHandler.sendMessage(sender, "&cFirst argument must be overworld or nether");
					}
				} else {
					MessageHandler.sendMessage(sender, "&cIncorrect usage &e/setBorder <overworld|nether> <radius>");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("compass", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0 && sender instanceof Player) {
					Player player = (Player) sender;
					if(!HostHandler.isHost(player.getUniqueId()) && !Ranks.STAFF.hasRank(player)) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				String name = arguments[0];
				Player target = ProPlugin.getPlayer(name);
				if(target == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " is not online");
				} else {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						if(!HostHandler.isHost(player.getUniqueId()) && !Ranks.isStaff(sender)) {
							MessageHandler.sendUnknownCommand(sender);
							return true;
						}
					}
					giveCompass(target);
				}
				return true;
			}
		};
		new CommandBase("getOutOfBounds") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0 && sender instanceof Player) {
					Player player = (Player) sender;
					if(!HostHandler.isHost(player.getUniqueId()) && !Ranks.STAFF.hasRank(player)) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				String message = "";
				int counter = 0;
				for(Player player : ProPlugin.getPlayers()) {
					boolean add = false;
					String worldName = player.getWorld().getName();
					if(worldName.equalsIgnoreCase(WorldHandler.getWorld().getName()) && getDistance(player.getLocation()) > BorderHandler.overworldBorder.getRadius()) {
						add = true;
					} else if((worldName.equalsIgnoreCase(WorldHandler.getNether().getName()) || worldName.equalsIgnoreCase(WorldHandler.getEnd().getName()))
							&& BorderHandler.overworldBorder.isShrinking()) {
						add = true;
					}
					if(add) {
						message += AccountHandler.getPrefix(player) + " &b(" + worldName + "), ";
						++counter;
					}
				}
				if(counter == 0) {
					MessageHandler.sendMessage(sender, "There are no players out of bounds at this time");
				} else {
					message = "Players out of bounds (&e" + counter + "&a): " + message;
					MessageHandler.sendMessage(sender, message.substring(0, message.length() - 2));
				}
				return true;
			}
		};
	}
	
	public static void registerEvents() {
		EventUtil.register(instance);
	}
	
	static void updateScoreboard() {
		Network.getSidebar().setText("Border Radius", overworldBorder.getRadius());
	}
	
	public static double getDistance(Location location) {
		int x = location.getBlockX();
		int z = location.getBlockZ();
		return Math.sqrt((0 - x) * (0 - x) + (0 - z) * (0 - z));
	}
	
	public static void giveCompass(Player player) {
		if(!player.getInventory().contains(Material.COMPASS)) {
			MessageHandler.sendLine(player);
			MessageHandler.sendMessage(player, "&6&lGiving out compasses that point to &c&l0&7&l, &c&l0");
			player.getInventory().addItem(new ItemStack(Material.COMPASS));
			if(!player.getInventory().contains(Material.COMPASS)) {
				MessageHandler.sendMessage(player, "&c&lYour inventory was full! Check the ground near you!");
				player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.COMPASS));
			}
			MessageHandler.sendLine(player);
		}
	}
	
	public static Border getBorder(World world) {
		Border border = null;
		if(world.getName().equalsIgnoreCase(WorldHandler.getWorld().getName())) {
			border = overworldBorder;
		} else if(world.getName().equalsIgnoreCase(WorldHandler.getNether().getName())) {
			border = netherBorder;
		}
		return border;
	}
	
	public static boolean isPlayerOutOfBounds(Player player) {
		boolean out = false;
		double distance = getDistance(player.getLocation());
		String worldName = player.getWorld().getName();
		if(worldName.equalsIgnoreCase(WorldHandler.getWorld().getName()) && distance > overworldBorder.getRadius()) {
			out = true;
		} else if(WorldHandler.getNether() != null && worldName.equalsIgnoreCase(WorldHandler.getNether().getName()) && Events.getMoveToCenter()) {
			out = true;
		} else if(WorldHandler.getEnd() != null && worldName.equalsIgnoreCase(WorldHandler.getEnd().getName()) && Events.getMoveToCenter()) {
			out = true;
		}
		return out;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 20) {
			for(Player player : ProPlugin.getPlayers()) {
				if(isPlayerOutOfBounds(player)) {
					String name = player.getName();
					if(!delayed.contains(name)) {
						delayed.add(name);
						new DelayedTask(() -> delayed.remove(name), 20 * delay);

						if(player.getWorld().getName().equalsIgnoreCase(WorldHandler.getWorld().getName())) {
							MessageHandler.sendLine(player);
							MessageHandler.sendMessage(player, "");
							MessageHandler.sendMessage(player, "&4&lYOU ARE OUT OF BOUNDS! " + (Events.getMoveToCenter() ? "GO TO &e&l0, 0" : ""));
							MessageHandler.sendMessage(player, "");
							MessageHandler.sendLine(player);
							giveCompass(player);
						}
					}
				}
			}
		} else if(ticks == 20 * 60) {
			if(Events.getMoveToCenter() && overworldBorder.getRadius() > 200) {
				int amount = 100;
				overworldBorder.setBorder(overworldBorder.getRadius() + (amount * -1));
				updateScoreboard();
				MessageHandler.alert("&6The border radius has decreased &b" + amount + " &6blocks to &b" + overworldBorder.getRadius());
				if(Events.getMoveToCenter()) {
					for(Player player : ProPlugin.getPlayers()) {
						if(!player.getWorld().getName().equalsIgnoreCase(WorldHandler.getWorld().getName())) {
							MessageHandler.sendMessage(player, "Go to the overworld and head to &e0, 0");
						}
					}
				}
			}
		}
	}
}