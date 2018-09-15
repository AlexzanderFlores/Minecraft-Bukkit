package network.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import network.Network;
import network.ProPlugin;
import network.customevents.game.GameStartEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.competitive.EloRanking;
import network.player.MessageHandler;
import network.player.TitleDisplayer;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class TeamHandler implements Listener {
	private List<Team> teams = null;
	private Map<String, Integer> shoutUses = null;
	private boolean enableTeamSelectorItem = false;
	private ItemStack item = null;
	private String name = null;
	
	public TeamHandler() {
		teams = new ArrayList<Team>();
		name = "Team Selector";
		item = new ItemCreator(Material.WOOL).setName("&a" + name).getItemStack();
		shoutUses = new HashMap<String, Integer>();
		new CommandBase("shout", 1, -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(shoutUses.containsKey(player.getName())) {
					int uses = shoutUses.get(player.getName());
					if(--uses <= 0) {
						shoutUses.remove(player.getName());
					} else {
						shoutUses.put(player.getName(), uses);
					}
					String msg = "";
					for(String argument : arguments) {
						msg += argument + " ";
					}
					msg = StringUtil.color(msg);
					if(!Ranks.OWNER.hasRank(player)) {
						for(ChatColor badColor : new ChatColor [] {ChatColor.BOLD, ChatColor.MAGIC, ChatColor.UNDERLINE, ChatColor.STRIKETHROUGH}) {
							msg = msg.replace(badColor + "", "");
						}
					}
					MessageHandler.alert("&6[Shout] " + EloRanking.getRank(player).getPrefix() + " " + AccountHandler.getPrefix(player) + "&f: " + msg);
					MessageHandler.alert("To shout use &e/shout <message>");
				} else {
					MessageHandler.sendMessage(player, "&cYou are out of shouts for this game");
				}
				return true;
			}
		}.setRequiredRank(Ranks.PRO);
		EventUtil.register(this);
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	public Team addTeam(String name) {
		Team team = Network.getScoreboard().registerNewTeam(name);
		teams.add(team);
		return team;
	}
	
	public void removeTeam(String team) {
		if(teams.contains(team)) {
			teams.remove(team);
			Network.getScoreboard().getTeam(team).unregister();
		}
	}
	
	public Team getTeam(String name) {
		for(Team team : teams) {
			if(team.getName().equals(name)) {
				return team;
			}
		}
		return null;
	}
	
	public Team getTeam(Player player) {
		for(Team team : teams) {
			if(isOnTeam(player, team)) {
				return team;
			}
		}
		return null;
	}
	
	public boolean isOnTeam(Player player, Team team) {
		return team.hasPlayer(player);
	}
	
	public boolean isOnSameTeam(Player playerOne, Player playerTwo) {
		return getTeam(playerOne) == getTeam(playerTwo);
	}
	
	public List<Player> getPlayers(Team team) {
		List<Player> players = new ArrayList<Player>();
		for(OfflinePlayer offlinePlayer : team.getPlayers()) {
			if(offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				players.add(player);
			}
		}
		return players;
	}
	
	public boolean getEnableTeamItem() {
		return enableTeamSelectorItem;
	}
	
	public void toggleTeamItem() {
		enableTeamSelectorItem = !enableTeamSelectorItem;
	}
	
	public void setTeam(Player player, Team newTeam) {
		for(Team team : teams) {
			team.removePlayer(player);
		}
		newTeam.addPlayer(player);
		for(ChatColor color : ChatColor.values()) {
			if(newTeam.getPrefix().startsWith(color.toString())) {
				String name = color + player.getName();
				if(name.length() > 16) {
					name = name.substring(0, 16);
				}
				player.setPlayerListName(name);
				break;
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(enableTeamSelectorItem && Network.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			player.getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			if(Ranks.PRO_PLUS.hasRank(player)) {
				shoutUses.put(player.getName(), 2);
			} else if(Ranks.PRO.hasRank(player)) {
				shoutUses.put(player.getName(), 1);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		MiniGame miniGame = Network.getMiniGame();
		if(miniGame != null && miniGame.getGameState() != GameStates.STARTED) {
			return;
		}
		Player player = event.getPlayer();
		Team team = getTeam(player);
		if(team != null) {
			for(ChatColor color : ChatColor.values()) {
				if(team.getPrefix().startsWith(color.toString())) {
					event.setFormat(team.getPrefix() + event.getFormat().replace(player.getName(), color + player.getName()));
					break;
				}
			}
		}
		for(Player online : ProPlugin.getPlayers()) {
			Team onlineTeam = getTeam(online);
			if(onlineTeam != team) {
				event.getRecipients().remove(online);
			}
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item.equals(this.item)) {
			if(Ranks.PRO.hasRank(player)) {
				if(item != null && item.equals(this.item)) {
					Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
					inventory.setItem(11, new ItemCreator(Material.WOOL, DyeColor.RED.getData()).setName("&cRed Team").getItemStack());
					inventory.setItem(15, new ItemCreator(Material.WOOL, DyeColor.BLUE.getData()).setName("&bBlue Team").getItemStack());
					player.openInventory(inventory);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			byte data = event.getItem().getData().getData();
			Team team = null;
			if(data == DyeColor.RED.getData()) {
				team = getTeam("red");
			} else if(data == DyeColor.BLUE.getData()) {
				team = getTeam("blue");
			}
			if(team == null) {
				MessageHandler.sendMessage(player, "&cError: team not found, please report this");
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else if(team.hasPlayer(player)) {
				new TitleDisplayer(player, "&cAlready on the", team.getPrefix() + "&eTeam").display();
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else {
				new TitleDisplayer(player, "&eYou joined the", team.getPrefix() + "&eTeam").display();
				MessageHandler.sendMessage(player, "You joined the " + team.getPrefix() + "&xteam");
				setTeam(player, team);
				EffectUtil.playSound(player, Sound.LEVEL_UP);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
