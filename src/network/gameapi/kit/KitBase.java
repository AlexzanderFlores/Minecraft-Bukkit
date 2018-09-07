package network.gameapi.kit;

import network.Network;
import network.Network.Plugins;
import network.customevents.player.*;
import network.gameapi.SpectatorHandler;
import network.player.CoinsHandler;
import network.player.MessageHandler;
import network.player.TitleDisplayer;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.servers.hub.items.Features.Rarity;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.UnicodeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public abstract class KitBase implements Listener {
	private static List<KitBase> kits = null;
	private static int lastSlot = -1;
	private String pluginData = null;
	private String kitType = null;
	private String kitSubType = null;
	private ItemStack icon = null;
	private ItemStack helmet = null;
	private ItemStack chestplate = null;
	private ItemStack leggings = null;
	private ItemStack boots = null;
	private List<ItemStack> items = null;
	private int slot = 0;
	private int price = 0;
	private int inventoryLimit = 0;
	private int quantity = 0;
	private List<String> users = null;
	private Map<String, Boolean> unlocked = null;
	private Rarity rarity = null;
	
	public KitBase(Plugins plugin, ItemStack icon, Rarity rarity, int price) {
		this(plugin, icon, rarity, price, -1, -1, -1);
	}

	public KitBase(Plugins plugin, ItemStack icon, Rarity rarity, int price, int slot) {
		this(plugin, icon, rarity, price, slot, -1, 1);
	}

	public KitBase(Plugins plugin, ItemStack icon, Rarity rarity, int price, int slot, int inventoryLimit) {
		this(plugin, icon, rarity, price, slot, inventoryLimit, 1);
	}
	
	public KitBase(Plugins plugin, ItemStack icon, Rarity rarity, int price, int slot, int inventoryLimit, int quantity) {
		if(kits == null) {
			kits = new ArrayList<KitBase>();
		}
		pluginData = plugin.getData();
		kitType = "kit";
		kitSubType = "";
		if(slot > -1) {
			lastSlot = slot;
		} else {
			slot = ++lastSlot;
		}
		items = new ArrayList<ItemStack>();
		this.rarity = rarity;
		this.price = price;
		this.slot = slot;
		ItemMeta meta = icon.getItemMeta();
		icon = new ItemCreator(icon).setName(meta.getDisplayName()).getItemStack();
		this.icon = icon;
		this.inventoryLimit = inventoryLimit;
		this.quantity = quantity;
		users = new ArrayList<String>();
		unlocked = new HashMap<String, Boolean>();
		EventUtil.register(this);
		kits.add(this);
	}

	public List<ItemStack> getItems() {
		return items;
	}

	public KitBase addItem(ItemStack item) {
		items.add(item);
		return this;
	}

	public KitBase addItem(Material material) {
		items.add(new ItemStack(material));
		return this;
	}

	public KitBase setItems(List<ItemStack> items) {
		this.items = items;
		return this;
	}

	public int getInventoryLimit() {
		return inventoryLimit;
	}

	public void setInventoryLimit(int inventoryLimit) {
		this.inventoryLimit = inventoryLimit;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getKitType() {
		return kitType;
	}
	
	public void setKitType(String kitType) {
		this.kitType = kitType;
	}
	
	public String getKitSubType() {
		return kitSubType;
	}
	
	public void setKitSubType(String kitSubType) {
		this.kitSubType = kitSubType;
	}
	
	public ItemStack getHelmet() {
		return helmet;
	}
	
	public KitBase setHelmet(ItemStack helmet) {
		this.helmet = helmet;
		return this;
	}

	public KitBase setHelmet(Material helmet) {
		this.helmet = new ItemStack(helmet);
		return this;
	}
	
	public ItemStack getChestplate() {
		return chestplate;
	}
	
	public KitBase setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
		return this;
	}

	public KitBase setChestplate(Material chestplate) {
		this.chestplate = new ItemStack(chestplate);
		return this;
	}
	
	public ItemStack getLeggings() {
		return leggings;
	}
	
	public KitBase setLeggings(ItemStack leggings) {
		this.leggings = leggings;
		return this;
	}

	public KitBase setLeggings(Material leggings) {
		this.leggings = new ItemStack(leggings);
		return this;
	}
	
	public ItemStack getBoots() {
		return boots;
	}
	
	public KitBase setBoots(ItemStack boots) {
		this.boots = boots;
		return this;
	}

	public KitBase setBoots(Material boots) {
		this.boots = new ItemStack(boots);
		return this;
	}
	
	public boolean owns(Player player) {
		if(!unlocked.containsKey(player.getName())) {
			Ranks rank = AccountHandler.getRank(player);
			if(rank == Ranks.YOUTUBER) {
				unlocked.put(player.getName(), true);
			} else {
				unlocked.put(player.getName(), DB.PLAYERS_KITS.isKeySet(new String [] {"uuid", "kit"}, new String [] {player.getUniqueId().toString(), getPermission()}));
			}
		}
		return unlocked.get(player.getName());
	}

	private boolean purchase(Player player) {
		PlayerKitPurchaseEvent event = new PlayerKitPurchaseEvent(player, this);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			if(CoinsHandler.getCoinsHandler(getPluginData()).getCoins(player) >= getPrice()) {
				if(getPermission() == null) {
					// Temporary item
					if(canGiveTemporaryItem(player)) {
						MessageHandler.sendMessage(player, "Purchased &b" + getName());
						player.getInventory().addItem(new ItemCreator(icon).setAmount(quantity).getItemStack());
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else {
						return false;
					}
				} else {
					// Standard kit
					giveKit(player);
					use(player);
				}
				CoinsHandler.getCoinsHandler(getPluginData()).addCoins(player, getPrice() * -1);
				return true;
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have enough coins for this.");
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				player.closeInventory();
			}
		}
		return false;
	}
	
	public boolean use(Player player) {
		return use(player, false);
	}
	
	public boolean use(Player player, boolean defaultKit) {
		if(owns(player)) {
			PlayerKitSelectEvent event = new PlayerKitSelectEvent(player, this);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				if(getPluginData().equals(Network.getPlugin().getData())) {
					for(KitBase kit : kits) {
						if(kit.getKitType().equals(getKitType())) {
							kit.remove(player);
						}
					}
					users.add(player.getName());
				}
				if(!defaultKit) {
					DefaultKit.setDefaultKit(player, this);
				}
				MessageHandler.sendMessage(player, "Selected &b" + getName());
				EffectUtil.playSound(player, Sound.LEVEL_UP);
				execute(player);
				player.closeInventory();
				return true;
			}
		} else if(price > 0) {
			return purchase(player);
		} else {
			MessageHandler.sendMessage(player, "&cUnlock this in crates &a/vote");
			EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			player.closeInventory();
		}
		return false;
	}
	
	public void giveKit(Player player) {
		if(owns(player)) {
			MessageHandler.sendMessage(player, "&cYou already own &e" + getName());
			new TitleDisplayer(player, "&cYou already own", "&e" + getName()).display();
		} else {
			UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.PLAYERS_KITS.insert("'" + uuid.toString() + "', '" + getPermission() + "'");
				}
			});
			unlocked.put(player.getName(), true);
			MessageHandler.sendMessage(player, "You unlocked &b" + getName());
			new TitleDisplayer(player, "&bYou unlocked", "&e" + getName()).display();
			Bukkit.getPluginManager().callEvent(new PlayerPostKitPurchaseEvent(player, this));
		}
	}

	private boolean canGiveTemporaryItem(Player player) {
		int count = 0;
		for(ItemStack itemStack : player.getInventory().getContents()) {
			if(itemStack != null && itemStack.getType() == icon.getType()) {
				count += itemStack.getAmount();
			}
		}
		boolean canGive = inventoryLimit == -1 || count + quantity <= inventoryLimit;
		if(canGive) {
			return true;
		}

		MessageHandler.sendMessage(player, "&cYou cannot have more than " + inventoryLimit + " of these at a time.");
		player.closeInventory();
		return false;
	}
	
	public String getPluginData() {
		return pluginData;
	}
	
	public String getName() {
		return ChatColor.stripColor(getIcon().getItemMeta().getDisplayName());
	}
	
	public ItemStack getIcon() {
		return icon.clone();
	}
	
	public ItemStack getIcon(Player player) {
		boolean singleItem = getPermission() == null;
		boolean owns = singleItem || owns(player);
		String name = (owns ? "&b" : "&c") + getName();
		String lore = "";

		if(!singleItem) {
			name += " " + (owns ? "&a" + UnicodeUtil.getUnicode("2714") : "&4" + UnicodeUtil.getUnicode("2716"));
			lore = "&7Status: " + (owns ? "&aUnlocked" : "&cLocked");
		}

		ItemStack item = getIcon();
		if(owns) {
			return new ItemCreator(item).setName(name).addLore(lore).getItemStack();
		} else {
			ItemCreator itemCreator = new ItemCreator(Material.INK_SACK, 8).setName(name)
					.setLores(item.getItemMeta().getLore()).addLore(lore);
			if(!lore.equals("")) {
				itemCreator.addLore("");
			}
			return itemCreator.getItemStack();
		}
	}
	
	public Rarity getKitRarity() {
		return rarity;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public int getPrice() {
		return price;
	}
	
	public boolean has(Player player) {
		return users != null && users.contains(player.getName()) && !SpectatorHandler.contains(player);
	}
	
	public void remove(Player player) {
		if(has(player)) {
			users.remove(player.getName());
		}
	}
	
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(has(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static List<KitBase> getKits() {
		return kits;
	}
	
	public static int getLastSlot() {
		return lastSlot;
	}
	
	/*public void executeArt(ArmorStand armorStand, boolean all, Player player) {
		armorStand.setHelmet(getHelmet());
		armorStand.setChestplate(getChestplate());
		armorStand.setLeggings(getLeggings());
		armorStand.setBoots(getBoots());
		armorStand.setItemInHand(getIcon());
	}
	
	public void disableArt(ArmorStand armorStand) {
		armorStand.setHelmet(new ItemStack(Material.AIR));
		armorStand.setChestplate(new ItemStack(Material.AIR));
		armorStand.setLeggings(new ItemStack(Material.AIR));
		armorStand.setBoots(new ItemStack(Material.AIR));
		armorStand.setItemInHand(new ItemStack(Material.AIR));
	}*/

	public void execute() {
		for(Player player : getPlayers()) {
			execute(player);
		}
	}

	public void execute(Player player) {
		Inventory inventory = player.getInventory();
		for(int a = 0; a < inventory.getSize(); ++a) {
			ItemStack item = inventory.getItem(a);
			if(item != null) {
				List<String> lores = item.getItemMeta().getLore();
				if(lores != null && !lores.isEmpty() && lores.get(0).equals("Default Item")) {
					inventory.setItem(a, null);
				}
			}
		}

		for(ItemStack item : getItems()) {
			ItemMeta meta = item.getItemMeta();
			meta.setLore(Arrays.asList("Default Item"));
			item.setItemMeta(meta);

			player.getInventory().addItem(item);
		}
		player.getInventory().setArmorContents(new ItemStack [] {
				getBoots(),
				getLeggings(),
				getChestplate(),
				getHelmet()
		});
	}

	public abstract String getPermission();
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(Network.getPlugin() == Plugins.HUB) {
			AsyncPlayerJoinEvent.getHandlerList().unregister(this);
		} else {
			owns(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		users.remove(event.getPlayer().getName());
		unlocked.remove(event.getPlayer().getName());
	}
}
