package edu.umn.midb.population.atlas.tasks;

/**
 * Encapsulates the necessary data for inserting a record into the admin_access table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class AdminAccessEntry extends TaskEntry {
	
	private String action = "unknown";
	private boolean validPassword = false;
	private String validPasswordString = "true";


	/**
	 * Returns the action that was requested by the remote client.
	 * 
	 * @return action - String
	 */
	public String getAction() {
		return action;
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
	 * Returns a boolean indicating if the provided password was valid
	 * 
	 * @return validPassword - boolean
	 */
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
