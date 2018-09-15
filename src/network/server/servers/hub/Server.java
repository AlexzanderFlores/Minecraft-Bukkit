package network.server.servers.hub;

import com.sun.net.httpserver.HttpServer;
import network.player.DiscordHandler;
import network.server.tasks.AsyncDelayedTask;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Server {
	Server() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				try {
					HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
					server.createContext("/link-discord", DiscordHandler.getInstance());
					server.setExecutor(null);
					server.start();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		});
    }

    public static void post(String path) {
		try {
			URL url = new URL(path);
			URLConnection con = url.openConnection();
			HttpURLConnection http = (HttpURLConnection) con;
			http.setRequestMethod("POST");
			http.setDoOutput(true);

			byte [] out = new byte [] {};

			http.setFixedLengthStreamingMode(out.length);
			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			http.connect();
			OutputStream stream = http.getOutputStream();
			stream.write(out);

			System.out.println(path);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
