package edu.umn.midb.population.atlas.study.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.EmailNotifier;
import edu.umn.midb.population.atlas.utils.SMSNotifier;

/**
 * Base class for handling creating (adding), removing, or updating a study. The subclasses are:
 * 
 * <ul>
 * <li> {@link CreateStudyHandler}</li>
 * <li> {@link RemoveStudyHandler}</li>
 * <li> {@link UpdateStudyHandler}</li>
 * </ul>
 * 
 * @author jjfair
 *
 */
public class StudyHandler {
	
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);
	protected static final String ROOT_DESTINATION_PATH = "/midb/studies/";
	protected static final String REPLACE_FOLDER_NAME = "${folderName}";
	private static final String TEMPLATE_ZIP_COMMAND = "/usr/bin/zip -r zips/${folderName}.zip ${folderName} -x \"*.DS_Store\" -x \"__MACOSX\" -x \"*.png\"";
	private static final String TEMPLATE_LINK = "<a href=\"${url}/\" target=\"_blank\" style=\"font-style: italic; color:DarkBlue;\">${linkText}</a>";


	protected static final String TEMPLATE_FOLDERS_SPACE_ERROR_MESSAGE = "folder.txt error: folder ${folderName} contains spaces in the name."
            + " Folders with spaces in name not allowed. Study not created.";


	protected ApplicationContext appContext = null;
	protected String absoluteStudyFolder = null;
	//zippedFoldersList is the list of folders found in the zip file
	//NOTE: the list of folders under volume should match those under the surface folder
	protected ArrayList<String> zippedFoldersList = new ArrayList<String>();
	//configuredFoldersList represents the folders listed in the uploaded folders.txt file
	protected ArrayList<String> configuredFoldersList = new ArrayList<String>();
	
	protected boolean combinedClustersFolderExists = false;
	protected boolean overlappingNetworksFolderExists = false;
	protected String menuEntry;


	protected String studyFolder = null;
	protected String surfaceZipFilePath = null;
	protected String volumeZipFilePath = null;
	protected boolean errorEncountered = false;
	protected String errorMessage = null;

	
	/**
	 * 
	 * Backs up the config files before creating the entries for the new study.
	 * This is done in case a rollback is necessary.
	 * 
	 */
	protected void createConfigBackup() {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createConfigBackup()...invoked.");

		String commandToExecute = "cp /midb/*.conf /midb/conf_backup";
		
    	Process process = null;
    	BufferedReader brStdIn = null;
    	BufferedReader brStdErr = null;
    	String stdInLine = null;
    	String stdErrLine = null;
    	String errorDetails = "";
    	int returnCode = 999;
    	ProcessBuilder pBuilder = new ProcessBuilder("bash", "-c", commandToExecute);

    	
    	try {
			LOGGER.trace(loggerId + "command=" + commandToExecute);
			process = pBuilder.start();    		
		
			brStdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			while ((stdInLine = brStdIn.readLine()) != null) {
				LOGGER.trace(loggerId + stdInLine);
			}
			
    		returnCode = process.waitFor();
    		LOGGER.trace(loggerId + "returnCode=" + returnCode);
    		
    		if(returnCode != 0) {
	    		brStdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				
				while ((stdErrLine = brStdErr.readLine()) != null) {
					LOGGER.error(stdErrLine);
					errorDetails += stdErrLine + "\n";
	    		}
				brStdIn.close();
    		}
    	}
    	catch(Exception e) {
    		LOGGER.trace(loggerId + "Error trying to copy conf files to /midb/conf_backup");
    		LOGGER.trace(e.getMessage(), e);
    	}
    	
		LOGGER.trace(loggerId + "createConfigBackup()...exit.");
	}
	
	
	/**
	 * 
	 * If a line in the summary entry contains a link, this method generates the
	 * appropriate html anchor tag.
	 * 
	 * @param summaryLine - String
	 * @return anchorTag - String
	 */
	protected String createUrlLinkEntry(String summaryLine) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createUrlLinkEntry()...invoked.");

		String linkEntry = null;
		if(!summaryLine.endsWith("]")) {
			int endIndex = summaryLine.indexOf("]");
			summaryLine = summaryLine.substring(0, endIndex);
		}
		summaryLine = summaryLine.replace("[", "");
		summaryLine = summaryLine.replace("]", "");

		String[] entryArray = summaryLine.split("@");
		linkEntry = TEMPLATE_LINK;
		linkEntry = linkEntry.replace("${linkText}", entryArray[0]);
		linkEntry = linkEntry.replace("${url}", entryArray[1]);
		LOGGER.trace(loggerId + "createUrlLinkEntry()...exit.");

		return linkEntry;
		
	}
	

	
	/**
	 * Returns an error message if one has been encountered.
	 * 
	 * @return errorMessage - String
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	
	/**
	 * Creates zip files for the various types of available network data. This is done
	 * so that the user can download the files for all 'thresholds' if desired.
	 * 
	 * @param volumeOnly - boolean indicating to generate zips only for volume data
	 * 
	 * @return success - boolean indicating if the subfolders were zipped successfully
	 */
	protected boolean createSubFolderZips() {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createSubFolderZips()...invoked.");
		
		long startTime = System.currentTimeMillis();
		
		Iterator<String> foldersIt = this.zippedFoldersList.iterator();
		String currentFolder = null;
		String surfaceRootFolder = this.absoluteStudyFolder + "surface";
		String volumeRootFolder = this.absoluteStudyFolder + "volume";
		int count = 0;
		boolean success = true;

		
		while(foldersIt.hasNext()) {
			count++;
			currentFolder = foldersIt.next();
			
			if(this.surfaceZipFilePath != null) {
				if(count==1) {
					String zipDirPath = surfaceRootFolder + File.separator + "zips";
					File zipDirectory = new File(zipDirPath);
					zipDirectory.mkdir();
				}
				success = runSystemZipFolderCommand(currentFolder, surfaceRootFolder);
				if(!success) {
					return success;
				}
			}
			if(this.volumeZipFilePath != null) {
				if(count==1) {
					String zipDirPath = volumeRootFolder + File.separator + "zips";
					File zipDirectory = new File(zipDirPath);
					zipDirectory.mkdir();
				}
				success = runSystemZipFolderCommand(currentFolder, volumeRootFolder);
				if(!success) {
					return success;
				}
			}
			
		}
		
		if(this.combinedClustersFolderExists) {
			if(this.surfaceZipFilePath != null) {
				currentFolder = "combined_clusters";
				success = runSystemZipFolderCommand(currentFolder, surfaceRootFolder);
				if(!success) {
					return success;
				}
			}
			if(this.volumeZipFilePath != null) {
				currentFolder = "combined_clusters";
				success = runSystemZipFolderCommand(currentFolder, volumeRootFolder);
				if(!success) {
					return success;
				}
			}
		}
		
		if(this.overlappingNetworksFolderExists) {
			if(this.surfaceZipFilePath != null) {
				currentFolder = "overlapping_networks";
				success = runSystemZipFolderCommand(currentFolder, surfaceRootFolder);
				if(!success) {
					return success;
				}
			}
			if(this.volumeZipFilePath != null) {
				currentFolder = "overlapping_networks";
				success = runSystemZipFolderCommand(currentFolder, volumeRootFolder);
				if(!success) {
					return success;
				}
			}
		}
		
		long finishTime = System.currentTimeMillis();
		long elapsedTime = finishTime - startTime;
		long elapsedSeconds = elapsedTime/1000;
		
		LOGGER.trace(loggerId + "createSubFolderZips()...exit. elapsedSeconds=" + elapsedSeconds);
		return success;
	}
	
	
	/**
	 * Returns a boolean indicating if an error has been encountered.
	 * 
	 * @return errorEncountered - boolean
	 */
	public boolean isErrorEncountered() {
		return this.errorEncountered;
	}
	
	
	/**
     * 
     * Executes the 'unzip' command as a bash command.
     * 
     * @param absoluteZipPath - String
     * @return success - boolean
     */
    protected boolean runSystemUnzipCommand(String absoluteZipPath) {

		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "runSystemUnzipCommand()...invoked.");
		boolean isSurface = false;
		boolean isVolume = false;
		boolean zipFormatErrorExists = false;
		boolean success = true;
		
		if(absoluteZipPath.contains("surface")) {
			isSurface = true;
		}
		else if(absoluteZipPath.contains("volume")) {
			isVolume = true;
		}
		
		
		String zipFileNameOnly = null;
		int index = 0;
		index = absoluteZipPath.lastIndexOf("/");
		
		if(index>0) {
			zipFileNameOnly = absoluteZipPath.substring(index+1);
		}

    	int returnCode = -1;
    	String baseCommand = "/usr/bin/unzip ";
    	String commandToExecute = baseCommand + absoluteZipPath;
    	//Runtime runtime = Runtime.getRuntime();
    	Process process = null;
    	BufferedReader brStdIn = null;
    	BufferedReader brStdErr = null;
    	String stdInLine = null;
    	String stdErrLine = null;
    	String errorDetails = "";
    	String errorMessage = null;

    	int lineCount = 0;
    	
		File workingDirectory = new File(this.absoluteStudyFolder);
    	ProcessBuilder pBuilder = new ProcessBuilder("bash", "-c", commandToExecute);
    	pBuilder.directory(workingDirectory);
    	
    	
    	try {
    		LOGGER.trace(loggerId + "command=" + commandToExecute);
    		process = pBuilder.start();    		
    	
    		brStdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		
			while ((stdInLine = brStdIn.readLine()) != null && !zipFormatErrorExists) {
				lineCount++;
				if(lineCount < 4) {
					LOGGER.trace(stdInLine);
					//completeOutput += stdInLine;
				}
				if(lineCount == 4) {
					LOGGER.trace("Unzipping remaining folders...");
				}
				if(lineCount==2 && isSurface) {
					if(!stdInLine.trim().contentEquals("creating: surface/")) {
						this.errorEncountered = true;
						LOGGER.trace(loggerId + "setZipFormatError=" + true);
						zipFormatErrorExists = true;
						success = false;
						this.errorMessage = "surface.zip format error: top level directory is not surface/";
					}
				}
				if(lineCount==2 && isVolume) {
					if(!stdInLine.trim().contentEquals("creating: volume/")) {
						this.errorEncountered = true;
						this.errorMessage = "volume.zip format error: top level directory is not volume/";
						LOGGER.trace(loggerId + "setZipFormatError=" + true);
						success = false;
						zipFormatErrorExists = true;
						this.errorEncountered = true;
					}
				}
				if(stdInLine.contains("DS_")) {
					LOGGER.trace(stdInLine);
				}
    		}
			brStdIn.close();
			
    		returnCode = process.waitFor();
    		LOGGER.trace(loggerId + "returnCode=" + returnCode);

			if(returnCode != 0) {
				// In uploadFile() the fileSize is received as a parameter. This value is
				// set by the client javascript and should match the actual uploaded size.
				// This check is also done in uploadFile().  If an error exists, an exception
				// will be thrown and the upload will fail with an error on the client.  That
				// is why at this point we ignore a non-zero return code because it has been 
				// shown to be caused by either an XHR error or zip file header error.  When 
				// those errors occur, but the size matches the uploaded size, then the extracted
				// data has proven to be good.  Nevertheless, a warning is sent via the 
				// EmailNotifier.
				/*
	    		String message = completeOutput;
	    		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	    		BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
	    		throw bfE;
	    		*/
				/*
				String message = "Study was created with possible zip file upload corruption: ";
				if(isSurface) {
					message += "surface.zip";
				}
				else if(isVolume) {
					message += "volume.zip";
				}
				this.appContext.setCreateStudyErrorMessage(message);
				this.appContext.setZipFileUnpackError(true);
				*/
				
	    		brStdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				
				while ((stdErrLine = brStdErr.readLine()) != null) {
					lineCount++;
					if(lineCount < 4) {
						LOGGER.error(stdErrLine);
						//completeOutput += stdInLine;
					}
					errorDetails += stdErrLine + "\n";
	    		}
				brStdIn.close();
				
				String fileName = null;
				if(isSurface) {
					fileName = "surface.zip";
				}
				else if(isVolume) {
					fileName = "volume.zip";
				}
				String domainName = NetworkProbabilityDownloader.getDomainName();
				String messageDetail = this.studyFolder + ": bad unzip return code for fileName=" + fileName;
				String message = "MIDB_APP_CREATE_STUDY_WARNING::::" + domainName + messageDetail;
				SMSNotifier.sendNotification(message, "CreateStudyHandler");
			}
    	}
    	catch(IOException ioE) {
    		LOGGER.trace(loggerId + "runSystemUnzipCommand()...error enountered");
    		LOGGER.trace(ioE.getMessage(), ioE);
    		String incidentId = DiagnosticsReporter.createDiagnosticsEntry(ioE);
    		this.errorEncountered = true;
    		this.errorMessage = "Error encountered while unzipping file.<br>" + incidentId;
    		success = false;
    	}
    	catch(InterruptedException iE) {
    		LOGGER.trace(loggerId + "runSystemUnzipCommand()...error enountered");
    		LOGGER.trace(iE.getMessage(), iE);
    		String message = iE.getMessage();
    		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
    		BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
    		String incidentId = DiagnosticsReporter.createDiagnosticsEntry(bfE);
    		this.errorMessage = "Error encountered while unzipping file.<br>" + incidentId;
    		success = false;
    	}
		LOGGER.trace(loggerId + "runSystemUnzipCommand()...exit, zipFormatErrorExists=" + zipFormatErrorExists);
		return success;
    }
    
	/**
	 * 
	 * Executes the bash command 'zip'.
	 * 
	 * @param folderName - String that is the folder to zip
	 * @param workingDirectoryPath - what the working directory should be
	 * 
	 * @return success - boolean indicating if the zip command completed successfully
	 */
    protected boolean runSystemZipFolderCommand(String folderName, String workingDirectoryPath) {
    	
    	String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "runSystemZipFolderCommand()...invoked.");

		boolean success = true;
    	int returnCode = -1;
    	//String zipFileName = this.studyFolder + "_" + dataType + "_" folderName;
    	String commandToExecute = TEMPLATE_ZIP_COMMAND.replace(REPLACE_FOLDER_NAME, folderName);
    	//Runtime runtime = Runtime.getRuntime();
		LOGGER.trace(loggerId + "runSystemZipFolderCommand()...command=" + commandToExecute);
    	Process process = null;
    	BufferedReader brStdIn = null;
    	BufferedReader brStdErr = null;
    	String stdInLine = null;
    	String stdErrLine = null;
    	String errorDetails = "";

    	int lineCount = 0;
    	
		File workingDirectory = new File(workingDirectoryPath);
    	ProcessBuilder pBuilder = new ProcessBuilder("bash", "-c", commandToExecute);
    	pBuilder.directory(workingDirectory);

    	try {
    		LOGGER.trace(loggerId + "command=" + commandToExecute);
    		process = pBuilder.start();    		
    	
    		brStdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		
			while ((stdInLine = brStdIn.readLine()) != null) {
				lineCount++;
				if(lineCount < 4) {
					LOGGER.trace(loggerId + stdInLine);
					//completeOutput += stdInLine;
				}

    		}
			brStdIn.close();
			
    		returnCode = process.waitFor();
    		LOGGER.trace(loggerId + "returnCode=" + returnCode);

			if(returnCode != 0) {
	    		brStdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				
				while ((stdErrLine = brStdErr.readLine()) != null) {
					lineCount++;
					if(lineCount < 4) {
						LOGGER.error(stdErrLine);
						//completeOutput += stdInLine;
					}
					errorDetails += stdErrLine + "\n";
	    		}
				brStdIn.close();
				
				LOGGER.fatal(errorDetails);
				String message = this.studyFolder + " :StudyHandler.runSystemZipFolderCommand()->> unable to create zip file for folder=" + folderName;
				this.errorEncountered = true;
				this.errorMessage = message;
				LOGGER.fatal(message);
				success = false;
				EmailNotifier.sendEmailNotification(message + "\n" + errorDetails);
			}
    	}
    	catch(IOException ioE) {
    		LOGGER.error(loggerId + "Unable to create zip files for " + workingDirectoryPath + folderName);
    		LOGGER.error(ioE.getMessage(), ioE);
    		DiagnosticsReporter.createDiagnosticsEntry(ioE);
    		success = false;
    	}
    	catch(InterruptedException iE) {
    		LOGGER.error(loggerId + "Unable to create zip files for " + workingDirectoryPath + folderName);
    		LOGGER.error(iE.getMessage(), iE);
    		DiagnosticsReporter.createDiagnosticsEntry(iE);
    		success = false;
    	}
    	return success;
    }
    
    
    
	/**
	 * 
	 * Validates that there is not a mismatch between the uploaded folders.txt file
	 * and the folders contained in the uploaded zip file.
	 * 
	 * @param dataType - either 'volume' or 'surface'
	 * @param isUpdate - boolean indicating this is occurring as part of a study update
	 * @return isValid - boolean
	 */
	protected boolean validateNetworkFoldersConfig(String dataType, boolean isUpdate) {
    	
    	boolean isValid = true;
    	// targetDirectory will be root + surface or volume
    	String targetDirectory = this.absoluteStudyFolder + dataType;
    	
    	File[] directories = new File(targetDirectory).listFiles(new FileFilter() {
    	    @Override
    	    public boolean accept(File file) {
    	        return file.isDirectory();
    	    }
    	});
    	
    	String currentFolderName = null;
    	for(int i=0; i<directories.length; i++) {
    		//we're creating a list of the single network names so we
    		//exclude combined_clusters and overlapping_networks
    		currentFolderName = directories[i].getName();
    		if(currentFolderName.contentEquals("combined_clusters")) {
    			this.combinedClustersFolderExists = true;
    			continue;
    		}
    		if(currentFolderName.contentEquals("overlapping_networks")) {
    			this.overlappingNetworksFolderExists = true;
    			continue;
    		}
    		this.zippedFoldersList.add(directories[i].getName());
    	}
    	
    	if(this.menuEntry.contains("combined_clusters")) {
    		if(!this.combinedClustersFolderExists) {
    			isValid = false;
    			if(!isUpdate) {
	    			this.errorMessage = "Combined Networks was selected as an available network type, " +
	    		           "but the 'combined_clusters' folder does not exist in the uploaded zip file." +
	    				   "<br>Study not created";
    			}
	    		else {
	    			this.errorMessage = "Combined Networks is an available network type in the study menu, " +
		    		           "but the 'combined_clusters' folder does not exist in the uploaded zip file." +
		    				   "<br>Study not created";
	    		}
    			this.errorEncountered = true;    			
    			return isValid;
    		}
    	}
    	
    	if(this.menuEntry.contains("overlapping")) {
    		if(!this.overlappingNetworksFolderExists) {
    			isValid = false;
    			if(!isUpdate) {
	    			this.errorMessage = "Integration Zone was selected as an available network type, " +
	    		           "but the 'overlapping_networks' folder does not exist in the uploaded zip file." +
	    					"<br>Study not created";
    			}
    			else {
	    			this.errorMessage = "Integration Zone is an available network type in the study menu, " +
		    		           "but the 'overlapping_networks' folder does not exist in the uploaded zip file." +
		    					"<br>Study not created";
    			}
    			this.errorEncountered = true;    			
    			return isValid;
    		}
    	}
    	
    	String errorMessage = null;
    	
    	if(this.zippedFoldersList.size() != this.configuredFoldersList.size()) {
    		isValid = false;
			this.errorEncountered = true;    			
    		
    		if(this.zippedFoldersList.size()>this.configuredFoldersList.size()) {
    			
    			Iterator<String> allFoldersListIt = zippedFoldersList.iterator();
    			String curFolderName = null;
    			String missingFolderName = "undetermined";
    			
    			while(allFoldersListIt.hasNext()) {
    				curFolderName = allFoldersListIt.next();
    				if(!this.configuredFoldersList.contains(curFolderName)) {
    					missingFolderName = curFolderName;
    					break;
    				}
    			}
    			
    			if(!isUpdate) {
	    			this.errorMessage = "Menu config error: " +
	    		         "the zip file contains more Single Network folders than are listed" +
	    				 " in the folders.txt file. The missing entry is: " + missingFolderName;
    			}
    			else if(dataType.equals("volume")) {
	    			this.errorMessage = "Menu config error: " +
		    		         "the zip file contains more Single Network folders than exist in surface data." +
		    				 "The extra folder is: " + missingFolderName;
    			}
       			else if(dataType.equals("surface")) {
	    			this.errorMessage = "Menu config error: " +
		    		         "the zip file contains more Single Network folders than exist in volume data." +
		    				 "The extra folder is: " + missingFolderName;
    			}
    		}
    		
    		if(this.configuredFoldersList.size()>this.zippedFoldersList.size()) {
    			
    			Iterator<String> foldersListIt = this.configuredFoldersList.iterator();
    			String curFolderName = null;
    			String missingFolderName = "undetermined";
     			
    			while(foldersListIt.hasNext()) {
    				curFolderName = foldersListIt.next();
    				if(!this.zippedFoldersList.contains(curFolderName)) {
    					missingFolderName = curFolderName;
    					break;
    				}
    			}
    			
    			if(!isUpdate) {
	      			this.errorMessage = "Folder config error: " +
	       		         "the folders.txt file contains more Single Network folders than are contained" +
	       				 " in the zip file. The missing folder in the zip file is: " + missingFolderName;
    			}
    			else if(dataType.equals("volume")) {
    		         this.errorMessage = "Menu config error: the zip file contains less Single Network folders than are contained" +
    	       				 " in the surface data. The missing folder in the zip file is: " + missingFolderName;
    			}
    			else if(dataType.equals("surface")) {
   		         	this.errorMessage = "Menu config error: the zip file contains less Single Network folders than are contained" +
   	       				 " in the volume data. The missing folder in the zip file is: " + missingFolderName;

    			}
    		}

			return isValid;
    	}
    	
    	Iterator<String> configFolderNamesIt = this.configuredFoldersList.iterator();
    	String aFolderName = null;
    	
    	while(configFolderNamesIt.hasNext()) {
    		aFolderName = configFolderNamesIt.next();
    		if(aFolderName.contains(" ")) {
    			isValid = false;
    			this.errorEncountered = true;
    			this.errorMessage = TEMPLATE_FOLDERS_SPACE_ERROR_MESSAGE;
    			this.errorMessage = this.errorMessage.replace(REPLACE_FOLDER_NAME, aFolderName);
    			break;
    		}
    	}
    	return isValid;
    }

	

}
