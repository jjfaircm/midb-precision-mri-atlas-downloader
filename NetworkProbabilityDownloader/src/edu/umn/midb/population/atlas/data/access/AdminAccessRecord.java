package edu.umn.midb.population.atlas.data.access;

/**
 * Object encapsulating data retrieved from the admin_access table in MYSQL. This
 * contains data that will be displayed in a table on the client browser.
 * 
 */
public class AdminAccessRecord extends BaseRecord {
	
	private String validIP = "true";
	private String action = "undefined";
	private String validPassword = "undefined";
	
	/**
	 * Returns a boolean indicating if the user input the correct password.
	 * 
	 * @return validPassword boolean
	 */
	public String getValidPassword() {
		return validPassword;
	}

	/**
	 * Sets the validPassword attribute.
	 * 
	 * @param validPassword - boolean
	 */
	public void setValidPassword(String validPassword) {
		this.validPassword = validPassword;
	}

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
	 *
	 * Returns a boolean indicating if the ip address of the remote user was in the acl.conf file
	 *
	 * @param isInvalidIP - boolean
	 */
	public void isValidIP(String isInvalidIP) {
		this.validIP = isInvalidIP;
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
	 * Sets the requested action (such as addStudy)
	 * 
	 * @param action - String
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	/**
	 * Sets boolean indicating if the requestor ip address was found in the acl.conf
	 * 
	 * @param trueOrFalse - boolean
	 */
	public void setValidIP(String trueOrFalse) {
		this.validIP = trueOrFalse;
	}
	

}
