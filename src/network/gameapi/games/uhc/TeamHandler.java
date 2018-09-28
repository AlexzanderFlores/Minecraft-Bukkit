package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import network.Network;
import network.ProPlugin;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PostPlayerJoinEvent;
import network.gameapi.SpectatorHandler;
import network.gameapi.MiniGame.GameStates;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class TeamHandler implements Listener {
	private static String name = null;
	private static String colorChar = null;
	private static String text = null;
	private static int maxSize = 1;
	private static Map<String, String> invites = null;
	private static Map<String, Team> teams = null;
	private static List<String> delayedRequests = null;
	private static List<String> possibleColors = null;
	private static List<String> teamChat = null;
	
	public TeamHandler() {
		name = "Team Size Selection";
		colorChar = ChatColor.WHITE.toString().substring(0, 1);
		text = "&6Team Size:";
		invites = new HashMap<>();
		delayedRequests = new ArrayList<>();
		teams = new HashMap<>();
		possibleColors = new ArrayList<>();
		teamChat = new ArrayList<>();
		String [] baseColors = new String [] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
		String [] extraColors = new String [] {null, "n", "o", "m", "l"};
		for(String baseColor : baseColors) {
			for(String extraColor : extraColors) {
				if(extraColor == null) {
					possibleColors.add(colorChar + baseColor);
				} else {
					possibleColors.add(colorChar + baseColor + colorChar + extraColor);
				}
			}
		}

		new CommandBase("team", 0, 3, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0 || (arguments.length == 1 && arguments[0].equalsIgnoreCase("help"))) {
					MessageHandler.sendMessage(player, "/team &eDisplays team commands");
					MessageHandler.sendMessage(player, "/team help &eDisplays team commands");
					MessageHandler.sendMessage(player, "/team <name> &eInvite or accept someone to your team");
					MessageHandler.sendMessage(player, "/team deny &eDeny an invite");
					MessageHandler.sendMessage(player, "/team leave &eLeave your current team");
					MessageHandler.sendMessage(player, "/team kick <name> &eKick a player from your team");
					MessageHandler.sendMessage(player, "/team color <color code> &eChange your team's color");
					MessageHandler.sendMessage(player, "/team list &eList all players in your team");
					MessageHandler.sendMessage(player, "/teamChat &eToggle team chat on/off");
					MessageHandler.sendMessage(player, "/teamChat <message> &eSend a message in team chat");
				} else if(arguments.length >= 1) {
					if(getMaxTeamSize() > 1) {
						if(arguments[0].equalsIgnoreCase("list")) {
							if(teams.containsKey(player.getName())) {
								MessageHandler.sendMessage(player, "Your team members:");
								for(OfflinePlayer member : teams.get(player.getName()).getPlayers()) {
									MessageHandler.sendMessage(player, member.getName());
								}
							} else {
								MessageHandler.sendMessage(player, "&cYou are not in a team");
							}
						} else if(Network.getMiniGame().getGameState() == GameStates.STARTED) {
							MessageHandler.sendMessage(player, "&cYou cannot use this command after the game has started");
						} else {
							if(arguments[0].equalsIgnoreCase("leave")) {
								if(teams.containsKey(player.getName())) {
									teamChat(player, "has left the team");
									teams.get(player.getName()).removePlayer(player);
									if(teams.get(player.getName()).getPlayers().isEmpty()) {
										teams.get(player.getName()).unregister();
									}
									teams.remove(player.getName());
								} else {
									MessageHandler.sendMessage(player, "&cYou are not in a team");
								}
							} else if(arguments[0].equalsIgnoreCase("kick")) {
								Player target = ProPlugin.getPlayer(arguments[1]);
								if(target == null) {
									MessageHandler.sendMessage(player, "&c" + arguments[1] + " is not online");
								} else if(teams.containsKey(player.getName()) && teams.containsKey(target.getName()) && teams.get(player.getName()) == teams.get(target.getName())) {
									teamChat(player, "was kicked from the team");
									teams.get(player.getName()).removePlayer(target);
									if(teams.get(target.getName()).getPlayers().isEmpty()) {
										teams.get(target.getName()).unregister();
									}
									teams.remove(target.getName());
								} else {
									MessageHandler.sendMessage(player, "&cYou are not in a team");
								}
							} else if(arguments[0].equalsIgnoreCase("color")) {
								Team team = teams.get(player.getName());
								if(team == null) {
									MessageHandler.sendMessage(player, "&cYou do not have a team");
									return true;
								}
								if(Ranks.PRO.hasRank(player)) {
									if(arguments.length == 2) {
										String color = arguments[1].toLowerCase();
										char character = ChatColor.WHITE.toString().charAt(0);
										if(possibleColors.contains(color.replace("&", String.valueOf(character)))) {
											if(color.length() == 2 && color.charAt(0) == '&') {
												char colorChar = color.charAt(1);
												if(isHexChar(colorChar)) {
													String prefix = team.getPrefix();
													if(prefix != null && !prefix.equals("")) {
														//Bukkit.getLogger().info("\"" + prefix + "\"");
														//Bukkit.getLogger().info(possibleColors.get(0));
														possibleColors.set(0, prefix);
														//Bukkit.getLogger().info(possibleColors.get(1));
													}
													String realColor = color.replace("&", String.valueOf(character));
													//Bukkit.getLogger().info("Real Color: \"" + realColor + "\"");
													possibleColors.remove(realColor);
													team.setPrefix(realColor);
													for(OfflinePlayer offlinePlayer : team.getPlayers()) {
														if(offlinePlayer.isOnline()) {
															Player onlinePlayer = offlinePlayer.getPlayer();
															HealthHandler.updateHealth(onlinePlayer);
														}
													}
													return true;
												}
											} else if(color.length() == 4 && color.charAt(0) == '&' && color.charAt(2) == '&') {
												char colorChar = color.charAt(1);
												if(isHexChar(colorChar)) {
													char colorChar2 = color.charAt(3);
													if(isHexChar(colorChar2)) {
														String prefix = team.getPrefix();
														if(prefix != null) {
															//Bukkit.getLogger().info("\"" + prefix + "\"");
															//Bukkit.getLogger().info(possibleColors.get(0));
															possibleColors.set(0, prefix);
															//Bukkit.getLogger().info(possibleColors.get(1));
														}
														String realColor = color.replace("&", String.valueOf(character));
														//Bukkit.getLogger().info("Real Color: \"" + realColor + "\"");
														possibleColors.remove(realColor);
														team.setPrefix(realColor);
														for(OfflinePlayer offlinePlayer : team.getPlayers()) {
															if(offlinePlayer.isOnline()) {
																Player onlinePlayer = offlinePlayer.getPlayer();
																HealthHandler.updateHealth(onlinePlayer);
															}
														}
														return true;
													}
												}
											}
										} else {
											MessageHandler.sendMessage(player, "&cThat is not a valid color to use at this time");
											return true;
										}
										MessageHandler.sendMessage(player, "&cYour color must be in this format: &e&X &cor &e&X&X");
										MessageHandler.sendMessage(player, "Replace X with your color code, run &b/coloredChat");
									} else {
										MessageHandler.sendMessage(player, "/team color <color code>");
									}
								} else {
									MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
								}
							} else if(arguments[0].equalsIgnoreCase("deny")) {
								if(invites.containsKey(player.getName())) {
									Player requester = ProPlugin.getPlayer(invites.get(player.getName()));
									if(requester != null) {
										MessageHandler.sendMessage(requester, AccountHandler.getPrefix(player) + " &chas denied your team request");
										final String name = requester.getName();
										delayedRequests.add(name);
										new DelayedTask(() -> delayedRequests.remove(name), 20 * 3);
									}
									invites.remove(player.getName());
								} else {
									MessageHandler.sendMessage(player, "&cYou do not have a pending team invite");
								}
							} else if(delayedRequests.contains(player.getName())) {
								MessageHandler.sendMessage(player, "&cCannot send invite due to being denied within the last &e3 &cseconds");
								MessageHandler.sendMessage(player, "Please try again in a few seconds");
							} else {
								String target = arguments[0];
								Player targetPlayer = ProPlugin.getPlayer(target);
								if(targetPlayer == null) {
									MessageHandler.sendMessage(player, "&c" + target + " is not online");
								} else if(invites.containsKey(player.getName()) && invites.get(player.getName()).equals(targetPlayer.getName())) {
									invites.remove(player.getName());
									Team team;
									if(Network.getScoreboard().getTeam(targetPlayer.getName()) == null) {
										team = Network.getScoreboard().registerNewTeam(targetPlayer.getName());
									} else {
										team = Network.getScoreboard().getTeam(targetPlayer.getName());
										if(team.getPlayers().size() >= getMaxTeamSize()) {
											MessageHandler.sendMessage(player, "&cTeam has already reached the max team size of &e" + getMaxTeamSize());
											return true;
										}
									}
									team.setAllowFriendlyFire(false);
									team.addPlayer(targetPlayer);
									team.addPlayer(player);
									teams.put(player.getName(), team);
									teams.put(targetPlayer.getName(), team);
									teamChat(targetPlayer, "has joined the team");
									teamChat(player, "has joined the team");
									updateColors();
								} else if(invites.containsKey(targetPlayer.getName())) {
									MessageHandler.sendMessage(player, AccountHandler.getPrefix(targetPlayer) + " &calready has a pending invite");
								} else if(targetPlayer.getName().equals(player.getName())) {
									MessageHandler.sendMessage(player, "&cYou cannot invite yourself to a team");
								} else if(teams.containsKey(targetPlayer.getName())) {
									MessageHandler.sendMessage(player, AccountHandler.getPrefix(targetPlayer) + " &cis already in a team");
								} else {
									if(teams.containsKey(player.getName())) {
										int size = teams.get(player.getName()).getSize();
										int inviteCount = 0;
										for(String inviter : invites.values()) {
											if(inviter.equals(player.getName())) {
												++inviteCount;
											}
										}
										if(size >= getMaxTeamSize() || size + inviteCount >= getMaxTeamSize()) {
											MessageHandler.sendMessage(player, "&cCannot invite more members: Max team size is &e" + getMaxTeamSize());
											return true;
										}
									}
									invites.put(targetPlayer.getName(), player.getName());
									MessageHandler.sendMessage(player, "You sent a team request to " + AccountHandler.getPrefix(targetPlayer));
									MessageHandler.sendMessage(targetPlayer, AccountHandler.getPrefix(player) + " &6&lhas sent you a team request");
									MessageHandler.sendMessage(targetPlayer, "Accept: &b/team " + player.getName() + " &aDeny: &b/team deny");
								}
							}
						}
					} else {
						MessageHandler.sendMessage(player, "&cThis is an FFA game");
					}
				}
				return true;
			}
		};
		new CommandBase("sc", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Team team = teams.get(player.getName());
				if(team == null) {
					MessageHandler.sendMessage(player, "&cYou are not in a team");
				} else {
					for(OfflinePlayer member : team.getPlayers()) {
						if(member.isOnline()) {
							Player onlineMember = member.getPlayer();
							if(!SpectatorHandler.contains(onlineMember)) {
								Location location = onlineMember.getLocation();
								String locationString = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
								for(OfflinePlayer member2 : team.getPlayers()) {
									if(member2.isOnline()) {
										Player onlineMember2 = member2.getPlayer();
										MessageHandler.sendMessage(onlineMember2, AccountHandler.getPrefix(onlineMember) + ": " + locationString);
									}
								}
							}
						}
					}
				}
				return true;
			}
		};
		new CommandBase("teamChat", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Team team = teams.get(player.getName());
				if(team == null) {
					MessageHandler.sendMessage(player, "&cYou are not in a team");
				} else if(arguments.length == 0) {
					if(teamChat.contains(player.getName())) {
						teamChat.remove(player.getName());
						MessageHandler.sendMessage(player, "Team chat is now &cOFF");
					} else {
						teamChat.add(player.getName());
						MessageHandler.sendMessage(player, "Team chat is on &eON");
					}
				} else {
					String message = "";
					for(int a = 0; a < arguments.length; ++a) {
						message += arguments[a] + " ";
					}
					teamChat(player, message);
				}
				return true;
			}
		};
		update();
		EventUtil.register(this);
	}
	
	private void addTeam(String nameOne, String nameTwo, String color) {
		Team team = Network.getScoreboard().registerNewTeam(nameOne);
		team.addPlayer(Bukkit.getOfflinePlayer(nameOne));
		team.addPlayer(Bukkit.getOfflinePlayer(nameTwo));
		team.setPrefix(StringUtil.color(color));
		teams.put(nameOne, team);
		teams.put(nameTwo, team);
	}
	
	private boolean isHexChar(char character) {
		return (character >= 48 && character <= 57) || (character >= 97 && character <= 102) || (character >= 108 && character <= 111);
	}
	
	public static boolean teamChat(Player player, String message) {
		Team team = teams.get(player.getName());
		if(team == null) {
			return false;
		} else {
			for(OfflinePlayer member : team.getPlayers()) {
				if(member.isOnline()) {
					Player onlineMember = member.getPlayer();
					MessageHandler.sendMessage(onlineMember, "&bTC: " + AccountHandler.getPrefix(player) + ": " + message);
				}
			}
			return true;
		}
	}
	
	public static Team getTeam(Player player) {
		return getTeam(player.getName());
	}
	
	public static Team getTeam(String name) {
		return teams.get(name);
	}
	
	public static void setTeam(Player player, Team team) {
		teams.put(player.getName(), team);
		updateColors();
	}
	
	public static void removeFromTeam(String name) {
		if(teams.containsKey(name)) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(name);
			Team team = teams.get(name);
			team.removePlayer(player);
			if(team.getPlayers().size() == 1) {
				for(OfflinePlayer member : team.getPlayers()) {
					removeFromTeam(member.getName());
				}
			}
			teams.remove(name);
			if(player.isOnline()) {
				updateColors();
			}
		}
	}
	
	public static List<Team> getTeams() {
		return new ArrayList<Team>(teams.values());
	}
	
	public static void open(Player player) {
		ItemStack enabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData()).setName("&aENABLED").addLore("&fClick the icon above to toggle").getItemStack();
		ItemStack disabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setName("&cDISABLED").addLore("&fClick the icon above to toggle").getItemStack();
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		int [] slots = new int [] {1, 3, 5, 7};
		String [] names = new String [] {"Solo", "Teams of 2", "Teams of 3", "Teams of 4"};
		for(int a = 0; a < slots.length; ++a) {
			inventory.setItem(slots[a], new ItemCreator(Material.SKULL_ITEM, 3).setAmount(a + 1).setName("&b" + names[a]).getItemStack());
			if(maxSize == a + 1) {
				inventory.setItem(slots[a] + 9, enabled);
			} else {
				inventory.setItem(slots[a] + 9, disabled);
			}
		}
		inventory.setItem(inventory.getSize() - 9, new ItemCreator(Material.ARROW).setName("&eBack").getItemStack());
		inventory.setItem(inventory.getSize() - 1, new ItemCreator(Material.ARROW).setName("&eMove to Other Options").getItemStack());
		player.openInventory(inventory);
	}
	
	public static int getMaxTeamSize() {
		return maxSize;
	}
	
	public static void setMaxTeamSize(int size) {
		maxSize = size;
		update();
	}
	
	private static void update() {
		Network.getSidebar().removeText(text);
		Network.getSidebar().setText(text, maxSize);
	}
	
	private static void updateColors() {
		for(Team team : TeamHandler.getTeams()) {
			if(team.getPrefix() == null || team.getPrefix().equals("")) {
				team.setPrefix(possibleColors.get(0));
				possibleColors.remove(0);
			}
			HealthHandler.updateHealth();
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			Material type = item.getType();
			if(type == Material.ARROW) {
				String name = event.getItemTitle();
				if(name.contains("Back")) {
					ScenarioManager.open(player);
				} else {
					OptionsHandler.open(player);
				}
			} else if(type == Material.STAINED_GLASS_PANE) {
				open(player);
			} else {
				if(Network.getMiniGame().getGameState() == GameStates.STARTED) {
					player.closeInventory();
					MessageHandler.sendMessage(player, "&cYou cannot edit the Team Size during the game");
				} else {
					maxSize = item.getAmount();
					open(player);
					update();
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		if(getMaxTeamSize() > 1 && Network.getMiniGame().getGameState() != GameStates.STARTED) {
			MessageHandler.sendLine(event.getPlayer(), "&c&k");
			MessageHandler.sendMessage(event.getPlayer(), "");
			MessageHandler.sendMessage(event.getPlayer(), "Make a team with &b/team");
			MessageHandler.sendMessage(event.getPlayer(), "Team chat toggle on/off: &b/teamChat");
			MessageHandler.sendMessage(event.getPlayer(), "Team chat single message: &b/teamChat <message>");
			MessageHandler.sendMessage(event.getPlayer(), "");
			MessageHandler.sendLine(event.getPlayer(), "&c&k");
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(teamChat.contains(event.getPlayer().getName())) {
			if(teamChat(event.getPlayer(), event.getMessage())) {
				event.setCancelled(true);
			}
		}
	}
}
