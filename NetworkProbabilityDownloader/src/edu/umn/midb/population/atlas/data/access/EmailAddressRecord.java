package edu.umn.midb.population.atlas.data.access;


/**
 * Encapsulates data from an email address record that is retrieved from the email_addresseses
 * table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class EmailAddressRecord {

	private String emailAddress = null;
	private String firstName = null;
	private String lastName = null;
	
	
	/**
	 * Hides the empty constructor.
	 */
	private EmailAddressRecord() {
		
	}
	
	/**
	 * 
	 * Public constructor
	 * 
	 * @param emailAddress - String
	 * @param firstName - String
	 * @param lastName - String
	 */
	public EmailAddressRecord(String emailAddress, String firstName, String lastName) {
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	/**
	 * Returns the emailAddress
	 * 
	 * @return emailAddress - String
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	
	/**
	 * Sets the emailAddress
	 * 
	 * @param emailAddress - String
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	/**
	 * Returns the firstName
	 * 
	 * @return firstName - String
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * 
	 * Sets the firstName
	 * 
	 * @param firstName - String
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * 
	 * Returns the lastName
	 * 
	 * @return lastName - String
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * 
	 * Sets the lastName
	 * 
	 * @param lastName - String
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
}
