package network.anticheat.detections.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import network.anticheat.AntiCheatBase;
import network.anticheat.events.PlayerJumpEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.util.EventUtil;

public class ConstantMovement extends AntiCheatBase {
    private Map<String, Integer> headlessViolations = null;
    private Map<String, Integer> movementViolations = null;
    private List<Double> violationMovements = null;

    public ConstantMovement() {
        super("ConstantMovement");
        headlessViolations = new HashMap<String, Integer>();
        movementViolations = new HashMap<String, Integer>();
        violationMovements = new ArrayList<Double>();
        violationMovements.add(-0.125);
        violationMovements.add(0.2000000000);
        EventUtil.register(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(isEnabled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            Location to = event.getTo();

			/*
			 * Check for:
			 * Headless
			 */
            float pitch = to.getPitch();
            if(pitch == 180.0f || pitch == -180.0f) {
                int headlessViolation = 0;
                if(headlessViolations.containsKey(name)) {
                    headlessViolation = headlessViolations.get(name);
                }
                headlessViolations.put(name, ++headlessViolation);
                if(headlessViolation >= 3) {
                    ban(player);
                }
                return;
            }

            Location from = event.getFrom();
            double difference = to.getY() - from.getY();
            if(difference != 0) {
            	if(("" + difference).startsWith("0.4199999")) {
            		Bukkit.getPluginManager().callEvent(new PlayerJumpEvent(player));
            	}
				/*
				 * Check for:
				 * Wurst High Jump
				 */
                if(("" + difference).startsWith("1.02000")) {
                	player.kickPlayer(ChatColor.RED + "Kicked for High Jump\nIs this an error? Tweet us:\n@OSTBNetwork");
                    //ban(player);
                    return;
                }
				
				/*
				 * Check for:
				 * Wurst Glide
				 * Wurst Spider
				 * Various other constant changing Y velocity cheats
				 */
                if(violationMovements.contains(difference) && !player.isFlying() && player.getTicksLived() >= 20 * 10) {
                	Material type = to.getBlock().getType();
                	if(type == Material.LADDER || type == Material.VINE) {
                		return;
                	}
                	int violation = 0;
                    if(movementViolations.containsKey(name)) {
                        violation = movementViolations.get(name);
                    }
                    movementViolations.put(name, ++violation);
                    if(violation >= 5) {
                    	player.kickPlayer(ChatColor.RED + "Kicked for Constant Movement\nIs this an error? Tweet us:\n@OSTBNetwork");
                    	//ban(player);
                    	return;
                    }
                } else {
                	movementViolations.remove(name);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if(isEnabled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            headlessViolations.remove(name);
            movementViolations.remove(name);
        }
    }
}
