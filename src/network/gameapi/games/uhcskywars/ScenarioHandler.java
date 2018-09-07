package network.gameapi.games.uhcskywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.Network;
import network.customevents.game.GameStartEvent;
import network.gameapi.uhc.scenarios.Scenario;
import network.gameapi.uhc.scenarios.scenarios.Fireless;
import network.gameapi.uhc.scenarios.scenarios.HeartModifier;
import network.gameapi.uhc.scenarios.scenarios.Speed;
import network.gameapi.uhc.scenarios.scenarios.Switcheroo;
import network.gameapi.uhc.scenarios.scenarios.TimeBomb;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.util.EventUtil;
import npc.NPCEntity;

public class ScenarioHandler implements Listener {
	private Map<String, VoteData> voteData = null;
	private Map<String, VoteData> playerVotes = null;
	private List<Scenario> scenarios = null;
	
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
			++this.votes;
			MessageHandler.sendMessage(player, "+1 &avote for &e" + getName());
		}
		
		public void removeVote(Player player) {
			if(playerVotes.containsKey(player.getName()) && playerVotes.get(player.getName()).getName().equals(this.getName())) {
				--this.votes;
				MessageHandler.sendMessage(player, "&c-1 &avote for &e" + playerVotes.get(player.getName()).getName());
			}
		}
	}
	
	public ScenarioHandler() {
		voteData = new HashMap<String, VoteData>();
		playerVotes = new HashMap<String, VoteData>();
		World world = Network.getMiniGame().getLobby();
		scenarios = new ArrayList<Scenario>();
		scenarios.add(TimeBomb.getInstance());
		scenarios.add(Fireless.getInstance());
		scenarios.add(Switcheroo.getInstance());
		scenarios.add(HeartModifier.getInstance("10 Hearts", 20));
		scenarios.add(Speed.getInstance());
		for(double a = 0, z = -3.5; a < scenarios.size(); ++a, z += 2) {
			String name = scenarios.get((int) a).getName();
			voteData.put(name, new VoteData(name));
			new NPCEntity(EntityType.SKELETON, "&e&n" + name, new Location(world, 13.5, 6, z)) {
				@Override
				public void onInteract(Player player) {
					if(Ranks.VIP.hasRank(player)) {
						voteData.get(ChatColor.stripColor(getName())).vote(player);
					} else {
						MessageHandler.sendMessage(player, Ranks.VIP.getNoPermission());
					}
				}
			};
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		VoteData winner = null;
		for(VoteData data : voteData.values()) {
			if((winner == null || data.getVotes() > winner.getVotes()) || (data.getVotes() == winner.getVotes() && new Random().nextBoolean())) {
				winner = data;
			}
		}
		if(!(winner.getName().contains("10 Hearts") && winner.getVotes() > 0)) {
			new HeartModifier("Double Health", 40);
		}
		if(winner.getVotes() == 0) {
			return;
		}
		MessageHandler.alert("");
		MessageHandler.alert(Ranks.VIP.getPrefix() + "&amodifier selected: " + winner.getName());
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!Ranks.VIP.hasRank(player)) {
				MessageHandler.sendMessage(player, "Vote for " + Ranks.VIP.getPrefix() + "&xmodifiers: &b/buy");
			}
		}
		MessageHandler.alert("");
		winner.setName(ChatColor.stripColor(winner.getName()));
		for(Scenario scenario : scenarios) {
			if(scenario.getName().equals(winner.getName())) {
				scenario.enable(false);
				break;
			}
		}
	}
}
