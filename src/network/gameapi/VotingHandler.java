package network.gameapi;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import network.Network;
import network.Network.Plugins;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.FileHandler;
import network.server.util.StringUtil;
import npc.NPCEntity;

public class VotingHandler implements Listener {
	public class VoteData {
		private String name = null;
		private int votes = 0;
		
		public VoteData(String name) {
			setName(name);
			voteData.put(getName(), this);
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public int getVotes() {
			return this.votes;
		}
		
		public void vote(Player player) {
			if(playerVotes.containsKey(player.getName())) {
				playerVotes.get(player.getName()).removeVote(player);
			}
			playerVotes.put(player.getName(), this);
			int votes = Ranks.getVotes(player);
			this.votes += votes;
			MessageHandler.sendMessage(player, "+" + votes + " &avotes for &e" + getName());
			if(!Ranks.VIP.hasRank(player)) {
				MessageHandler.sendMessage(player, "&eRanks give you more map votes! &b/buy");
			}
		}
		
		public void removeVote(Player player) {
			if(playerVotes.containsKey(player.getName()) && playerVotes.get(player.getName()).getName().equals(this.getName())) {
				int votes = Ranks.getVotes(player);
				this.votes -= votes;
				MessageHandler.sendMessage(player, "&c-" + votes + "&a votes for &e" + playerVotes.get(player.getName()).getName());
			}
		}
	}
	
	private List<String> recentlyClicked = null;
	private static List<String> possibleMaps = null;
	private static Map<String, VoteData> voteData = null;
	private static Map<String, VoteData> playerVotes = null;
	
	public VotingHandler(final List<String> options) {
		recentlyClicked = new ArrayList<String>();
		voteData = new HashMap<String, VoteData>();
		playerVotes = new HashMap<String, VoteData>();
		World world = Network.getMiniGame().getLobby();
		for(Entity entity : world.getEntities()) {
			if(entity instanceof Creeper) {
				entity.remove();
			}
		}
		Location [] locations = new Location [] {
			new Location(world, 5.5, 6, 12.5), new Location(world, 0.5, 6, 14.5), new Location(world, -4.5, 6, 12.5)
		};
		if(possibleMaps == null) {
			for(String map : options) {
				new VoteData(StringUtil.color("&a" + map.replace("_", " ")));
			}
		} else {
			for(String map : possibleMaps) {
				new VoteData(StringUtil.color("&a" + map.replace("_", " ")));
			}
		}
		for(int a = 0; a < locations.length && a < options.size(); ++a) {
			String option = StringUtil.color("&a" + options.get(a).replace("_", " "));
			new NPCEntity(EntityType.CREEPER, option, locations[a]) {
				@Override
				public void onInteract(final Player player) {
					String map = getName();
					Creeper creeper = (Creeper) getLivingEntity();
					voteData.get(map).vote(player);
					playEffect(player, creeper, map);
				}
			};
		}
		EventUtil.register(this);
	}
	
	private void playEffect(final Player player,final Creeper creeper, String map) {
		EffectUtil.playSound(player, Sound.LEVEL_UP);
		//ParticleTypes.FLAME.displaySpiral(creeper.getLocation());
		if(!creeper.isPowered()) {
			creeper.setPowered(true);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					creeper.setPowered(false);
				}
			}, 30);
		}
		if(!recentlyClicked.contains(player.getName()) && Ranks.VIP_PLUS.hasRank(player)) {
			recentlyClicked.add(player.getName());
			MessageHandler.alert(AccountHandler.getPrefix(player) + "&e has voted for &c" + map);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					recentlyClicked.remove(player.getName());
				}
			}, 20 * 5);
		}
	}
	
	private static String getGame() {
		Plugins plugin = Network.getPlugin();
		return plugin.toString().toLowerCase().replace("_", "");
	}
	
	public static void loadMaps() {
		loadMaps(getGame());
	}
	
	public static void loadMaps(String game) {
		if(possibleMaps == null) {
			possibleMaps = new ArrayList<String>();
		}
		File file = new File(Bukkit.getWorldContainer().getPath() + "/../resources/maps/" + game);
		String [] folders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		List<String> maps = new ArrayList<String>();
		for(String folder : folders) {
			if(new File(file.getPath() + "/" + folder + "/spawns.yml").exists()) {
				possibleMaps.add(folder);
			}
		}
		int max = 4;
		if(possibleMaps.size() <= max) {
			new VotingHandler(possibleMaps);
		} else {
			for(int a = 0; a < max && a < possibleMaps.size() && maps.size() <= max; ++a) {
				String map = null;
				do {
					map = possibleMaps.get(new Random().nextInt(possibleMaps.size()));
				} while(maps.contains(map));
				maps.add(map);
			}
			maps.add("Click For Other Maps");
			new VotingHandler(maps);
		}
	}
	
	public static VoteData getWinner() {
		VoteData winner = null;
		for(VoteData data : voteData.values()) {
			if((winner == null || data.getVotes() > winner.getVotes()) || (data.getVotes() == winner.getVotes() && new Random().nextBoolean())) {
				winner = data;
			}
		}
		winner.setName(ChatColor.stripColor(winner.getName()));
		World world = Network.getMiniGame().getLobby();
		for(Entity entity : world.getEntities()) {
			if(entity instanceof Creeper) {
				entity.remove();
			}
		}
		if(possibleMaps != null) {
			possibleMaps.clear();
			possibleMaps = null;
		}
		voteData.clear();
		voteData = null;
		playerVotes = null;
		return winner;
	}
	
	public static World loadWinningWorld() {
		return loadWinningWorld(getGame());
	}
	
	public static World loadWinningWorld(String game) {
		VoteData voteData = getWinner();
		final String worldName = voteData.getName().replace(" ", "_");
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String game = Network.getPlugin().toString();
				String [] keys = new String [] {"game_name", "map"};
				String [] values = new String [] {game, worldName};
				if(DB.NETWORK_MAP_VOTES.isKeySet(keys, values)) {
					int times = DB.NETWORK_MAP_VOTES.getInt(keys, values, "times_voted") + 1;
					DB.NETWORK_MAP_VOTES.updateInt("times_voted", times, keys, values);
				} else {
					DB.NETWORK_MAP_VOTES.insert("'" + game + "', '" + worldName + "', '1'");
				}
			}
		});
		File world = new File("/root/" + Network.getServerName().toLowerCase() + "/" + worldName);
		if(world.exists()) {
			FileHandler.delete(world);
		}
		FileHandler.copyFolder("/root/resources/maps/" + game + "/" + worldName, world.getPath());
		MessageHandler.alert(worldName.replace("_", " ") + " has won with " + voteData.getVotes() + " votes");
		MessageHandler.alert("Want more votes? &b/buy");
		World arena = Bukkit.createWorld(new WorldCreator(worldName));
		for(Entity entity : arena.getEntities()) {
			if(entity instanceof LivingEntity) {
				entity.remove();
			}
		}
		return arena;
	}
}