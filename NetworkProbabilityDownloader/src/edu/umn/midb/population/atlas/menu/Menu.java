package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;

/**
 * 
 * Encapsulates the study menu details that will be displayed on the client browser.
 * The client will dynamically build the menu based on the menu data retrieved from
 * the server.
 * 
 * @author jjfair
 *
 */
public class Menu {
	
	private ArrayList<MenuEntry> menuEntryList = new ArrayList<MenuEntry>();
	
	/**
	 * Add a {@link MenuEntry} for a specific study such as: ABCD - Template Matching.
	 * 
	 * 
	 * @param menuEntry - {@link MenuEntry}
	 */
	public void addMenuEntry(MenuEntry menuEntry) {
		this.menuEntryList.add(menuEntry);
	}
	
	/**
	 * 
	 * Returns the ArrayList of {@link MenuEntry} instances.
	 * 
	 * @return menuEntryList - ArrayList
	 */
	public ArrayList<MenuEntry> getMenuEntries() {
		return this.menuEntryList;
	}

}
