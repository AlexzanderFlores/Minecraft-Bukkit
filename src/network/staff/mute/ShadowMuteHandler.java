package network.staff.mute;

import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShadowMuteHandler implements Listener {
    private static List<String> shadowMuted = null;
    private List<String> checkedForShadowMuted;

    public ShadowMuteHandler() {
        checkedForShadowMuted = new ArrayList<>();
        shadowMuted = new ArrayList<>();
        new CommandBase("shadowMute", 1) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                new AsyncDelayedTask(() -> {
                    Player target = ProPlugin.getPlayer(arguments[0]);
                    String name = null;
                    UUID uuid = null;
                    if(target == null) {
                        name = arguments[0];
                        uuid = AccountHandler.getUUID(name);
                        if(uuid == null) {
                            MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
                            return;
                        }
                    } else {
                        name = target.getName();
                        uuid = target.getUniqueId();
                    }
                    if(DB.STAFF_SHADOW_MUTES.isUUIDSet(uuid)) {
                        DB.STAFF_SHADOW_MUTES.deleteUUID(uuid);
                        shadowMuted.remove(name);
                        MessageHandler.sendMessage(sender, name + " is no longer shadow muted");
                    } else {
                        DB.STAFF_SHADOW_MUTES.insert("'" + uuid.toString() + "'");
                        shadowMuted.add(name);
                        MessageHandler.sendMessage(sender, name + " is now shadow muted");
                    }
                });
                return true;
            }
        }.setRequiredRank(Ranks.OWNER);
        EventUtil.register(this);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if(!checkedForShadowMuted.contains(player.getName())) {
            checkedForShadowMuted.add(player.getName());
            if(DB.STAFF_SHADOW_MUTES.isUUIDSet(player.getUniqueId())) {
                shadowMuted.add(player.getName());
            }
        }

        if(shadowMuted.contains(player.getName())) {
            event.getRecipients().clear();
            event.getRecipients().add(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        checkedForShadowMuted.remove(event.getPlayer().getName());
        shadowMuted.remove(event.getPlayer().getName());
    }
}
