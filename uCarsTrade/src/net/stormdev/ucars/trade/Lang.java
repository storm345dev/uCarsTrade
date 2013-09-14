package net.stormdev.ucars.trade;

public class Lang {
public static String get(String key){
    String val = getRaw(key);
    val = main.colorise(val);
	return val;
}
public static String getRaw(String key){
	if(!main.lang.contains(key)){
		return key;
	}
	return main.lang.getString(key);
}
}
