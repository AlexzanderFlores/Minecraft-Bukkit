package network.gameapi.competitive;

import network.player.MessageHandler;
import npc.NPCEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StatsNPC {
    public StatsNPC(Location location) {
        new NPCEntity(EntityType.SNOWMAN, "&eView Your Stats&7 (Click)", location, location.getWorld().getSpawnLocation()) {
            @Override
            public void onInteract(Player player) {
                MessageHandler.sendMessage(player, "&cComing soon");
            }
        };
    }
}
