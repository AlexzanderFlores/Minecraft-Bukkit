package network.server;

import network.customevents.player.InventoryItemClickEvent;
import network.server.util.EventUtil;
import network.server.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SocialMediaItems implements Listener {
    private static String discord;
    private static ItemStack discordItem;

    private static String twitter;
    private static ItemStack twitterItem;

    public SocialMediaItems() {
        discord = "&bJoin our Discord! &7(Click)";
        discordItem = ItemUtil.getSkull("Discord");

        twitter = "&bFollow us on Social Media! &7(Click)";
        twitterItem = ItemUtil.getSkull("Twitter");

        EventUtil.register(this);
    }

    public static void setDiscord(Inventory inventory, int slot) {
        inventory.setItem(slot, discordItem);
    }

    public static void setTwitter(Inventory inventory, int slot) {
        inventory.setItem(slot, twitterItem);
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(discord.equals(event.getItemTitle())) {
            Player player = event.getPlayer();
            player.closeInventory();
            player.chat("/discord");
            event.setCancelled(true);
        } else if(twitter.equals(event.getItemTitle())) {
            Player player = event.getPlayer();
            player.closeInventory();
            player.chat("/socialMedia");
            event.setCancelled(true);
        }
    }
}
