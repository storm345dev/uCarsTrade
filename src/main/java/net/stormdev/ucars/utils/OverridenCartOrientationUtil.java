package net.stormdev.ucars.utils;

import com.useful.ucars.CartOrientationUtil.CartOrientationUtilOverride;
import com.useful.ucars.Reflect;
import net.stormdev.ucars.entity.Car;
import net.stormdev.ucars.entity.CarMinecraftEntity;
import net.stormdev.ucars.trade.main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class OverridenCartOrientationUtil implements CartOrientationUtilOverride {
    @Override
    public void setPitch(Entity cart, float pitch) {
        if(!(cart instanceof Minecart)){
            Car hc = CarMinecraftEntity.getCart(cart);
            if(hc != null){
                hc.setPitch(pitch);
                main.plugin.protocolManipulator.updateBoatRotationAngle((Car) cart);
            }
            return;
        }
        try {
            Class<?> cmr = cart.getClass();
            Method getHandle = cmr.getMethod("getHandle");
            Class<?> ema = Reflect.getNMSClass("EntityMinecartAbstract");
            Object nmsCart = getHandle.invoke(cmr.cast(cart));
            Field p = ema.getField("pitch");
            p.setAccessible(true);
            p.set(ema.cast(nmsCart), -pitch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setYaw(Entity cart, float yaw) {
        if(!(cart instanceof Minecart)){
            Car hc = CarMinecraftEntity.getCart(cart);
            if(hc != null){
                hc.setYaw(yaw);
            }
            return;
        }
        try {
            Class<?> cmr = cart.getClass();
            Method getHandle = cmr.getMethod("getHandle");
            Class<?> ema = Reflect.getNMSClass("EntityMinecartAbstract");
            Object nmsCart = getHandle.invoke(cmr.cast(cart));
            Field p = ema.getField("yaw");
            p.setAccessible(true);
            p.set(ema.cast(nmsCart), yaw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(cart instanceof Car) {
            main.plugin.protocolManipulator.updateBoatRotationAngle((Car) cart);
        }
    }

    @Override
    public void setRoll(Entity cart, float roll) {
        if(!(cart instanceof Minecart)){
            Car hc = CarMinecraftEntity.getCart(cart);
            if(hc != null){
                hc.setRoll(roll);
            }
            return;
        }
    }
}
