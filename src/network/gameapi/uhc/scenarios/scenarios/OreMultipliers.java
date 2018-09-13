package network.gameapi.uhc.scenarios.scenarios;

import network.gameapi.uhc.scenarios.Scenario;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class OreMultipliers extends Scenario {
    private static OreMultipliers instance = null;
    private static int multiplier = 0;
    
    public OreMultipliers(String name, String shortName, int multiplier, Material material) {
        this(name, shortName, multiplier, new ItemStack(material));
    }

    public OreMultipliers(String name, String shortName, int multiplier, ItemStack item) {
        super(name, shortName, item);
        instance = this;
        setMultiplier(multiplier);
    }

    public static OreMultipliers getInstance(String name, String shortName, int multiplier, Material material) {
        return getInstance(name, shortName, multiplier, new ItemStack(material));
    }

    public static OreMultipliers getInstance(String name, String shortName, int multiplier, ItemStack item) {
        if(instance == null) {
            new OreMultipliers(name, shortName, multiplier, item);
        }
        return instance;
    }

    public static void setMultiplier(int multiplier) {
        OreMultipliers.multiplier = multiplier;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if(multiplier >= 2 && block.getType().toString().endsWith("_ORE")) {
            for(int a = 0; a < multiplier - 1; ++a) {
                for(ItemStack drop : block.getDrops()) {
                    block.getWorld().dropItem(block.getLocation(), drop);
                }
            }
        }
    }
}