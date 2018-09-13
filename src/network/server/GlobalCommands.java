package network.server;

import network.Network;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.servers.hub.crate.Beacon;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.StringUtil;
import network.server.util.TimeUtil;
import network.staff.StaffMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GlobalCommands {
	public GlobalCommands() {
		new CommandBase("socialMedia") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendLine(sender);
				MessageHandler.sendMessage(sender, "Server's Twitter &bhttps://twitter.com/ProMcGames");
				MessageHandler.sendMessage(sender, "Owner's Twitter &bhttps://twitter.com/AlexzanderFlors");
				MessageHandler.sendLine(sender);
				return true;
			}
		};

//		new CommandBase("linkTwitter", true) {
//			@Override
//			public boolean execute(final CommandSender sender, String [] arguments) {
//				if(Network.getPlugin() == Plugins.HUB && HubBase.getHubNumber() == 1) {
//					new AsyncDelayedTask(new Runnable() {
//						@Override
//						public void run() {
//							Player player = (Player) sender;
//							UUID uuid = player.getUniqueId();
//							if(DB.PLAYERS_TWITTER_API_KEYS.isUUIDSet(uuid)) {
//								MessageHandler.sendMessage(player, "&cYour Twitter is already linked with 1v1s, if there is a problem please contact us on Twitter or our forums");
//							} else {
//								String address = player.getAddress().getAddress().getHostAddress();
//								String url = OAuth.getURL();
//								if(DB.PLAYERS_TWITTER_AUTH_URLS.isKeySet("address", address)) {
//									DB.PLAYERS_TWITTER_AUTH_URLS.updateString("url", url, "address", address);
//								} else {
//									DB.PLAYERS_TWITTER_AUTH_URLS.insert("'" + address + "', '" + url + "'");
//								}
//								MessageHandler.sendMessage(player, "Please confirm the linking of your Twitter account: &a" + url);
//							}
//						}
//					});
//				} else {
//					MessageHandler.sendMessage(sender, "&cYou can only run this command in HUB1");
//					if(sender instanceof Player) {
//						Player player = (Player) sender;
//						ChatClickHandler.sendMessageToRunCommand(player, " &6Click here", "Go to HUB1", "/hub 1", "&aTo go to HUB1");
//					}
//				}
//				return true;
//			}
//		}.enableDelay(2);
		
		new CommandBase("sudo", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				Player player = ProPlugin.getPlayer(name);
				if(player == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " is not online");
				} else {
					String command = "";
					for(int a = 1; a < arguments.length; ++a) {
						command += arguments[a] + " ";
					}
					player.performCommand(command);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("uhc", 0, 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						if(arguments.length == 1) {
							MessageHandler.sendMessage(player, arguments[0]);
						} else {
							int counter = 0;
//							for(String url : DB.NETWORK_UHC_URL.getAllStrings("url")) {
//								ChatClickHandler.sendMessageToRunCommand(player, "&bClick here", "Click to view URL", "/uhc " + url, "&a&lUHC &eGame #" + ++counter + ": ");
//							}
							if(counter == 0) {
								MessageHandler.sendMessage(player, "&cThere are no UHC games at this time.");
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		
		new CommandBase("giveKey", 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				int amount = Integer.valueOf(arguments[1]);
				String type = arguments[2];
				if(type.equalsIgnoreCase("voting") || type.equalsIgnoreCase("super")) {
					Beacon.giveKey(uuid, amount, arguments[2]);
				} else {
					MessageHandler.sendMessage(sender, "Unknown key type, use:");
					MessageHandler.sendMessage(sender, "voting");
					MessageHandler.sendMessage(sender, "super");
					MessageHandler.sendMessage(sender, "sky_wars");
					MessageHandler.sendMessage(sender, "speed_uhc");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("vote") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "&cVoting is coming soon.");
//				MessageHandler.sendLine(sender, "&6");
//				MessageHandler.sendMessage(sender, "http://minecraftservers.org/server/387074");
//				MessageHandler.sendMessage(sender, "http://minecraft-mp.com/server/134839/vote/");
//				MessageHandler.sendLine(sender, "&6");
//				if(sender instanceof Player) {
//					Player player = (Player) sender;
//					EffectUtil.playSound(player, Sound.LEVEL_UP);
//				}
				return true;
			}
		};

		new CommandBase("discord") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "&chttps://discord.gg/cXB5Q8k");
				return true;
			}
		};
		
		new CommandBase("sysTime") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, TimeUtil.getTime());
				return true;
			}
		};
		
		new CommandBase("colorCodes") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "Edit the color of your chat with \"&&xx\" where 'x' is a number from 0-9 or a letter from a-f. Example: \"&&xa&aHey!&x\"");
				return true;
			}
		};
		
		new CommandBase("join", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				ProPlugin.sendPlayerToServer(player, arguments[0]);
				return true;
			}
		};
		
		new CommandBase("tpPos", 3, 4, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				try {
					double x = Integer.valueOf(arguments[0]);
					double y = Integer.valueOf(arguments[1]);
					double z = Integer.valueOf(arguments[2]);
					Player player = (Player) sender;
					player.setAllowFlight(true);
					player.setFlying(true);
					if(arguments.length == 3) {
						player.teleport(new Location(player.getWorld(), x, y, z));
					} else {
						player.teleport(new Location(Bukkit.getWorld(arguments[3]), x, y, z));
					}
				} catch(NumberFormatException e) {
					return false;
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("buy") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "&cStore coming soon.");
				return true;
			}
		};
		
		new CommandBase("getLoc", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				MessageHandler.sendMessage(sender, "World: " + location.getWorld().getName());
				MessageHandler.sendMessage(sender, "X: " + location.getX());
				MessageHandler.sendMessage(sender, "Y: " + location.getY());
				MessageHandler.sendMessage(sender, "Z: " + location.getZ());
				MessageHandler.sendMessage(sender, "Yaw: " + location.getYaw());
				MessageHandler.sendMessage(sender, "Pitch: " + location.getPitch());
				return true;
			}
		};
		
		new CommandBase("gmc", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setGameMode(GameMode.CREATIVE);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("gms", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setGameMode(GameMode.SURVIVAL);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("hub", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					ProPlugin.sendPlayerToServer(player, "hub");
				} else if(arguments.length == 1) {
					ProPlugin.sendPlayerToServer(player, "hub" + arguments[0]);
				}
				return true;
			}
		};
		
		new CommandBase("list") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(Bukkit.getOnlinePlayers().isEmpty()) {
					MessageHandler.sendMessage(sender, "&cThere are no players online");
				} else {
					String players = "";
					int online = 0;
					for(Player player : ProPlugin.getPlayers()) {
						if(!StaffMode.getInstance().contains(player)) {
							players += AccountHandler.getRank(player).getColor() + player.getName() + ", ";
							++online;
						}
					}
					MessageHandler.sendMessage(sender, online + " Players: " + players.substring(0, players.length() - 2));
					if(Network.getMiniGame() != null && SpectatorHandler.isEnabled() && SpectatorHandler.getNumberOf() > 0) {
						String spectators = "";
						online = 0;
						for(Player player : SpectatorHandler.getPlayers()) {
							if(!Ranks.isStaff(player)) {
								spectators += AccountHandler.getRank(player).getColor() + player.getName() + ", ";
								++online;
							}
						}
						MessageHandler.sendMessage(sender, online + " Spectators: " + spectators.substring(0, spectators.length() - 2));
					}
				}
				return true;
			}
		};
		
		new CommandBase("say", -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String message = "";
				for(String argument : arguments) {
					message += argument + " ";
				}
				message = ChatColor.GREEN + StringUtil.color(message.substring(0, message.length() - 1));
				Bukkit.getLogger().info(message);
				for(Player player : Bukkit.getOnlinePlayers()) {
					player.sendMessage(message);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
}
