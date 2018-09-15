package network.player;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network.Network;
import network.customevents.player.PlayerRankChangeEvent;
import network.server.CommandBase;
import network.server.DB;
import network.server.servers.hub.Server;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordHandler implements HttpHandler, Listener {
    private static DiscordHandler instance = null;
    private static Map<String, UUID> pendingDiscordLinks = null; // <Discord ID, UUID>

    public DiscordHandler() {
        instance = this;
        pendingDiscordLinks = new HashMap<String, UUID>();

        new CommandBase("discord") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                MessageHandler.sendLine(sender);
                MessageHandler.sendMessage(sender, "Join our Discord: &bhttps://discord.gg/HKAVuPc");
                MessageHandler.sendMessage(sender, "");
                MessageHandler.sendMessage(sender, "Post your IGN in the &b#link-minecraft&x text channel to link your account with our Discord server.");
                MessageHandler.sendLine(sender);
                return true;
            }
        };

        new CommandBase("confirmDiscord", true) {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
                if(!Network.getServerName().equalsIgnoreCase("HUB1")) {
                    MessageHandler.sendMessage(sender, "&cYou can only run this command on hub 1, run &b/join hub1");
                    return true;
                }

                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();

                for(String discord : pendingDiscordLinks.keySet()) {
                    if(pendingDiscordLinks.get(discord) == uuid) {
                        DB.PLAYERS_DISCORD.insert("'" + uuid.toString() + "', '" + discord + "'");
                        MessageHandler.sendMessage(player, "Your Discord has been successfully linked!");
                        return true;
                    }
                }

                MessageHandler.sendMessage(player, "&cYou have no pending Discord link requests, please post your IGN in the &b#link-minecraft&c text channel.");

                return true;
            }
        };

        EventUtil.register(this);
    }

    @EventHandler
    public void onPlayerRankChange(PlayerRankChangeEvent event) {
        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                UUID uuid = player.getUniqueId();

                if(DB.PLAYERS_DISCORD.isUUIDSet(uuid)) {
                    String discordId = DB.PLAYERS_DISCORD.getString("uuid", uuid.toString(), "discord");
                    if(discordId != null) {
                        Server.post("http://localhost:8081/change-rank?d=" + discordId + "&r=" + event.getRank().toString());
                    }
                }
            }
        });
    }

    public static DiscordHandler getInstance() {
        if(instance == null) {
            new DiscordHandler();
        }
        return instance;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response;

        BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        String input = reader.readLine();
        String [] split = input.split("-");
        String name = split[0];
        String discord = split[1];

        Player player = Bukkit.getPlayer(name);
        if(player == null || !player.isOnline()) {
            response = "You can only run this command while you are in Hub 1";
        } else {
            UUID uuid = player.getUniqueId();
            pendingDiscordLinks.put(discord, uuid);
            response = "Please run **/confirmDiscord** in hub1 within the next 30 seconds";
            MessageHandler.sendMessage(player, "Please run &b/confirmDiscord&x within the next 60 seconds ");

            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    pendingDiscordLinks.remove(discord);
                }
            }, 20 * 30);
        }

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
