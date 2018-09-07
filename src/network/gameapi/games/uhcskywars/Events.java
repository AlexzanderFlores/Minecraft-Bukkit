package network.gameapi.games.uhcskywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import network.Network;
import network.ProPlugin;
import network.customevents.game.GameStartEvent;
import network.customevents.game.GameStartingEvent;
import network.customevents.game.PostGameStartEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.PlayerOpenNewChestEvent;
import network.gameapi.MiniGame;
import network.gameapi.SpawnPointHandler;
import network.gameapi.games.uhcskywars.cages.Cage;
import network.gameapi.games.uhcskywars.cages.SmallCage;
import network.gameapi.kit.KitBase;
import network.gameapi.mapeffects.MapEffectHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;

public class Events implements Listener {
	private Map<Team, Location> teamSpawns = null;
	
	public Events() {
		teamSpawns = new HashMap<Team, Location>();
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameStarting(GameStartingEvent event) {
		World world = Network.getMiniGame().getMap();
		world.setGameRuleValue("naturalRegeneration", "false");
		new MapEffectHandler(world);
		SpawnPointHandler spawnPointHandler = new SpawnPointHandler(world);
		List<Location> spawns = spawnPointHandler.getSpawns();
		List<Team> spawnedCages = new ArrayList<Team>();
		List<Player> players = ProPlugin.getPlayers();
		int counter = 0;
		int numberOfSpawns = spawns.size();
		List<Team> teams = TeamHandler.getTeams();
		if(teams != null) {
			for(Team team : teams) {
				if(!teamSpawns.containsKey(team)) {
					teamSpawns.put(team, spawns.get(counter++));
				}
			}
		}
		for(Player player : players) {
			if(counter >= numberOfSpawns) {
				counter = 0;
			}
			Location location = spawns.get(counter++);
			Team team = TeamHandler.getTeam(player);
			if(team != null && teamSpawns.containsKey(team)) {
				location = teamSpawns.get(team);
			}
			player.teleport(location);
			if(team == null || !spawnedCages.contains(team)) {
				boolean usedCage = false;
				for(KitBase kit : KitBase.getKits()) {
					if(kit.has(player) && kit.getKitType().equals("cage")) {
						Bukkit.getLogger().info(kit.getName() + " used");
						kit.execute(player);
						usedCage = true;
						break;
					}
				}
				if(!usedCage) {
					new SmallCage(new ItemCreator(Material.GLASS).setName("DefaultKit Cage").setLores(new String [] {}).getItemStack(), 0).execute(player);
				}
				if(team != null && !spawnedCages.contains(team)) {
					spawnedCages.add(team);
				}
			}
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(Cage.getCages() != null) {
			for(Cage cage : Cage.getCages()) {
				cage.remove();
			}
			Cage.getCages().clear();
		}
		MiniGame miniGame = Network.getMiniGame();
		miniGame.setAllowFoodLevelChange(true);
		miniGame.setAllowDroppingItems(true);
		miniGame.setAllowPickingUpItems(true);
		miniGame.setDropItemsOnLeave(true);
		miniGame.setAllowBuilding(true);
		miniGame.setAllowEntityCombusting(true);
		miniGame.setAllowPlayerInteraction(true);
		miniGame.setAllowBowShooting(true);
		miniGame.setAllowInventoryClicking(true);
		miniGame.setAllowItemSpawning(true);
		miniGame.setFlintAndSteelUses(4);
		miniGame.setCounter(60 * 8);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MiniGame miniGame = Network.getMiniGame();
				miniGame.setAllowEntityDamageByEntities(true);
				miniGame.setAllowEntityDamage(true);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onPostGameStart(PostGameStartEvent event) {
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getKitType().equals("kit")) {
				kit.execute();
			}
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		String type = event.getBlock().getType().toString();
		if(type.contains("LAVA") || type.contains("WATER")) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (byte) 4));
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.AIR));
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getItem().getType() == Material.INK_SACK) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Player killer = player.getKiller();
		if(killer != null) {
			event.setRespawnLocation(killer.getLocation());
		}
	}
	
	@EventHandler
	public void onPlayerOpenNewChest(PlayerOpenNewChestEvent event) {
		Chest chest = event.getChest();
		Inventory inventory = chest.getInventory();
		Random random = new Random();
		int slot = 0;
		do {
			slot = random.nextInt(inventory.getSize());
		} while(inventory.getItem(slot) != null);
		if(random.nextBoolean()) {
			inventory.setItem(slot, new ItemStack(Material.APPLE, 2));
		} else {
			inventory.setItem(slot, new ItemStack(Material.GOLD_INGOT, 8));
		}
	}
}