package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;

public class StudySummary {
	
	private String id = null;
	private ArrayList<String> entryList = new ArrayList<String>();
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void addEntry(String entry) {
		this.entryList.add(entry);
	}
	
	public ArrayList<String> getSummaryEntries() {
		return this.entryList;
	}
	
	public void setSummaryEntries(ArrayList<String> summaryEntries) {
		this.entryList = summaryEntries;
	}

}
