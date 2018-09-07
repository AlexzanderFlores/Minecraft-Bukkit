package network.anticheat.detections.movement;

import network.anticheat.AntiCheatBase;
import network.anticheat.events.BPSEvent;
import network.customevents.TimeEvent;
import network.customevents.player.AsyncPlayerLeaveEvent;
import network.server.PerformanceHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpeedFix extends AntiCheatBase {
    private Map<String, List<Long>> violations = null;
    private List<String> badBlockDelay = null;
    private String [] badBlocks = null;
    private long ticks = 0;

    public SpeedFix() {
        super("Speed");
        violations = new HashMap<String, List<Long>>();
        badBlockDelay = new ArrayList<String>();
        badBlocks = new String [] {"STAIR", "SLAB", "ICE"};
        EventUtil.register(this);
    }
    
    @EventHandler
    public void onTime(TimeEvent event) {
    	long ticks = event.getTicks();
    	if(ticks == 1) {
    		++this.ticks;
    	}
    }

    @EventHandler
    public void onBPS(BPSEvent event) {
        if(isEnabled()) {
            Player player = event.getPlayer();
            final String name = player.getName();
            if(PerformanceHandler.getPing(player) > getMaxPing()) {
            	violations.remove(name);
                return;
            }
            if(!player.isFlying() && player.getVehicle() == null && !player.hasPotionEffect(PotionEffectType.SPEED)) {
            	if(notIgnored(player) && !badBlockDelay.contains(name) && player.getWalkSpeed() == 0.2f) {
                	Location location = player.getLocation();
                    for(int a = -2; a <= 0; ++a) {
                        Block block = location.getBlock().getRelative(0, a, 0);
                        for(String badBlock : badBlocks) {
                            if(block.getType().toString().contains(badBlock)) {
                                if(!badBlockDelay.contains(name)) {
                                    badBlockDelay.add(name);
                                    new DelayedTask(new Runnable() {
                                        @Override
                                        public void run() {
                                            badBlockDelay.remove(name);
                                        }
                                    }, 20 * 2);
                                }
                                violations.remove(name);
                                return;
                            }
                        }
                    }
                    if(location.getBlock().getRelative(0, 2, 0).getType() != Material.AIR) {
                    	violations.remove(name);
                    	return;
                    }
                    double distance = event.getDistance();
                    double max = 9;
                    if(distance > max) {
                    	Bukkit.getLogger().info(name + ": " + distance);
                    	List<Long> violation = violations.get(name);
                        if(violation == null) {
                            violation = new ArrayList<Long>();
                        }
                        violation.add(ticks);
                        violations.put(name, violation);
                        int recent = 0;
                        for(long ticks : violation) {
                            if(this.ticks - ticks <= 120) {
                                if(++recent >= 2) {
                                	player.kickPlayer(ChatColor.RED + "Kicked for Speed\nIs this invalid? Tweet at us:\n@OSTBNetwork");
                                    //ban(player);
                                    return;
                                }
                            }
                        }
                    } else {
                    	violations.remove(name);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
        if(isEnabled()) {
            String name = event.getName();
            if(violations.containsKey(name)) {
                UUID uuid = event.getUUID();
                List<Long> loggings = violations.get(name);
                if(loggings != null) {
                    int average = 0;
                    for(long logging : loggings) {
                        average += logging;
                    }
                    if(average > 0) {
//                        DB.NETWORK_DISTANCE_LOGS.insert("'" + uuid.toString() + "', '" + (average / loggings.size()) + "'");
                    }
                    violations.get(name).clear();
                    loggings.clear();
                }
                violations.remove(name);
            }
        }
    }
}
