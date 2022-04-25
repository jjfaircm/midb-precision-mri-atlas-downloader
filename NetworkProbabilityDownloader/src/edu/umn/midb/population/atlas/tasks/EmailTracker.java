package edu.umn.midb.population.atlas.tasks;

import org.apache.logging.log4j.LogManager;
import logs.ThreadLocalLogTracker;


/**

 * 
 * An asynchronous thread that receives {@link EmailAddressEntry} instances that are entered
 * into the email_addresses table in MYSQL. This class conforms to the Singleton pattern so
 * that there is only 1 EmailTracker instance in the jvm.
 * 
 * @author jjfair
 *
 */
public class EmailTracker extends Tracker {
	
	private static EmailTracker instance = null;

	/**
	 * Hides the constructor since this adheres to the Singleton pattern.
	 * 
	 */
	private EmailTracker() {
		this.LOGGER = LogManager.getLogger(EmailTracker.class);
		this.subclassName = this.getClass().getName();
		int index = this.subclassName.lastIndexOf(".");
		this.subclassName = this.subclassName.substring(index+1);
		this.LOGGER_ID = "::LOGGERID=EMAIL_TRACKER:: ";
		this.jdbcInsertString = "INSERT INTO email_addresses (first_name, last_name, email_address) " +
                                " VALUES (?,?,?)";

		initializeJDBCConnection();
		
	}
		
	/**
	 * Returns the Singleton instance
	 * 
	 * @return instance - EmailTracker
	 */
	public static synchronized EmailTracker getInstance() {
		
		if(instance==null) {
			instance = new EmailTracker();
			instance.start();
		}
		return instance;
	}
	
	/**
	 * Adds a {@link EmailAddressEntry} instance to the queue.
	 * 
	 * @param emailAddressEntry - {@link EmailAddressEntry}
	 */
	public void addEmailAddressEntry(EmailAddressEntry emailAddressEntry) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "addEmaiAddressEntry()...invoked, entry=>>" + emailAddressEntry);
		this.blockingQueue.add(emailAddressEntry);
		LOGGER.trace(loggerId + "addEmaiAddressEntry()...exit.");
	}		
}
