package network.server.servers.hub;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.player.MessageHandler;
import network.server.DB;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class WhitelistHandler implements HttpHandler, Listener {
    private static WhitelistHandler instance = null;

    WhitelistHandler() {
        instance = this;
        EventUtil.register(instance);
    }

    public static WhitelistHandler getInstance() {
        if(instance == null) {
            new WhitelistHandler();
        }

        return instance;
    }

    @EventHandler
    public void AsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName().toLowerCase();
        UUID uuid = event.getUniqueId();

        if(!DB.PLAYERS_WHITELISTED.isUUIDSet(uuid) && !DB.PLAYERS_WHITELISTED.isKeySet("name", name)) {
            event.setKickMessage(ChatColor.RED + "You are not whitelisted!\nCheck @ProMcGames on Twitter for instructions on how to whitelist yourself");
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler
    public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        UUID uuid = player.getUniqueId();

        DB.PLAYERS_WHITELISTED.updateString("uuid", uuid.toString(), "name", name);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "You have been whitelisted!";

        BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        String name = reader.readLine();

        DB.PLAYERS_WHITELISTED.insert("'" + null + "', '" + name + "'");

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
