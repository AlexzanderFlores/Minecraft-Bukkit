package network.server.servers.hub.items.features.pets.entities;

import net.minecraft.server.v1_8_R3.EntitySheep;
import net.minecraft.server.v1_8_R3.EnumColor;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.servers.hub.items.features.pets.EntityPet;
import network.server.servers.hub.items.features.pets.PathfinderGoalWalkToOwner;
import network.server.util.ItemCreator;
import network.server.util.ReflectionUtil;
import network.server.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class SheepPet extends EntitySheep implements EntityPet {
    public static List<Sheep> rainbowSheep = null;
    private String rainbow = null;

    public SheepPet(World world) {
        super(world);
        try {
            for(String fieldName : new String[]{"b", "c"}) {
                Field field = ReflectionUtil.getDeclaredField(PathfinderGoalSelector.class, fieldName);
                field.setAccessible(true);
                field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
                field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSpawn(Player player) {
        rainbow = StringUtil.color("&aR&ba&ci&en&6b&3o&9w");
    }

    @Override
    public void walkTo(Player player, float speed) {
        this.goalSelector.a(0, new PathfinderGoalWalkToOwner(this, speed, player));
    }

    @Override
    public Inventory getOptionsInventory(Player player, Inventory inventory) {
        Inventory newInventory = Bukkit.createInventory(player, 9 * 3, inventory.getTitle());
        for(ItemStack itemStack : inventory.getContents()) {
            if(itemStack != null && itemStack.getType() != Material.AIR) {
                newInventory.addItem(itemStack);
            }
        }
        int counter = 9;
        for(DyeColor dyeColor : DyeColor.values()) {
            newInventory.setItem(counter++, new ItemCreator(new ItemStack(Material.WOOL, 1, dyeColor.getWoolData())).setName("&aDye your sheep " + dyeColor.toString().toLowerCase().replace("_", " ")).getItemStack());
        }
        newInventory.setItem(counter, new ItemCreator(Material.EMERALD).setName(rainbow).getItemStack());
        return newInventory;
    }

    @Override
    public void clickedOnCustomOption(Player player, ItemStack clicked) {
        if(clicked.getType() == Material.WOOL) {
            if(Ranks.PRO.hasRank(player)) {
                setColor(EnumColor.fromColorIndex(clicked.getData().getData()));
            } else {
                MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
            }
        } else if(clicked.getType() == Material.EMERALD) {
            if(Ranks.PRO_PLUS.hasRank(player)) {
                Sheep sheep = (Sheep) getBukkitEntity();
                if(rainbowSheep != null && rainbowSheep.contains(sheep)) {
                    rainbowSheep.remove(sheep);
                    MessageHandler.sendMessage(player, rainbow + " &amode &cdisabled");
                } else {
                    if(rainbowSheep == null) {
                        rainbowSheep = new ArrayList<Sheep>();
                    }
                    rainbowSheep.add(sheep);
                    MessageHandler.sendMessage(player, rainbow + "&a mode &eenabled");
                }
            } else {
                MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
            }
        }
    }

    @Override
    public void togglePetStaying(Player player) {

    }

    @Override
    public void togglePetSounds(Player player) {

    }

    @Override
    public void makeSound(Player player) {
//        makeSound(super.t(), this.bf(), this.bg());
    }

    @Override
    public void makeHurtSound(Player player) {
//        makeSound(super.aT(), this.bf(), this.bg());
    }

    @Override
    public void remove(Player player) {
        if(rainbowSheep != null) {
            Sheep sheep = (Sheep) getBukkitEntity();
            rainbowSheep.remove(sheep);
        }
    }

//    @Override
//    protected String t() {
//        return null;
//    }
//
//    @Override
//    protected String aT() {
//        return null;
//    }
//
//    @Override
//    protected String aU() {
//        return null;
//    }
//
//    @Override
//    protected void a(int i, int j, int k, Block block) {
//
//    }
//
//    @Override
//    public void aD() {
//        super.aD();
//        this.getAttributeInstance(GenericAttributes.b).setValue(1000.0D);
//    }
//
//    @Override
//    public void e(float f, float f1) {
//        if(this.passenger != null && (this.passenger instanceof EntityLiving)) {
//            this.lastYaw = (this.yaw = this.passenger.yaw);
//            this.pitch = (this.passenger.pitch * 0.5F);
//            b(this.yaw, this.pitch);
//            this.aO = (this.aM = this.yaw);
//            f = ((EntityLiving) this.passenger).bd * 0.5F;
//            f1 = ((EntityLiving) this.passenger).be;
//            if(f1 <= 0.0F) {
//                f1 *= 0.25F;
//            }
//            this.W = 1.0F;
//            this.aQ = (bl() * 0.1F);
//            if(!this.world.isStatic) {
//                i((float) getAttributeInstance(GenericAttributes.d).getValue());
//                super.e(f, f1);
//            }
//            this.aE = this.aF;
//            double d0 = this.locX - this.lastX;
//            double d1 = this.locZ - this.lastZ;
//            float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
//            if(f4 > 1.0F) {
//                f4 = 1.0F;
//            }
//            this.aF += (f4 - this.aF) * 0.4F;
//            this.aG += this.aF;
//        } else {
//            this.W = 0.5F;
//            this.aQ = 0.02F;
//            super.e(f, f1);
//        }
//    }
}
