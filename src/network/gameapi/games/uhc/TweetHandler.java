package network.gameapi.games.uhc;

import java.io.File;

import network.customevents.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.ProPlugin;
import network.customevents.player.MouseClickEvent;
import network.gameapi.uhc.scenarios.Scenario;
import network.player.MessageHandler;
import network.server.AlertHandler;
import network.server.CommandBase;
import network.server.util.CountDownUtil;
import network.server.util.EventUtil;
import network.server.util.FileHandler;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;

public class TweetHandler implements Listener {
	private static ItemStack item = null;
	private static int opensIn = -1;
	private static CountDownUtil countDown = null;
	private static boolean hasTweeted = false;
	private static String tweet = null;
	private static long id = 0;
	
	public TweetHandler() {
		if(item == null) {
			tweet = "";
			item = new ItemCreator(Material.NAME_TAG).setName("&aLaunch Tweet").getItemStack();
			new CommandBase("opensIn", 1) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					try {
						opensIn = Integer.valueOf(arguments[0]);
						if(opensIn < 5 || opensIn > 10) {
							MessageHandler.sendMessage(sender, "&cYour game must open in 5 - 10 minutes");
							opensIn = -1;
						} else {
							MessageHandler.sendMessage(sender, "This game will open in &c" + opensIn);
							MessageHandler.sendMessage(sender, "To continue click the \"&cLaunch Tweet&a\" item again");
						}
					} catch(NumberFormatException e) {
						return false;
					}
					return true;
				}
			};
			new CommandBase("tweet", true) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					Player player = (Player) sender;
					if(HostHandler.isHost(player.getUniqueId())) {
						if(hasTweeted) {
							MessageHandler.sendMessage(player, "&cThis game has already been tweeted");
						} else {
//							String message = getTeamSize() + " " + getScenarios() + "\n\n" + getIP() + "\n" + getCommand() + "\n\n" + getOpensIn();
//							id = Tweeter.tweet(message, "uhc.jpg");
//							if(id == -1) {
//								MessageHandler.sendMessage(player, "&cFailed to send Tweet! Possible duplicate tweet");
//							} else {
//								hasTweeted = true;
//								MessageHandler.alert("Tweet sent! Game will open in &c" + opensIn + " &aminutes");
//								countDown = new CountDownUtil(60 * opensIn);
//							}
						}
					} else {
						MessageHandler.sendUnknownCommand(player);
					}
					return true;
				}
			};

			new CommandBase("endTweet", 2, -1, true) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					Player player = (Player) sender;
					if(HostHandler.isHost(player.getUniqueId())) {
						String url = arguments[0];
						if(FileHandler.isImage(url)) {
							String message = "Congrats to ";
							if(arguments.length > 2) {
								for(int a = 1; a < arguments.length; ++a) {
									if(a == arguments.length - 1) {
										message = message.substring(0, message.length() - 2);
										message += " & " + arguments[a];
									} else {
										message += arguments[a] + ", ";
									}
								}
							} else {
								message += arguments[1];
							}
							message += " for winning this UHC!";
							if(!message.equals(tweet)) {
								MessageHandler.sendLine(player);
								MessageHandler.sendMessage(player, "");
								MessageHandler.sendMessage(player, "Does this tweet look correct? If so run this command again");
								MessageHandler.sendMessage(player, "");
								MessageHandler.sendMessage(player, message);
								MessageHandler.sendMessage(player, "");
								MessageHandler.sendLine(player);
								tweet = message;
							} else {
//								String path = Bukkit.getWorldContainer().getPath() + "/../resources/winner.png";
//								FileHandler.delete(new File(path));
//								FileHandler.downloadImage(url, path);
//								long id = Tweeter.tweet(tweet, path);
//								if(id == -1) {
//									MessageHandler.sendMessage(player, "&cFailed to send tweet. Possible duplicate tweet.");
//								} else {
//									MessageHandler.sendLine(player);
//									MessageHandler.sendMessage(player, "");
//									MessageHandler.sendMessage(player, "&6To restart the server run &b/uhcOver");
//									MessageHandler.sendMessage(player, "");
//									MessageHandler.sendLine(player);
//									MessageHandler.alert("");
//									MessageHandler.alert("Tweet Sent! &ehttps://twitter.com/ProMcUHC/status/" + id);
//									MessageHandler.alert("");
//								}
								new CommandBase("uhcOver", true) {
									@Override
									public boolean execute(CommandSender sender, String [] arguments) {
										Player player = (Player) sender;
										if(HostHandler.isHost(player.getUniqueId())) {
											ProPlugin.restartServer();
										} else {
											MessageHandler.sendUnknownCommand(player);
										}
										return true;
									}
								};
							}
						} else {
							MessageHandler.sendMessage(player, "&cYou cannot use this URL! Please right click the image and click \"Copy Image Location\" then paste that URL");
						}
					} else {
						MessageHandler.sendUnknownCommand(player);
					}
					return true;
				}
			}.enableDelay(5);
			EventUtil.register(this);
		}
		for(Player player : ProPlugin.getPlayers()) {
			if(HostHandler.isHost(player.getUniqueId())) {
				if(!player.getInventory().contains(item)) {
					player.getInventory().addItem(item);
				}
				MessageHandler.sendMessage(player, "To continue click the \"&cLaunch Tweet&a\" item");
			}
		}
	}
	
	public static String getTeamSize() {
		int maxTeamSize = TeamHandler.getMaxTeamSize();
		return maxTeamSize == 1 ? "FFA" : "FFA - To" + maxTeamSize;
	}
	
	public static String getScenarios() {
		String scenarios = "";
		for(Scenario scenario : ScenarioManager.getActiveScenarios()) {
			scenarios += scenario.getName() + ", ";
		}
		scenarios = scenarios.substring(0, scenarios.length() - 2);
		if(OptionsHandler.isRush()) {
			scenarios += " Rush";
		}
		return scenarios;
	}
	
	private static String getIP() {
		return "IP: play.network.com";
	}
	
	private static String getCommand() {
		return "Run /join " + Network.getServerName();
	}
	
	private static String getOpensIn() {
		return "Opens in " + opensIn + " minute" + (opensIn == 1 ? "" : "s");
	}
	
	public static String getURL() {
		try {
			if(id != -1) {
				return " &ehttps://twitter.com/ProMcUHC/status/" + id;
			}
		} catch(Exception e) {
			
		}
		return "";
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), item)) {
			if(opensIn <= 0) {
				MessageHandler.sendMessage(player, "Set how long (in minutes) until this game opens:");
				MessageHandler.sendMessage(player, "&e/opensIn <delay in minutes>");
			} else {
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, "Is this information correct? &aRun &e/tweet &aif it is");
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, getTeamSize() + " " + getScenarios());
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, getIP());
				MessageHandler.sendMessage(player, getCommand());
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, getOpensIn());
				MessageHandler.sendLine(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 20 && countDown != null && countDown.getCounter() > 0) {
			if(countDown.canDisplay()) {
				MessageHandler.alert("Game opening in " + countDown.getCounterAsString());
			}
			int counter = countDown.getCounter();
			if(counter % 60 == 0) {
				AlertHandler.alert("&6&l" + getTeamSize() + " " + getScenarios() + " &6&lUHC OPENING IN &b&l" + (counter / 60) + " &6&lMINUTE" + ((counter / 60) == 1 ? "" : "S") + getURL());
			}
			countDown.decrementCounter();
			if(countDown.getCounter() <= 0) {
				WhitelistHandler.unWhitelist();
			}
		}
	}
}
