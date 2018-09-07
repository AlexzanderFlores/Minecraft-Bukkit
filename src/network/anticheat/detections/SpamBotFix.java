package network.anticheat.detections;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.PerformanceHandler;
import network.server.util.EventUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SpamBotFix implements Listener {
    public SpamBotFix() {
        EventUtil.register(this);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(PerformanceHandler.getPing(event.getPlayer()) == 0 && !AccountHandler.Ranks.VIP.hasRank(event.getPlayer())) {
            MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot talk in chat without a rank when you have a ping of 0. This is to prevent spam bots.");
            event.setCancelled(true);
        }
    }
}
