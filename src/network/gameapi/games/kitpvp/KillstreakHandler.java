package network.gameapi.games.kitpvp;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.ItemLine;
import com.sainttx.holograms.api.line.TextLine;
import network.customevents.TimeEvent;
import network.customevents.game.GameDeathEvent;
import network.customevents.player.NewPlayerJoinEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.kitpvp.events.KillstreakEvent;
import network.gameapi.games.kitpvp.killstreaks.*;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.DB;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.StringUtil;
import npc.NPCEntity;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class KillstreakHandler implements Listener {
	private Location location = null;
	private boolean beingUsed = false;
	private int requiredLevels = 5;
	private double counter = 0;
	private double maxTime = 7.5;
	private Hologram hologram = null;
	private Player player = null;
	private List<String> usedKillStreak = null;
	private List<String> usedKillStreakEver = null;
	private static List<String> newPlayers = null;
	private Map<String, Integer> timesUsed = null;
	
	public KillstreakHandler(double x, double y, double z) {
		World world = Bukkit.getWorlds().get(0);
		location = new Location(world, x, y, z);
		location.getBlock().setType(Material.ENCHANTMENT_TABLE);
		usedKillStreak = new ArrayList<>();
		usedKillStreakEver = new ArrayList<>();
		newPlayers = new ArrayList<>();
		timesUsed = new HashMap<>();

		hologram = new Hologram("killstreak_selector", location.clone().add(0.5, 2, 0.5));
		hologram.addLine(new TextLine(hologram, StringUtil.color("&aKillstreak Selector &7(Click)")));

		new PoisonBow();
		new ExtraHealth();
		new Strength();
		new Speed();
		new Juggernaut();
		new ExplosiveBow();
		new SnowballFight();
		new SlimeTime();
		EventUtil.register(this);
	}
	
	public static boolean isNew(Player player) {
		return newPlayers != null && newPlayers.contains(player.getName());
	}
	
	public static void sendMessage(Player player) {
		MessageHandler.sendLine(player, "&6");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendMessage(player, "&bWelcome to Kit PVP, &e" + player.getName() + "&b!");
		MessageHandler.sendMessage(player, "&bDue to being new you've been given &e5 &blevels");
		MessageHandler.sendMessage(player, "&bClick the &eEnchantment Table &bfor a cool perk!");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendLine(player, "&6");
	}
	
	private void selectKillstreak() {
		if(player.isOnline()) {
			Killstreak selected = null;
			for(Killstreak killstreak : Killstreak.getKillstreaks()) {
				ItemLine itemLine = (ItemLine) hologram.getLine(1);
				if(itemLine != null) {
					if(killstreak.getItemStack().equals(itemLine.getItem())) {
						selected = killstreak;
						break;
					}
				}
			}

			if(selected != null) {
				selected.execute(player);
				Bukkit.getPluginManager().callEvent(new KillstreakEvent(player, selected));
				if(!usedKillStreakEver.contains(player.getName())) {
					usedKillStreakEver.add(player.getName());
				}
			}

			if(hologram.getLine(1) != null) {
				hologram.removeLine(hologram.getLine(1));
			}
		}

		//ParticleTypes.FLAME.displaySpiral(npc.getLivingEntity().getLocation());
		EffectUtil.playSound(player, Sound.ENDERDRAGON_WINGS);
		player = null;
		beingUsed = false;
		counter = 0;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(location != null && location.equals(event.getClickedBlock().getLocation())) {
				Player player = event.getPlayer();
				if(SpectatorHandler.contains(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot be a spectator when using this");
				} else if(player.getLevel() >= requiredLevels) {
					if(beingUsed) {
						MessageHandler.sendMessage(player, "&cKillstreak selector is already in use");
					} else {
						if(usedKillStreak.contains(player.getName()) && !AccountHandler.Ranks.OWNER.hasRank(player)) {
							MessageHandler.sendMessage(player, "&cYou can only use this once per life");
						} else {
							newPlayers.remove(player.getName());
							usedKillStreak.add(player.getName());
							beingUsed = true;
							player.setLevel(player.getLevel() - requiredLevels);
							this.player = player;
						}
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou need &e" + requiredLevels + " &clevels to use the killstreak selector");
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 5 && player != null) {
			if(counter <= maxTime) {
				Killstreak killstreak = Killstreak.getKillstreaks().get(new Random().nextInt(Killstreak.getKillstreaks().size()));
				if(hologram.getLine(1) != null) {
					hologram.removeLine(hologram.getLine(1));
				}
				hologram.addLine(new ItemLine(hologram, killstreak.getItemStack()));
				counter += .25;
			} else if(counter > maxTime) {
				selectKillstreak();
			}
		}
	}
	
	@EventHandler
	public void onNewPlayerJoin(NewPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(!newPlayers.contains(player.getName())) {
			newPlayers.add(player.getName());
			player.setLevel(5);
			sendMessage(player);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		usedKillStreak.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(timesUsed.containsKey(player.getName())) {
			int times = timesUsed.get(player.getName());
			if(DB.PLAYERS_KIT_PVP_LEVEL_PURCHASES.isUUIDSet(player.getUniqueId())) {
				DB.PLAYERS_KIT_PVP_LEVEL_PURCHASES.updateInt("purchases", times, "uuid", player.getUniqueId().toString());
			} else {
				DB.PLAYERS_KIT_PVP_LEVEL_PURCHASES.insert("'" + player.getUniqueId().toString() + "', '" + times + "'");
			}
			timesUsed.remove(player.getName());
		}
		newPlayers.remove(player.getName());
		usedKillStreak.remove(player.getName());
		usedKillStreakEver.remove(player.getName());
	}
}