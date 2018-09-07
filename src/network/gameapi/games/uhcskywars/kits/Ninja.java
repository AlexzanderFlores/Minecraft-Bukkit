package network.gameapi.games.uhcskywars.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.Network.Plugins;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.MouseClickEvent.ClickType;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.SpectatorHandler;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.SkyWarsShop;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.servers.hub.items.Features.Rarity;
import network.server.tasks.DelayedTask;
import network.server.util.ItemCreator;
import network.server.util.UnicodeUtil;

public class Ninja extends KitBase {
	private static final int amount = 10;
	private static boolean enabled = false;
	private List<String> delayed = null;
	private static final long delay = 5;
	private Random random = null;
	
	public Ninja() {
		super(Plugins.UHCSW, new ItemCreator(Material.NETHER_STAR).setName("Ninja").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Throwing Stars",
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aClick to throw star",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aStars deal 0 or .5 damage each",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.RARE;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemCreator(Material.NETHER_STAR).setAmount(amount).setName("&fThrowing Star").getItemStack());
		}
		delayed = new ArrayList<String>();
		random = new Random();
		enabled = true;
	}
	
	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(enabled && event.getClickType() == ClickType.RIGHT_CLICK) {
			Player player = event.getPlayer();
			if(!SpectatorHandler.contains(player) && Network.getMiniGame().getGameState() == GameStates.STARTED && !delayed.contains(player.getName())) {
				final String name = player.getName();
				delayed.add(name);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(name);
					}
				}, delay);
				ItemStack itemStack = player.getItemInHand();
				if(itemStack != null && itemStack.getType() == Material.NETHER_STAR) {
					Snowball snowball = player.launchProjectile(Snowball.class);
					snowball.setFireTicks(4000);
					int amount = itemStack.getAmount() - 1;
					if(amount <= 0) {
						player.setItemInHand(new ItemStack(Material.AIR));
					} else {
						player.setItemInHand(new ItemCreator(itemStack).setAmount(amount).getItemStack());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(enabled && event.getEntity() instanceof Player && event.getDamager() instanceof Snowball) {
			Snowball snowball = (Snowball) event.getDamager();
			if(snowball.getFireTicks() > 0 && snowball.getShooter() instanceof Player) {
				Player shooter = (Player) snowball.getShooter();
				if(has(shooter) && random.nextBoolean()) {
					event.setDamage(1);
					Player player = (Player) event.getEntity();
					MessageHandler.sendMessage(player, "&cYou have been hit with " + AccountHandler.getPrefix(shooter) + "&x's throwing star");
				}
			}
		}
	}
}