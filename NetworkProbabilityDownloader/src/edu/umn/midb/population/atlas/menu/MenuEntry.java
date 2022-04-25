package edu.umn.midb.population.atlas.menu;



/**
 * Encapsulates a study menu entry which includes a 'displayName', an 'id', a 'dataType', and a list 
 * of 'subOptions' such as:
 * <ul>
 * <li>Combined Networks</li>
 * <li>Integration Zone</li>
 * <li>Single Networks</li>
 * </ul>
 * <p>
 * The data type can either be 'surface, 'volume', or 'surface_volume'.
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
