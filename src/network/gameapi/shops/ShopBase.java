package network.gameapi.shops;

import de.inventivegames.hologram.Hologram;
import network.Network;
import network.Network.Plugins;
import network.customevents.TimeEvent;
import network.customevents.player.*;
import network.gameapi.kit.KitBase;
import network.player.CoinsHandler;
import network.server.CommandBase;
import network.server.DB;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ShopBase implements Listener {
	private String name = null;
	private String permission = null;
	private Plugins plugin = null;
	protected Map<String, Integer> pages = null;
	private int maxPages = 1;
	private int coinsSlot = 0;
	private ItemStack itemStack = null;
	private List<Hologram> holograms = null;
	private String [] colors = null;
	private int counter = 0;
	private int [] slots = null;
	
	public class KitData {
		private String title = null;
		private int total = 0;
		private int owned = 0;
		private int percentage = 0;
		
		public KitData(Player player, String title, String kitType) {
			this(player, title, kitType, "");
		}
		
		public KitData(Player player, String title, String kitType, String kitSubType) {
			this.title = title;
			total = 0;
			owned = 0;
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPluginData().equals(plugin.getData()) && kit.getKitType().equals(kitType) && kit.getKitSubType().equals(kitSubType)) {
					++total;
					if(kit.owns(player)) {
						++owned;
					}
				}
			}
			percentage = (int) (owned * 100.0 / total + 0.5);
		}
		
		public ItemStack getItem() {
			return new ItemCreator(Material.DIAMOND).setName("&7" + title + ": &e" + owned + "&8/&e" + total + " &7(&e" + percentage + "%&7)").getItemStack();
		}
	}
	
	public ShopBase(String name, String permission, DB table, Plugins plugin, int maxPages, int coinsSlot) {
		this.name = name;
		this.permission = permission;
		new CoinsHandler(table, plugin.getData());
		this.plugin = plugin;
		this.maxPages = maxPages;
		this.coinsSlot = coinsSlot;
		pages = new HashMap<String, Integer>();
		itemStack = new ItemCreator(Material.CHEST).setName("&aShop").setGlow(true).getItemStack();
		slots = new int [] {3, 5};
		if(Network.getMiniGame() != null) {
			holograms = new ArrayList<Hologram>();
			colors = new String [] {
				ChatColor.YELLOW + "",
				ChatColor.RED + "",
				ChatColor.GREEN + "",
			};
			/*World lobby = OSTB.getMiniGame().getLobby();
			holograms._add(new Hologram(new Location(lobby, 11, 5, 0, 90.0f, 0.0f), "") {
				@Override
				public void interact(Player player) {
					hologramClick(player);
				}
			});
			holograms._add(new Hologram(new Location(lobby, -11, 5, 0, 270.0f, 0.0f), "") {
				@Override
				public void interact(Player player) {
					hologramClick(player);
				}
			});
			String bottomText = "&bClick the Chest";
			new Hologram(new Location(lobby, 11, 4.65, 0, 90.0f, 0.0f), bottomText) {
				@Override
				public void interact(Player player) {
					hologramClick(player);
				}
			}.getArmorStand().getEquipment().setHelmet(new ItemCreator(Material.CHEST).setGlow(true).getItemStack());
			new Hologram(new Location(lobby, -11, 4.65, 0, 270.0f, 0.0f), bottomText) {
				@Override
				public void interact(Player player) {
					hologramClick(player);
				}
			}.getArmorStand().getEquipment().setHelmet(new ItemCreator(Material.CHEST).setGlow(true).getItemStack());*/
			new CommandBase("shop", true) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					Player player = (Player) sender;
					openShop(player);
					return true;
				}
			};
		}
		EventUtil.register(this);
	}
	
	/*private void hologramClick(Player player) {
		EffectUtil.playSound(player, Sound.CHEST_OPEN);
		openShop(player);
	}*/
	
	public String getName() {
		return name;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public int getPage(Player player) {
		if(!pages.containsKey(player.getName())) {
			pages.put(player.getName(), 1);
		}
		return pages.get(player.getName());
	}
	
	public void updateCoinsItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			updateCoinsItem(player, player.getOpenInventory().getTopInventory());
//			player.getOpenInventory().setItem(player.getOpenInventory().getTopInventory().getSize() - 4, CoinsHandler.getCoinsHandler(plugin.getData()).getItemStack(player));
		}
	}
	
	public void updateCoinsItem(Player player, Inventory inventory) {
		inventory.setItem(coinsSlot, CoinsHandler.getCoinsHandler(plugin.getData()).getItemStack(player));
	}
	
	public void openShop(Player player) {
		openShop(player, getPage(player));
	}
	
	protected boolean hasCrate(Player player, InventoryView view) {
		return view.getTitle().equals(getName()) && view.getItem(4).getType() != Material.AIR;
	}
	
	protected void setBackItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page > 1) {
			inventory.setItem(inventory.getSize() - 8, new ItemCreator(Material.ARROW).setName("&bPage #" + (page - 1)).getItemStack());
		}
	}
	
	protected void setNextItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page < maxPages) {
			inventory.setItem(inventory.getSize() - 2, new ItemCreator(Material.ARROW).setName("&bPage #" + (page + 1)).getItemStack());
		}
	}
	
	protected void updateItems(Player player, Inventory inventory) {
		setBackItem(player, inventory);
		setNextItem(player, inventory);
		updateInfoItem(player, inventory);
		if(Network.getPlugin() == Plugins.HUB) {
			inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
		}
		updateCoinsItem(player, inventory);
	}
	
	public void openShop(Player player, int page) {
		updateItems(player, player.getOpenInventory().getTopInventory());
	}

	public abstract void updateInfoItem(Player player);
	public abstract void updateInfoItem(Player player, Inventory inventory);
	public abstract void onInventoryItemClick(InventoryItemClickEvent event);
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 5) {
			if(holograms != null) {
				String topText = "Kits / Shop";
				for(Hologram hologram : holograms) {
					hologram.setText(colors[counter] + "" + topText);
				}
				if(++counter >= colors.length) {
					counter = 0;
				}
			}
//			for(Player player : Bukkit.getOnlinePlayers()) {
//				InventoryView view = player.getOpenInventory();
//				if(view != null && view.getTitle().startsWith("Shop - ")) {
//					for(int slot : slots) {
//						ItemStack item = view.getItem(slot);
//						if(item == null || item.getType() == Material.AIR) {
//							view.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, 14).setName(" ").setGlow(true).getItemStack());
//						} else {
//							view.setItem(slot, new ItemStack(Material.AIR));
//						}
//					}
//				}
//			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(Network.getMiniGame() != null && Network.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			player.getInventory().remove(itemStack);
			player.getInventory().addItem(itemStack);
		} else {
			PlayerJoinEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(Network.getMiniGame() != null && Network.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			if(player.getItemInHand().equals(itemStack)) {
				EffectUtil.playSound(player, Sound.CHEST_OPEN);
				openShop(player);
			}
		} else {
			MouseClickEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onPlayerPostKitPurchase(PlayerPostKitPurchaseEvent event) {
		Player player = event.getPlayer();
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			player.getOpenInventory().setItem(event.getKit().getSlot(), event.getKit().getIcon(player));
		}
	}
	
	@EventHandler
	public void onCoinUpdate(CoinUpdateEvent event) {
		updateCoinsItem(event.getPlayer());
		updateInfoItem(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		pages.remove(event.getPlayer().getName());
	}
}
