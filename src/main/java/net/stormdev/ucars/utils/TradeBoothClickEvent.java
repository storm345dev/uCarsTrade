package net.stormdev.ucars.utils;

import net.stormdev.ucars.trade.guis.IconMenu;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TradeBoothClickEvent extends Event implements Cancellable{
    public Boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();
    IconMenu.OptionClickEvent clickEvent = null;
    int page = 1;
    Object[] args = new Object[]{};
    TradeBoothMenuType type = TradeBoothMenuType.MENU;
	public TradeBoothClickEvent(IconMenu.OptionClickEvent clickEvent, TradeBoothMenuType type, int absolutePageNumber) {
		this.clickEvent = clickEvent;
		this.page = absolutePageNumber;
		this.type = type;
	}
	public TradeBoothClickEvent(IconMenu.OptionClickEvent clickEvent, TradeBoothMenuType type, int absolutePageNumber, Object[] args) {
		this.clickEvent = clickEvent;
		this.page = absolutePageNumber;
		this.type = type;
		this.args = args;
	}
	public Object[] getArgs(){
		return args;
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
	public int getPage(){
		return page;
	}
	public IconMenu.OptionClickEvent getClickEvent(){
		return clickEvent;
	}
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
