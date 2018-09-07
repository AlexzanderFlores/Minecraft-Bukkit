package network.gameapi.uhc.scenarios.scenarios;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

import network.server.util.EventUtil;

public class AppleRates implements Listener {
	private static int rates = 0;
	
	public AppleRates(int rates) {
		AppleRates.rates = rates;
		EventUtil.register(this);
	}
	
	public static int getRates() {
		return rates;
	}
	
	public static void setRates(int rates) {
		AppleRates.rates = rates;
	}
	
	private boolean spawn() {
		return (new Random().nextInt(100) + 1) >= rates;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Material type = block.getType();
		if((type == Material.LEAVES || type == Material.LEAVES_2) && spawn()) {
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
		}
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		if(spawn()) {
			Block block = event.getBlock();
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
		}
	}
}