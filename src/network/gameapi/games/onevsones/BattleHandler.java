package network.gameapi.games.onevsones;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.events.BattleRequestEvent;
import network.gameapi.games.onevsones.events.QuitCommandEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.CommandBase;
import network.server.util.DoubleUtil;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.UnicodeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class BattleHandler implements Listener {
    private static List<Battle> battles = null;
    private static Map<String, Battle> playerBattles = null;
    private static Map<Location, Integer> mapCoords = null; // <target X, map number>
    private static Map<String, List<Block>> playersPlaced = null;
    private static Map<String, List<Entity>> spawnedEntities = null;
    private final BlockFace [] faces = new BlockFace [] {
        BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
        BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    public BattleHandler() {
        battles = new ArrayList<Battle>();
        playerBattles = new HashMap<String, Battle>();
        mapCoords = new HashMap<Location, Integer>();
        playersPlaced = new HashMap<String, List<Block>>();
        spawnedEntities = new HashMap<String, List<Entity>>();

        if(Network.getPlugin() == Plugins.ONEVSONE) {
            new CommandBase("quit", true) {
                @Override
                public boolean execute(CommandSender sender, String[] arguments) {
                    Player player = (Player) sender;
                    QuitCommandEvent event = new QuitCommandEvent(player);
                    Bukkit.getPluginManager().callEvent(event);

                    Battle battle = getBattle(player);
                    if(battle != null) {
                        MessageHandler.sendMessage(player, "You were given a death for quiting");
                        Player competitor = battle.getCompetitor(player);
                        if(competitor != null) {
                            MessageHandler.sendMessage(competitor, "You were given a kill for your opponent quiting");
//                                StatsHandler.addKill(competitor);
//                                StatsHandler.addDeath(player);
                        }
                        battle.end(player);
                    }
                    return true;
                }
            };
        }
        EventUtil.register(this);
    }

    public static List<Battle> getBattles() {
        return battles;
    }

    public static Battle getBattle(Player player) {
        return playerBattles.get(player.getName());
    }

    public static void addPlayerBattle(Player player, Battle battle) {
        playerBattles.put(player.getName(), battle);
    }

    public static void removePlayerBattle(Player player) {
        if(playerBattles != null) {
            playerBattles.remove(player.getName());
        }
    }

    public static boolean isInBattle(Player player) {
        return getBattle(player) != null;
    }

    public static void addBattle(Battle battle) {
        battles.add(battle);
    }

    public static void removeBattle(Battle battle) {
        battles.remove(battle);
    }

    public static int getMapNumber(int targetX) {
        return mapCoords.get(targetX);
    }

    public static void setTargetX(Location targetLocation, int mapNumber) {
        mapCoords.put(targetLocation, mapNumber);
    }

    public static void removeMapCoord(Location targetLocation) {
        mapCoords.remove(targetLocation);
    }

    @EventHandler
    public void onBattleRequest(BattleRequestEvent event) {
        if(getBattle(event.getPlayerOne()) != null) {
            MessageHandler.sendMessage(event.getPlayerOne(), "&cYou are already in a battle");
            event.setCancelled(true);
        } else if(getBattle(event.getPlayerTwo()) != null) {
            MessageHandler.sendMessage(event.getPlayerOne(), AccountHandler.getPrefix(event.getPlayerTwo()) + " &cis already in a battle");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuitCommand(QuitCommandEvent event) {
        Battle battle = getBattle(event.getPlayer());
        if(battle != null) {
            battle.end(event.getPlayer());
        }
        removePlayerBattle(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EffectUtil.playSound(player, Sound.ZOMBIE_DEATH);
        Player killer = player.getKiller();
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(playerBattles.get(player.getName())));
        if(killer == null) {
            if(Network.getPlugin() == Plugins.ONEVSONE) {
                player.sendMessage(event.getDeathMessage());
            }
        } else {
            EffectUtil.playSound(killer, Sound.LEVEL_UP);
            double health = DoubleUtil.round(((double) killer.getHealth() / 2), 2);
            if(health <= 0) {
                health = 0.10;
            }
            event.setDeathMessage(event.getDeathMessage() + ChatColor.translateAlternateColorCodes('&', " &fwith &c" + health + " &4" + UnicodeUtil.getHeart()));
            if(Network.getPlugin() == Plugins.ONEVSONE) {
                MessageHandler.sendMessage(player, event.getDeathMessage());
                MessageHandler.sendMessage(killer, event.getDeathMessage());
            }
            removePlayerBattle(killer);
        }
        removePlayerBattle(player);
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.blockList() != null) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if(event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            ProjectileSource projectileSource = arrow.getShooter();
            if(projectileSource instanceof Player) {
                Player shooter = (Player) projectileSource;
                Battle battle = getBattle(shooter);
                if(battle != null) {
                    addEntity(shooter, arrow);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getCause() == DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(event.getItemDrop().getItemStack().getType() == Material.POTION) {
            event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
        }
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Battle battle = getBattle(player);
        if(battle != null) {
            Block block = event.getBlockClicked().getRelative(event.getBlockFace());
            addBlock(player, block);
        }
    }
    
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
    	Material type = event.getBlock().getType();
        if(type == Material.WATER || type == Material.STATIONARY_WATER || type == Material.LAVA || type == Material.STATIONARY_LAVA) {
            Block toBlock = event.getToBlock();
            if(toBlock.getType() == Material.AIR) {
                Battle currentBattle = null;
                for(Battle battle : getBattles()) {
                    Location loc = battle.getTargetLocation();
                    double distance = loc.distance(toBlock.getLocation());
                    if(distance <= 60) {
                        currentBattle = battle;
                        break;
                    }
                }
                if(currentBattle != null) {
                    addBlock(currentBattle.getPlayers().get(0), toBlock);
                }
            }
            event.setCancelled(false);
        }
    }
    
    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
    	for(Player player : event.getBattle().getPlayers()) {
    		List<Block> blocks = playersPlaced.get(player.getName());
        	if(blocks != null) {
        	    for(Block block : blocks) {
                    block.setType(Material.AIR);
                    block.setData((byte) 0);
                }
        	}

        	List<Entity> entities = spawnedEntities.get(player.getName());
        	if(entities != null) {
        	    for(Entity entity : entities) {
        	        entity.remove();
                }
            }
    	}
    }

    private void addBlock(Player player, Block block) {
        List<Block> blocks = playersPlaced.get(player.getName());
        if(blocks == null) {
            blocks = new ArrayList<Block>();
        }
        blocks.add(block);
        playersPlaced.put(player.getName(), blocks);
    }

    private void addEntity(Player player, Entity entity) {
        List<Entity> entities = spawnedEntities.get(player.getName());
        if(entities == null) {
            entities = new ArrayList<Entity>();
        }
        entities.add(entity);
        spawnedEntities.put(player.getName(), entities);
    }
}
