package network.gameapi.games.onevsones;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerStaffModeEvent;
import network.customevents.player.StatsChangeEvent;
import network.gameapi.competitive.EloHandler;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.events.BattleStartEvent;
import network.gameapi.games.onevsones.events.QuitCommandEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class Battle implements Listener {
    private List<String> sentMessage = null;
    private List<String> pearlDelay = null;
    private List<Block> placedBlocks = null;
    private List<Item> items = null;
    private List<Entity> entities = null;
    private List<Team> teams = null;
    private OneVsOneKit kit = null;
    private int timer = 0;
    private int startAt = 3;
    private Location targetLocation = null;
    private boolean started = false;
    private boolean tournament = false;
    private boolean ranked = true;

    public Battle(Location newTargetLocation, boolean tournament, boolean ranked, Team ... teamList) {
        this.sentMessage = new ArrayList<String>();
        this.pearlDelay = new ArrayList<String>();
        this.placedBlocks = new ArrayList<Block>();
        this.items = new ArrayList<Item>();
        this.entities = new ArrayList<Entity>();
        this.teams = Arrays.asList(teamList);
        this.kit = teamList[0].getKit();
        this.targetLocation = newTargetLocation;
        this.tournament = tournament;
        this.ranked = ranked;

        for(Team team : this.teams) {
            for(Player player : team.getPlayers()) {
                BattleHandler.addPlayerBattle(player, this);
                for(PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.setAllowFlight(false);
                player.getLocation().setPitch(0.0f);
                MessageHandler.sendMessage(player, "To quit this battle do &e/quit");
            }
        }

        Location firstMap = targetLocation.clone();
        firstMap.setZ(-30);
        Vector distance = MapProvider.spawnDistances.get(firstMap);

        Location teamOneLocation = targetLocation.clone().add(distance.getX() + .5, 0, distance.getZ() + .5);
        teamOneLocation.setY(15);
        teamOneLocation.setPitch(0.0f);
        for(Player player : teams.get(0).getPlayers()) {
            player.teleport(teamOneLocation);
        }

        if(teams.size() > 1) {
            Location teamTwoLocation = targetLocation.clone().add(distance.getX() * -1, 0, distance.getZ() * -1);
            teamTwoLocation.setY(15);
            teamTwoLocation.setPitch(0.0f);
            for(Player player : teams.get(1).getPlayers()) {
                player.teleport(teamTwoLocation);
            }
        }

        BattleHandler.addBattle(this);
        EventUtil.register(this);
    }

    public boolean contains(Player player) {
        return getTeam(player) != null;
    }

//    public Team getCompetitor(Player player) {
//        if(teamOne.isInTeam(player)) {
//            return teamOne;
//        } else {
//            return teamTwo;
//        }
//    }
    
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<Player>();
        for(Team team : teams) {
            players.addAll(team.getPlayers());
        }
    	return players;
    }

    public void removePlayer(Player player) {
        Team team = getTeam(player);
        if(team != null) {
            if(team.removePlayer(player)) {
                end(team);
            }
        }
        BattleHandler.removePlayerBattle(player);
    }

    public List<Block> getPlacedBlocks() {
        return this.placedBlocks;
    }

    public OneVsOneKit getKit() {
        return this.kit;
    }

//    public int getMapNumber() {
//        return this.map;
//    }

    public Location getTargetLocation() {
        return this.targetLocation;
    }

    public int incrementTimer() {
        return ++this.timer;
    }

    public int getTimer() {
        return this.timer;
    }

    public boolean isRanked() {
        return ranked;
    }

    public boolean isTournament() {
        return tournament;
    }

    public Team getTeam(Player player) {
        for(Team team : teams) {
            if(team.isInTeam(player)) {
                return team;
            }
        }
        return null;
    }

    public List<Team> getTeams() {
        return this.teams;
    }

    public List<Team> getOtherTeams(Team team) {
        List<Team> otherTeams = new ArrayList<Team>();
        for(Team t : teams) {
            if(t.getColor() != team.getColor()) {
                otherTeams.add(t);
            }
        }
        return otherTeams;
    }

    public void start() {
        started = true;
        if(kit != null) {
            String name = kit.getName();
            if(name.equals("One Hit Wonder") || name.equals("Quickshot")) {
                for(Player player : getPlayers()) {
                    player.setHealthScale(1.0d);
                }
            }
        }
        Bukkit.getPluginManager().callEvent(new BattleStartEvent(this));
    }

    public boolean isStarted() {
        return this.started;
    }

    public void end(Team losingTeam) {
        if(ranked) {
            Team winningTeam = getOtherTeams(losingTeam).get(0);
    	    Player winner = winningTeam.getPlayers().get(0);
    	    Player loser = losingTeam.getPlayers().get(0);
            EloHandler.calculateWin(winner, loser, 1);
    	}

    	for(Player player : getPlayers()) {
    	    ProPlugin.resetPlayer(player);
    	    LobbyHandler.spawn(player);
    	    BattleHandler.removePlayerBattle(player);
        }

        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this));

