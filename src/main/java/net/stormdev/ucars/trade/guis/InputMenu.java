package net.stormdev.ucars.trade.guis;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class InputMenu implements Listener,InventoryHolder {

	private String name;
	private int size;
	private OptionClickEventHandler handler;
	private Plugin plugin;

	private String[] optionNames;
	private ItemStack[] optionIcons;
	private ArrayList<Integer> cancelSlots = new ArrayList<Integer>();
	
	private Inventory inventory;
	private boolean destroyOnClose;

	public InputMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin, boolean destroyOnClose) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.plugin = plugin;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		this.destroyOnClose = destroyOnClose;
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public InputMenu setOption(int position, ItemStack icon, String name,
			String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		cancelSlots.add(position);
		return this;
	}
	
	public InputMenu setItem(int position, ItemStack icon, String name,
			String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		//cancelSlots.add(position);
		return this;
	}
	
	public void create(InventoryHolder holder){
		inventory = Bukkit.createInventory(this, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
		handler = null;
		plugin = null;
		optionNames = null;
		optionIcons = null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void close(InventoryCloseEvent event){
		if(!(event.getPlayer() instanceof Player)){
			return;
		}
		if (event.getView().getTopInventory().getHolder() != null && event.getView().getTopInventory().getHolder().equals(this)) {
			try {
				if(inventory != null && handler != null){
					handler.onClose((Player)event.getPlayer(), inventory);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(destroyOnClose){
				destroy();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onInventoryClick(InventoryClickEvent event) {
		if(event.isShiftClick()){
			event.setCancelled(true);
			return;
		}
		if (event.getView().getTopInventory().equals(inventory) && event.getView().getTopInventory().getHolder() != null && event.getView().getTopInventory().getHolder().equals(this)) {
			int slot = event.getRawSlot();
			if (cancelSlots.contains(slot)) {
				event.setCancelled(true);
			}
			if (slot >= 0 && slot < size) {
				Plugin plugin = this.plugin;
				OptionClickEvent e = new OptionClickEvent(
						(Player) event.getWhoClicked(), slot,
						optionNames[slot], event.getInventory(), this, event);
				handler.onOptionClick(e);
				if (e.willClose()) {
					final Player p = (Player) event.getWhoClicked();
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							p.closeInventory();
						}
					}, 1l);
				}
				if(e.willCancelClick()){
					event.setCancelled(true);
				}
				if (e.willDestroy()) {
					destroy();
				}
			}
		}
	}

	public interface OptionClickEventHandler {
		public void onOptionClick(OptionClickEvent event);
		public void onClose(Player player, Inventory inv);
	}

	public class OptionClickEvent {
		private Player player;
		private int position;
		private String name;
		private boolean close;
		private boolean destroy;
		private boolean cancelClick = false;
		private Inventory i = null;
		private InputMenu im = null;
		private InventoryClickEvent parent;

		public OptionClickEvent(Player player, int position, String name,
				Inventory i, InputMenu im, InventoryClickEvent parent) {
			this.player = player;
			this.position = position;
			this.name = name;
			this.close = false;
			this.destroy = false;
			this.i = i;
			this.im = im;
			this.parent = parent;
		}
		
		public InventoryClickEvent getParentEvent(){
			return this.parent;
		}

		public InputMenu getMenu() {
			return im;
		}

		public Inventory getInventory() {
			return i;
		}

		public Player getPlayer() {
			return player;
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return name;
		}

		public boolean willClose() {
			return close;
		}

		public boolean willDestroy() {
			return destroy;
		}
		
		public boolean willCancelClick(){
			return cancelClick;
		}
		
		public void setCancelClick(boolean click){
			cancelClick = click;
		}

		public void setWillClose(boolean close) {
			this.close = close;
		}

		public void setWillDestroy(boolean destroy) {
			this.destroy = destroy;
		}
	}

	public ItemStack setItemNameAndLore(ItemStack item, String name,
			String... lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}
