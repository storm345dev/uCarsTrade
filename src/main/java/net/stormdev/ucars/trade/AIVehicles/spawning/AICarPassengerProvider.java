package net.stormdev.ucars.trade.AIVehicles.spawning;

import net.stormdev.ucarstrade.cars.DrivenCar;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface AICarPassengerProvider {
    public LivingEntity spawnNewPassenger(Location spawnLoc, DrivenCar carTheyWillBeDriving, String entityName);
    public void setPassengerYaw(LivingEntity passenger, float yaw);
}
