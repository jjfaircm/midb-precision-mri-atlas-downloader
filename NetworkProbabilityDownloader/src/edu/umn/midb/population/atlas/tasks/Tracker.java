package edu.umn.midb.population.atlas.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.data.access.DBManager;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.IPInfoRequestor;
import edu.umn.midb.population.atlas.utils.IPLocator;
import edu.umn.midb.population.atlas.utils.SMSNotifier;

/**
 * Superclass of the various Tracker subclasses that represent asynchronous threads that
 * receive a subclass of {@link TaskEntry}. The 3 Tracker subclasses and the related TaskEntry types are:
 * <ul>
 * <li>{@link EmailTracker} : {@link EmailAddressEntry}</li>
 * <li>{@link DownloadTracker} : {@link FileDownloadEntry}</li>
 * <li>{@link WebHitsTracker} : {@link WebHitEntry}</li>
 * </ul>
 * 
 * 
 * @author jjfair
 *
 */
public class Tracker extends Thread {
	
	protected static final String EMAIL_ADDRESSES_BACKUP_CSV_FILE = "/midb/email_addresses_backup.csv";
	protected static final String EMAIL_ADDRESS_CSV_TEMPLATE = "EMAIL_ADDRESS,FIRST_NAME,LAST_NAME";
	protected static final String DOWNLOAD_REQUESTS_CSV_FILE = "/midb/tracking/download_requests.csv";
	protected static final String DOWNLOAD_ENTRY_CSV_TEMPLATE = "ID,IP_ADDRESS,TIMESTAMP,DOWNLOAD_REQUESTED_FILE";
	protected static final String WEB_HITS_CSV_FILE = "/midb/tracking/web_hits.csv";
	protected static final String WEB_HIT_ENTRY_TEMPLATE = "ID,IP_ADDRESS,TIMESTAMP,USER_AGENT";
	private static String DOUBLE_QUOTE = "\"";
	protected Logger LOGGER = null;
	protected String LOGGER_ID = "UNASSIGNED";
	private long pollingTimeoutSeconds = 300;
	protected BlockingQueue<TaskEntry> blockingQueue = new LinkedBlockingDeque<TaskEntry>();
	private TaskEntry currentTaskEntry = null;
	private PreparedStatement preparedInsertStatement = null;
    protected String jdbcInsertString = null;
    private Connection jdbcConnection = null;;
    private AtomicReference<String> failedConnectionMessageRef = new AtomicReference<String>();
    //lastExecutionTimeMS represents the last time the database connection was used
    private long lastExecutionTimeMS = 0;
    //timeout set to 120 minutes...if the connection is idle longer than this
    //then the checkDatabaseConnection() method will be executed to refresh the
    //idle time to 0 which will prevent a StaleConnection or MySQLNonTransientConnectionException error
    private long autoConnectionCheckTimeout = 1000*60*120;
    private boolean firstHealthCheckSent = false;
    private int healthCheckTargetHour1 = 8;
    private int healthCheckTargetHour2 = 20;
	protected FileReader fileReader = null;
	protected BufferedReader bufReader = null;
	protected String subclassName = null;
	private boolean healthCheckNotificationPending = false;
	

