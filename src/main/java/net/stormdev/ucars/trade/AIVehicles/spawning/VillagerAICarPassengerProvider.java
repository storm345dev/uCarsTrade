package net.stormdev.ucars.trade.AIVehicles.spawning;

import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.utils.NPCOrientationUtil;
import net.stormdev.ucars.utils.NoMobAI;
import net.stormdev.ucarstrade.cars.DrivenCar;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;

public class VillagerAICarPassengerProvider implements AICarPassengerProvider{
    @Override
    public LivingEntity spawnNewPassenger(Location spawnLoc, DrivenCar carTheyWillBeDriving, String name) {
        Location sl = spawnLoc.clone();
        sl.setYaw(sl.getYaw()-90);
        final Villager v = (Villager) spawnLoc.getWorld().spawnEntity(sl, EntityType.VILLAGER);
        v.setAdult();
        v.setBreed(false);
        v.setAgeLock(true);
        v.setCanPickupItems(false);
        v.setCustomName(name);
        v.setCustomNameVisible(true);
        v.setMaxHealth(5);
        v.setHealth(5);
        try {
            NoMobAI.clearAI(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void setPassengerYaw(LivingEntity passenger, float yaw) {
        NPCOrientationUtil.setYaw(passenger, yaw);
    }
}
