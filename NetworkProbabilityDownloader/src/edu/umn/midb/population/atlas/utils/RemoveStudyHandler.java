package edu.umn.midb.population.atlas.utils;

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

import logs.ThreadLocalLogTracker;

public class RemoveStudyHandler {

	private static final String ROOT_STUDY_PATH = "/midb/studies/";
	private static final String MENU_CONFIG_FILE = "/midb/menu.conf";
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);


	private String studyFolder = null;
	private ArrayList<String> existingConfigLines = new ArrayList<String>();

	
	public RemoveStudyHandler(String studyFolder) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "RemoveStudyHandler()...invoked.");
		this.studyFolder = studyFolder;
		LOGGER.trace(loggerId + "RemoveStudyHandler()...exit.");
	}
		
	protected void cacheExistingMenuEntries() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...invoked.");

		File file = new File(MENU_CONFIG_FILE);
		boolean shouldContinue = true;
				
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				if(outputLine.trim().length() == 0) {
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
					existingConfigLines.add(outputLine);
				}
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "cacheExistingMenuEntries()...exit.");
	}
	
	public void removeStudy() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "removeStudy()...invoked.");
		
		cacheExistingMenuEntries();
		updateMenuConfig();
		removeStudyDirectory();
		
		LOGGER.trace(loggerId + "removeStudy()...exit.");
	}
	
	protected void removeStudyDirectory() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "removeStudyDirectory()...invoked.");

		File targetDirectory = new File(ROOT_STUDY_PATH + this.studyFolder);
		FileUtils.deleteDirectory(targetDirectory);
		
		LOGGER.trace(loggerId + "removeStudyDirectory()...exit.");
	}
	
	protected void updateMenuConfig() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");

		PrintWriter pw = new PrintWriter(new FileWriter(MENU_CONFIG_FILE));
		 
		Iterator<String> cachedEntriesIt = this.existingConfigLines.iterator();
		
		while(cachedEntriesIt.hasNext()) {
			pw.println(cachedEntriesIt.next());
		}
	 
		pw.close();
		
		AtlasDataCacheManager.getInstance().reloadMenuConfig();
		LOGGER.trace(loggerId + "updateMenuConfig()...exit.");
	}
}
