package net.stormdev.ucars.trade.guis;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class IconMenu implements Listener,InventoryHolder {

	private String name;
	private int size;
	private OptionClickEventHandler handler;
	private Plugin plugin;

	private String[] optionNames;
	private ItemStack[] optionIcons;
	private Boolean enabled = true;
	private String metaData;
	private boolean destroyOnClose = false;
	private UUID selfID = UUID.randomUUID();

	public IconMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.plugin = plugin;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		this.metaData = "menu." + UUID.randomUUID().toString();
		if(this.plugin == null){
			System.out.println("UH OH Plugin null in iconmenu = memory leak");
		}
		/*plugin.getServer().getPluginManager().registerEvents(this, plugin);*/
	}
	
	public IconMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin, boolean destroyOnClose) {
		this(name, size, handler, plugin);
		this.destroyOnClose = destroyOnClose;
	}

	public IconMenu setOption(int position, ItemStack icon, String name,
			String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}
	
	public IconMenu appendOption(ItemStack icon, String name, List<String> info){
		int position = 0;
		for(int i=position;position<optionIcons.length;i++){
			try {
				if(optionIcons[i] == null){
					break;
				}
			} catch (Exception e) {
				break;
			}
			position++;
		}
		
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}

	public IconMenu setOption(int position, ItemStack icon, String name,
			List<String> info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}

	public void open(Player player) {
		Inventory inventory = Bukkit.createInventory(this, size, name);
		enabled = true;
		name = inventory.getTitle();
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		player.openInventory(inventory);
		UEntityMeta.setMetadata(player, metaData, new StatValue(null, plugin));
		//Bukkit.broadcastMessage("Player has got meta:"+metaData);
	}

	public void destroy() {
		/*try {
			HandlerList.unregisterAll(this);
		} catch (Exception e) {
			System.out.println("UH OH Bukkit didn't want to unregister the IconMenu's events! Therefore MEMORY LEAK!!!");
			e.printStackTrace();
		}*/
		handler = null;
		optionNames = null;
		optionIcons = null;
		enabled = false;
	}
	
/*	@EventHandler(priority = EventPriority.MONITOR)*/
	void invClose(InventoryCloseEvent event){
		if(/*event.getPlayer().hasMetadata(metaData)*/event.getInventory().getHolder() != null && event.getInventory().getHolder().equals(this)){
			if(this.plugin == null){
				Bukkit.broadcastMessage("PLUGIN NULL HALP HALP");
			}
			if(!event.getInventory().getName().equals(name)){
				return;
			}
			UEntityMeta.removeMetadata(event.getPlayer(), metaData);
			if(destroyOnClose){
				destroy();
			}
		}
	}

	/*@EventHandler(priority = EventPriority.MONITOR)*/
	void onInventoryClick(InventoryClickEvent event) {
		InventoryHolder ih = event.getInventory().getHolder();
		boolean sameHolder = ih != null && ih instanceof IconMenu && ((IconMenu)ih).selfID.equals(this.selfID);
		if (/*event.getInventory().getTitle().equals(name) && */
				/*&& event.getWhoClicked().hasMetadata(metaData)&&*/ sameHolder) {
			event.setCancelled(true);
			if(!enabled){
				return;
			}
			int slot = event.getRawSlot();
			if (slot >= 0 && slot < size && optionNames[slot] != null) {
				final Plugin plugin = this.plugin;
				OptionClickEvent e = new OptionClickEvent(event.getInventory(), this,
						(Player) event.getWhoClicked(), slot, optionNames[slot], event);
				handler.onOptionClick(e);
				if (e.willClose()) {
					final Player p = (Player) event.getWhoClicked();
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							p.closeInventory();
							UEntityMeta.removeMetadata(p, metaData);
						}
					}, 1);
				}
				if (e.willDestroy()) {
					destroy();
				}
			}
		}
	}

	public interface OptionClickEventHandler {
		public void onOptionClick(OptionClickEvent event);
	}

	public class OptionClickEvent {
		private Player player;
		private int position;
		private String name;
		private boolean close;
		private boolean destroy;
		private Inventory inv;
		private IconMenu menu;
		private InventoryClickEvent parentEvent;

		public OptionClickEvent(Inventory inv, IconMenu menu, Player player, int position, String name, InventoryClickEvent parentEvent) {
			this.player = player;
			this.position = position;
			this.name = name;
			this.close = true;
			this.destroy = false;
			this.inv = inv;
			this.menu = menu;
			this.parentEvent = parentEvent;
		}
		
		public InventoryClickEvent getParentEvent(){
			return this.parentEvent;
		}
		
		public Inventory getInventory(){
			return inv;
		}
		
		public IconMenu getMenu(){
			return menu;
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

		public void setWillClose(boolean close) {
			this.close = close;
		}

		public void setWillDestroy(boolean destroy) {
			this.destroy = destroy;
		}
	}
	
	public ItemStack[] getOptions(){
		return this.optionIcons;
	}
	
	public void setOptions(ItemStack[] opts){
		this.optionIcons = opts;
	}

	public ItemStack setItemNameAndLore(ItemStack item, String name,
			String... lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}

	public ItemStack setItemNameAndLore(ItemStack item, String name,
			List<String> lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}
