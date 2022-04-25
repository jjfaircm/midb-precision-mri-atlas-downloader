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
 * Subclass of {@link Tracker} that receives {@link WebHitEntry} instances that are
 * processed by a Singleton instance of this class which runs as an asynchronous thread.
 * When an entry is received, a record is inserted in the web_hits table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class WebHitsTracker extends Tracker {
	
	private static WebHitsTracker instance = null;

	/**
	 * Returns the Singleton instance.
	 * 
	 * @return instance - {@link WebHitsTracker}
	 */
	public static synchronized WebHitsTracker getInstance() {
		
		if(instance==null) {
			instance = new WebHitsTracker();
			instance.start();
		}
		return instance;
	}
	
	/**
	 * 
	 * Hides the default constructor since this class adheres to the Singleton pattern which
	 * means that only one instance of this class will exist in the JVM. The instance is an
	 * asynchronous thread that receives instances of {@link WebHitEntry} that are queued to
	 * a BlockingQueue.
	 * 
	 */
	private WebHitsTracker() {
		
		this.subclassName = this.getClass().getName();
		int index = this.subclassName.lastIndexOf(".");
		this.subclassName = this.subclassName.substring(index+1);
		this.LOGGER_ID = " ::LOGGERID=WEB_HITS_TRACKER:: ";
		this.LOGGER = LogManager.getLogger(WebHitsTracker.class);
		
		this.jdbcInsertString = "INSERT INTO web_hits (timestamp_id, ip_address, user_agent, city, state, country, latitude, longitude) VALUES(?,?,?,?,?,?,?,?)";
		initializeJDBCConnection();
	}
	
	public void addWebHitEntry(WebHitEntry whEntry) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "addWebHitEntry()...invoked");
		this.blockingQueue.add(whEntry);
		LOGGER.trace(loggerId + "addWebHitEntry()...exit.");
	}
		
}
