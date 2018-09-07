package network.gameapi.games.uhcskywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import network.Network.Plugins;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.MouseClickEvent.ClickType;
import network.gameapi.kit.KitBase;
import network.gameapi.shops.SkyWarsShop;
import network.server.servers.hub.items.Features.Rarity;
import network.server.util.ItemCreator;
import network.server.util.UnicodeUtil;

public class Bomber extends KitBase {
	private static final int amount = 2;
	private static boolean enabled = false;
	
	public Bomber() {
		super(Plugins.UHCSW, new ItemCreator(Material.TNT).setName("Bomber").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " TNT",
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aLeft click will throw TNT",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aPlacing will place primed TNT",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.COMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemStack(Material.TNT, amount));
		}
		enabled = true;
	}
	
	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(enabled) {
			Player player = event.getPlayer();
			if(has(player) && event.getClickType() == ClickType.LEFT_CLICK) {
				ItemStack item = player.getItemInHand();
				if(item != null && item.getType() == Material.TNT) {
					TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
					tnt.setVelocity(player.getLocation().getDirection().multiply(1.5d));
					tnt.setFuseTicks(tnt.getFuseTicks() / 2);
					removeTNT(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(enabled) {
			Player player = event.getPlayer();
			if(has(player) && event.getBlock().getType() == Material.TNT) {
				TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(tnt.getFuseTicks() / 2);
				removeTNT(player);
				event.setCancelled(true);
			}
		}
	}
	
	private void removeTNT(Player player) {
		ItemStack item = player.getItemInHand();
		int amount = item.getAmount() - 1;
		if(amount <= 0) {
			player.setItemInHand(new ItemStack(Material.AIR));
		} else {
			player.setItemInHand(new ItemStack(Material.TNT, amount));
		}
	}
}