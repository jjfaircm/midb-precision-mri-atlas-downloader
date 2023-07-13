package edu.umn.midb.population.atlas.study.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	private int totalNumFilesRenamed = 0;
	
	
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
	 * Adds the study prefix to all files in all folders for the given data type (surface
	 * or volume).
	 * 
	 * @param dataType - String representing surface or volume
	 * @return success - boolean
	 */
	public boolean addStudyPrefixToFileNames(String dataType) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "addStudyPrefixToFileNames()...invoked.");
		
		boolean success = true;
		
		if(dataType.equals("surface")) {
			this.surfaceZipFilePath = absoluteStudyFolder;
		}
		else if(dataType.equals("volume")) {
			this.volumeZipFilePath = absoluteStudyFolder;
		}

	    ArrayList<String> subDirs = new ArrayList<String>();
	    String targetRootPath = this.absoluteStudyFolder + dataType + "/";	
	    
	    File targetRootPathFile = new File(targetRootPath);
	    if(!targetRootPathFile.exists()) {
	    	this.errorMessage = "Data type=" + dataType + " for study=" + this.studyFolder + "<br>does not exist";
	    	this.errorEncountered = true;
	    	return false;
	    }
	    
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(targetRootPath))) {
	        for (Path path : stream) {
	            if (Files.isDirectory(path)) {
	            	subDirs.add(path.toString());
	            }
	        }
	    }
	    catch(Exception e) {
	    	LOGGER.error(loggerId + e.getMessage(), e);
	    	DiagnosticsReporter.createDiagnosticsEntry(e);
	    	this.errorMessage = "Unable to get list of subdirectories for path: " + targetRootPath;
	    	this.errorEncountered = true;
	    	return false;
	    }
	    
	   Iterator<String> dirIt = subDirs.iterator(); 
	   String[] fileNamesArray = null;
	   ArrayList<String> fileNames = null;
	   Iterator<String> fileNamesIt = null;
	   String subDirName = null;
	   long count = 0;
	   int numFilesRenamed = 0;
	   
	   while(dirIt.hasNext()) {
		    subDirName = dirIt.next();
		    numFilesRenamed = 0;

		    try {
			   count = Files.list(Paths.get(subDirName))
			            .filter(p -> p.toFile().isFile())
			            .count();
			   LOGGER.trace(loggerId + subDirName + ": count before = " + count);
		    } 
		    catch (IOException e) {
				LOGGER.error(loggerId + e.getMessage(), e);
		    }
		    		    
		    File subDir = new File(subDirName);
		    fileNamesArray = subDir.list();
		    fileNames = new ArrayList<String>(Arrays.asList(fileNamesArray));
		    fileNamesIt = fileNames.iterator();
		    String fileName = null;
		    String absoluteFileName = null;
		    String shortOrigFileName = null;
		    String absoluteOrigFileName = null;
		    String newShortFileName = null;
		    String newAbsoluteFileName = null;
		    File newFile = null;
		    File origFile = null;
		    
		    while(fileNamesIt.hasNext()) {
		    	fileName = fileNamesIt.next();
		    	if(fileName.startsWith(this.studyFolder)) {
		    		continue;
		    	}
		    	shortOrigFileName = fileName;
		    	absoluteOrigFileName = subDirName + "/" + shortOrigFileName;
            	newShortFileName = this.studyFolder + "_" + shortOrigFileName;
            	newAbsoluteFileName = subDirName + "/" + newShortFileName;
                origFile = new File(absoluteOrigFileName);
                newFile = new File(newAbsoluteFileName);
                origFile.renameTo(newFile);
                numFilesRenamed++;
                this.totalNumFilesRenamed++;
		    }
		    try {
		    	LOGGER.trace(loggerId + "subdir name=" + subDirName + "==>>number files renamed=" + numFilesRenamed);
				count = Files.list(Paths.get(subDirName))
				        .filter(p -> p.toFile().isFile())
				        .count();
				LOGGER.trace(loggerId + subDirName + ": count after = " + count);
			} 
		    catch (IOException e) {
				LOGGER.error(loggerId + e.getMessage(), e);
			}
	   }
	    LOGGER.trace(loggerId + "addStudyPrefixToFileNames()...completed renaming files...");
	    if(this.totalNumFilesRenamed>0) {
	    	this.recreateSubFolderZips(dataType);
	    }
	    else {
	    	this.errorMessage = "All file names already begin with study prefix.<br>No action performed.";
	    	this.errorEncountered = true;
	    	LOGGER.trace(loggerId + "No files renamed, not recreating subFolder zip files.");
	    }
		LOGGER.trace(loggerId + "addStudyPrefixToFileNames()...exit.");
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
	 * Removes the old surface or volume folder and the old zip file relating
	 * to the Add Study or the last Update Study.
	 * 
	 * @param dataType - String representing surface or volume data
	 */
	private void cleanup(String dataType) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "cleanup()...invoked.");
		
		boolean success = true;
		
		if(dataType.equals("surface")) {
			
			success = removeBackupSurfaceDirectory();
			if(!success) {
				LOGGER.error("cleanup()...unable to remove backup surface directory, study path=" + this.absoluteStudyFolder);
				Exception e = new Exception("cleanup()...unable to remove backup surface directory, study path=\" + this.absoluteStudyFolder");
				DiagnosticsReporter.createDiagnosticsEntry(e, true);
			}
			
			success = removeBackupSurfaceZipFile();
			if(!success) {
				LOGGER.error("cleanup()...unable to remove backup surface zip file, study path=" + this.absoluteStudyFolder);
				Exception e = new Exception("cleanup()...unable to remove backup surface zip file, study path=\" + this.absoluteStudyFolder");
				DiagnosticsReporter.createDiagnosticsEntry(e, true);
			}
			
		}
		
		LOGGER.trace(loggerId + "cleanup()...exit.");
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
	
	/**
	 * Main wrapper method for deploying the new data in the zip file.
	 * 
	 * @return success - boolean indicating if all operations were successful
	 */
	private boolean deployZippedSurfaceData() {

		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "deployZippedSurfaceData()...invoked.");
		boolean success = true;

		success = renameSurfaceDirectory();
		
		
		if(!success) {
			return success;
		}
		
		success = runSystemUnzipCommand(this.surfaceZipFilePath);
		
		if(!success) {
			rollback("surface");
			return false;
		}
		
		success = validateNetworkFoldersConfig("surface", true);
		if(!success) {
			rollback("surface");
			return false;
		}
		
		success = validateFileNames("surface", true);
		if(!success) {
			rollback("surface");
			return false;
		}
		
		success = validateDscalarFiles("surface");
		if(!success) {
			rollback("surface");
			return false;
		}
		
		success = validateThresholdFiles("surface");
		if(!success) {
			rollback("surface");
			return false;
		}

		success = updateMenuConfig(false);
		cleanup("surface");
		
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
		
		success = validateDscalarFiles("volume");
		if(!success) {
			rollback("volume");
			return false;
		}
			
		success = updateMenuConfig(true);
		
		File uploadedZipFile = new File(this.volumeZipFilePath);
		
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
	
	private boolean removeExistingZipsSubFolder(String targetPath) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeExistingZipsSubFolder()...invoked.");
		
		boolean success = true;
		
		File targetDirectory = new File(targetPath);
		if(!targetDirectory.exists()) {
			return false;
		}
		
		// removes directory recursively so directory and all contents removed
		try {
			FileUtils.deleteDirectory(targetDirectory);
		}
		catch(IOException ioE) {
			LOGGER.error(ioE.getMessage(), ioE);
			success = false;
		}
		return success;
	}
	
	/**
	 * Removes a file/folder from the local file system.
	 * 
	 * @param absolutePathAndFileName - String
	 * 
	 * @return success - boolean indicating success
	 */
	private boolean removeFile(String absolutePathAndFileName) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeFile()...invoked.");
		
		boolean success = true;
		
		File fileToRemove = new File(absolutePathAndFileName);
		
		if(fileToRemove.exists()) {
			success = fileToRemove.delete();
		}
		
		LOGGER.trace(loggerId + "removeFile()...exit.");
		return success;
	}
	
	/**
	 * Recreates the zip files contained in the 'zips' folder
	 * @param dataType - String indicating 'surface' or 'volume' data
	 * @return success - boolean
	 */
	private boolean recreateSubFolderZips(String dataType) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "recreateSubFolderZips()...invoked...war=NPDownloader_0116_2315_2023.war");
		
		boolean success = true;
		boolean existingZipsFolderFound = false;
		
    	String targetDirectory = this.absoluteStudyFolder + dataType;

    	File[] directories = new File(targetDirectory).listFiles();
    	File currentFile = null;
    	
    	String currentFolderName = null;
    	for(int i=0; i<directories.length; i++) {
    		currentFile = directories[i];
    		currentFolderName = currentFile.getName();
    		//ignore this file...may happen on MAC platform
    		if(currentFolderName.contains(".DS_Store")) {
    			continue;
    		}
    		if(!currentFile.isDirectory()) {
    			this.errorEncountered = true;
    			this.errorMessage = "Unexpected file found in " + dataType + " folder: " + currentFolderName;
    			LOGGER.trace(loggerId + errorMessage);
    			return false;
    		}
    		//we're creating a list of the single network names so we
    		//exclude combined_clusters and overlapping_networks
    		currentFolderName = directories[i].getName();
    		
    		if(currentFolderName.contentEquals("zips")) {
    			existingZipsFolderFound = true;
    			continue;
    		}
    		
    		
    		if(currentFolderName.contentEquals("combined_clusters")) {
    			this.combinedClustersFolderExists = true;
    			continue;
    		}
    		if(currentFolderName.contentEquals("overlapping_networks")) {
    			this.overlappingNetworksFolderExists = true;
    			continue;
    		}
    		this.zippedFoldersList.add(directories[i].getName());
    		LOGGER.trace(loggerId + "added entry to zippedFoldersList:" + directories[i].getName());
    	}
    	
    	if(existingZipsFolderFound) {
    		removeExistingZipsSubFolder(targetDirectory + "/zips");
    	}
    	
    	this.asyncRunner.start();
    	
		LOGGER.trace(loggerId + "recreateSubfolderZips()...exit.");
		return success;
	}
	
	/**
	 * Removes the backup surface folder, which is the n-1 version.
	 * 
	 * @return success - boolean
	 */
	private boolean removeBackupSurfaceDirectory() {

		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeBackupSurfaceDirectory()...invoked.");

		boolean success = true;
		
		String backupSurfaceDirectoryString = this.absoluteStudyFolder + "surface_backup";
		File backupSurfaceDirectory = new File(backupSurfaceDirectoryString);
		
		if(backupSurfaceDirectory.exists()) {
			
			try {
				FileUtils.deleteDirectory(backupSurfaceDirectory);
			}
			catch(IOException ioE) {
				String message = "Unable to delete " + backupSurfaceDirectoryString;
				LOGGER.trace(loggerId + message);
				String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
				this.errorEncountered = true;
				this.errorMessage = message;
				LOGGER.trace(loggerId + incidentId);
				success = false;
			}
		}
		LOGGER.trace(loggerId + "removeBackupSurfaceDirectory()...exit.");
		return success;
	}
	
	/**
	 * Removes the backup surface zip file which is the n-1 zip file.
	 * 
	 * @return success - boolean
	 */
	private boolean removeBackupSurfaceZipFile() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeBackupSurfaceZipFile()...invoked.");
		
		boolean success = true;
		File backupSurfaceZipFile = new File(this.surfaceZipFilePath + "_backup");
		
		if(backupSurfaceZipFile.exists()) {
			success = backupSurfaceZipFile.delete();
		}
		return success;
	}
	
	/**
	 * Removes the surface directory. This is invoked if the file is found to contain
	 * invalid data or structure.
	 * 
	 * @return success - boolean
	 */
	private boolean removeSurfaceDirectory() {

		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeSurfaceDirectory()...invoked.");

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
		LOGGER.trace(loggerId + "removeSurfaceDirectory()...exit.");
		return success;
	}
	
	/**
	 * Removes the uploaded zip file if the data was found to be invalid.
	 * 
	 * @param dataType - String indicating surface or volume data
	 * @return success - boolean
	 */
	private boolean removeUploadedZipFile(String dataType) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeUploadedZipFile()...invoked.");

		boolean success = false;
		
		File fileToDelete = null;
		
		if(dataType.equals("surface")) {
			fileToDelete = new File(this.surfaceZipFilePath);
			success = fileToDelete.delete();
		}
		
		LOGGER.trace(loggerId + "removeUploadedZipFile()...exit.");
		return success;
	}
	
	/**
	 * Removes the volume folder is the data was found to be invalid
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
	 * Renames the backup surface directory if it already exists. The original surface folder
	 * is renamed 'surface_backup' when the Update Study process begins. If the new data is
	 * found to be invalid a rollback is done and the surface_backup folder is renamed 'surface'.
	 * Returns false if the directory does not exist.
	 * 
	 * @return success - boolean denoting outcome
	 */
	private boolean renameBackupSurfaceDirectory() {
		
		boolean success = true;
		
		String targetSurfaceDirectory = this.absoluteStudyFolder + "surface/";
		String backupSurfaceDirectory = this.absoluteStudyFolder + "surface_backup/";
				
		File sourceDirectory = new File(backupSurfaceDirectory);
		File targetDirectory = new File(targetSurfaceDirectory);
		
		if(!sourceDirectory.exists()) {
			return false;
		}
		
		success = sourceDirectory.renameTo(targetDirectory);
		return success;
	}
	
	/**
	 * When the Update Study process begins the original zip file that contains the original
	 * data is renamed by adding '_backup' to the name. If the new data is found to be invalid
	 * then a rollback is done and the '_backup' suffix is removed from the original zip file.
	 * 
	 * @return success - boolean
	 */
	protected boolean renameBackupSurfaceZipFile() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "renameBackupSurfaceZipFile()...invoked.");
		
		boolean success = true;
		
		File fileToDelete = new File(this.surfaceZipFilePath);
		fileToDelete.delete();
		
		
		File targetZipFile = new File(this.surfaceZipFilePath);
		File sourceZipFile = new File(this.surfaceZipFilePath + "_backup");
		
		success = sourceZipFile.renameTo(targetZipFile);
		LOGGER.trace(loggerId + "renameBackupSurfaceZipFile()...exit.");
		return success;
	}
	
	/**
	 * Removes the backup volume zip file. This file is only deleted once the study update
	 * has been successfully completed.
	 * 
	 * @return success - boolean
	 */
	protected boolean renameBackupVolumeZipFile() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "renameBackupVolumeZipFile()...invoked.");
		
		boolean success = true;
		
		File fileToDelete = new File(this.volumeZipFilePath);
		fileToDelete.delete();
		
		
		File targetZipFile = new File(this.volumeZipFilePath);
		File sourceZipFile = new File(this.volumeZipFilePath + "_backup");
		
		success = sourceZipFile.renameTo(targetZipFile);
		LOGGER.trace(loggerId + "renameBackupVolumeZipFile()...exit.");
		return success;
	}
		
	/**
	 * When the Update Study process begins the original (or n-1) zip file is renamed
	 * by adding '_backup' to it.
	 * 
	 * @param zipFileName - String
	 * @return success - boolean
	 */
	protected boolean renameExistingZipFile(String zipFileName) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "renameExistingZipFile()...invoked.");
		
		boolean success = true;
		
		File sourceZipFile = new File(this.absoluteStudyFolder + zipFileName);
		File targetZipFile = new File(this.absoluteStudyFolder + zipFileName + "_backup");
		
		success = sourceZipFile.renameTo(targetZipFile);
		LOGGER.trace(loggerId + "renameExistingZipFile()...exit.");
		return success;
	}
		
	
	/**
	 * Renames the surface directory if it already exists. Returns true if the directory
	 * does not exist.
	 * 
	 * @return success - boolean denoting outcome
	 */
	private boolean renameSurfaceDirectory() {
		
		boolean success = true;
		
		String existingSurfaceDirectory = this.absoluteStudyFolder + "surface/";
		String targetSurfaceDirectory = this.absoluteStudyFolder + "surface_backup/";
				
		File sourceDirectory = new File(existingSurfaceDirectory);
		File targetDirectory = new File(targetSurfaceDirectory);
		
		if(!sourceDirectory.exists()) {
			return success;
		}

		success = sourceDirectory.renameTo(targetDirectory);
		
		if(!success) {
			this.errorEncountered = true;
			this.errorMessage = "Update failed, unable to create backup source directory";
		}
		return success;
	}
	
	/** 
	 * Wrapper method for invoking methods required to do a rollback if the new data is
	 * found to be invalid.
	 * 
	 * @param dataType - String indicating surface or volume data
	 */
	private void rollback(String dataType) {
		
		if(dataType.equals("surface")) {
			removeSurfaceDirectory();
			removeUploadedZipFile("surface");
			renameBackupSurfaceDirectory();
			renameBackupSurfaceZipFile();
		}
	}
	
	/**
	 * Updates the /midb/menu.conf file
	 * 
	 * @param isVolumeData - boolean specifying if the update is adding volume data
	 * 
	 * @return success - boolean
	 */
	public boolean updateMenuConfig(boolean isVolumeData) {
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
					// we assume that all studies have surface data
					// if we are adding volume data, we change available data types
					// from surface to surface_volume
					if(isVolumeData && !configLine.contains("surface_volume")) {
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
		boolean success = true;

		String fileName = null;
		String absolutePathAndFileName = null;

		try {
		
			for (Part part : request.getParts()) {
				fileName = part.getSubmittedFileName();
				
				absolutePathAndFileName = absoluteStudyFolder + fileName;
				
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
				renameExistingZipFile(fileName);
			    part.write(absoluteStudyFolder + fileName);
			}
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			BIDS_FatalException bfE = new BIDS_FatalException(e.getMessage(), ste);
			throw bfE;
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
				success = validateZipFileTopLevelFolder("volume");
				if(!success) {
					renameBackupVolumeZipFile();
					return fileName;
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
				success = validateZipFileTopLevelFolder("surface");
				if(!success) {
					renameBackupSurfaceZipFile();
					return fileName;
				}
			}
			
			success = cacheMenuEntriesConfig();
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