//        teams.clear();
    	teams = null;

        if(placedBlocks != null) {
            for(Block block : placedBlocks) {
                block.setType(Material.AIR);
                block.setData((byte) 0);
            }
            placedBlocks.clear();
        }

        for(Item item : items) {
        	if(item != null && !item.isDead()) {
        		item.remove();
        	}
        }
        items.clear();

        sentMessage.clear();

        BattleHandler.removeBattle(this);
        BattleHandler.removeMapCoord(targetLocation);
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onQuitCommand(QuitCommandEvent event) {
        Player player = event.getPlayer();

        if(contains(player)) {
            MessageHandler.sendMessage(player, "You were given a death for quiting");
//            StatsHandler.addDeath(player);
            removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        Player player = event.getPlayer();

        if(contains(player)) {
            removePlayer(player);
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(contains(player)) {
                String name = kit.getName();
                if((name.equals("Build UHC") || (name.equals("Speed UHC"))) && event.getRegainReason() == RegainReason.SATIATED) {
                	event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        new DelayedTask(new Runnable() {
            @Override
            public void run() {
                if(contains(player)) {
                    removePlayer(player);
                }
            }
        });
    }

    @EventHandler
    public void onStatsChange(StatsChangeEvent event) {
        if(contains(event.getPlayer()) && !isRanked()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(contains(event.getPlayer())/* && event.getBlock().getY() < 10*/) {
            if(isStarted()) {
                Material type = event.getBlock().getType();
                if(type == Material.TNT) {
                    Player player = event.getPlayer();
                    ItemStack item = event.getItemInHand();
                    int amount = item.getAmount();
                    if(amount <= 1) {
                        player.setItemInHand(new ItemStack(Material.AIR));
                    } else {
                        player.setItemInHand(new ItemStack(type, amount - 1));
                    }
                    TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(event.getBlock().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(tnt.getFuseTicks() / 2);
                } else if(type == Material.FIRE || type == Material.COBBLESTONE) {
                    if(!placedBlocks.contains(event.getBlock())) {
                        placedBlocks.add(event.getBlock());
                    }
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if(event.getItem().getType() == Material.POTION) {
            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                }
            });
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(contains(player) && placedBlocks.contains(event.getBlock())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if(event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if(arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();
                if(contains(player) && getTimer() < startAt) {
                    MessageHandler.sendMessage(player, "&cCannot shoot your bow at this time");
                    event.setCancelled(true);
                }
            }
        } else if(event.getEntity() instanceof EnderPearl) {
        	EnderPearl enderPearl = (EnderPearl) event.getEntity();
        	if(enderPearl.getShooter() instanceof Player) {
        		Player player = (Player) enderPearl.getShooter();
                String name = player.getName();

        		if(contains(player) && getTimer() < startAt) {
        			MessageHandler.sendMessage(player, "&cCannot throw Ender Pearls at this time");
                    event.setCancelled(true);
        		} else {
            		if(!pearlDelay.contains(name)) {
            			pearlDelay.add(name);
            			new DelayedTask(new Runnable() {
    						@Override
    						public void run() {
    							pearlDelay.remove(name);
    						}
    					}, 20 * 15);
            		}
        		}
        	}
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();
    	if(pearlDelay.contains(player.getName())) {
    		if(itemStack != null && itemStack.getType() == Material.ENDER_PEARL) {
    			MessageHandler.sendMessage(player, "&cYou can only throw an Ender Pearl once every 15s");
    			event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    	event.setCancelled(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(contains(event.getPlayer()) && getTimer() < startAt) {
        	Location to = event.getTo();
        	Location from = event.getFrom();
        	if(to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) {
        		if(!sentMessage.contains(event.getPlayer().getName())) {
                    sentMessage.add(event.getPlayer().getName());
                }
                event.setTo(event.getFrom());
        	}
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            if(contains(player)) {
                if(isStarted()) {
                    Location to = event.getTo();
                    if(to.getBlockY() > 16 || !player.getLocation().toVector().isInSphere(to.toVector(), 65)) {
                        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                        MessageHandler.sendMessage(player, "&cCannot teleport to that location");
                        event.setCancelled(true);
                    }
                } else {
                    player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                    MessageHandler.sendMessage(player, "&cYou cannot use Ender Pearls at this time");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
    	if(contains(event.getPlayer())) {
    		items.add(event.getItemDrop());
    		event.setCancelled(false);
    	}
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    	if(contains(event.getPlayer())) {
    		event.setCancelled(false);
    	}
    }
    
    @EventHandler
    public void onPlayerStaffMode(PlayerStaffModeEvent event) {
    	if(contains(event.getPlayer())) {
    		event.setCancelled(true);
    	}
    }
}
