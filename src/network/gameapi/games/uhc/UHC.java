package network.gameapi.games.uhc;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;

import network.Network;
import network.ProPlugin;
import network.gameapi.MiniGame;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.uhc.anticheat.AntiIPVP;
import network.gameapi.games.uhc.anticheat.CommandSpy;
import network.gameapi.games.uhc.anticheat.DiamondTracker;
import network.gameapi.games.uhc.anticheat.MuteChat;
import network.gameapi.games.uhc.anticheat.StripMineDetection;
import network.gameapi.games.uhc.anticheat.WaterBucketLogging;
import network.gameapi.uhc.scenarios.Scenario;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.ChatClickHandler;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.DelayedTask;
import network.server.util.FileHandler;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class UHC extends MiniGame {
	public static String ultraGoldenApple = null;
	
	public UHC() {
		super("UHC");
		Network.setSidebar(new SidebarScoreboardUtil(" &aUHC "));
		setRequiredPlayers(60);
		setVotingCounter(-1);
		setStartingCounter(60 * 3 + 30);
		setEndingCounter(20);
		setAutoJoin(false);
		setCanJoinWhileStarting(true);
		setResetPlayerUponJoining(true);
		setRestartWithOnePlayerLeft(false);
		setDoDaylightCycle(true);
		new Events();
		new HostHandler();
		new WhitelistHandler();
		new ScenarioManager();
		new TeamHandler();
		new OptionsHandler();
		new DiamondTracker();
		new WorldHandler();
		new AntiIPVP();
		new HealthHandler();
		new Spectating();
		new StripMineDetection();
		new BelowNameHealthScoreboardUtil();
		new QuestionAnswerer();
		new WaterBucketLogging();
		new CommandSpy();
		new MuteChat();

		Network.getSidebar().setText(new String [] {
				" ",
				"&6Info:",
				"&b/info",
				"  ",
				"&6Rules:",
				"&b/rules"
		});

		new CommandBase("heal", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!HostHandler.isHost(player.getUniqueId())) {
						MessageHandler.sendUnknownCommand(sender);
						return true;
					}
				}
				String target = null;
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						target = player.getName();
					} else {
						MessageHandler.sendPlayersOnly(sender);
						return true;
					}
				} else if(arguments.length == 1) {
					target = arguments[0];
				}
				if(target.equalsIgnoreCase("all")) {
					for(Player player : ProPlugin.getPlayers()) {
						heal(player);
					}
					MessageHandler.sendMessage(sender, "You've healed all players");
				} else {
					Player player = ProPlugin.getPlayer(target);
					if(player == null) {
						MessageHandler.sendMessage(player, "&c" + target + " is not online");
					} else {
						heal(player);
						MessageHandler.sendMessage(sender, "You've healed " + AccountHandler.getPrefix(player));
					}
				}
				HealthHandler.updateHealth();
				return true;
			}
		};

		new CommandBase("feed", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!HostHandler.isHost(player.getUniqueId())) {
						MessageHandler.sendUnknownCommand(sender);
						return true;
					}
				}
				String target = null;
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						target = player.getName();
					} else {
						MessageHandler.sendPlayersOnly(sender);
						return true;
					}
				} else if(arguments.length == 1) {
					target = arguments[0];
				}
				if(target.equalsIgnoreCase("all")) {
					for(Player player : ProPlugin.getPlayers()) {
						feed(player);
					}
					MessageHandler.sendMessage(sender, "You've fed all players");
				} else {
					Player player = ProPlugin.getPlayer(target);
					if(player == null) {
						MessageHandler.sendMessage(player, "&c" + target + " is not online");
					} else {
						feed(player);
						MessageHandler.sendMessage(sender, "You've fed " + AccountHandler.getPrefix(player));
					}
				}
				return true;
			}
		};

		new CommandBase("invSee", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(HostHandler.isHost(player.getUniqueId()) || Ranks.isStaff(player)) {
					if(SpectatorHandler.contains(player)) {
						Player target = ProPlugin.getPlayer(arguments[0]);
						if(target == null) {
							MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
						} else {
							player.openInventory(target.getInventory());
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou must be a spectator to run this command");
					}
				} else {
					MessageHandler.sendUnknownCommand(sender);
				}
				return true;
			}
		};

		new CommandBase("info") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String scenarios = "";
				for(Scenario scenario : ScenarioManager.getActiveScenarios()) {
					scenarios += scenario.getName() + ", ";
				}
				scenarios = scenarios.substring(0, scenarios.length() - 2);
				MessageHandler.sendLine(sender);
				MessageHandler.sendMessage(sender, "Scenario: &b" + scenarios);
				if(sender instanceof Player) {
					Player player = (Player) sender;
					ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK FOR SCENARIO INFO", "Click for info", "/sinfo");
				}
				MessageHandler.sendMessage(sender, "Team Size: &b" + (TeamHandler.getMaxTeamSize() == 1 ? "Solo" : TeamHandler.getMaxTeamSize()));
				MessageHandler.sendMessage(sender, "Is Rush: &b" + OptionsHandler.isRush());
				MessageHandler.sendMessage(sender, "Nether Enabled: &b" + OptionsHandler.isNetherEnabled());
				MessageHandler.sendMessage(sender, "Apple Rates: &b" + OptionsHandler.getAppleRates() + "&a%");
				MessageHandler.sendMessage(sender, "Horses Enabled: &b" + OptionsHandler.allowHorses());
				MessageHandler.sendMessage(sender, "Horse Healing Enabled: &b" + OptionsHandler.allowHorseHealing());
				MessageHandler.sendMessage(sender, "Notch Apples Enabled: &b" + OptionsHandler.allowNotchApples());
				MessageHandler.sendMessage(sender, "Team damage: &bFalse");
				MessageHandler.sendMessage(sender, "Ender Pearl damage: &b" + OptionsHandler.allowPearlDamage());
				MessageHandler.sendMessage(sender, "Absorption: &b" + OptionsHandler.getAbsorption());
				MessageHandler.sendLine(sender);
				return true;
			}
		};

		new CommandBase("sinfo") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				for(Scenario scenario : ScenarioManager.getActiveScenarios()) {
					MessageHandler.sendMessage(sender, "");
					String info = scenario.getInfo();
					if(info == null) {
						MessageHandler.sendMessage(sender, "&cNo info to display at this time for " + scenario.getName());
					} else {
						MessageHandler.sendMessage(sender, "&b&l" + scenario.getName() + " &e" + info);
					}
					MessageHandler.sendMessage(sender, "");
				}
				return true;
			}
		};

		new CommandBase("uhcKick", 2, -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(HostHandler.isHost(player.getUniqueId()) || Ranks.STAFF.hasRank(player)) {
					Player target = ProPlugin.getPlayer(arguments[0]);
					if(target == null) {
						MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
					} else {
						String reason = "";
						for(int a = 1; a < arguments.length; ++a) {
							reason += arguments[a] + " ";
						}
						DB.NETWORK_UHC_KICKS.insert("'" + player.getUniqueId().toString() + "', '" + target.getUniqueId().toString() + "', '" + reason + "'");
						MessageHandler.alert(AccountHandler.getPrefix(target) + " &cwas kicked: " + reason);
						target.kickPlayer(ChatColor.RED + reason);
					}
				} else {
					MessageHandler.sendUnknownCommand(player);
				}
				return true;
			}
		};

		new CommandBase("uhcKill", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(HostHandler.isHost(player.getUniqueId()) || Ranks.STAFF.hasRank(player)) {
					Player target = ProPlugin.getPlayer(arguments[0]);
					if(target == null) {
						MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
					} else {
						String reason = "";
						for(int a = 1; a < arguments.length; ++a) {
							reason += arguments[a] + " ";
						}
						DB.NETWORK_UHC_KILLS.insert("'" + player.getUniqueId().toString() + "', '" + target.getUniqueId().toString() + "', '" + reason + "'");
						MessageHandler.alert(AccountHandler.getPrefix(target) + " &cwas killed: " + reason);
						target.setHealth(0.0d);
					}
				} else {
					MessageHandler.sendUnknownCommand(player);
				}
				return true;
			}
		};

		new DelayedTask(() -> {
			new CommandBase("rules") {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					MessageHandler.sendLine(sender);
					MessageHandler.sendMessage(sender, "Strip Mining: &cNOT ALLOWED");
					MessageHandler.sendMessage(sender, "Stair Casing: ONLY START ABOVE Y 32");
					MessageHandler.sendMessage(sender, "Roller Coastering: ONLY START ABOVE AND COME BACK TO Y 32");
					MessageHandler.sendMessage(sender, "Mining to Coordinates: &cNOT ALLOWED");
					MessageHandler.sendMessage(sender, "iPVP: &cNOT ALLOWED");
					MessageHandler.sendMessage(sender, "Cross Teaming: " + (OptionsHandler.getCrossTeaming() ? "&eALLOWED" : "&cNOT ALLOWED"));
					MessageHandler.sendMessage(sender, "Spoiling While Spectating: &cNOT ALLOWED");
					MessageHandler.sendMessage(sender, "Sky Basing: &eONLY BEFORE MEET UP");
					MessageHandler.sendMessage(sender, "Ground Hogging: &eONLY BEFORE MEET UP");
					MessageHandler.sendMessage(sender, "Poke holing: &eALLOWED");
					MessageHandler.sendMessage(sender, "Mining to Sounds: &eALLOWED");
					MessageHandler.sendMessage(sender, "Stalking: &eALLOWED");
					MessageHandler.sendMessage(sender, "&bHave a Question? &c/helpop <question>");
					MessageHandler.sendLine(sender);
					return true;
				}
			};

			new CommandBase("s", 1, -1, true) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					Player player = (Player) sender;
					if(Ranks.TRIAL.hasRank(player) || HostHandler.isHost(player.getUniqueId())) {
						String name = AccountHandler.getPrefix(sender);
						String message = "";
						for(String argument : arguments) {
							message += argument + " ";
						}
						for(Player online : Bukkit.getOnlinePlayers()) {
							if(Ranks.isStaff(online) || HostHandler.isHost(online.getUniqueId())) {
								MessageHandler.sendMessage(online, "&bStaff: " + name + ": " + StringUtil.color(message.substring(0, message.length() - 1)));
							}
						}
					} else {
						MessageHandler.sendUnknownCommand(player);
					}
					return true;
				}
			};
		});

		/*new CommandBase("test") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				new XrayDetection();
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);*/

		ultraGoldenApple = ChatColor.LIGHT_PURPLE + "Ultra Golden Apple (" + ChatColor.GOLD + "+4 Hearts" + ChatColor.LIGHT_PURPLE + ")";
		ShapedRecipe ultraApple = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName(ultraGoldenApple).getItemStack());
		ultraApple.shape("012", "345", "678");
		ultraApple.setIngredient('0', Material.GOLD_INGOT);
		ultraApple.setIngredient('1', Material.GOLD_INGOT);
		ultraApple.setIngredient('2', Material.GOLD_INGOT);
		ultraApple.setIngredient('3', Material.GOLD_INGOT);
		ultraApple.setIngredient('4', Material.SKULL_ITEM, 3);
		ultraApple.setIngredient('5', Material.GOLD_INGOT);
		ultraApple.setIngredient('6', Material.GOLD_INGOT);
		ultraApple.setIngredient('7', Material.GOLD_INGOT);
		ultraApple.setIngredient('8', Material.GOLD_INGOT);
		Bukkit.getServer().addRecipe(ultraApple);
	}
	
	@Override
	public void disable() {
		super.disable();
		String container = Bukkit.getWorldContainer().getPath();
		Bukkit.unloadWorld(getLobby(), false);
		File newWorld = new File(container + "/../resources/maps/uhc");
		if(newWorld.exists() && newWorld.isDirectory()) {
			FileHandler.delete(new File(container + "/lobby"));
			FileHandler.copyFolder(newWorld, new File(container + "/lobby"));
		}
	}
	
	private void heal(Player player) {
		player.setHealth(player.getMaxHealth());
		feed(player);
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}
	
	private void feed(Player player) {
		player.setFoodLevel(20);
	}
}
