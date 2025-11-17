package edu.umn.midb.population.atlas.menu;



/**
 * Encapsulates a study menu entry which includes a 'displayName', an 'id', a 'dataType', and a list 
 * of 'subOptions' such as:
 * <ul>
 * <li>Combined Networks</li>
 * <li>Integration Zone</li>
 * <li>Single Networks</li>
 * </ul>
 * 
 * <p>
 * The data type can either be 'surface, 'volume', or 'surface_volume'.
 * <p>
 * A MenuEntry object is constructed from entries in the menu.conf file that is found in the
 * midb folder (which is located in the root directory of the system). Here is an excerpt
 * from that file which represents a menu entry:
 * 
 * <p>MENU ENTRY (ID=abcd_template_matching)
 * <p>ABCD - Template Matching (abcd_template_matching) (surface)
 * <p>Single Networks (single)
 * <p>Combined Networks (combined_clusters)
 * <p>Integration Zone (overlapping)
 * <p>END MENU ENTRY
 * 
 * 
 * @author jjfair
 *
 */
public class MenuEntry {
	
	private String displayName = null;
	private String id = null;
	private String dataType = null;
	
	private String[] subOptions = null;


	/**
	 * Returns the dataType (surface, volume, or surface_volume).
	 * 
	 * @return dataType - String
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * Returns the display name of this study menu entry. For example: ABCD - Template Matching
	 * 
	 * @return displayName - String
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the id which maps to a folder under /midb/studies. The id is built from the
	 * study name. For example ABCD - Template Matching maps to a folder named abcd_template_matching
	 * 
	 * 
	 * @return id - String
	 */
	public String getId() {
		return id;
	}

	/**
	 * A list of the submenu options.
	 * 
	 * @return subOptions - ArrayList of String
	 */
	public String[] getSubOptions() {
		return subOptions;
	}

	/**
	 * Sets the dataType
	 * 
	 * @param dataType - String
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * Sets the displayName
	 * 
	 * @param displayName - String
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Sets the id
	 * 
	 * @param id - String
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the subOptions (which are the submenu options).
	 * 
	 * @param subOptions - ArrayList of String
	 */
	public void setSubOptions(String[] subOptions) {
		this.subOptions = subOptions;
	}

}
