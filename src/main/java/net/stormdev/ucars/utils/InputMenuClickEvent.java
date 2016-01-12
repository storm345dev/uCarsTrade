package net.stormdev.ucars.utils;

import net.stormdev.ucars.trade.guis.InputMenu;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InputMenuClickEvent extends Event implements Cancellable{
    public Boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();
    InputMenu.OptionClickEvent clickEvent = null;
    TradeBoothMenuType type = TradeBoothMenuType.MENU;
	public InputMenuClickEvent(InputMenu.OptionClickEvent clickEvent, TradeBoothMenuType type) {
		this.clickEvent = clickEvent;
		this.type = type;
	}
	public boolean isCancelled() {
		return this.cancelled;
	}
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}
	public TradeBoothMenuType getMenuType(){
		return type;
	}
	public InputMenu.OptionClickEvent getClickEvent(){
		return clickEvent;
	}
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
