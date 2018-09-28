package network.gameapi.games.uhc;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import network.Network;
import network.customevents.game.GameStartEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.games.uhc.border.BorderHandler;
import network.player.MessageHandler;
import network.server.BiomeSwap;
import network.server.util.EventUtil;
import network.server.util.FileHandler;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class WorldHandler implements Listener {
	private static String name = null;
	private static String pregenName = null;
	private static ItemStack item = null;
	private static ItemStack pregenItem = null;
	private static World world = null;
	private static World nether = null;
	private static World end = null;
	private static boolean preGenerated = false;
	
	public WorldHandler() {
		name = "World Options";
		pregenName = "Pregen Options";
		item = new ItemCreator(Material.GRASS).setName("&a" + name).getItemStack();
		pregenItem = new ItemCreator(Material.GRASS).setName("&a" + pregenName).getItemStack();
		BiomeSwap.setUpUHC();
		if(!BorderHandler.isEnabled()) {
			new BorderHandler();
		}
		generateWorld();
		EventUtil.register(this);
	}
	
	public static void generateWorld() {
		MessageHandler.alert("Generating World...");
		if(world != null) {
			MessageHandler.alert("Deleting old World...");
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(player.getWorld().getName().equals(world.getName())) {
					player.teleport(new Location(Network.getMiniGame().getLobby(), 0.5, 26, 0.5, -90.0f, -0.0f));
				}
			}
			Bukkit.unloadWorld(world, false);
			FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world.getName()));
			MessageHandler.alert("Deleting old World... Complete!");
		}
		world = Bukkit.createWorld(new WorldCreator("world"));
		world.setSpawnLocation(0, getGround(new Location(world, 0, 0, 0)).getBlockY(), 0);
		world.setGameRuleValue("naturalRegeneration", "false");
		world.setDifficulty(Difficulty.HARD);
		MessageHandler.alert("Generating World... Complete!");
		BorderHandler.setOverworldBorder();
	}
	
	public static void generateNether() {
		MessageHandler.alert("Generating Nether...");
		if(nether != null) {
			MessageHandler.alert("Deleting old Nether...");
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(player.getWorld().getName().equals(nether.getName())) {
					player.teleport(new Location(Network.getMiniGame().getLobby(), 0.5, 26, 0.5, -90.0f, -0.0f));
				}
			}
			Bukkit.unloadWorld(nether, false);
			FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + nether.getName()));
			MessageHandler.alert("Deleting old Nether... Complete!");
		}
		WorldCreator worldCreator = new WorldCreator("world_nether");
		worldCreator.environment(Environment.NETHER);
		nether = Bukkit.createWorld(worldCreator);
		nether.setGameRuleValue("naturalRegeneration", "false");
		nether.setDifficulty(Difficulty.HARD);
		MessageHandler.alert("Generating Nether... Complete!");
		BorderHandler.setNetherBorder();
		BorderHandler.getNetherBorder().pregenSettings();
	}
	
	public static void generateEnd() {
		MessageHandler.alert("Generating End...");
		if(end != null) {
			MessageHandler.alert("Deleting old End...");
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(player.getWorld().getName().equals(end.getName())) {
					player.teleport(new Location(Network.getMiniGame().getLobby(), 0.5, 26, 0.5, -90.0f, -0.0f));
				}
			}
			Bukkit.unloadWorld(end, false);
			FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + end.getName()));
			MessageHandler.alert("Deleting old End... Complete!");
		}
		WorldCreator worldCreator = new WorldCreator("world_end");
		worldCreator.environment(Environment.THE_END);
		end = Bukkit.createWorld(worldCreator);
		end.setGameRuleValue("naturalRegeneration", "false");
		end.setDifficulty(Difficulty.HARD);
		MessageHandler.alert("Generating End... Complete!");
	}
	
	public static boolean isPreGenerated() {
		return preGenerated;
	}
	
	public static Location getGround(Location location) {
		location.setY(250);
        while(location.getBlock().getType() == Material.AIR) {
        	location.setY(location.getBlockY() - 1);
        }
        return location.add(0, 1, 0);
	}
	
	public static World getWorld() {
		return world;
	}
	
	public static World getNether() {
		return nether;
	}
	
	public static World getEnd() {
		return end;
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		BorderHandler.registerEvents();
		BorderHandler.getOverworldBorder().pregenSettings();
		if(BorderHandler.getNetherBorder() != null) {
			BorderHandler.getNetherBorder().pregenSettings();
		}
		if(OptionsHandler.getEnd() || OptionsHandler.getNether()) {
			new PortalHandler();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(WhitelistHandler.isWhitelisted() && Network.getMiniGame().getGameState() != GameStates.STARTED) {
			Player player = event.getPlayer();
			if(HostHandler.isHost(player.getUniqueId())) {
				player.getInventory().setItem(1, item);
				player.getInventory().setItem(2, pregenItem);
			}
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), item)) {
			Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
			inventory.setItem(9, new ItemCreator(Material.GRASS).setName("&aTeleport to &eLobby").getItemStack());
			inventory.setItem(11, new ItemCreator(Material.GRASS).setName("&aTeleport to &eWorld").getItemStack());
			inventory.setItem(20, new ItemCreator(Material.LOG).setName("&cRemove &atrees near 0, 0").getItemStack());
			inventory.setItem(13, new ItemCreator(Material.NETHERRACK).setName("&aTeleport to &cNether").getItemStack());
			inventory.setItem(15, new ItemCreator(Material.GRASS).setName("&aRemake &eWorld").getItemStack());
			inventory.setItem(17, new ItemCreator(Material.NETHERRACK).setName("&aRemake &cNether").getItemStack());
			inventory.setItem(39, new ItemCreator(Material.ENDER_STONE).setName("&aTeleport to &fEnd").getItemStack());
			inventory.setItem(41, new ItemCreator(Material.ENDER_STONE).setName("&aRemake &fEnd").getItemStack());
			player.openInventory(inventory);
			event.setCancelled(true);
		} else if(ItemUtil.isItem(player.getItemInHand(), pregenItem)) {
			Inventory inventory = Bukkit.createInventory(player, 9 * 3, pregenName);
			inventory.setItem(11, new ItemCreator(Material.GRASS).setName("&aPregen &eWorld").getItemStack());
			inventory.setItem(15, new ItemCreator(Material.BEDROCK).setName("&cCancel Pregen").getItemStack());
			player.openInventory(inventory);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			String name = ChatColor.stripColor(event.getItemTitle());
			player.closeInventory();
			if(name.contains("Teleport to Lobby")) {
				player.teleport(Network.getMiniGame().getLobby().getSpawnLocation());
				player.setAllowFlight(true);
				player.setFlying(true);
			} else if(name.contains("Teleport to World")) {
				player.teleport(getWorld().getSpawnLocation());
				player.setAllowFlight(true);
				player.setFlying(true);
				player.getWorld().setTime(0);
			} else if(name.contains("Remove trees near 0, 0")) {
				MessageHandler.alert("Removing trees...");
				World world = getWorld();
				Location location = world.getSpawnLocation();
				int max = 100;
				for(int x = -location.getBlockX() - max; x <= location.getBlockX() + max; ++x) {
					for(int y = 55; y <= 150; ++y) {
						for(int z = -location.getBlockZ() - max; z <= location.getBlockZ() + max; ++z) {
							Block block = world.getBlockAt(x, y, z);
							Material type = block.getType();
							if(type == Material.LOG || type == Material.LOG_2 || type == Material.LEAVES || type == Material.LEAVES_2) {
								block.setType(Material.AIR);
								block.setData((byte) 0);
							}
						}
					}
				}
				MessageHandler.alert("Removed trees!");
			} else if(name.contains("Teleport to Nether")) {
				if(getNether() == null) {
					MessageHandler.sendMessage(player, "&cThe nether world is not available currently");
				} else {
					player.teleport(getNether().getSpawnLocation());
					player.setAllowFlight(true);
					player.setFlying(true);
				}
			} else if(name.contains("Teleport to End")) {
				if(getEnd() == null) {
					MessageHandler.sendMessage(player, "&cThe end world is not available currently");
				} else {
					player.teleport(getEnd().getSpawnLocation());
					player.setAllowFlight(true);
					player.setFlying(true);
				}
			} else if(name.contains("Remake World")) {
				generateWorld();
			} else if(name.contains("Remake Nether")) {
				if(getNether() == null) {
					MessageHandler.sendMessage(player, "&cThe nether world is not available currently");
				} else {
					generateNether();
				}
			} else if(name.contains("Remake End")) {
				if(getEnd() == null) {
					MessageHandler.sendMessage(player, "&cThe nether world is not available currently");
				} else {
					generateEnd();
				}
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals(pregenName)) {
			Player player = event.getPlayer();
			String perm = "worldborder.*";
			PermissionAttachment permission = player.addAttachment(Network.getInstance());
			permission.setPermission(perm, true);
			Material type = event.getItem().getType();
			if(type == Material.BEDROCK) {
				player.chat("/wb fill cancel");
			} if(type == Material.GRASS) {
				int border = BorderHandler.getOverworldBorder().getRadius();
				player.chat("/wb " + getWorld().getName() + " set " + border + " " + border + " 0 0");
				player.chat("/wb " + getWorld().getName() + " fill 60");
				player.chat("/wb fill confirm");
				BorderHandler.getOverworldBorder().pregenSettings();
				BorderHandler.registerCommands();
			}
			permission.unsetPermission(perm);
			permission.remove();
			permission = null;
			preGenerated = true;
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
