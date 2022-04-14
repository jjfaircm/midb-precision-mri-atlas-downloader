package edu.umn.midb.population.atlas.tasks;

/**
 * 
 * Encapsulates the data necessary to add a record to the email_addresses table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class EmailAddressEntry extends TaskEntry {
	
	private String firstName = null;
	private String lastName = null;
	private String emailAddress = null;
	
	/**
	 * Constructor
	 * 
	 * @param firstName - String
	 * @param lastName - string
	 * @param emailAddress - String
	 */
	public EmailAddressEntry(String firstName, String lastName, String emailAddress) {
		if(firstName==null) {
			throw new IllegalArgumentException("First name can not be null.");
		}
		if(lastName==null) {
			throw new IllegalArgumentException("Last name cannot be null.");
		}
		if(emailAddress==null) {
			throw new IllegalArgumentException("Email address cannot be null.");
		}

		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.subclassName = "EmailAddressEntry";
	}
	
	/**
	 * Default constructor. This is used to send an entry to the {@link EmailTracker} thread
	 * that shutdown is occuring and the run() should terminate. The {@link #setShutdownTrigger(boolean)}
	 * must be invoked with a boolean value of true to set the shutdownTrigger.
	 */
	public EmailAddressEntry() {
		//empty constructor when used as shutdown trigger
	}
	
	/**
	 * Returns the first name associated with this email address.
	 * 
	 * @return firstName - String
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * Sets the first name associated with this email address.
	 * 
	 * 
	 * @param firstName - String
	 */
	public void setFirstName(String firstName) {
		if(firstName==null) {
			throw new IllegalArgumentException("First name cannot be null.");
		}
		this.firstName = firstName;
	}
	
	/**
	 * 
	 * Returns the last name associated with this email address.
	 * 
	 * @return lastName - String
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * Sets the last name associated with this email address.
	 * 
	 * @param lastName - String
	 */
	public void setLastName(String lastName) {
		if(lastName==null) {
			throw new IllegalArgumentException("Last name cannot be null.");
		}
		this.lastName = lastName;
	}
	
	/**
	 * 
	 * Returns the email address.
	 * 
	 * @return emailAddress - String
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	
	/**
	 * 
	 * Sets the email address
	 * 
	 * @param emailAddress - String
	 */
	public void setEmailAddress(String emailAddress) {
		if(emailAddress==null) {
			throw new IllegalArgumentException("Email address cannot be null.");
		}
		this.emailAddress = emailAddress;
	}

}
