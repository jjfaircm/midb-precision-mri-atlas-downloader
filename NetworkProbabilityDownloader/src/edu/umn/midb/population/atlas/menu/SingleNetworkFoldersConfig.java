package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;


/**
 * 
 * Encapsulates the types of Single Neural Networks available for a specific study.
 * Each study will have a folderNamesConfig which is alist of available Single Network
 * names and the folders that map to the Single Network names. Examples are:

 * <ul>
 * <li>Auditory Network - Aud
 * <li>Visual Network - Vis
 * <li>Default Mode Network - DMN
 * </ul>
 * 
 * 
 * @author jjfair
 *
 */
public class SingleNetworkFoldersConfig {
	
	private String id = null;
	private ArrayList<String> folderNamesConfig = new ArrayList<String>();
	
	/**
	 * Returns the id of the folderNamesConfig. The id maps to the study name, such as
	 * abcd_template_matching.
	 * 
	 * @return id - String
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 
	 * Sets the id of the folderNamesConfig, The id maps to the study, such as
	 * abcd_template_matching.
	 * 
	 * @param id - String
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * Returns the folderNamesConfig which is an ArrayList of available Single Network names
	 * and the folder that each network name maps to.
	 * 
	 * @return folderNamesConfig - ArrayList of String
	 */
	public ArrayList<String> getFolderNamesConfig() {
		return folderNamesConfig;
	}
	
	/**
	 * Sets the folderNamesConfig which is an ArrayList of String. Each String will resemble an entry
	 * such as:  Auditory=Aud
	 * 
	 * @param folderNamesConfig - ArrayList of String
	 */
	public void setFolderNamesConfig(ArrayList<String> folderNamesConfig) {
		this.folderNamesConfig = folderNamesConfig;
	}

}
