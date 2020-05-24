package net.stormdev.ucars.utils;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;

public class CustomEntityHandler {
	
	public static void registerEntity(String name, int id, Class<?> nmsClass, Class<? extends Entity> customClass) {
        try {

            MinecraftKey minecraftKey = new MinecraftKey(name);
            EntityTypes.b.a(id, minecraftKey, customClass);


//            /*
//            * First, we make a list of all HashMap's in the EntityTypes class
//            * by looping through all fields. I am using reflection here so we
//            * have no problems later when minecraft changes the field's name.
//            * By creating a list of these maps we can easily modify them later
//            * on.
//            */
//            List<Map<?, ?>> dataMaps = new ArrayList<Map<?, ?>>();
//            for (Field f : EntityTypes.class.getDeclaredFields()) {
//                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
//                    f.setAccessible(true);
//                    dataMaps.add((Map<?, ?>) f.get(null));
//                }
//            }
//
//            /*
//            * since minecraft checks if an id has already been registered, we
//            * have to remove the old entity class before we can register our
//            * custom one
//            *
//            * map 0 is the map with names and map 2 is the map with ids
//            */
//            if (dataMaps.get(2).containsKey(id)) {
//                dataMaps.get(0).remove(name);
//                dataMaps.get(2).remove(id);
//            }
//
//            /*
//            * now we call the method which adds the entity to the lists in the
//            * EntityTypes class, now we are actually 'registering' our entity
//            */
//            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
//            method.setAccessible(true);
//            method.invoke(null, customClass, name, id);
//
//            /*
//            * after doing the basic registering stuff , we have to register our
//            * mob as to be the default for every biome. This can be done by
//            * looping through all BiomeBase fields in the BiomeBase class, so
//            * we can loop though all available biomes afterwards. Here, again,
//            * I am using reflection so we have no problems later when minecraft
//            * changes the fields name
//            */
//            for (Field f : BiomeBase.class.getDeclaredFields()) {
//                if (f.getType().getSimpleName().equals(BiomeBase.class.getSimpleName())) {
//                    if (f.get(null) != null) {
//
//                        /*
//                        * this peace of code is being called for every biome,
//                        * we are going to loop through all fields in the
//                        * BiomeBase class so we can detect which of them are
//                        * Lists (again, to prevent problems when the field's
//                        * name changes), by doing this we can easily get the 4
//                        * required lists without using the name (which probably
//                        * changes every version)
//                        */
//                        for (Field list : BiomeBase.class.getDeclaredFields()) {
//                            if (list.getType().getSimpleName().equals(List.class.getSimpleName())) {
//                                list.setAccessible(true);
//                                @SuppressWarnings("unchecked")
//                                List<BiomeBase.BiomeMeta> metaList = (List<BiomeBase.BiomeMeta>) list.get(f.get(null));
//
//                                /*
//                                * now we are almost done. This peace of code
//                                * we're in now is called for every biome. Loop
//                                * though the list with BiomeMeta, if the
//                                * BiomeMeta's entity is the one you want to
//                                * change (for example if EntitySkeleton matches
//                                * EntitySkeleton) we will change it to our
//                                * custom entity class
//                                */
//                                for (BiomeBase.BiomeMeta meta : metaList) {
//                                    Field clazz = BiomeBase.BiomeMeta.class.getDeclaredFields()[0];
//                                    if (clazz.get(meta).equals(nmsClass)) {
//                                        clazz.set(meta, customClass);
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//                }
//            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/*public static void registerEntityType(Class<?> inClass, String name, int inID)
	{
		try
		{
            @SuppressWarnings("rawtypes")
            Class[] args = new Class[3];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = int.class;
 
            Method a = EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);
 
            a.invoke(a, inClass, name, inID);
        }
		catch (Exception e)
		{
            e.printStackTrace();
        }
	}
	public static void registerEntityType(Class<?> inClass, String name, int inID, int ma, int mb)
	{
		try
		{
            @SuppressWarnings("rawtypes")
            Class[] args = new Class[5];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = int.class;
            args[3] = int.class;
            args[4] = int.class;
 
            Method a = EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);
 
            a.invoke(a, inClass, name, inID, ma, mb);
        }
		catch (Exception e)
		{
            e.printStackTrace();
        }
	}*/
}
