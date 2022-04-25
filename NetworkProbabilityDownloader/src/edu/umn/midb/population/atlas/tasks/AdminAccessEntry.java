package edu.umn.midb.population.atlas.tasks;

/**
 * Encapsulates the necessary data for inserting a record into the admin_access table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class AdminAccessEntry extends TaskEntry {
	
	private String action = "unknown";
	private boolean validIP = true;
	private String validIPString = "true";
	private boolean validPassword = false;
	private String validPasswordString = "true";


	/**
	 * The action that was requested by the remote client.
	 * 
	 * @return action - String
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Returns a String ('true' or 'false') indicating if the remote ip address was found
	 * in the access control list (acl.conf).
	 * 
	 * 
	 * @return validIPString - String
	 */
	public String getValidIPString() {
		return validIPString;
	}

	/**
	 * 
	 * Returns a String ('true' or 'false') indicating if the client has input the
	 * correct password.
	 * 
	 * @return validPasswordString - String
	 */
	public String getValidPasswordString() {
		return validPasswordString;
	}

	/**
	 * Returns a boolean indicating if the ip address was in the access control list (acl.conf).
	 * 
	 * @return validIP - boolean
	 */
	public boolean isValidIP() {
		return validIP;
	}

	public boolean isValidPassword() {
		return validPassword;
	}

	/**
	 * Sets the action that was requested by the remote client.
	 * 
	 * @param action - String
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Sets the validIP attributes that indicates if the remote ip address was found in
	 * the access control list (acl.conf).
	 * 
	 * @param validIP - boolean
	 */
	public void setValidIP(boolean validIP) {
		this.validIP = validIP;
		if(!this.validIP) {
			this.validIPString = "false";
		}
		else {
			this.validIPString = "true";
		}
	}
	
	/**
	 * Sets the validPassword boolean attribute indicating if the client input the correct
	 * password.
	 * 
	 * @param validPassword - boolean indicating if the password was correct
	 */
	public void setValidPassword(boolean validPassword) {
		this.validPassword = validPassword;
		if(!validPassword) {
			this.validPasswordString = "false";
		}
		else {
			this.validPassword = true;
			this.validPasswordString = "true";
		}
	}

}
