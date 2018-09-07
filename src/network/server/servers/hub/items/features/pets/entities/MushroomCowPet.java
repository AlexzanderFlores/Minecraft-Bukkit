package network.server.servers.hub.items.features.pets.entities;

import net.minecraft.server.v1_8_R3.EntityMushroomCow;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import network.server.servers.hub.items.features.pets.EntityPet;
import network.server.servers.hub.items.features.pets.PathfinderGoalWalkToOwner;
import network.server.util.ReflectionUtil;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class MushroomCowPet extends EntityMushroomCow implements EntityPet {
    public MushroomCowPet(World world) {
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

    }

    @Override
    public void walkTo(Player player, float speed) {
        this.goalSelector.a(0, new PathfinderGoalWalkToOwner(this, speed, player));
    }

    @Override
    public Inventory getOptionsInventory(Player player, Inventory inventory) {
        return inventory;
    }

    @Override
    public void clickedOnCustomOption(Player player, ItemStack clicked) {

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
