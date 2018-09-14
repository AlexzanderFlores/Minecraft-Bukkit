package network.server.effects.images;

import network.server.tasks.AsyncDelayedTask;
import network.server.util.FileHandler;
import network.server.util.ImageMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.inventivetalent.animatedframes.AnimatedFrame;
import org.inventivetalent.animatedframes.AnimatedFramesPlugin;
import org.inventivetalent.animatedframes.Callback;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class DisplayImage {
    private Location bottomLeft;
    private Location topRight;
    private String url;
    private UUID name;

    public DisplayImage(Location bottomLeft, Location topRight) {
        this(bottomLeft, topRight, null);
    }

    public DisplayImage(Location bottomLeft, Location topRight, String url) {
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.url = url;
        this.name = UUID.randomUUID();
    }

    protected String getUrl() {
        return this.url;
    }

    protected DisplayImage setUrl(String url) {
        this.url = url;
        return this;
    }

    protected String getName() {
        return this.name.toString();
    }

    public void display() {
        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                AnimatedFramesPlugin plugin = (AnimatedFramesPlugin) Bukkit.getPluginManager().getPlugin("AnimatedFrames");

                ItemFrame firstFrame = ImageMap.getItemFrame(bottomLeft);
                ItemFrame secondFrame = ImageMap.getItemFrame(topRight);

                if(firstFrame == null || secondFrame == null) {
                    return;
                }

                AnimatedFrame animatedFrame = null;
                do {
                    try {
                        animatedFrame = plugin.frameManager.createFrame(UUID.randomUUID().toString(), url, firstFrame, secondFrame);
                    } catch(Exception e) {}
                } while(animatedFrame == null);

                AnimatedFrame frame = animatedFrame;
                Bukkit.getLogger().info("");
                Bukkit.getLogger().info(frame.getHeight() + " x " + frame.getWidth());
                Bukkit.getLogger().info("");
//                frame.creator = player.getUniqueId();

                // Save frame & index
                plugin.frameManager.writeToFile(frame);
                plugin.frameManager.writeIndexToFile();

                frame.refresh();
                plugin.frameManager.startFrame(frame);

                frame.startCallback = new Callback<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
//                                frame.addViewer(player);

                                // Add players in the world
                                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        for(Player player : bottomLeft.getWorld().getPlayers()) {
//                                            if(player.getUniqueId().equals(player.getUniqueId())) {
//                                                continue; // Skip the creator
//                                            }
                                            frame.addViewer(player);
                                        }
                                    }
                                }, 20);
                            }
                        }, 40);
                    }
                };

                frame.setPlaying(true);
            }
        });
    }
}