	/**
	 * 
	 * Adds a {@link FileDownloadEntry} to the /midb/tracking/download_requests.csv file.
	 * This occurs when the attempt to add the entry to the database has failed.
	 * 
	 * @param fdEntry - {@link FileDownloadEntry}
	 */
	private void addDownloadEntryToCSVFile(FileDownloadEntry fdEntry) {

		LOGGER.info(LOGGER_ID + "addDownloadEntryToCSVFile()...invoked.");
		
	    String downloadEntry = DOWNLOAD_ENTRY_CSV_TEMPLATE.replace("ID", fdEntry.getId());
		downloadEntry = downloadEntry.replace("IP_ADDRESS", fdEntry.getRequestorIPAddress());
		downloadEntry = downloadEntry.replace("DOWNLOAD_REQUESTED_FILE", fdEntry.getFilePath());
		downloadEntry = downloadEntry.replace("TIMESTAMP", fdEntry.getFormattedTimeStamp());
		
		LOGGER.info(LOGGER_ID + "addDownloadEntryToCSVFile()...exit");
				
		try {
			FileWriter fw = new FileWriter(DOWNLOAD_REQUESTS_CSV_FILE, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(downloadEntry);
			pw.close();
		}
		catch(IOException ioE) {
			LOGGER.error(LOGGER_ID + "Failed to add downloadEntry to csv file=>>" + downloadEntry);
    	    LOGGER.error(ioE.getMessage(), ioE);
		}
		catch(Exception e) {
			LOGGER.error(LOGGER_ID + "Failed to add downloadEntry to csv file=>>" + downloadEntry);
    	    LOGGER.error(e.getMessage(), e);
		}
		LOGGER.info(LOGGER_ID + "addDownloadEntryToCSVFile()...exit.");
	}
	
	
	/**
	 * 
	 * Adds an {@link EmailAddressEntry} to the email_addresses table in MYSQL.
	 * 
	 * @param entry - {@link EmailAddressEntry}
	 */
	private void addEmailAddressEntryToDatabase(EmailAddressEntry entry) {

		LOGGER.info(LOGGER_ID + "addEmailEntryToDatabase()...invoked.");
		
		int updateCount = -1;
		
		boolean connectionOK = true;
		boolean emailRecordAlreadyExists = false;
		
		//if last usage is greater than 30 minutes then check connection
		long currentTimeMS = System.currentTimeMillis();
		long elapsedTime = currentTimeMS - this.lastExecutionTimeMS;
		
		if(elapsedTime >= DBManager.MAX_IDLE_TIME) {
			connectionOK = checkDatabaseConnection(1);
		}
		
		if(!connectionOK) {
			initializeJDBCConnection();
			connectionOK = checkDatabaseConnection(2);
			
			if(!connectionOK) {
				String emailAddress = entry.getEmailAddress();
				LOGGER.fatal(LOGGER_ID + "Unable to get jdbcConnection...Not able to add email record, emailAddress=" + emailAddress);
				String domainName = NetworkProbabilityDownloader.getDomainName();
				String message = "MIDB_APP_ERROR:" + domainName + ": UNABLE TO SUCCESSFULLY CHECK JDBC CONNECTION FROM EmailTracker";
				message +=  "->emailAddress=" + emailAddress;
				SMSNotifier.sendNotification(message, this.subclassName);
			}
		}
		
		if(connectionOK) {
			try {
				String existingEmailAddress = DOUBLE_QUOTE + entry.getEmailAddress() + DOUBLE_QUOTE;
				String checkForDuplicateString = "SELECT COUNT(*) FROM email_addresses WHERE email_address LIKE " + existingEmailAddress;
				Statement statement = this.jdbcConnection.createStatement();
				statement.execute(checkForDuplicateString);
				ResultSet rs = statement.getResultSet();
				rs.next();
				int count = rs.getInt(1);
				// for clarity
				if(count > 0) {
					emailRecordAlreadyExists = true;
				}
				this.lastExecutionTimeMS = System.currentTimeMillis();
				
				//test for DiagnosticsReporter
				String emailAddress = entry.getEmailAddress();
				if(emailAddress.contains("bogusbob_1")) {
					SQLException e = new SQLException("bogusbob_1 is not a real email address!");
					throw e;
				}
				
				if(!emailRecordAlreadyExists) {
					this.preparedInsertStatement.clearParameters();
					this.preparedInsertStatement.setString(1, entry.getFirstName());
					this.preparedInsertStatement.setString(2, entry.getLastName());
					this.preparedInsertStatement.setString(3, entry.getEmailAddress());
					this.preparedInsertStatement.execute();
					this.lastExecutionTimeMS = System.currentTimeMillis();
					updateCount = preparedInsertStatement.getUpdateCount();
					if(updateCount == 0) {
						throw new SQLException("Failed to insert email record into database");
					}
					else {
						LOGGER.trace(LOGGER_ID + "Added emailAddress to database");
					}
				}
			}
			catch(SQLException sqlE) {
				DiagnosticsReporter.createDiagnosticsEntry(entry.getAppContext(), entry.getRequest(), null, sqlE);
				LOGGER.fatal(sqlE.getMessage(), sqlE);
				connectionOK = false;
			}
		} 
		if(!connectionOK){
			addEmailAddressToBackupCSVFile(entry);
		}		
	}
		
	/**
	 * Adds a {@link EmailAddressEntry} to the /midb/email_addresses_backup.csv file.
	 * This occurs when the attempt to add the entry to the database fails.
	 * 
	 * 
	 * @param eaEntry - {@link EmailAddressEntry}
	 */
	private void addEmailAddressToBackupCSVFile(EmailAddressEntry eaEntry) {

		try {
			FileWriter fw = new FileWriter(EMAIL_ADDRESSES_BACKUP_CSV_FILE, true);
			PrintWriter pw = new PrintWriter(fw);
			String entryLine = EMAIL_ADDRESS_CSV_TEMPLATE;
			entryLine = entryLine.replace("EMAIL_ADDRESS", eaEntry.getEmailAddress());
			entryLine = entryLine.replace("FIRST_NAME", eaEntry.getFirstName());
			entryLine = entryLine.replace("LAST_NAME", eaEntry.getLastName());
			pw.println(entryLine);
			pw.close();
		}
		catch(IOException ioE) {
			LOGGER.fatal(LOGGER_ID + "Unable to write emailAddress to " + EMAIL_ADDRESSES_BACKUP_CSV_FILE);
			String details = eaEntry.getFirstName() + "::" + eaEntry.getLastName() + "::" + eaEntry.getEmailAddress();
			LOGGER.fatal(LOGGER_ID + details);
			DiagnosticsReporter.createDiagnosticsEntry(ioE);
		}
	
	}
	
	/**
	 * Adds a {@link FileDownloadEntry} to the file_downloads table in MYSQL.
	 * 
	 * @param fdEntry - {@link FileDownloadEntry}
	 */
	private void addFileDownloadEntryToDatabase(FileDownloadEntry fdEntry) {

		LOGGER.info(LOGGER_ID + "addDownloadEntryToDatabase(FileDownloadEntry)()=>>" + fdEntry.getFileName());
		
		int updateCount = -1;
		
		boolean connectionOK = true;
		
		
		//IPLocator gets more specific city information.
		//However, it sometimes can not resolve certain ip addresses,
		//especially in countries like China, etc.
		//In that case, we use IPInfoRequestor
		IPLocator.locateIP(fdEntry);	
		
		String resolvedCountry = fdEntry.getCountry();
		String resolvedState = fdEntry.getState();
		String resolvedCity = fdEntry.getCity();
		
		if(resolvedCountry.equalsIgnoreCase("unknown") || 
		   resolvedState.equalsIgnoreCase("unknown") ||
		   resolvedCity.equalsIgnoreCase("unknown")) {
			
				IPInfoRequestor.getIPInfo(fdEntry);	
		}
		   
		
		//if last usage is greater than 30 minutes then check connection
		long currentTimeMS = System.currentTimeMillis();
		long elapsedTime = currentTimeMS - this.lastExecutionTimeMS;
		
		if(elapsedTime >= DBManager.MAX_IDLE_TIME) {
			connectionOK = checkDatabaseConnection(1);
		}
		
		if(!connectionOK) {
			LOGGER.trace(LOGGER_ID + "Connection check failed, getting new connection");
			initializeJDBCConnection();
			connectionOK = checkDatabaseConnection(2);
			
			if(!connectionOK) {
				LOGGER.fatal(LOGGER_ID + "run()...Unable to successfully check database connection.");
				String message = "MIDB_APP_ERROR: UNABLE TO SUCCESSFULLY CHECK CONNECTION AFTER RETRY";
				SMSNotifier.sendNotification(message, this.subclassName);
			}
		}
		if(connectionOK) {
			try {
				this.preparedInsertStatement.clearParameters();
				this.preparedInsertStatement.setString(1, fdEntry.getStudy());
				this.preparedInsertStatement.setString(2, fdEntry.getNeuralNetworkName());
				this.preparedInsertStatement.setString(3, fdEntry.getFileName());
				this.preparedInsertStatement.setString(4, fdEntry.getFilePath());
				//have to put emailAddress in quotes because of special character: @
				this.preparedInsertStatement.setString(5, fdEntry.getEmailAddress());
				this.preparedInsertStatement.setString(6, fdEntry.getRequestorIPAddress());
				this.preparedInsertStatement.setString(7, fdEntry.getCity());
				this.preparedInsertStatement.setString(8, fdEntry.getState());
				this.preparedInsertStatement.setString(9, fdEntry.getCountry());
				this.preparedInsertStatement.setString(10, fdEntry.getLatitude());
				this.preparedInsertStatement.setString(11, fdEntry.getLongitude());


				this.preparedInsertStatement.execute();
				this.lastExecutionTimeMS = System.currentTimeMillis();
				updateCount = preparedInsertStatement.getUpdateCount();
				if(updateCount == 0) {
					throw new SQLException("Failed to insert email record into database");
				}
				else {
					LOGGER.trace(LOGGER_ID + "Added downloadEntry to database");
				}
			}
			catch (SQLException sqlE) {
				LOGGER.fatal(LOGGER_ID + "Failed to insert record into file_downloads table");
				LOGGER.fatal(sqlE.getMessage(), sqlE);
				DiagnosticsReporter.createDiagnosticsEntry(fdEntry.getAppContext(), fdEntry.getRequest(), null, sqlE);
				connectionOK = false;
			}
		}
		
	    if(!connectionOK) {
	    	addDownloadEntryToCSVFile(fdEntry);
	    }
	}
	
	/**
	 * 
	 * Determines what type of TaskEntry has been received and calls the appropriate
	 * method for processing.
	 * 
	 * @param tEntry - {@link TaskEntry}
	 */
	protected void addTaskEntryToDatabase(TaskEntry tEntry) {
		
		LOGGER.trace(LOGGER_ID + "addTaskEntryToDatabase()...invoked, entryType=" + tEntry.getSubclassName());
		
		String subclassType = tEntry.getSubclassName();
		
		switch (subclassType) {
		
		case "EmailAddressEntry":
			EmailAddressEntry emailAddressEntry = (EmailAddressEntry)tEntry;
			addEmailAddressEntryToDatabase(emailAddressEntry);
			break;
		case "WebHitEntry":
			WebHitEntry whEntry = (WebHitEntry)tEntry;
			addWebHitEntryToDatabase(whEntry);
			break;
		case "FileDownloadEntry":
			FileDownloadEntry fdEntry = (FileDownloadEntry)tEntry;
			addFileDownloadEntryToDatabase(fdEntry);
		}
		LOGGER.trace(LOGGER_ID + "addTaskEntryToDatabase()...exit, entryType=" + tEntry.getSubclassName());
	}

	/**
	 * Adds a {@link WebHitEntry} to the /midb/tracking/web_hits.csv file.
	 * This occurs when the attempt to add the entry to the database fails.
	 * 
	 * @param whEntry - {@link WebHitEntry}
	 */
	protected void addWebHitEntryToCSVFile(WebHitEntry whEntry) {

		LOGGER.info(LOGGER_ID + "addHitEntryToFile()...invoked");
		
		String hitEntry = WEB_HIT_ENTRY_TEMPLATE.replace("ID", whEntry.getId());
		hitEntry = hitEntry.replace("IP_ADDRESS", whEntry.getRequestorIPAddress());
		hitEntry = hitEntry.replace("TIMESTAMP", whEntry.getFormattedTimeStamp());
		hitEntry = hitEntry.replace("USER_AGENT", whEntry.getUserAgent());
		
		try {
			FileWriter fw = new FileWriter(WEB_HITS_CSV_FILE, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(hitEntry);
			pw.close();		
		}
		catch(IOException ioE) {
			LOGGER.error(LOGGER_ID + "Failed to add hitEntry=>>" + hitEntry);
    	    LOGGER.error(ioE.getMessage(), ioE);
		}
		catch(Exception e) {
			LOGGER.error(LOGGER_ID + "Failed to add hitEntry=>>" + hitEntry);
    	    LOGGER.error(e.getMessage(), e);
		}
		LOGGER.info(LOGGER_ID + "addHitEntryToFile()...exit.");
	}

	
	/**
	 * Adds a {@link WebHitEntry} to the web_hits table in MYSQL.
	 * 
	 * @param whEntry - {@link WebHitEntry}
	 */
	protected void addWebHitEntryToDatabase(WebHitEntry whEntry) {


		LOGGER.info(LOGGER_ID + "addWebHitEntryToDatabase()...invoked.");
		
		//IPLocator gets more specific city information.
		//However, it sometimes can not resolve certain ip addresses,
		//especially in countries like China, etc.
		//In that case, we use IPInfoRequestor
		IPLocator.locateIP(whEntry);	
		
		String resolvedCountry = whEntry.getCountry();
		String resolvedState = whEntry.getState();
		String resolvedCity = whEntry.getCity();
		
		if(resolvedCountry.equalsIgnoreCase("unknown") || 
		   resolvedState.equalsIgnoreCase("unknown") ||
		   resolvedCity.equalsIgnoreCase("unknown")) {
			
				IPInfoRequestor.getIPInfo(whEntry);	
		}	
		
		int updateCount = -1;
		boolean connectionOK = true;
		
		//if last usage is greater than 30 minutes then check connection
		long currentTimeMS = System.currentTimeMillis();
		long elapsedTime = currentTimeMS - this.lastExecutionTimeMS;
		
		if(elapsedTime >= DBManager.MAX_IDLE_TIME) {
			connectionOK = checkDatabaseConnection(1);
		}
		
		if(!connectionOK) {
			initializeJDBCConnection();
			connectionOK = checkDatabaseConnection(2);
			
			if(!connectionOK) {
				LOGGER.fatal(LOGGER_ID + "run()...Unable to successfully check database connection.");
				String message = "MIDB_APP_ERROR: UNABLE TO SUCCESSFULLY CHECK CONNECTION AFTER RETRY";
				SMSNotifier.sendNotification(message, this.subclassName);
			}
		}
		
		if(connectionOK) {
			try {
				this.preparedInsertStatement.clearParameters();
				this.preparedInsertStatement.setString(1, whEntry.getId());
				this.preparedInsertStatement.setString(2, whEntry.getRequestorIPAddress());
				String userAgent = whEntry.getUserAgent();
				if(userAgent.length()>256) {
					userAgent = userAgent.substring(0, 255);
				}
				this.preparedInsertStatement.setString(3, userAgent);
				this.preparedInsertStatement.setString(4, whEntry.getCity());
				this.preparedInsertStatement.setString(5, whEntry.getState());
				this.preparedInsertStatement.setString(6, whEntry.getCountry());
				this.preparedInsertStatement.setString(7, whEntry.getLatitude());
				this.preparedInsertStatement.setString(8, whEntry.getLongitude());


				this.preparedInsertStatement.execute();
				this.lastExecutionTimeMS = System.currentTimeMillis();
				updateCount = preparedInsertStatement.getUpdateCount();
				if(updateCount == 0) {
					throw new SQLException("Failed to insert web hit record into database");
				}
				else {
					LOGGER.trace(LOGGER_ID + "Added hitEntry to database");
				}
			}
			catch(SQLException sqlE) {
				DiagnosticsReporter.createDiagnosticsEntry(whEntry.getAppContext(), whEntry.getRequest(), null, sqlE);
				LOGGER.fatal(sqlE.getMessage(), sqlE);
				connectionOK = false;
			}
		} 
		
		if(!connectionOK) {
			addWebHitEntryToCSVFile(whEntry);
		}
		LOGGER.info(LOGGER_ID + "addWebHitEntryToDatabase()...exit.");
	}
	
	/**
	 * 
	 * Checks to verify that the JDBC Connection is still valid. The task is delegated to
	 * {@link DBManager#checkDatabaseConnection(Connection, AtomicReference, String)} which
	 * executes a simple test query. This test acts as a keep-alive action which prevents
	 * a StaleConnectionException from occurring. Typically this check will occur every
	 * time the connection has remained idle for 2 hours. The connection is also checked
	 * before completing a received TaskEntry. If the connection throws an error then a new
	 * connection will be created.
	 * 
	 * @param checkCount - int
	 * @return connectionOK - boolean
	 */
	protected boolean checkDatabaseConnection(int checkCount) {

		LOGGER.trace(LOGGER_ID + "checkDatabaseConnection()...invoked, checkCount=" + checkCount);
		
		boolean connectionOK = false;
		connectionOK = DBManager.getInstance().checkDatabaseConnection(this.jdbcConnection, this.failedConnectionMessageRef, this.subclassName);
		
		if(!connectionOK && checkCount>1) {
			String domainName = NetworkProbabilityDownloader.getDomainName();
			String message = "MIDB_APP_ERROR SYSTEM=" + domainName + ":::" + this.failedConnectionMessageRef.get();
			SMSNotifier.sendNotification(message, this.subclassName);
		}
		this.lastExecutionTimeMS = System.currentTimeMillis();
		
		LOGGER.trace(LOGGER_ID + "checkDatabaseConnection()...exit");
		return connectionOK;
	}
	
	/**
	 * 
	 * Returns the number of seconds remaining until a health check notification
	 * should be sent via the {@link SMSNotifier#sendNotification(String, String)}.
	 * The value returned informs the run() method how long to sleep before sending
	 * an SMS health check notification. If the remaining seconds is greater than
	 * the normal polling time for waiting on the queue, then -1 is returned indicating
	 * that the normal polling time should be used. Otherwise, the value returned 
	 * represents an alternate polling time that should override the normal polling
	 * time.
	 * 
	 * @return remainingSeconds - long
	 */
		protected long getRemainingNotificationTime() {
		
		//LOGGER.trace(LOGGER_ID + "getRemainingNotificationTime()...invoked.");
		
		long remainingNotificationSeconds = -1;
		LocalTime currentTime = LocalTime.now();
		int hour = currentTime.getHour();
		int seconds = currentTime.getSecond();
		int minutes = currentTime.getMinute();
		int remainingMinutes = 60 - minutes;
		int adjustedTargetHour1 = 0;
		int adjustedTargetHour2 = 0;
		
		
		// the target hour is always 1 hour less than the actual target hour, otherwise
		// we would end up being late. We then add 60 minutes as a buffer and then subtract
		// the current minutes of the hour. So if the target hour is 8 then we use 7 as the
		// target hour and then if the current hour is actually 7 then we wait 60 minutes minus
		// the current minutes of the current time. 
		if(this.healthCheckTargetHour1 == 0) {
			adjustedTargetHour1 = 23;
		}
		else {
			adjustedTargetHour1 = healthCheckTargetHour1-1;
		}
		if(this.healthCheckTargetHour2 == 0) {
			adjustedTargetHour2 = 23;
		}
		else {
			adjustedTargetHour2 = healthCheckTargetHour2-1;
		}
		
		
		if(!this.firstHealthCheckSent) {
			remainingNotificationSeconds = 0;
		}
		
		else if(hour == adjustedTargetHour1 || hour == adjustedTargetHour2) {
			//LOGGER.trace("Entering calculation section...");
			if(remainingMinutes <= this.pollingTimeoutSeconds/60) {
				//LOGGER.trace("Entering minutes section...");
				remainingNotificationSeconds = remainingMinutes * 60;
				remainingNotificationSeconds = remainingNotificationSeconds - seconds;
			}
		}
		else if(this.healthCheckNotificationPending) {
			// the polling thread may have woke up due to an entry arriving while it was waiting
			// to send a health check notification. This may have caused the current hour to go
			// beyond the target hour. This is a slim possibility, but this will prevent a possible
			// missed notification.
			remainingNotificationSeconds = 0;
		}
		//LOGGER.trace(LOGGER_ID + "getRemainingNotificationTime()...exit, remainingSeconds=" + remainingNotificationSeconds);
		return remainingNotificationSeconds;
}
	
	/**
	 * 
	 * Gets a JDBC Connection from the {@link DBManager} and prepares the insert statement
	 * that will be used to insert a record into the appropriate table. This is handled
	 * by the inheriting subclass such as {@link WebHitsTracker};
	 * 
	 */
	protected void initializeJDBCConnection() {
		try {
			jdbcConnection = DBManager.getInstance().getDBConnection();
			preparedInsertStatement = jdbcConnection.prepareStatement(jdbcInsertString);
			LOGGER.info(LOGGER_ID + "initializeJDBCConnection()...successfully retrieved JDBC connection and prepared statement.");
		} 
		catch (SQLException e) {
			DiagnosticsReporter.createDiagnosticsEntry(e, true);
			LOGGER.fatal(LOGGER_ID + "Unable to get connection and/or prepare statement with new connection.");
			LOGGER.fatal(LOGGER_ID + "SUBCLASS_NAME=" + this.subclassName);
			LOGGER.fatal(e.getMessage(), e);
		}
	
	}
	
	/**
	 * 
	 * Remains in a polling loop checking if a {@link TaskEntry} has been queued.
	 * It does this until a TaskEntry with its shutdownTrigger enabled has been
	 * received. Every 12 hours, at a pre-determined time, a health check SMS notificaton
	 * will be sent via the {@link SMSNotifier#sendNotification(String, String)}.
	 * 
	 */
	@Override
	public void run() {
		
		boolean shouldContinue = true;						
		long currentTimeMS = 0;
		long executionLapsedTimeMS = 0;
		boolean connectionOK = true;
		
		LOGGER.info(LOGGER_ID + "entering dequeue loop...");


		while(shouldContinue) {
			
			currentTimeMS = System.currentTimeMillis();
			executionLapsedTimeMS = currentTimeMS - this.lastExecutionTimeMS;
			
			if(executionLapsedTimeMS > this.autoConnectionCheckTimeout) {
				LOGGER.trace(LOGGER_ID + "performing auto check on jdbc connect");
				connectionOK = checkDatabaseConnection(1);
				if(!connectionOK) {
					// jjf
					initializeJDBCConnection();
					connectionOK = checkDatabaseConnection(2);
				}
				if(!connectionOK) {
					LOGGER.fatal(LOGGER_ID + "run()...Unable to successfully check database connection.");
					String message = "MIDB_APP_ERROR: UNABLE TO SUCCESSFULLY CHECK CONNECTION AFTER RETRY";
					SMSNotifier.sendNotification(message, this.subclassName);
				}
			}

			try {
				long alternatePollingTimeoutSeconds = this.getRemainingNotificationTime();
				if(alternatePollingTimeoutSeconds == 0) {
					sendHealthCheckNotification(connectionOK);
					this.healthCheckNotificationPending = false;
					currentTaskEntry = this.blockingQueue.poll(pollingTimeoutSeconds, TimeUnit.SECONDS);
				}
				else if(alternatePollingTimeoutSeconds > 0) {
					this.healthCheckNotificationPending = true;
					currentTaskEntry = this.blockingQueue.poll(alternatePollingTimeoutSeconds, TimeUnit.SECONDS);
					if(currentTaskEntry==null) {
						// we woke up due to alternatePollingTimeout, therefore send notification
						sendHealthCheckNotification(connectionOK);
						this.healthCheckNotificationPending = false;
					}
				}
				else {
					currentTaskEntry = this.blockingQueue.poll(pollingTimeoutSeconds, TimeUnit.SECONDS);
				}
				
				if(currentTaskEntry==null) {
					continue;
				}
				else if(currentTaskEntry.isShutdownTrigger()) {
					LOGGER.info(LOGGER_ID + "Received SHUTDOWN entry, terminating.");

					try {
						this.preparedInsertStatement.close();
						this.jdbcConnection.close();
					}
					catch(SQLException sqlE) {
						LOGGER.error(LOGGER_ID + "Failed to close jdbcConnection.");
						LOGGER.error(sqlE.getMessage(), sqlE);
					}
					shouldContinue = false;
				}
				
				else {
					LOGGER.trace(LOGGER_ID + "received taskEntry =>>" + currentTaskEntry.getSubclassName());
					addTaskEntryToDatabase(currentTaskEntry);
				}
			}
			catch(InterruptedException iE) {
				LOGGER.error(LOGGER_ID + "Interrupted: failed to retrieve entry from queue.");
			}
		}
		
		try {
			preparedInsertStatement.close();
			jdbcConnection.close();
		}
		catch(SQLException sqlE) {
			LOGGER.error(LOGGER_ID + "error closing jdbc connection in EmailTracker at shutdown");
			LOGGER.error(sqlE.getMessage(), sqlE);
		}
		LOGGER.info(LOGGER_ID + "run() method exit.");
	}
	
	/**
	 * 
	 * Sends a health check notification via the {@link SMSNotifier#sendNotification(String, String)}
	 * 
	 * @param connectionOK - boolean
	 */
	protected void sendHealthCheckNotification(boolean connectionOK) {
		LOGGER.trace(LOGGER_ID + "sendHealthCheckNotification()...invoked");
		LocalTime now = LocalTime.now();
		int hour = now.getHour();
		int minutes = now.getMinute();
		String minutesStr = null;
		if(minutes<10) {
			minutesStr = "0" + minutes;
		}
		else {
			minutesStr = "" + minutes;
		}
		String time = hour +":" + minutesStr;
		time = "time=" + time;
		String domainName = NetworkProbabilityDownloader.getDomainName();
		String message = "MIDB_APP_HEALTH_CHECK::::" + domainName + "::::";
		message += this.subclassName + "::::" + time + "::::";
		message += "connectionOK=" + connectionOK;
		SMSNotifier.sendNotification(message, this.subclassName);
		firstHealthCheckSent = true;
		LOGGER.trace(LOGGER_ID + "sendHealthCheckNotification()...exit, class=" + this.subclassName);
	}
	
}
