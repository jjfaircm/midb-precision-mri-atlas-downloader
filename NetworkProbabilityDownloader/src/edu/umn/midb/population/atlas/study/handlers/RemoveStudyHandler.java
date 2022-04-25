package edu.umn.midb.population.atlas.study.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.data.access.AtlasDataCacheManager;
import logs.ThreadLocalLogTracker;

/**
 * Handles the tasks involved in removing a study.
 * 
 * @author jjfair
 *
 */
public class RemoveStudyHandler {

	private static final String ROOT_STUDY_PATH = "/midb/studies/";
	private static final String MENU_CONFIG_FILE = "/midb/menu.conf";
	private static final String SUMMARY_CONFIG_FILE = "/midb/summary.conf";
	private static final String FOLDER_NAMES_CONFIG_FILE = "/midb/network_folder_names.conf";


	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);


	private String studyFolder = null;
	private ArrayList<String> existingMenuConfigLines = new ArrayList<String>();
	private ArrayList<String> existingSummaryConfigLines = new ArrayList<String>();
	private ArrayList<String> existingFolderConfigLines = new ArrayList<String>();



	
	/**
	 * Public constructor.
	 * 
	 * @param studyFolder - String representing the folder which contains the study data.
	 */
	public RemoveStudyHandler(String studyFolder) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "RemoveStudyHandler()...invoked.");
		this.studyFolder = studyFolder;
		LOGGER.trace(loggerId + "RemoveStudyHandler()...exit.");
	}
	
	/**
	 * Loads network folder names configuration that contains the entries for all studies.
	 * These entries are stored in the /midb/network_folder_names.conf file.
	 * 
	 */
	protected void cacheExistingFolderNamesEntries() {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...invoked.");

		File file = new File(FOLDER_NAMES_CONFIG_FILE);
		int lineLength = 0;
		boolean shouldContinue = true;
				
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				outputLine = outputLine.trim();
				lineLength = outputLine.length();
				
				if(lineLength < 3 ) {
					continue;
				}
				if(outputLine.contains("NETWORK FOLDERS ENTRY") && outputLine.contains(this.studyFolder)) {
					while (shouldContinue) {
						outputLine = br.readLine();
						if(outputLine.contains("END NETWORK FOLDERS ENTRY")) {
							shouldContinue = false;
						}
					}
				}
				else {
					existingFolderConfigLines.add(outputLine);
					if(outputLine.contains("END NETWORK FOLDERS ENTRY")) {
						existingFolderConfigLines.add("\n");
					}
				}
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...exit.");
	}

	/**
	 * 
	 * Loads the menu entries for all studies from the /midb/menu.conf file.
	 * 
	 */
	protected void cacheExistingMenuEntries() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...invoked.");

		File file = new File(MENU_CONFIG_FILE);
		int lineLength = 0;
		boolean shouldContinue = true;
				
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				outputLine = outputLine.trim();
				lineLength = outputLine.length();
				
				if(lineLength < 3 ) {
					continue;
				}
				if(outputLine.contains("MENU ENTRY") && outputLine.contains(this.studyFolder)) {
					while (shouldContinue) {
						outputLine = br.readLine();
						if(outputLine.contains("END MENU ENTRY")) {
							shouldContinue = false;
						}
					}
				}
				else {
					existingMenuConfigLines.add(outputLine);
					if(outputLine.contains("END MENU")) {
						existingMenuConfigLines.add("\n");
					}
				}
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...exit.");
	}
	
	/**
	 * Removes the study identified by the studyFolder attribute.
	 * 
	 * @throws IOException - Unhandled exception
	 */
	public void removeStudy() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "removeStudy()...invoked.");
		
		cacheExistingMenuEntries();
		updateMenuConfig();
		cacheExistingSummaryEntries();
		updateSummaryConfig();
		cacheExistingFolderNamesEntries();
		updateFolderNamesConfig();
		removeStudyDirectory();
		
		LOGGER.trace(loggerId + "removeStudy()...exit.");
	}
	
	/**
	 * 
	 * Removes the directory containing the data for the study.
	 * 
	 * @throws IOException - Unhandled exception
	 */
	protected void removeStudyDirectory() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "removeStudyDirectory()...invoked.");

		File targetDirectory = new File(ROOT_STUDY_PATH + this.studyFolder);
		FileUtils.deleteDirectory(targetDirectory);
		
		LOGGER.trace(loggerId + "removeStudyDirectory()...exit.");
	}
	
	/**
	 * 
	 * Updates the /midb/network_folder_names.conf file by removing the entry for
	 * the study.
	 * 
	 * @throws IOException - Unhandled exception
	 */
	protected void updateFolderNamesConfig() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateFolderNamesConfig()...invoked.");

		PrintWriter pw = new PrintWriter(new FileWriter(FOLDER_NAMES_CONFIG_FILE));
		 
		Iterator<String> cachedEntriesIt = this.existingFolderConfigLines.iterator();
		
		while(cachedEntriesIt.hasNext()) {
			pw.println(cachedEntriesIt.next());
		}
	 
		pw.close();
		
		AtlasDataCacheManager.getInstance().reloadMenuConfig();
		LOGGER.trace(loggerId + "updateFolderNamesConfig()...exit.");
	}
	
	/**
	 * 
	 * Updates the /midb/menu.conf file by removing the entry for the study.
	 * 
	 * @throws IOException - Unhandled exception
	 */
	protected void updateMenuConfig() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");

		PrintWriter pw = new PrintWriter(new FileWriter(MENU_CONFIG_FILE));
		 
		Iterator<String> cachedEntriesIt = this.existingMenuConfigLines.iterator();
		
		while(cachedEntriesIt.hasNext()) {
			pw.println(cachedEntriesIt.next());
		}
	 
		pw.close();
		
		AtlasDataCacheManager.getInstance().reloadMenuConfig();
		LOGGER.trace(loggerId + "updateMenuConfig()...exit.");
	}
	
	/**
	 * Updates the /midb/summary.conf file by removing the entry for the study.
	 * 
	 * @throws IOException - Unhandled exception
	 */
	protected void updateSummaryConfig() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");

		PrintWriter pw = new PrintWriter(new FileWriter(SUMMARY_CONFIG_FILE));
		 
		Iterator<String> cachedEntriesIt = this.existingSummaryConfigLines.iterator();
		String currentEntry = null;
		
		while(cachedEntriesIt.hasNext()) {
			currentEntry = cachedEntriesIt.next();
			pw.println(currentEntry);
			if(currentEntry.contains("END SUMMARY")) {
				pw.println();
			}
		}
	 
		pw.close();
		
		AtlasDataCacheManager.getInstance().reloadMenuConfig();
		LOGGER.trace(loggerId + "updateMenuConfig()...exit.");
	}
	
	
	/**
	 * 
	 * Loads all summary entries from the /midb/summary.conf file.
	 * 
	 */
	protected void cacheExistingSummaryEntries() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...invoked.");

		File file = new File(SUMMARY_CONFIG_FILE);
		boolean shouldContinue = true;
		int lineLength = 0;
				
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				lineLength = outputLine.length();
				
				if(lineLength < 3 ) {
					continue;
				}
				
				if(outputLine.contains("BEGIN SUMMARY") && outputLine.contains(this.studyFolder)) {
					while (shouldContinue) {
						outputLine = br.readLine();
						if(outputLine.contains("END SUMMARY")) {
							shouldContinue = false;
						}
					}
				}
				else {
					existingSummaryConfigLines.add(outputLine);
				}
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...exit.");
	
	}
}
