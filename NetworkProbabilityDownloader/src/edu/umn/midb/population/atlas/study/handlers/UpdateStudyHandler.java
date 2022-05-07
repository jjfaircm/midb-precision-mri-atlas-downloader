package edu.umn.midb.population.atlas.study.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.data.access.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;

/**
 * 
 * Handles the tasks required to update a study. Available update actions are 1) update summary
 * and 2) add volume data.
 * 
 * @author jjfair
 *
 */
public class UpdateStudyHandler extends StudyHandler {
	
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);
	
	private static final String MENU_CONFIG_FILE_PATH = "/midb/menu.conf";
	
	private static final String TEMPLATE_BEGIN_SUMMARY = "BEGIN SUMMARY (ID=${studyFolderName})";
	private static final String TEMPLATE_BEGIN_FOLDERS_ENTRY = "NETWORK FOLDERS ENTRY (id=${studyFolderName})";
    
	private static final String REPLACE_STUDY_FOLDER_NAME = "${studyFolderName}";
	private static final String END_SUMMARY_STRING = "END SUMMARY";
	private static final String END_FOLDERS_ENTRY_STRING = "END NETWORK FOLDERS ENTRY";

	
	private static final String MENU_ID_STRING = "ID=";
	private static final String END_MENU_STRING = "END MENU ENTRY";
	private static final String SUMMARY_CONF_PATH = "/midb/summary.conf";
	private static final String NETWORK_FOLDERS_CONF_PATH = "/midb/network_folder_names.conf";


	private String newSummaryTextFilePath = null;
	private ArrayList<String> newSummaryLines = new ArrayList<String>();
	private ArrayList<String> existingSummaryLines = new ArrayList<String>();
	private ArrayList<String> newSummaryConfLines = new ArrayList<String>();
	private ArrayList<String> existingMenuConfigLines = new ArrayList<String>();
	
	/**
	 * Public constructor
	 * 
	 * @param studyFolderId - String
	 * @param appContext - {@link ApplicationContext}
	 */
	public UpdateStudyHandler(String studyFolderId, ApplicationContext appContext) {
		super();
		this.appContext = appContext;
		this.studyFolder = studyFolderId;
		this.absoluteStudyFolder = ROOT_DESTINATION_PATH + studyFolder + File.separator;
		
		if(!this.absoluteStudyFolder.endsWith("/")) {
			this.absoluteStudyFolder += "/";
		}
	}
	
	/**
	 * Loads the menu entries from the /midb/menu.conf file.
	 * 
	 * 
	 * @return success - boolean
	 */
	private boolean cacheMenuEntriesConfig() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "cacheMenuEntriesConfig()...invoked.");

		boolean success = true;
		
		try {
			FileReader fr = new FileReader(MENU_CONFIG_FILE_PATH);
			BufferedReader br = new BufferedReader(fr);
			String configLine = null;
			String targetMenuEntryAsString = "";
			boolean targetEntryFound = false;
			boolean targetEntryEnded = false;
			
			while ((configLine = br.readLine()) != null) {
				this.existingMenuConfigLines.add(configLine);
				
				if(configLine.contains(this.studyFolder)) {
					targetEntryFound = true;
				}
				if(targetEntryFound && !targetEntryEnded) {
					targetMenuEntryAsString += configLine;
					if(configLine.contains(END_MENU_STRING)) {
						targetEntryEnded = true;
					}
				}
			}
			br.close();
			this.menuEntry = targetMenuEntryAsString;
		} 
		catch (IOException ioE) {
	   		LOGGER.trace(loggerId + "cacheMenuEntriesConfig()...error enountered");
    		LOGGER.trace(ioE.getMessage(), ioE);
    		String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
    		this.errorEncountered = true;
    		this.errorMessage = "Unable to read menu.conf file. Update aborted.<br>" + incidentId;
    		success = false;
		}
		LOGGER.trace(loggerId + "cacheMenuEntriesConfig()...exit.");
		return success;
	}
	
	/**
	 * Loads the entry lines fromthe /midb/network_folder_names.conf file.
	 * 
	 * @return success - boolean
	 */
	private boolean cacheSingleNetworkFolderNamesList() {
		
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "cacheSingleNetworkFolderNamesList()...invoked.");

		boolean success = true;
		
		try {
			File foldersConfFile = new File(NETWORK_FOLDERS_CONF_PATH);
			FileReader fr = new FileReader(foldersConfFile);
			BufferedReader br = new BufferedReader(fr);
			
			String entryLine = null;
			String startCachingTriggerLine = TEMPLATE_BEGIN_FOLDERS_ENTRY.replace(REPLACE_STUDY_FOLDER_NAME, this.studyFolder);
			String[] entryArray = null;
			String folderName = null;
			boolean shouldCache = false;
			
			while ((entryLine = br.readLine()) != null) {
				if(entryLine.contains(startCachingTriggerLine)) {
					shouldCache = true;
					//we don't want the first line of the entry...it only designates the
					//beginning of an entry, then the actual folder names start on the
					//next line
					continue;
				}
				if(shouldCache  && entryLine.contains(END_FOLDERS_ENTRY_STRING)) {
					shouldCache = false;
					continue;
				}
				
				if(shouldCache) {
					entryArray = entryLine.split("=");
					folderName = entryArray[1].trim(); 
					this.configuredFoldersList.add(folderName);
				}
			}
			br.close();
		}
		catch(IOException ioE) {
			LOGGER.fatal(loggerId + "Error reading " + NETWORK_FOLDERS_CONF_PATH);
			LOGGER.fatal(ioE.getMessage(), ioE);
			String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
			this.errorEncountered = true;
			this.errorMessage = ("Error encountered, update failed: " + incidentId);
			success = false;
		}
		LOGGER.trace(loggerId + "cacheSingleNetworkFolderNamesList()...invoked.");

		return success;
	}
	
	/**
	 * Loads the existing entries in the /midb/summary.txt file. The entry for the study
	 * be updated is omitted.
	 * 
	 * @return success - boolean
	 */
	private boolean cacheExistingSummaryEntryLines() {
		
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "cacheExistingSummaryEntryLines()...invoked.");

		boolean success = true;
		
		try {
			File summaryConfFile = new File(SUMMARY_CONF_PATH);
			FileReader fr = new FileReader(summaryConfFile);
			BufferedReader br = new BufferedReader(fr);
			
			String entryLine = null;
			String stopCachingTriggerLine = TEMPLATE_BEGIN_SUMMARY.replace(REPLACE_STUDY_FOLDER_NAME, this.studyFolder);
			boolean shouldCache = true;
			
			while ((entryLine = br.readLine()) != null) {
				if(shouldCache) {
					this.existingSummaryLines.add(entryLine);
				}
				
				if(entryLine.contains(stopCachingTriggerLine)) {
					shouldCache = false;
				}
				if(!shouldCache  && entryLine.contains(END_SUMMARY_STRING)) {
					this.existingSummaryLines.add(entryLine);
					shouldCache = true;
				}
			}
			br.close();
		}
		catch(IOException ioE) {
			LOGGER.fatal(loggerId + "Error reading existing summaryLines");
			LOGGER.fatal(ioE.getMessage(), ioE);
			String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
			this.errorEncountered = true;
			this.errorMessage = ("Error encountered, update failed: " + incidentId);
			success = false;
		}
		LOGGER.trace(loggerId + "cacheExistingSummaryEntryLines()...exit.");

		return success;
		
	}
	
	/**
	 * Loads the summary entry lines from the uploaded summary.txt file.
	 * 
	 * @return success - boolean
	 */
	private boolean cacheNewSummaryEntryLines()  {
		
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "cacheNewSummaryEntryLines()...invoked.");
		
		boolean success = true;

		try {
			File newSummaryFile = new File(this.newSummaryTextFilePath);
			FileReader fr = new FileReader(newSummaryFile);
			BufferedReader br = new BufferedReader(fr);
			String entryLine = null;
			
			while ((entryLine = br.readLine()) != null) {
				if(entryLine.contains("[") && entryLine.contains("]")) {
					entryLine = createUrlLinkEntry(entryLine);
				}
				this.newSummaryLines.add(entryLine);
			}
			
			br.close();
		}
		catch(IOException ioE) {
			LOGGER.fatal(loggerId + "Error reading new summaryLines");
			LOGGER.fatal(ioE.getMessage(), ioE);
			String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
			this.errorEncountered = true;
			this.errorMessage = ("Error encountered, update failed: " + incidentId);
			success = false;
		}
		LOGGER.trace(loggerId + "cacheNewSummaryEntryLines()...exit.");
		return success;
	}
	
	/**
	 * Updates the /midb/network_folder_names.conf file by inserting the updated entry for
	 * the study.
	 * 
	 * @return success - boolean
	 */
	private boolean createNewSummaryConf()  {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "cacheNewSummaryEntryLines()...invoked.");
	
		boolean success = true;
		
		Iterator<String> existingLinesIt = this.existingSummaryLines.iterator();
		Iterator<String> newLinesIt = this.newSummaryLines.iterator();
		String startMergeTriggerLine = TEMPLATE_BEGIN_SUMMARY.replace(REPLACE_STUDY_FOLDER_NAME, this.studyFolder);

		String existingLine = null;
		
		while(existingLinesIt.hasNext()) {
			existingLine = existingLinesIt.next();
			this.newSummaryConfLines.add(existingLine);
			
			if(existingLine.contains(startMergeTriggerLine)) {
				while(newLinesIt.hasNext()) {
					this.newSummaryConfLines.add(newLinesIt.next());
				}
			}
		}
		
		Iterator<String> newConfLinesIt = this.newSummaryConfLines.iterator();
		
		try {
			FileWriter fw = new FileWriter(SUMMARY_CONF_PATH);
			PrintWriter pw = new PrintWriter(fw);
			
			while(newConfLinesIt.hasNext()) {
				pw.println(newConfLinesIt.next());
			}
			pw.close();

		}
		catch(IOException ioE) {
			LOGGER.fatal(loggerId + "Error creating new summary.conf file");
			LOGGER.fatal(ioE.getMessage(), ioE);
			String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
			this.errorEncountered = true;
			this.errorMessage = ("Error creating new summary.conf file, update failed: " + incidentId);
			success = false;
		}
		
		//delete the uploaded file
		File uploadedFile = new File(this.newSummaryTextFilePath);
		uploadedFile.delete();
		//load the new config
		AtlasDataCacheManager.getInstance().reloadSummaryConfig();

		return success;
	}
	
	private boolean deployZippedSurfaceData() {

		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "deployZippedSurfaceData()...invoked.");
		boolean success = true;
		
		//remove folder if already exists
		success = removeSurfaceFolder();
		
		if(!success) {
			return success;
		}
		
		success = runSystemUnzipCommand(this.surfaceZipFilePath);
		
		if(!success) {
			File uploadedZipFile = new File(this.surfaceZipFilePath);
			uploadedZipFile.delete();
			return false;
		}
		
		success = validateNetworkFoldersConfig("surface", true);
		if(!success) {
			File uploadedZipFile = new File(this.surfaceZipFilePath);
			uploadedZipFile.delete();

			removeSurfaceFolder();

			return false;
		}
		
		
		if(!success) {
			return success;
		}
		
		success = updateMenuConfig();
		
		File uploadedZipFile = new File(this.surfaceZipFilePath);
		uploadedZipFile.delete();
		
		asyncRunner.start();
		
		LOGGER.trace(loggerId + "deployZippedSurfaceData()...exit.");

		return success;
	
	}
	
	/**
	 * Entry point for deploying the volume data contained in the uploaded volume.zip file.
	 * 
	 * @return success - boolean
	 */
	private boolean deployZippedVolumeData() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "deployZippedVolumeData()...invoked.");
		boolean success = true;
		
		//remove folder if already exists
		success = removeVolumeFolder();
		
		if(!success) {
			return success;
		}
		
		success = runSystemUnzipCommand(this.volumeZipFilePath);
		
		if(!success) {
			File uploadedZipFile = new File(this.volumeZipFilePath);
			uploadedZipFile.delete();
			return false;
		}
		
		success = validateNetworkFoldersConfig("volume", true);
		if(!success) {
			File uploadedZipFile = new File(this.volumeZipFilePath);
			uploadedZipFile.delete();
			
			removeVolumeFolder();

			return false;
		}
		
		if(!success) {
			return success;
		}
		
		success = updateMenuConfig();
		
		File uploadedZipFile = new File(this.volumeZipFilePath);
		uploadedZipFile.delete();
		
		asyncRunner.start();

		LOGGER.trace(loggerId + "deployZippedVolumeData()...exit.");

		return success;
	}
	
	/**
	 * Returns the study folder name of the study which is used as the id.
	 * 
	 * @return studyFolder - String
	 */
	public String getStudyId() {
		return this.studyFolder;
	}
	
	/**
	 * Removes the surface folder.
	 * 
	 * @return success - boolean
	 */
	private boolean removeSurfaceFolder() {

		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeSurfaceFolder()...invoked.");

		boolean success = true;
		
		String surfaceDirectoryString = this.absoluteStudyFolder + "surface";
		File surfaceDirectory = new File(surfaceDirectoryString);
		
		if(surfaceDirectory.exists()) {
			
			try {
				FileUtils.deleteDirectory(surfaceDirectory);
			}
			catch(IOException ioE) {
				String message = "Unable to delete " + surfaceDirectoryString;
				LOGGER.trace(loggerId + message);
				String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
				this.errorEncountered = true;
				this.errorMessage = message;
				LOGGER.trace(loggerId + incidentId);
				success = false;
			}
		}
		LOGGER.trace(loggerId + "removeSurfaceFolder()...exit.");
		return success;
	}
	
	
	/**
	 * Removes the volume folder.
	 * 
	 * @return success - boolean
	 */
	private boolean removeVolumeFolder() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeVolumeFolder()...invoked.");

		boolean success = true;
		
		String volumeDirectoryString = this.absoluteStudyFolder + "volume";
		File volumeDirectory = new File(volumeDirectoryString);
		
		if(volumeDirectory.exists()) {
			
			try {
				FileUtils.deleteDirectory(volumeDirectory);
			}
			catch(IOException ioE) {
				String message = "Unable to delete " + volumeDirectoryString;
				LOGGER.trace(loggerId + message);
				String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
				this.errorEncountered = true;
				this.errorMessage = message;
				LOGGER.trace(loggerId + incidentId);
				success = false;
			}
		}
		LOGGER.trace(loggerId + "removeVolumeFolder()...exit.");
		return success;
	}
	
	/**
	 * Updates the /midb/menu.conf file
	 * 
	 * @return success - boolean
	 */
	public boolean updateMenuConfig() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");
		
		boolean success = true;
		
		try {
			FileWriter fw = new FileWriter(MENU_CONFIG_FILE_PATH);
			PrintWriter pw = new PrintWriter(fw);
			
			Iterator<String> configLinesIt = this.existingMenuConfigLines.iterator();
			String configLine = null;
			
			while(configLinesIt.hasNext()) {
				configLine = configLinesIt.next();
				if(configLine.contains(MENU_ID_STRING) && configLine.contains(this.studyFolder)) {
					pw.println(configLine);
					configLine = configLinesIt.next();
					// in case we are just adding a newer version of volume data
					if(!configLine.contains("surface_volume")) {
						configLine = configLine.replace("surface", "surface_volume");
					}
				}
				pw.println(configLine);
			}
			pw.close();
			AtlasDataCacheManager.getInstance().reloadMenuConfig();
		}
		catch(IOException ioE) {
			String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
			String message = "Unable to read " + MENU_CONFIG_FILE_PATH + "<br> " + incidentId;
			LOGGER.trace(loggerId + "Unable to read " + MENU_CONFIG_FILE_PATH);
			this.errorEncountered = true;
			this.errorMessage = message;
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Handles receiving the uploaded file which is either summary.txt or volume.zip.
	 * 
	 * @param request - HttpServletRequest
	 * @param fileSize - size of the file as a long
	 * @return fileName - name of the uploaded file
	 */
	public String uploadFile(HttpServletRequest request, long fileSize) {
		
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "uploadFile()...invoked.");
		
		boolean isVolumeZip = false;
		boolean isSurfaceZip = false;
		boolean isSummaryText = false;
		long uploadedZipSize = 0;
		boolean shouldContinue = true;

		String fileName = null;

		try {
		
			for (Part part : request.getParts()) {
				fileName = part.getSubmittedFileName();
				
				if(fileName.toLowerCase().contains("volume")) {
					isVolumeZip = true;
					this.volumeZipFilePath = absoluteStudyFolder + fileName;
				}
				if(fileName.toLowerCase().contains("surface")) {
					isSurfaceZip = true;
					this.surfaceZipFilePath = absoluteStudyFolder + fileName;
				}
				else if(fileName.toLowerCase().equals("summary.txt")) {
					isSummaryText = true;
					this.newSummaryTextFilePath = absoluteStudyFolder + fileName;
				}
			    part.write(absoluteStudyFolder + fileName);
			}
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
			//StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			//BIDS_FatalException bfE = new BIDS_FatalException(e.getMessage(), ste);
			//throw bfE;
			LOGGER.fatal(loggerId + "Error creating new summary.conf file");
			LOGGER.fatal(e.getMessage(), e);
			String incidentId = DiagnosticsReporter.createDiagnosticsEntry(e);
			this.errorEncountered = true;
			this.errorMessage = ("Error creating new summary.conf file, update failed: " + incidentId);
			shouldContinue = false;
		}
		
		if(!shouldContinue) {
			return fileName;
		}
		
		createConfigBackup();
		
		if(isVolumeZip || isSurfaceZip) {
			if(isVolumeZip) {
				File volumeZipFile = new File(this.volumeZipFilePath);
				uploadedZipSize = volumeZipFile.length();
				if(uploadedZipSize != fileSize) {
					String message = "File upload error for file " + fileName;
					message += ". Target file size=" + fileSize;
					message +=  ". Received fileSize=" + uploadedZipSize;
					StackTraceElement[] ste = Thread.currentThread().getStackTrace();
					BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
					throw bfE;
				}
			}
			if(isSurfaceZip) {
				File surfaceZipFile = new File(this.surfaceZipFilePath);
				uploadedZipSize = surfaceZipFile.length();
				if(uploadedZipSize != fileSize) {
					String message = "File upload error for file " + fileName;
					message += ". Target file size=" + fileSize;
					message +=  ". Received fileSize=" + uploadedZipSize;
					StackTraceElement[] ste = Thread.currentThread().getStackTrace();
					BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
					throw bfE;
				}
			}
			boolean success = cacheMenuEntriesConfig();
			if(!success) {
				return fileName;
			}
			
			success = cacheSingleNetworkFolderNamesList();
			if(!success) {
				return fileName;
			}
			if(isVolumeZip) {
				success = deployZippedVolumeData();
			}
			if(isSurfaceZip) {
				success = deployZippedSurfaceData();
			}
			
		}
	
		else if(isSummaryText) {
			shouldContinue = true;
			shouldContinue = cacheExistingSummaryEntryLines();
			if(shouldContinue) {
				shouldContinue = cacheNewSummaryEntryLines();
			}
			if(shouldContinue) {
				createNewSummaryConf();
			}
		}
		
		LOGGER.info(loggerId + "uploadFile()...file name=" + fileName);
		LOGGER.info(loggerId + "uploadFile()...fileSize parameter=" + fileSize);
		LOGGER.info(loggerId + "uploadFile()...uploaded file size=" + uploadedZipSize);
		
		LOGGER.trace(loggerId + "uploadFile()...exit.");
		return fileName;
		
	}
	

}
