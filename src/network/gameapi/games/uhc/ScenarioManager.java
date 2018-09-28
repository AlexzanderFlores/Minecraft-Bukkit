package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.gameapi.uhc.scenarios.ScenarioStateChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.uhc.scenarios.Scenario;
import network.gameapi.uhc.scenarios.scenarios.Barebones;
import network.gameapi.uhc.scenarios.scenarios.CutClean;
import network.gameapi.uhc.scenarios.scenarios.OrePower;
import network.gameapi.uhc.scenarios.scenarios.TimeBomb;
import network.gameapi.uhc.scenarios.scenarios.TripleOres;
import network.gameapi.uhc.scenarios.scenarios.TrueLove;
import network.gameapi.uhc.scenarios.scenarios.Vanilla;
import network.player.MessageHandler;
import network.server.CommandBase;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class ScenarioManager implements Listener {
	private static Map<Integer, Scenario> scenarios = null;
	private ItemStack item = null;
	private static String name = null;
	
	public ScenarioManager() {
		scenarios = new HashMap<Integer, Scenario>();
		scenarios.put(10, Vanilla.getInstance());
		scenarios.put(11, CutClean.getInstance());
		scenarios.put(12, TripleOres.getInstance());
//		scenarios.put(13, DoubleOrNothing.getInstance());
		scenarios.put(14, Barebones.getInstance());
		scenarios.put(15, TimeBomb.getInstance());
//		scenarios.put(16, BestPVE.getInstance());
		scenarios.put(28, TrueLove.getInstance());
		resetScenarios();
		name = "Scenario Manager";
		item = new ItemCreator(Material.EYE_OF_ENDER).setName("&a" + name).getItemStack();
		new CommandBase("scenarios", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(!open(player)) {
					MessageHandler.sendUnknownCommand(sender);
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static List<Scenario> getActiveScenarios() {
		List<Scenario> scenarios2 = new ArrayList<Scenario>();
		for(Scenario scenario : scenarios.values()) {
			if(scenario.isEnabled()) {
				scenarios2.add(scenario);
			}
		}
		return scenarios2;
	}
	
	private List<Scenario> getScenarios() {
		List<Scenario> scenarios2 = new ArrayList<Scenario>();
		for(Scenario scenario : scenarios.values()) {
			scenarios2.add(scenario);
		}
		return scenarios2;
	}
	
	private void resetScenarios() {
		for(Scenario scenario : getScenarios()) {
			scenario.disable(false);
		}
		Vanilla.getInstance().enable(false);
	}
	
	public static boolean open(Player player) {
		if(HostHandler.isHost(player.getUniqueId())) {
			if(HostHandler.getMainHost() == null) {
				MessageHandler.sendMessage(player, "&cA main host has not been set yet! &f/host");
			} else {
				if(WorldHandler.isPreGenerated()) {
					ItemStack reset = new ItemCreator(Material.EYE_OF_ENDER).setName("&aReset Scenarios").getItemStack();
					ItemStack enabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData()).setName("&aENABLED").addLore("&fClick the icon above to toggle").getItemStack();
					ItemStack disabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setName("&cDISABLED").addLore("&fClick the icon above to toggle").getItemStack();
					Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
					for(int a : scenarios.keySet()) {
						Scenario scenario = scenarios.get(a);
						inventory.setItem(a, scenario.getItem());
						if(scenario.isEnabled()) {
							inventory.setItem(a + 9, enabled);
						} else {
							inventory.setItem(a + 9, disabled);
						}
					}
					for(int a : new int [] {0, 8, 45}) {
						inventory.setItem(a, reset);
					}
					inventory.setItem(inventory.getSize() - 1, new ItemCreator(Material.ARROW).setName("&eMove to Team Selection").getItemStack());
					player.openInventory(inventory);
				} else {
					MessageHandler.sendMessage(player, "&cCannot open the Scenario Manager: &eWorld is not pregenerated!");
				}
			}
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(WhitelistHandler.isWhitelisted() && HostHandler.isHost(player.getUniqueId()) && Network.getMiniGame().getGameState() != GameStates.STARTED) {
			player.getInventory().setItem(0, item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), item)) {
			open(player);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			Material type = item.getType();
			if(type == Material.EYE_OF_ENDER) {
				resetScenarios();
				open(player);
			} else if(type == Material.STAINED_GLASS_PANE) {
				open(player);
			} else if(type == Material.ARROW) {
				TeamHandler.open(player);
			} else {
				Scenario scenario = null;
				for(Scenario scenario2 : getScenarios()) {
					if(scenario2.getItem().equals(item)) {
						scenario = scenario2;
						break;
					}
				}
				if(scenario != null) {
					if(scenario.isEnabled()) {
						scenario.disable(false);
					} else {
						scenario.enable(false);
					}
				}
				open(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onScenarioStateChange(ScenarioStateChangeEvent event) {
		Scenario scenario = event.getScenario();
		List<Scenario> scenarios = getScenarios();
		// Disable all other scenarios if Vanilla is being enabled
		if(scenario instanceof Vanilla) {
			if(event.isEnabling()) {
				for(Scenario scenario2 : scenarios) {
					if(!(scenario2 instanceof Vanilla)) {
						scenario2.disable(true);
					}
				}
			}
		} else {
			// If any other scenarios are being enabled we want to disable vanilla
			Vanilla.getInstance().disable(true);
			// Be sure that Cut Clean is always enabled with Triple Ores
			if(scenario instanceof TripleOres) {
				CutClean.getInstance().enable(true);
			} else if(scenario instanceof CutClean && !event.isEnabling()) {
				TripleOres.getInstance().disable(true);
				Barebones.getInstance().disable(true);
			}
			// Be sure that Ore Power and Bare Bones are never enabled at the same time
			if(scenario instanceof Barebones && event.isEnabling()) {
				CutClean.getInstance().enable(true);
				OrePower.getInstance().disable(true);
			}
			if(scenario instanceof OrePower && event.isEnabling()) {
				Barebones.getInstance().disable(true);
			}
		}
		// Make sure at least vanilla is enabled
		boolean anyEnabled = false;
		for(Scenario scenario2 : scenarios) {
			if(scenario2.isEnabled()) {
				anyEnabled = true;
				break;
			}
		}
		if(!anyEnabled) {
			Vanilla.getInstance().enable(true);
		}
	}
}
