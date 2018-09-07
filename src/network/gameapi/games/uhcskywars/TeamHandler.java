package network.gameapi.games.uhcskywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.Team;

import network.Network;
import network.ProPlugin;
import network.customevents.game.GameDeathEvent;
import network.customevents.game.GameStartingEvent;
import network.customevents.game.GameWinEvent;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.MiniGame.GameStates;
import network.player.MessageHandler;
import network.player.TitleDisplayer;
import network.server.CommandBase;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;

public class TeamHandler implements Listener {
	private static List<Team> teams = null;
	private List<String> colors = null;
	private Map<String, List<String>> ignores = null;
	private String name = null;
	
	public TeamHandler() {
		teams = new ArrayList<Team>();
		colors = new ArrayList<String>();
		ignores = new HashMap<String, List<String>>();
		name = "Team Invite - ";
		colors.add("&1");
		colors.add("&2");
		colors.add("&3");
		colors.add("&4");
		colors.add("&5");
		colors.add("&6");
		colors.add("&9");
		colors.add("&a");
		colors.add("&b");
		colors.add("&c");
		colors.add("&d");
		colors.add("&e");
		new CommandBase("team", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(player, "Team commands:");
					MessageHandler.sendMessage(player, "/team <name> &eInvite a player to your team");
					MessageHandler.sendMessage(player, "/team leave &eLeave your team");
					MessageHandler.sendMessage(player, "/team list &eDisplays your team's players");
				} else if(arguments[0].equalsIgnoreCase("list")) {
					Team team = getTeam(player);
					if(team == null) {
						MessageHandler.sendMessage(player, "&cYou are not in a team &x/team help");
					} else {
						MessageHandler.sendMessage(player, "Players on your team:");
						for(OfflinePlayer offlinePlayer : team.getPlayers()) {
							MessageHandler.sendMessage(player, offlinePlayer.getName());
						}
					}
				} else if(arguments[0].equalsIgnoreCase("listTeams")) {
					int index = 0;
					MessageHandler.sendMessage(player, "Teams: (" + teams.size() + ")");
					for(Team team : teams) {
						MessageHandler.sendMessage(player, "Team #" + (++index));
						MessageHandler.sendMessage(player, "Prefix + Name: " + team.getPrefix() + team.getName());
						MessageHandler.sendMessage(player, "Players: (" + team.getPlayers().size() + ")");
						for(OfflinePlayer offlinePlayer : team.getPlayers()) {
							MessageHandler.sendMessage(player, "  - " + offlinePlayer.getName());
						}
					}
				} else if(arguments[0].equalsIgnoreCase("leave")) {
					Team team = getTeam(player);
					if(team == null) {
						MessageHandler.sendMessage(player, "&cYou are not in a team &x/team help");
					} else {
						remove(player);
					}
				} else {
					if(Network.getMiniGame().getJoiningPreGame()) {
						String name = arguments[0];
						Player target = ProPlugin.getPlayer(name);
						if(target == null) {
							MessageHandler.sendMessage(player, "&c" + name + " is not online");
						} else {
							List<String> ignore = ignores.get(target.getName());
							if(ignore == null || !ignore.contains(player.getName())) {
								Inventory inventory = Bukkit.createInventory(target, 9 * 3, "Team Invite - " + player.getName());
								inventory.setItem(11, new ItemCreator(Material.WOOL, 5).setName("&aAccept").getItemStack());
								inventory.setItem(13, new ItemCreator(Material.WOOL, 15).setName("&cIgnore " + player.getName()).getItemStack());
								inventory.setItem(15, new ItemCreator(Material.WOOL, 14).setName("&cDeny").getItemStack());
								target.openInventory(inventory);
							} else {
								MessageHandler.sendMessage(player, "&c" + target.getName() + " ignored team invited from you for this game");
							}
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou cannot invite players to teams at this time");
					}
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static List<Team> getTeams() {
		return teams;
	}
	
	private void setTeam(Player playerOne, Player playerTwo) {
		Team team = Network.getScoreboard().registerNewTeam(playerOne.getName());
		team.addPlayer(playerOne);
		if(playerTwo != null) {
			team.addPlayer(playerTwo);
		}
		team.setAllowFriendlyFire(false);
		team.setPrefix(StringUtil.color(colors.get(0)));
		teams.add(team);
		colors.remove(0);
		alert(team, playerOne.getName() + " has joined the team");
		if(playerTwo != null) {
			alert(team, playerTwo.getName() + " has joined the team");
		}
		String tab = team.getPrefix() + playerOne.getName();
		if(tab.length() > 16) {
			tab = tab.substring(0, 16);
		}
		playerOne.setPlayerListName(tab);
		if(playerTwo != null) {
			tab = team.getPrefix() + playerTwo.getName();
			if(tab.length() > 16) {
				tab = tab.substring(0, 16);
			}
			playerTwo.setPlayerListName(tab);
		}
	}
	
	private void alert(Team team, String alert) {
		for(OfflinePlayer offlinePlayer : team.getPlayers()) {
			if(offlinePlayer.isOnline()) {
				Player onlinePlayer = (Player) offlinePlayer;
				MessageHandler.sendMessage(onlinePlayer, alert);
			}
		}
	}
	
	public static Team getTeam(Player player) {
		if(teams != null) {
			for(Team team : teams) {
				if(team.hasPlayer(player)) {
					return team;
				}
			}
		}
		return null;
	}
	
	private void remove(Player player) {
		Team team = getTeam(player);
		if(team != null) {
			alert(team, "&c" + player.getName() + " has left the team");
			team.removePlayer(player);
			if(team.getSize() <= 0) {
				alert(team, "&cTeam deleted");
				colors.add(team.getPrefix());
				team.unregister();
				teams.remove(team);
				if(teams.size() == 1) {
					Bukkit.getPluginManager().callEvent(new GameWinEvent(team));
				} else if(teams.isEmpty()) {
					Network.getMiniGame().setGameState(GameStates.ENDING);
				}
			}
		}
		if(ignores.containsKey(player.getName())) {
			ignores.get(player.getName()).clear();
			ignores.remove(player.getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(Network.getMiniGame().getJoiningPreGame()) {
			new TitleDisplayer(event.getPlayer(), "&bTeam Commands:", "&b/team").setFadeOut(20 * 5).display();
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith(name)) {
			Player player = event.getPlayer();
			String [] split = event.getTitle().split(" - ");
			String senderName = split[split.length - 1];
			if(event.getSlot() == 11) {
				Player sender = ProPlugin.getPlayer(senderName);
				if(sender == null) {
					MessageHandler.sendMessage(player, "&c" + senderName + " is no longer online");
				} else {
					setTeam(sender, player);
				}
			} else if(event.getSlot() == 13) {
				List<String> ignore = ignores.get(player.getName());
				if(ignore == null) {
					ignore = new ArrayList<String>();
				}
				if(!ignore.contains(senderName)) {
					ignore.add(senderName);
					ignores.put(player.getName(), ignore);
				}
				MessageHandler.sendMessage(player, "You will no longer get team invites from " + senderName);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		List<Player> noTeam = new ArrayList<Player>();
		for(Player player : ProPlugin.getPlayers()) {
			if(getTeam(player) == null) {
				noTeam.add(player);
			}
		}
		while(noTeam.size() % 2 != 0 && noTeam.size() > 1) {
			setTeam(noTeam.get(0), noTeam.get(1));
			noTeam.remove(0);
			noTeam.remove(0);
		}
		for(Player player : ProPlugin.getPlayers()) {
			if(getTeam(player) == null) {
				setTeam(player, null);
			}
		}
	}
}