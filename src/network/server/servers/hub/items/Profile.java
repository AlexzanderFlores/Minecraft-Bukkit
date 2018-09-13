package network.server.servers.hub.items;

import java.util.Calendar;
import java.util.UUID;

import network.server.servers.hub.crate.CrateTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.player.LevelHandler;
import network.player.account.AccountHandler;
import network.player.account.PlaytimeTracker;
import network.player.account.AccountHandler.Ranks;
import network.player.account.PlaytimeTracker.TimeType;
import network.server.DB;
import network.server.servers.hub.HubItemBase;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;

public class Profile extends HubItemBase {
	private static String itemName = null;
	private static ItemStack finalItem = null;
	
	public Profile() {
		super(new ItemCreator(Material.SKULL_ITEM, 3).setName("&eProfile"), 7);
		Profile.itemName = this.getName();
		finalItem = getItem();
		//new Settings();
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(isItem(event.getPlayer())) {
			open(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith(ChatColor.stripColor(getName()))) {
			Player player = event.getPlayer();
			String title = event.getTitle();
			Material type = event.getItem().getType();
			if(type == Material.EXP_BOTTLE || type == Material.SKULL_ITEM || type == Material.WATCH || type == Material.CHEST || type == Material.NAME_TAG) {
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else if(!title.contains(" - " + player.getName())) {
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else {
				//Settings.open(player);
			}
			event.setCancelled(true);
		}
	}
	
	@Override
	public void giveItem(Player player) {
		player.getInventory().setItem(getSlot(), ItemUtil.getSkull(player.getName(), getItem().clone()));
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof Player && event.getPlayer().isSneaking()) {
			Player player = (Player) event.getRightClicked();
			open(event.getPlayer(), player.getName());
		}
	}
	
	private static String getItemName() {
		return itemName;
	}
	
	public static ItemStack getItem(Player player) {
		return ItemUtil.getSkull(player.getName(), finalItem);
	}
	
	public static void open(Player player) {
		open(player, player.getName());
	}
	
	public static void open(final Player player, final String targetName) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID targetUUID = null;
				if(player.getName().equals(targetName)) {
					targetUUID = player.getUniqueId();
				} else {
					targetUUID = AccountHandler.getUUID(targetName);
				}
				String uuid = targetUUID.toString();
				int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
				int month = Calendar.getInstance().get(Calendar.MONTH);
				Inventory inventory = Bukkit.createInventory(player, 9 * 5, ChatColor.stripColor(getItemName() + " - " + targetName));
				inventory.setItem(10, new ItemCreator(Material.EXP_BOTTLE).setName("&bLevel Information").setLores(new String [] {
					"",
					"&7Current level: &e" + LevelHandler.getLevel(targetName),
					"&7Progress: &e" + LevelHandler.getExp(targetName) + "&8/&e" + LevelHandler.getNeededForLevelUp(targetName) + " &8(&e" + LevelHandler.getPercentageDone(targetName) + "%&8)",
					""
				}).getItemStack());
				Ranks rank = AccountHandler.getRank(targetUUID);
				inventory.setItem(12, new ItemCreator(ItemUtil.getSkull(targetName)).setName("&bAccount Information").setLores(new String [] {
					"",
					"&7First joined: &e" + DB.PLAYERS_ACCOUNTS.getString("uuid", uuid, "join_time"),
					"&7Current rank: " + (rank == Ranks.PLAYER ? Ranks.PLAYER.getColor() + "[Player]" : rank.getPrefix()),
					""
				}).getItemStack());
				try {
					inventory.setItem(14, new ItemCreator(Material.WATCH).setName("&bPlaytime").setLores(new String [] {
						"",
						"&7Lifetime: &e" + PlaytimeTracker.getPlayTime(targetName).getDisplay(TimeType.LIFETIME),
						"&7Monthly: &e" + PlaytimeTracker.getPlayTime(targetName).getDisplay(TimeType.MONTHLY),
						"&7Weekly: &e" + PlaytimeTracker.getPlayTime(targetName).getDisplay(TimeType.WEEKLY),
						""
					}).getItemStack());
				} catch(NullPointerException e) {
					inventory.setItem(14, new ItemCreator(Material.WATCH).setName("&bPlaytime").setLores(new String [] {
						"",
						"&7Lifetime: &4N/A &7(Reopen profile item)",
						"&7Monthly: &4N/A &7(Reopen profile item)",
						"&7Weekly: &4N/A &7(Reopen profile item)",
						""
					}).getItemStack());
				}
				inventory.setItem(16, new ItemCreator(Material.EMERALD).setName("&bAchievements").setLores(new String [] {
					"",
					"&7Click to view your achievements",
					""
				}).getItemStack());
				inventory.setItem(28, new ItemCreator(Material.DIAMOND_SWORD).setName("&bGame Stats").setLores(new String [] {
					"",
					"&cUnder development",
					"",
					"&7&mClick to view your game stats",
					"&7&mor use &c&m/stats &7&min a game's hub"
				}).getItemStack());
				inventory.setItem(30, new ItemCreator(Material.REDSTONE_COMPARATOR).setName("&bSettings").setLores(new String [] {
					"",
					"&cUnder development",
					"",
					"&7&mClick to view your settings",
					""
				}).getItemStack());
				String [] keys = new String [] {"uuid", "type"};
				String [] values = new String [] {player.getUniqueId().toString(), CrateTypes.VOTING.getName()};
				inventory.setItem(32, new ItemCreator(Material.CHEST).setName("&bCrate Stats").setLores(new String [] {
					"",
					"&7" + CrateTypes.VOTING.getDisplay() + " Crate Keys owned: &e" + DB.HUB_CRATE_KEYS.getInt(keys, values, "amount"),
					"&7Key Fragments owned: &e" + DB.PLAYERS_KEY_FRAGMENTS.getInt("uuid", uuid, "amount"),
					"",
					"&7" + CrateTypes.VOTING.getDisplay() + " Crates opened lifetime: &e" + DB.HUB_LIFETIME_CRATES_OPENED.getInt(keys, values, "amount"),
					"&7" + CrateTypes.VOTING.getDisplay() + " Crates opened this month: &e" + DB.HUB_MONTHLY_CRATES_OPENED.getInt(new String [] {"uuid", "type", "month"}, new String [] {uuid, "voting", month + ""}, "amount"),
					"&7" + CrateTypes.VOTING.getDisplay() + " Crates opened this week: &e" + DB.HUB_WEEKLY_CRATES_OPENED.getInt(new String [] {"uuid", "type", "week"}, new String [] {uuid, "voting", week + ""}, "amount"),
					"",
				}).getItemStack());
				inventory.setItem(34, new ItemCreator(Material.NAME_TAG).setName("&bVote Stats").setLores(new String [] {
					"",
					"&7Lifetime votes: &e" + DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "amount"),
					"&7Monthly votes: &e" + DB.PLAYERS_MONTHLY_VOTES.getInt(new String [] {"uuid", "month"}, new String [] {uuid, month + ""}, "amount"),
					"&7Weekly votes: &e" + DB.PLAYERS_WEEKLY_VOTES.getInt(new String [] {"uuid", "week"}, new String [] {uuid, week + ""}, "amount"),
					"",
					"&7Vote streak: &e" + DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "streak"),
					"&7Best streak: &e" + DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "highest_streak"),
					""
				}).getItemStack());
				player.openInventory(inventory);
			}
		});
	}
}
