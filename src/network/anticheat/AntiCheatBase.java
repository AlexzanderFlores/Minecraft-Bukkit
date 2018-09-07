package network.anticheat;

import network.anticheat.detections.*;
import network.anticheat.detections.combat.*;
import network.anticheat.detections.combat.killaura.InventoryKillAuraDetection;
import network.anticheat.detections.combat.killaura.KillAuraSpectatorCheck;
import network.anticheat.detections.movement.*;
import network.customevents.player.PlayerLeaveEvent;
import network.server.PerformanceHandler;
import network.server.util.EventUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class AntiCheatBase implements Listener {
    private static boolean enabled = true;
    private List<String> banned = new ArrayList<String>();
    private String name = null;
    private int maxPing = 135;

    public AntiCheatBase() {
        new BlocksPerSecondLogger();
        new InvisibleFireGlitchFix();
        new FastBowFix();
        new AutoCritFix();
        new AttackDistanceLogger();
        new SpeedFix();
        new FlyFix();
        new InventoryKillAuraDetection();
        new KillAuraSpectatorCheck();
        new SpamBotFix();
        new WaterWalkDetection();
        new AutoClicker();
        new ClickPatternDetector();
        new AutoArmorFix();
        new AutoEatFix();
        //new AutoSprintFix(); // TODO: Fix
        new AutoStealFix();
        new FastEatFix();
        new ConstantMovement();
        new AutoRegenFix();
        EventUtil.register(this);
    }

    public AntiCheatBase(String name) {
        this.name = name;
    }

    public static boolean isEnabled() {
        return enabled && PerformanceHandler.getTicksPerSecond() >= 18.75 && PerformanceHandler.getMemory() <= 85;
    }

    public static void setEnabled(boolean enabled) {
        AntiCheatBase.enabled = enabled;
    }

    protected int getMaxPing() {
        return maxPing;
    }

    public boolean notIgnored(Player player) {
        int ping = PerformanceHandler.getPing(player);
        return ping > 0 && ping <= maxPing && player.getGameMode() == GameMode.SURVIVAL && PerformanceHandler.getTicksPerSecond() >= 19;
    }

    public void ban(Player player) {
        ban(player, false);
    }

    public void ban(Player player, boolean queue) {
        if(notIgnored(player) && !banned.contains(player.getName())) {
            banned.add(player.getName());
            if(queue) {
//                UUID uuid = player.getUniqueId();
//                new AsyncDelayedTask(new Runnable() {
//                    @Override
//                    public void run() {
//                        DB.NETWORK_ANTI_CHEAT_BAN_QUEUE.insert("'" + uuid.toString() + ", '" + name + "''");
//                    }
//                });
            } else {
//                Bukkit.getPluginManager().callEvent(new PlayerBanEvent(player.getUniqueId(), player.getName(), name, queue));
//                new AsyncDelayedTask(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(DB.NETWORK_ANTI_CHEAT_DATA.isKeySet("cheat", name)) {
//                            int amount = DB.NETWORK_ANTI_CHEAT_DATA.getInt("cheat", name, "bans") + 1;
//                            DB.NETWORK_ANTI_CHEAT_DATA.updateInt("bans", amount, "cheat", name);
//                        } else {
//                            DB.NETWORK_ANTI_CHEAT_DATA.insert("'" + name + "', '1'");
//                        }
//                    }
//                });
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        banned.remove(event.getPlayer().getName());
    }
}
