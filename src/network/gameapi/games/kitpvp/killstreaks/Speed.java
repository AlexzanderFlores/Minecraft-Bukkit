package network.gameapi.games.kitpvp.killstreaks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.ItemCreator;

public class Speed extends Killstreak {
	private static Speed instance = null;
	
	public Speed() {
		super(new ItemCreator(Material.DIAMOND_BOOTS).setName("Speed").getItemStack());
		instance = this;
	}
	
	public static Speed getInstance() {
		if(instance == null) {
			new Speed();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 40, 0));
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet speed I for 30 seconds");
	}
}