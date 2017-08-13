package net.comorevi.moneysland;

import cn.nukkit.plugin.PluginBase;
import net.comorevi.moneysland.SQLite3DataProvider;

public class MoneySLand extends PluginBase {
	
	private SQLite3DataProvider sql;
	
	@Override
	public void onEnable() {
		getDataFolder().mkdir();
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public SQLite3DataProvider getSQL() {
		return this.sql;
	}
}