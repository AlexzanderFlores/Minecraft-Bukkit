package network.gameapi.games.kitpvp.killstreaks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.ItemCreator;

public class Strength extends Killstreak {
	private static Strength instance = null;
	
	public Strength() {
		super(new ItemCreator(Material.DIAMOND_SWORD).setName("Strength").getItemStack());
		instance = this;
	}
	
	public static Strength getInstance() {
		if(instance == null) {
			new Strength();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 40, 0));
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet strength I for 30 seconds");
	}
}