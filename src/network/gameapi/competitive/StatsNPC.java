package network.gameapi.competitive;

import npc.NPCEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StatsNPC {
    public StatsNPC(Location location, Location target) {
        new NPCEntity(EntityType.SNOWMAN, "&eView Your Stats&7 (Click)", location, target) {
            @Override
            public void onInteract(Player player) {
                player.chat("/stats");
            }
        };
    }
}
