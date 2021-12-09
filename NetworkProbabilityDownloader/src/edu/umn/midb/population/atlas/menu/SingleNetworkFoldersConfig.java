package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;

public class SingleNetworkFoldersConfig {
	
	private String id = null;
	private ArrayList<String> folderNamesConfig = new ArrayList<String>();
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public ArrayList<String> getFolderNamesConfig() {
		return folderNamesConfig;
	}
	
	public void setFolderNamesConfig(ArrayList<String> folderNamesConfig) {
		this.folderNamesConfig = folderNamesConfig;
	}

}
