package network.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import network.customevents.TimeEvent;
import network.server.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.Network;
import network.ProPlugin;
import network.customevents.game.GameDeathEvent;
import network.customevents.game.GameEndingEvent;
import network.customevents.game.GameStartEvent;
import network.customevents.game.GameStartingEvent;
import network.customevents.game.WhitelistDisabledEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerBanEvent;
import network.customevents.player.PlayerHeadshotEvent;
import network.gameapi.GracePeriod;
import network.gameapi.MiniGame;
import network.gameapi.MiniGame.GameStates;
import network.gameapi.SpectatorHandler;
import network.gameapi.games.uhc.anticheat.MuteChat;
import network.gameapi.games.uhc.border.BorderHandler;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.AlertHandler;
import network.server.tasks.DelayedTask;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	private ItemStack forceStartItem = null;
	private Random random = null;
	private static boolean moveToBox = false;
	private static boolean firstMinute = false;
	private static boolean moveToCenter = false;
	private static boolean postStart = false;
	private static boolean canBreakBlocks = false;
	private static List<Block> glass = null;
	private static Map<Block, String> blocks = null;
	private static int alertCounter = 0;
	
	public Events() {
		forceStartItem = new ItemCreator(Material.NAME_TAG).setName("&aForce Start Game").getItemStack();
		random = new Random();
		blocks = new HashMap<>();
		EventUtil.register(this);
	}
	
	private static int countBlocks(Player player) {
		int counter = 0;
		for(String name : blocks.values()) {
			if(name.equals(player.getName())) {
				++counter;
			}
		}
		return counter;
	}
	
	public static void start() {
		WorldHandler.getWorld().setTime(0);

		for(Block block : glass) {
			block.setType(Material.AIR);
			block.setData((byte) 0);
		}
		glass.clear();
		glass = null;

		new DelayedTask(() -> {
			String winner = null;
			int bestScore = 0;
			MessageHandler.alertLine();

			for(Player player : ProPlugin.getPlayers()) {
				for(PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}

				int score = countBlocks(player);
				MessageHandler.sendMessage(player, "Glass Coloring Score: &e" + score);

				if(score > bestScore) {
					winner = AccountHandler.getPrefix(player);
					bestScore = score;
				}

				if(HostHandler.isHost(player.getUniqueId())) {
					String perm = "worldborder.*";
					PermissionAttachment permission = player.addAttachment(Network.getInstance());
					permission.setPermission(perm, true);
					player.chat("/wb clear all");
					permission.unsetPermission(perm);
					permission.remove();
					permission = null;
					if(HostHandler.getMainHost() != null && HostHandler.getMainHost().getName().equals(player.getName())) {
						SpectatorHandler.getInstance().add(player);
					}
				}
			}

			MessageHandler.alert("&eMost glass colored: " + winner == null ? "none" : winner + " &ewith &c" + bestScore);
			MessageHandler.alertLine();
			blocks.clear();

			for(Entity entity : WorldHandler.getWorld().getEntities()) {
				if(entity instanceof Pig || entity instanceof Sheep) {
					LivingEntity livingEntity = (LivingEntity) entity;
					if(!ScatterHandler.isSaved(livingEntity)) {
						livingEntity.remove();
					}
				}
			}

			ScatterHandler.doneSaving();
			if(OptionsHandler.isRush()) {
				Network.getMiniGame().setCounter(60 * 30);
				new GracePeriod(60 * 10);
			} else {
				Network.getMiniGame().setCounter(60 * 60);
				new GracePeriod(60 * 15);
			}

			firstMinute = true;

			new DelayedTask(() -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.setFireTicks(0);
				}
				postStart = true;
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "heal all");
				MessageHandler.alert("&cFall/mob damage is now enabled");
			}, 20 * 15);

			MessageHandler.alert("&cDrowning is now disabled for the next 60 seconds");

			new DelayedTask(() -> {
				firstMinute = false;
				MessageHandler.alert("&cDrowning damage is now enabled");
			}, 20 * 60);

			canBreakBlocks = true;
		}, 20 * 5);
	}
	
	public static boolean getMoveToCenter() {
		return moveToCenter;
	}
	
	public static void setMoveToCenter(boolean moveToCenter) {
		Events.moveToCenter = moveToCenter;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();

		if(ticks == 20) {
			MiniGame miniGame = Network.getMiniGame();
			GameStates gameState = miniGame.getGameState();

			if(gameState == GameStates.STARTED) {
				int counter = miniGame.getCounter();
				if(counter <= 0) {
					WorldHandler.getWorld().setGameRuleValue("doDaylightCycle", "false");
					WorldHandler.getWorld().setTime(6000);

					new SurfaceHandler();
					Network.getSidebar().setName("&aGo to 0, 0!");
					setMoveToCenter(true);

					for(Player player : ProPlugin.getPlayers()) {
						BorderHandler.giveCompass(player);
					}
				} else {
					if(GracePeriod.isRunning()) {
						Network.getSidebar().update("&bGrace For " + GracePeriod.getGraceCounterString());
					} else {
						Network.getSidebar().update("&aIn Game " + miniGame.getCounterAsString());
						if(counter <= 5 || (counter < 60 && counter % 10 == 0)) {
							MessageHandler.alert("Meetup in &e" + miniGame.getCounterAsString());
						}
//							if(Network.getMiniGame().getUpdateBossBar()) {
//								BossBar.display("&c&lMeetup in &e" + miniGame.getCounterAsString());
//							}
					}

					if(++alertCounter == 10 || alertCounter == 60) {
						MessageHandler.alertLine();
						MessageHandler.alert("");
						MessageHandler.alert("&4&lIf you have questions please run these:");
						MessageHandler.alert("");
						MessageHandler.alert("  " + UnicodeUtil.getUnicode("27A4") + " &b&l/sInfo");
						MessageHandler.alert("  " + UnicodeUtil.getUnicode("27A4") + " &b&l/info");
						MessageHandler.alert("  " + UnicodeUtil.getUnicode("27A4") + " &b&l/rules");
						MessageHandler.alert("");
						MessageHandler.alertLine();
					}
				}
			}
		} else if(ticks == 20 * 5) {
			/*if(getMoveToCenter() && !HostedEvent.isEvent()) {
				BossBar.display("&e&lGo to 0, 0!", BossBar.maxHealth);
			} else */if(moveToBox) {
				World world = WorldHandler.getWorld();
				int counter = 0;
				for(Player player : ProPlugin.getPlayers()) {
					if(!player.getWorld().getName().equals(world.getName())) {
						if(++counter >= 10) {
							break;
						}

						player.teleport(new Location(world, 0, 201, 0));
						if(player.getAllowFlight()) {
							player.setFlying(false);
							player.setAllowFlight(false);
						}
					}
				}

				if(counter == 0) {
					moveToBox = false;
				}
			}
		} else if(ticks == 20 * 30) {
			if(!firstMinute && MuteChat.isMuted() && !MuteChat.hasBeenManuallyMuted()) {
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(Ranks.TRIAL.hasRank(player) || HostHandler.isHost(player.getUniqueId())) {
						MessageHandler.sendMessage(player, "");
						MessageHandler.sendMessage(player, "&c&lWARNING: &bChat is still auto muted");
						MessageHandler.sendMessage(player, "Toggle chat mute with &b/muteChat");
						MessageHandler.sendMessage(player, "");
					}
				}
			}

			if(glass != null) {
				for(Block block : glass) {
					block.setType(Material.STAINED_GLASS);
					block.setData((byte) 0);
				}
			}
		} else if(ticks == 20 * 60) {
			GameStates state = Network.getMiniGame().getGameState();
			if((state == GameStates.WAITING || (state == GameStates.STARTING && Network.getMiniGame().getCounter() > 30)) && !WhitelistHandler.isWhitelisted()) {
				AlertHandler.alert("&6&l" + TweetHandler.getTeamSize() + " " + TweetHandler.getScenarios() + " UHC OPEN NOW! &c&l/join UHC1" + TweetHandler.getURL());
			}
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		glass = new ArrayList<>();
		World world = WorldHandler.getWorld();
		world.setTime(0);
		for(Entity entity : world.getEntities()) {
			if(!(entity instanceof Player) && entity instanceof Monster) {
				entity.remove();
			}
		}

		Location center = world.getSpawnLocation();
		center.setY(200);
		int radius = 14;

		for(int x = -radius; x <= radius; ++x) {
			for(int z = -radius; z <= radius; ++z) {
				for(int y = 200; y <= 206; ++y) {
					Block block = world.getBlockAt(x, y, z);
					boolean inRadius = block.getLocation().toVector().isInSphere(center.toVector(), radius);
					if((y == 200 || y == 206) && inRadius) {
						block.setType(Material.STAINED_GLASS);
						glass.add(block);
					} else if(y > 200 && !inRadius) {
						try {
							block.setType(Material.STAINED_GLASS);
							glass.add(block);
						} catch(Exception e) {
							Bukkit.getLogger().info(ChatColor.RED + e.getMessage());
						}
					}
				}
			}
		}
		moveToBox = true;
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		Network.getProPlugin().removeFlags();
		for(Player player : ProPlugin.getPlayers()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999999, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 0));
		}

		new DisconnectHandler();
		new ScatterHandler();

		Network.getMiniGame().setResetPlayerUponJoining(false);
		Network.getMiniGame().setCounter(60 * 60);
		Network.getSidebar().removeScoresBelow(TeamHandler.getMaxTeamSize() + 1);

		new KillLogger();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		GameStates state = Network.getMiniGame().getGameState();

		if(state == GameStates.WAITING || state == GameStates.VOTING) {
			player.teleport(Network.getMiniGame().getLobby().getSpawnLocation());
		}

		if(HostHandler.isHost(player.getUniqueId()) && !WhitelistHandler.isWhitelisted() && (state == GameStates.WAITING || state == GameStates.VOTING)) {
			player.getInventory().addItem(forceStartItem);
		}
	}
	
	@EventHandler
	public void onWhitelistDisable(WhitelistDisabledEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			player.closeInventory();
			player.getInventory().clear();
			if(HostHandler.isHost(player.getUniqueId())) {
				player.getInventory().addItem(forceStartItem);
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if(event.getEntity().getCustomName() != null && event.getEntityType() == EntityType.SHEEP) {
			for(Entity entity : event.getEntity().getNearbyEntities(15, 15, 15)) {
				if(entity instanceof Sheep) {
					Sheep sheep = (Sheep) entity;
					sheep.setCustomName(null);
				}
			}
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), forceStartItem)) {
			if(Network.getMiniGame().getGameState() == GameStates.STARTING) {
				MessageHandler.sendMessage(player, "&cThe game is already starting");
			} else {
				Network.getMiniGame().setGameState(GameStates.STARTING);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(Network.getMiniGame().getGameState() == GameStates.STARTING && event.getClickedBlock() != null) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			byte data = 0;
			Random random = new Random();
			do {
				data = (byte) random.nextInt(15);
			} while(data == block.getData());
			block.setData(data);
			if(random.nextBoolean()) {
				EffectUtil.playSound(player, Sound.NOTE_PIANO);
			} else {
				EffectUtil.playSound(player, Sound.NOTE_PLING);
			}
			blocks.put(block, player.getName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(postStart) {
			if(event.getEntity() instanceof Player && firstMinute && event.getCause() == DamageCause.DROWNING) {
				Player player = (Player) event.getEntity();
				EffectUtil.playSound(player, Sound.ENDERDRAGON_DEATH);
				MessageHandler.sendMessage(player, "&cGet out of the water, you're drowning!");
				event.setCancelled(true);
			} else if(event.getCause() != DamageCause.ENTITY_ATTACK) {
				event.setCancelled(false);
			}
		} else if(event.getEntity() instanceof Player || event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(postStart && Network.getMiniGame().getGameState() == GameStates.STARTED) {
			if(event.getEntity() instanceof Player) {
				Player damager = null;
				if(event.getDamager() instanceof Player) {
					damager = (Player) event.getDamager();
				} else if(event.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) event.getDamager();
					if(projectile.getShooter() instanceof Player) {
						damager = (Player) projectile.getShooter();
					}
				}
				if(damager == null) {
					event.setCancelled(false);
				}
			} else {
				event.setCancelled(false);
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.GOLDEN_APPLE) {
			Player player = event.getPlayer();
			Particles.ParticleTypes.FLAME.displaySpiral(player.getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromTo(BlockFromToEvent event) {
		if(!event.getBlock().getWorld().getName().equals("lobby")) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onGameDeath(GameDeathEvent event) {
		if(Network.getMiniGame().getGameState() == GameStates.STARTED) {
			Player player = event.getPlayer();
			Location location = player.getLocation();
			location.getBlock().setType(Material.NETHER_FENCE);
			Block block = location.getBlock().getRelative(0, 1, 0);
			block.setType(Material.SKULL);
			block.setData((byte) 1);
			Skull skull = (Skull) block.getState();
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwner(player.getName());
			skull.update();
		}
	}
	
	@EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		ItemStack item = event.getItem();
		if(item != null && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
			if(item.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Ultra Golden Apple (" + ChatColor.GOLD + "+4 Hearts" + ChatColor.LIGHT_PURPLE + ")")) {
				Player player = event.getPlayer();
				double newHealth = event.getPlayer().getHealth() + 4.0d;
				player.setHealth(newHealth > player.getMaxHealth() ? player.getMaxHealth() : newHealth);
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 0));
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			if(SpectatorHandler.contains(player) && (HostHandler.isHost(player.getUniqueId()) || Ranks.isStaff(player))) {
				Player target = (Player) event.getRightClicked();
				player.chat("/invSee " + target.getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		if(event.getCause() == TeleportCause.NETHER_PORTAL && !OptionsHandler.isNetherEnabled()) {
			MessageHandler.sendMessage(event.getPlayer(), "&cThe Nether is disabled");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		Network.getMiniGame().setGameState(GameStates.STARTED);
		Bukkit.getLogger().info("Game ending");
//		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerHeadshot(PlayerHeadshotEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerBan(PlayerBanEvent event) {
		Player player = Bukkit.getPlayer(event.getUUID());
		if(player != null) {
			player.setHealth(0.0d);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		EntityType type = event.getEntityType();
		if(event.getSpawnReason() != SpawnReason.CUSTOM && (type == EntityType.PIG || type == EntityType.SHEEP)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(!canBreakBlocks) {
			event.setCancelled(true);
		}
		Block block = event.getBlock();
		if(block.getWorld().getName().equals("lobby")) {
			event.setCancelled(true);
		} else if(!event.isCancelled() && handleAppleSpawning(block)) {
			event.setCancelled(true);
		}
		if(block.getType() == Material.SKULL) {
			Skull skull = (Skull) block.getState();
			ItemStack item = new ItemCreator(Material.SKULL_ITEM, 3).setName(skull.getOwner() + "'s Head").getItemStack();
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			meta.setOwner(skull.getOwner());
			item.setItemMeta(meta);
			block.getWorld().dropItem(block.getLocation(), item);
			block.setType(Material.AIR);
			event.setCancelled(true);
		}
		/*if(!event.isCancelled()) {
			Material type = block.getType();
			if(type == Material.REDSTONE_ORE || type == Material.GLOWING_REDSTONE_ORE || type == Material.LAPIS_ORE) {
				if(!delayed.contains(event.getPlayer().getName())) {
					final String name = event.getPlayer().getName();
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * 3);
					String blockName = type == Material.LAPIS_ORE ? "Lapis" : "Redstone";
					MessageHandler.sendMessage(event.getPlayer(), "&cCancelling block dropping for " + blockName + " for performance reasons");
				}
				block.setType(Material.AIR);
				ExperienceOrb exp = (ExperienceOrb) block.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
				exp.setExperience(1);
				event.setCancelled(true);
			}
		}*/
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material type = event.getEntity().getItemStack().getType();
		if(type == Material.EGG || type == Material.SEEDS || type == Material.SULPHUR || type == Material.WOOL) {
			event.setCancelled(true);
		} else if(type == Material.SPIDER_EYE && !OptionsHandler.isNetherEnabled()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(event.getBlockClicked().getLocation().getY() >= 175) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot empty buckets at this height");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		Block block = event.getBlock();
		if(block.getWorld().getName().equals("lobby")) {
			event.setCancelled(true);
		} else if(!event.isCancelled()) {
			handleAppleSpawning(block);
		}
	}
	
	private boolean handleAppleSpawning(Block block) {
		Material type = block.getType();
		byte data = block.getData();
		if((type == Material.LEAVES && data == 0) || (type == Material.LEAVES && data == 8) || (type == Material.LEAVES_2 && data == 1)) {
			block.setType(Material.AIR);
			if(random.nextInt(100) + 1 <= OptionsHandler.getAppleRates()) {
				block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.APPLE));
				return true;
			}
		}
		return false;
	}
}
