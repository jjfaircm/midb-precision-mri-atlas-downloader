package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;

/**
 * 
 * Encapsulates the summary text that appears for a study in the browser underneath
 * the main image panel. The summary displays as a list.
 * 
 * @author jjfair
 *
 */
public class StudySummary {
	
	private String id = null;
	private ArrayList<String> entryList = new ArrayList<String>();
	
	/**
	 * Adds an entry to the entryList. Each entry will appear as a list item in the 
	 * browser.
	 *  
	 * @param entry - String
	 */
	public void addEntry(String entry) {
		this.entryList.add(entry);
	}
	
	/**
	 * Returns the id which is the name of the study, such as abcd_template_matching
	 * 
	 * 
	 * @return id - String
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 
	 * Returns the entryList which is an ArrayList of String.
	 * 
	 * @return entryList - ArrayList of String
	 */
	public ArrayList<String> getSummaryEntries() {
		return this.entryList;
	}
	
	/**
	 * 
	 * Sets the id, which maps to the study name containing this summary.
	 * 
	 * @param id - String
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * Sets the entryList.
	 * 
	 * @param summaryEntries - ArrayList of String
	 */
	public void setSummaryEntries(ArrayList<String> summaryEntries) {
		this.entryList = summaryEntries;
	}

}
