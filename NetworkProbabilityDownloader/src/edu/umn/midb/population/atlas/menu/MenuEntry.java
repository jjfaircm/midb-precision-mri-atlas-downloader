package edu.umn.midb.population.atlas.menu;

public class MenuEntry {
	
	private String displayName = null;
	private String id = null;
	private String dataType = null;
	
	private String[] subOptions = null;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String[] getSubOptions() {
		return subOptions;
	}

	public void setSubOptions(String[] subOptions) {
		this.subOptions = subOptions;
	}

}
