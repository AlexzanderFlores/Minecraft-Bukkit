package network.anticheat.detections.movement;

import network.anticheat.AntiCheatBase;
import network.customevents.player.PlayerLeaveEvent;
import network.server.util.EventUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoSprintFix extends AntiCheatBase {
    private List<String> reported = null;

    public AutoSprintFix() {
        super("AutoSprint");
        reported = new ArrayList<String>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(isEnabled()) {
            Player player = event.getPlayer();
            Location to = event.getTo();
            Location from = event.getFrom();
            String distance = to.distance(from) + "";
            if(distance.startsWith("0.1809")) {
                if(!reported.contains(player.getName())) {
                    reported.add(player.getName());
//                    UUID uuid = player.getUniqueId();
//                    new AsyncDelayedTask(new Runnable() {
//                        @Override
//                        public void run() {
//                            DB.NETWORK_AUTO_SPRINT_TEST.insert("'" + uuid.toString() + "'");
//                        }
//                    });
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if(isEnabled()) {
            reported.remove(event.getPlayer().getName());
        }
    }
}
