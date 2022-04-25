package edu.umn.midb.population.atlas.data.access;

/**
 * Object encapsulating data retrieved from the admin_access table in MYSQL. This
 * contains data that will be displayed in a table on the client browser.
 * 
 */
public class AdminAccessRecord extends BaseRecord {
	
	private String action = "undefined";
	private String validPassword = "undefined";
	
	/**
	 * Hides the default constructor
	 * 
	 */
	private AdminAccessRecord() {
		
	}

	/**
	 * Constructor 
	 * @param ipAddress - String, ipAddress of the client that was accessing the admin console
	 * @param currentAction - String, designates the action being requested, such as removedStudy
	 */
	public AdminAccessRecord(String ipAddress, String currentAction) {
		this.ipAddress = ipAddress;
		this.action = currentAction;
	}

	/**
	 * Returns the action that was requested (such as addStudy)
	 * 
	 * @return action - String
	 */
	public String getAction() {
		return action;
	}
	
	/**
	 * Returns a boolean indicating if the user input the correct password.
	 * 
	 * @return validPassword boolean
	 */
	public String getValidPassword() {
		return validPassword;
	}


	/**
	 * Sets the requested action (such as addStudy)
	 * 
	 * @param action - String
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Sets the validPassword attribute.
	 * 
	 * @param validPassword - boolean
	 */
	public void setValidPassword(String validPassword) {
		this.validPassword = validPassword;
	}
	


}
