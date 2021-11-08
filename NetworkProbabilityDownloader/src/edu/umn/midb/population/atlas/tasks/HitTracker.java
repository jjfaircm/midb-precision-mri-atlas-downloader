package edu.umn.midb.population.atlas.tasks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.config.PropertyManager;
import logs.ThreadLocalLogTracker;


public class HitTracker extends Thread {
	
	private static final String TRACKING_HITS_FILE = "/midb/tracking/hits_tracker.csv";
	private static Logger LOGGER = null;
	private static final String LOGGER_ID = " ::LOGGERID=HITS_TRACKER:: ";
	private long pollingTimeoutSeconds = 1200;
	private BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<String>();
	private static HitTracker instance = null;
	private FileWriter fileWriter = null;
	private BufferedWriter bufWriter = null;
	private long totalHits = 0;
	
	static {
		LOGGER = LogManager.getLogger(DownloadTracker.class);
	}
	
	private HitTracker() {
		try {
			fileWriter = new FileWriter(TRACKING_HITS_FILE, true);
			bufWriter = new BufferedWriter(fileWriter);
			LOGGER.info(LOGGER_ID + "created file writer successfully.");
		}
		catch(IOException ioE) {
			LOGGER.error(LOGGER_ID + "Failed to create buffered file writer.");
			LOGGER.error(ioE.getMessage(), ioE);
		}
	}
	
	public static synchronized HitTracker getInstance() {
		
		if(instance==null) {
			instance = new HitTracker();
			instance.start();
		}
		return instance;
	}
	
	public void addHitEntry(String entry) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "addHitEntry()...invoked, entry=>>" + entry);
		this.blockingQueue.add(entry);
		LOGGER.trace(loggerId + "addHitEntry()...exit.");
	}
	
	private void addHitEntryToFile(String entry) {
		LOGGER.info(LOGGER_ID + "addHitEntryToFile()=>>" + entry);
		try {
			this.bufWriter.write(entry);
			this.bufWriter.newLine();
			this.bufWriter.flush();
		}
		catch(IOException ioE) {
			LOGGER.error(LOGGER_ID + "Failed to add downloadEntry=>>" + entry);
    	    LOGGER.error(ioE.getMessage(), ioE);
		}
		catch(Exception e) {
			LOGGER.error(LOGGER_ID + "Failed to add downloadEntry=>>" + entry);
    	    LOGGER.error(e.getMessage(), e);
		}
		LOGGER.info(LOGGER_ID + "addHitEntryToFile()...exit.");
	}
	
	@Override
	public void run() {
		
		boolean shouldContinue = true;
		String newHitEntry = null;
		LOGGER.info(LOGGER_ID + "entering dequeue loop...");

		while(shouldContinue) {
			LOGGER.info(LOGGER_ID + "entering top of loop...");
			try {
				newHitEntry = this.blockingQueue.poll(pollingTimeoutSeconds, TimeUnit.SECONDS);
				if(newHitEntry==null || newHitEntry.contains("null")) {
					continue;
				}
				else if(newHitEntry.contains("SHUTDOWN")) {
					LOGGER.info(LOGGER_ID + "Received SHUTDOWN entry, terminating.");
					shouldContinue = false;
					try {
						this.bufWriter.flush();
						this.bufWriter.close();
					}
					catch(IOException ioE) {
						LOGGER.error(LOGGER_ID + "Failed to close file.");
						LOGGER.error(ioE.getMessage(), ioE);
					}
				}
				else {
					addHitEntryToFile(newHitEntry);
				}
			}
			catch(InterruptedException iE) {
				LOGGER.error(LOGGER_ID + "Failed to retrieve download entry from queue.");
			}
		}
		LOGGER.info(LOGGER_ID + "run() method exit.");
	}
}
