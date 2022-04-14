package edu.umn.midb.population.atlas.tasks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;

import logs.ThreadLocalLogTracker;

/**
 * 
 * An asynchronous thread that receives {@link FileDownloadEntry} entries that are entered
 * into the file_downloads table in MYSQL. This class conforms to the Singleton pattern so
 * that there is only 1 DownloadTracker instance in the jvm.
 * 
 * @author jjfair
 *
 */
public class DownloadTracker extends Tracker {
	
	private static final String TRACKING_DOWNLOAD_REQUESTS_FILE = "/midb/tracking/download_requests.csv";
	public static final String DOWNLOAD_ENTRY_TEMPLATE = "ID,IP_ADDRESS,TIMESTAMP,DOWNLOAD_REQUESTED_FILE";
	
	private static DownloadTracker instance = null;

	/**
	 * Hides the constructor since the Singleton pattern is being implemented.
	 * 
	 */
	private DownloadTracker() {
		
		this.subclassName = this.getClass().getName();
		int index = this.subclassName.lastIndexOf(".");
		this.subclassName = this.subclassName.substring(index+1);
		this.LOGGER_ID = " ::LOGGERID=DOWNLOAD_TRACKER:: ";
		this.LOGGER = LogManager.getLogger(DownloadTracker.class);
		this.jdbcInsertString = "INSERT INTO file_downloads (study, network, file_name, file_path, email_address, ip_address, city, state, country, latitude, longitude) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		initializeJDBCConnection();

	}
	
	/**
	 * Returns the Singleton instance.
	 * 
	 * @return instance - DownloadTracker
	 */
	public static synchronized DownloadTracker getInstance() {
		
		if(instance==null) {
			instance = new DownloadTracker();
			instance.start();
		}
		return instance;
	}
	
	/**
	 * 
	 * Adds a {@link FileDownloadEntry} to the queue that will be processed resulting in
	 * a database record added to the file_downloads table in MYSQL.
	 * 
	 * @param entry - {@link FileDownloadEntry}
	 */
	public void addDownloadEntry(FileDownloadEntry entry) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "addDownloadEntry()...invoked, entry=>>" + entry);
		this.blockingQueue.add(entry);
		LOGGER.trace(loggerId + "addDownloadEntry()...exit.");
	}
}

