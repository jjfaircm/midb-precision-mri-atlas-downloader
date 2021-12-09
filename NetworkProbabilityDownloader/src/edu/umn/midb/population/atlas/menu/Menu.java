package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;

public class Menu {
	
	private ArrayList<MenuEntry> menuEntryList = new ArrayList<MenuEntry>();
	
	public void addMenuEntry(MenuEntry menuEntry) {
		this.menuEntryList.add(menuEntry);
	}
	
	public ArrayList<MenuEntry> getMenuEntries() {
		return this.menuEntryList;
	}

}
