package edu.umn.midb.population.atlas.study.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.CommandRunner;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import edu.umn.midb.population.atlas.utils.ServerStorageStats;

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
	protected static final String REPLACE_ZIP_FILE_NAME = "${zipFileName}";
	private static final String TEMPLATE_ZIP_COMMAND = "/usr/bin/zip -r zips/${zipFileName}.zip ${folderName} -x \"*.DS_Store\" -x \"__MACOSX\" -x \"*.png\"";
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
	protected String availableDataTypes = null;



	protected String studyFolder = null;
	protected String surfaceZipFilePath = null;
	protected String volumeZipFilePath = null;
	protected boolean errorEncountered = false;
	protected String errorMessage = null;
	protected AsyncRunner asyncRunner = null;

	
	/**
	 * Inner class used for asynchronously completing the creation of a new study. Since it
	 * is a protected inner member, it is not possible for external classes to start this
	 * thread.
	 * 
	 * @author jjfair
	 *
	 */
	protected class AsyncRunner extends Thread {
		
		/**
		 * Invokes the {@link StudyHandler#createSubFolderZips()} method to asynchronously
		 * complete the addition of the study so the client does not have to wait for this
		 * process.
		 */
		public void run() {
			createSubFolderZips();
		}
		
	}
	/**
	 * Constructor
	 */
	protected StudyHandler() {
		asyncRunner = new AsyncRunner();
	}
	
	/**
	 * Checks available storage to determine if it is sufficient to add a study.
	 * 
	 * @return isSufficient - boolean
	 */
	protected boolean checkSufficientServerStorage() {
		
		boolean isSufficient = false;
		float requiredStorage = 0;
		
		ServerStorageStats freeStorageStats = CommandRunner.getFreeStorageStats();
		
		if(this.availableDataTypes.equals("surface_volume")) {
			requiredStorage = 2*5;
		}
		else {
			requiredStorage = 5;
		}
		
		if(freeStorageStats.getUnitOfMeasure().equals("TB")) {
			isSufficient = true;
		}
		else if(freeStorageStats.getAmount()>requiredStorage) {
			isSufficient = true;
		}
		return isSufficient;
	}

	
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
		
		String nonLinkTextPrefix = "";
		String linkText = "";
		String nonLinkTextSuffix = "";
		String linkEntry = null;
		int linkTextBeginIndex = summaryLine.indexOf("[");
		
		
		int linkTextEndIndex = summaryLine.indexOf("]");
		
		if(linkTextEndIndex < summaryLine.length()-1) {
			nonLinkTextSuffix = summaryLine.substring(linkTextEndIndex+1);
		}

		if(linkTextBeginIndex==0) {
			linkText = summaryLine.substring(0, linkTextEndIndex);
		}
		else {
			nonLinkTextPrefix = summaryLine.substring(0, linkTextBeginIndex);
			linkText = summaryLine.substring(linkTextBeginIndex+1, linkTextEndIndex);
		}
		
		linkText = linkText.replace("[", "");
		linkText = linkText.replace("[", "");
		String[] entryArray = linkText.split("@");
		linkEntry = TEMPLATE_LINK;
		linkEntry = linkEntry.replace("${linkText}", entryArray[0]);
		linkEntry = linkEntry.replace("${url}", entryArray[1]);
		
		String newSummaryLine = nonLinkTextPrefix + linkEntry + nonLinkTextSuffix;
		
		LOGGER.trace(loggerId + "createUrlLinkEntry()...exit.");

		return newSummaryLine;
		
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
	 * so that the user can download the files for all 'thresholds' if desired. This method
	 * runs asynchronously so the administrator does not have to wait for it to complete when
	 * adding/creating a new study.
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
				success = runSystemZipFolderCommand(currentFolder, surfaceRootFolder, "surface");
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
				success = runSystemZipFolderCommand(currentFolder, volumeRootFolder, "volume");
				if(!success) {
					return success;
				}
			}
			
		}
		
		if(this.combinedClustersFolderExists) {
			if(this.surfaceZipFilePath != null) {
				currentFolder = "combined_clusters";
				success = runSystemZipFolderCommand(currentFolder, surfaceRootFolder, "surface");
				if(!success) {
					return success;
				}
			}
			if(this.volumeZipFilePath != null) {
				currentFolder = "combined_clusters";
				success = runSystemZipFolderCommand(currentFolder, volumeRootFolder, "volume");
				if(!success) {
					return success;
				}
			}
		}
		
		if(this.overlappingNetworksFolderExists) {
			if(this.surfaceZipFilePath != null) {
				currentFolder = "overlapping_networks";
				success = runSystemZipFolderCommand(currentFolder, surfaceRootFolder, "surface");
				if(!success) {
					return success;
				}
			}
			if(this.volumeZipFilePath != null) {
				currentFolder = "overlapping_networks";
				success = runSystemZipFolderCommand(currentFolder, volumeRootFolder, "volume");
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
	 * Renames a file that is using precision-2 decimal point in the name to use
	 * precision-1 name. This is done for compatibility with earlier studies that
	 * used only precision-1 names. An example of a name being changed is as follows:
	 * abcd_template_matching_v2_Aud_thresh0.10.png
	 * abcd_template_matching_v2_Aud_thresh0.1.png
	 * 
	 * @param oldFileName
	 * @param newFileName
	 * @return boolean denoting successful rename
	 */
	protected boolean renamePrecision2File(String oldFileName, String newFileName) {
		String loggerId = this.appContext.getLoggerId();
		//LOGGER.trace(loggerId + "renamePrecision2File()...invoked, newFileName=" + newFileName);
		boolean success = true;
		
		try {
			File originalFile = new File(oldFileName);
			File newFile = new File(newFileName);
			originalFile.renameTo(newFile);
		}
		catch(Exception e) {
			LOGGER.trace(loggerId + e);
			success = false;
		}
		return success;
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
				if(stdInLine.trim().contains("Written using ZipKit")) {
					LOGGER.trace(stdInLine);
					continue;
				}
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
	 * Executes the bash command 'zip'. This method is utilized by {@link #createSubFolderZips()}.
	 * The zip files are created so that a remote client can download all of the threshold files
	 * for a given network.
	 * 
	 * @param folderName - String that is the folder to zip
	 * @param workingDirectoryPath - what the working directory should be
	 * @param dataType - String specifying 'surface' or 'volume'
	 * 
	 * @return success - boolean indicating if the zip command completed successfully
	 */
    protected boolean runSystemZipFolderCommand(String folderName, String workingDirectoryPath, String dataType) {
    	
    	String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "runSystemZipFolderCommand()...invoked.");

		boolean success = true;
    	int returnCode = -1;
    	String zipFileName = this.studyFolder + "_" + dataType + "_" + folderName;
    	String commandToExecute = TEMPLATE_ZIP_COMMAND.replace(REPLACE_ZIP_FILE_NAME, zipFileName);
    	commandToExecute = commandToExecute.replace(REPLACE_FOLDER_NAME, folderName);

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
     * Checks if each file begins with the studyName prefix. The study prefix is the same
     * as the name of the study folder with a trailing underscore added at the end. The
     * folder name of the study is also used as the unique study id.
     * 
     * @param dataType - String indicating surface or volume data
     * @param isUpdate - boolean indicating if the operation is Add Study or Update Study
     * @return success - boolean indicating if every file begins with the study prefix
     */
    protected boolean validateFileNames(String dataType, boolean isUpdate) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateFileNames()...invoked.");
		
		boolean success = true;
		String requiredPrefix = this.studyFolder + "_";
		if(dataType.equals("volume")) {
			requiredPrefix += "vol_";
		}
		
	    ArrayList<String> subDirs = new ArrayList<String>();
	    String targetRootPath = this.absoluteStudyFolder + dataType + "/";	
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
	   String shortSubDirName = null;
	   int slashIndex = 0;
	   
	   while(dirIt.hasNext()) {
		    subDirName = dirIt.next();
		    slashIndex = subDirName.lastIndexOf("/");
		    shortSubDirName = subDirName.substring(slashIndex);
		    shortSubDirName = dataType + shortSubDirName;
		    
	        if(subDirName.endsWith("/zips")) {
	    	   continue;
	        }
    		    
		    File subDir = new File(subDirName);
		    fileNamesArray = subDir.list();
		    fileNames = new ArrayList<String>(Arrays.asList(fileNamesArray));
		    fileNamesIt = fileNames.iterator();
		    String fileName = null;

		    
		    while(fileNamesIt.hasNext()) {
		    	fileName = fileNamesIt.next();
		    	if(!fileName.startsWith(requiredPrefix)) {
		    		this.errorEncountered = true;
		    		this.errorMessage = "study prefix missing for file: " + shortSubDirName + "/" + fileName;
		    		this.errorMessage += "<br>All file names for data type of " + dataType
		    				+ " must begin with: " + requiredPrefix;
		    		if(!isUpdate) {
		    			this.errorMessage += "<br><br>Study not created";
		    		}
		    		else {
		    			this.errorMessage += "<br><br>Study not updated";
		    		}
		    		return false;
		    	}
		    }
	   }
	    
		LOGGER.trace(loggerId + "validateFileNames()...exit.");
		return success;    	
    }
    
    public void setErrorEncountered(ApplicationContext appContext, boolean errorIndicator) {
    	
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "setErrorEncountered()...invoked.");
		
		this.errorEncountered = errorIndicator;
		LOGGER.trace(loggerId + "setErrorEncountered()...exit.");

    }
    
    public void setErrorMessage(ApplicationContext appContext, String errorMessage) {
    	
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "setErrorMessage()...invoked.");
		
		this.errorMessage = errorMessage;
		LOGGER.trace(loggerId + "setErrorMessage()...exit.");

    }
    
    /**
     * Validates that there is a folder named 'combined_clusters' if combined_clusters
     * was denoted as available data by the admin user in the interface to create a study.
     * If no combined_clusters data was specified then the folder should not exist.
     * 
     * @param isUpdate - boolean
     * @return isValid - boolean
     */
    protected boolean validateCombinedClustersConfig(boolean isUpdate) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateCombinedClustersConfig()...invoked.");
		
		boolean isValid = true;
		
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
    	
    	
    	if(!this.menuEntry.contains("combined_clusters")) {
    		if(this.combinedClustersFolderExists) {
    			isValid = false;
    			if(!isUpdate) {
	    			this.errorMessage = "Combined Networks was not selected as an available network type, " +
	    		           "but the 'combined_clusters' folder does exist in the uploaded zip file." +
	    				   "<br>Study not created";
    			}
	    		else {
	    			this.errorMessage = "Combined Networks was not selected as an available network type, " +
		    		           "but the 'combined_clusters' folder does exist in the uploaded zip file." +
		    				   "<br>Study not updated";
	    		}
    			this.errorEncountered = true;    			
    			return isValid;
    		}
    	}			
		LOGGER.trace(loggerId + "validateCombinedClustersConfig()...exit.");
		return isValid;
    }
    
    /**
     * Checks that the dscalar files exist in each network folder. There are two dscalar files: 
     * the .nii file an the .png file. The name of the dscalar .nii file must end with '.dscalar.nii'.
     * The dscalar .png file name must end with '_network_probability.png'.
     * 
     * @param dataType - String indicating surface or volume data
     * @return success - boolean indicating if all dscalar files for all networks were detected
     */
    protected boolean validateDscalarFiles(String dataType, boolean isUpdate) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateDscalarFiles()...invoked.");
		
		boolean success = true;
		
		String directoryPath = null;
		File targetDirectory = null;
		
		if(dataType.equalsIgnoreCase("surface")) {
			directoryPath = this.absoluteStudyFolder + "surface/";
			targetDirectory = new File(directoryPath);
		}
		else if(dataType.equalsIgnoreCase("volume")) {
			directoryPath = this.absoluteStudyFolder + "volume/";
			targetDirectory = new File(directoryPath);
		}
		
		String[] folderArray = targetDirectory.list();
		List<String> folderList = Arrays.asList(folderArray);
		Iterator<String> folderListIt = folderList.iterator();
		String currentFolderName = null;
		String shortFolderName = null;
		String[] fileList = null;
		
		
		while(folderListIt.hasNext()) {
			shortFolderName = folderListIt.next();
			currentFolderName = directoryPath + shortFolderName;
			
			//combined_clusters does not have a dscalar file
			if(currentFolderName.contains("combined_clusters")) {
				continue;
			}
			
			File currentDirectory = new File(currentFolderName);
			
			if(!currentDirectory.isDirectory()) {
				this.errorMessage = "Unexpected file found: " + currentFolderName;
				this.errorEncountered = true;
				return false;
			}
			
			fileList = currentDirectory.list();
			String currentFileName = null;
			File currentFile = null;
			boolean dscalar_nii_found = false;
			boolean dscalar_png_found = false;
			boolean duplicate_dscalar_found = false;
			
			for(int i=0; i<fileList.length; i++) {
				currentFileName = fileList[i];
				currentFile = new File(currentFileName);
				
				if(currentFile.isDirectory()) {
					this.errorMessage = "Unexpected directory found: " + shortFolderName;
					this.errorEncountered = true;
					return false;
				}
				
				if(currentFileName.endsWith(".dscalar.nii")) {
					if(dscalar_nii_found) { // we already found a dscalar file
						this.errorMessage = "More than 1 dscalar file was found in folder " + shortFolderName;
						this.errorMessage += "<br><br>Study not created";
						this.errorEncountered = true;
						duplicate_dscalar_found = true;
					}
					dscalar_nii_found = true;
				}
				else if(currentFileName.endsWith("network_probability.png") ||
					currentFileName.endsWith("number_of_nets.png")) {
					dscalar_png_found = true;
				}
				//if(dscalar_nii_found && dscalar_png_found) {
				//	break;
				//}
			}
			
			if(duplicate_dscalar_found) { 
				return false;
			}
			
			if(!dscalar_nii_found || !dscalar_png_found) {
				this.errorMessage = "...dscalar.nii and/or ...network_probability.png<br> missing in directory: " + shortFolderName;
	    		if(!isUpdate) {
	    			this.errorMessage += "<br><br>Study not created";
	    		}
	    		else {
	    			this.errorMessage += "<br><br>Study not updated";
	    		}
				this.errorEncountered = true;
				return false;
			}
		}
		
		LOGGER.trace(loggerId + "validateDscalarFiles()...exit.");
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
    	
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateNetworkFoldersConfig()...invoked.");
		
    	boolean isValid = true;
    	// targetDirectory will be root + surface or volume
    	String targetDirectory = this.absoluteStudyFolder + dataType;

    	File[] directories = new File(targetDirectory).listFiles();
    	File currentFile = null;
    	
    	String currentFolderName = null;
    	for(int i=0; i<directories.length; i++) {
    		currentFile = directories[i];
    		currentFolderName = currentFile.getName();
    		LOGGER.trace(loggerId + "current file/dir name=" + currentFolderName);

    		if(!currentFile.isDirectory()) {
        		LOGGER.trace(loggerId + currentFolderName + " is not a directory, aborting study creation");
    			this.errorEncountered = true;
    			this.errorMessage = "Unexpected file found in " + dataType + " folder: " + currentFolderName;
    			return false;
    		}
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
	    			this.errorMessage += "<br>Study not created";
    			}
    			else if(dataType.equals("volume")) {
	    			this.errorMessage = "Menu config error: " +
		    		         "the zip file contains more Single Network folders than exist in surface data." +
		    				 "The extra folder is: " + missingFolderName;
	    			this.errorMessage += "<br>Study not updated";
    			}
       			else if(dataType.equals("surface")) {
	    			this.errorMessage = "Menu config error: " +
		    		         "the zip file contains more Single Network folders than exist in volume data." +
		    				 "The extra folder is: " + missingFolderName;
	    			this.errorMessage += "<br>Study not updated";
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
	    			this.errorMessage += "<br>Study not created";

    			}
    			else if(dataType.equals("volume")) {
    		         this.errorMessage = "Menu config error: the zip file contains less Single Network folders than are contained" +
    	       				 " in the surface data. The missing folder in the zip file is: " + missingFolderName;
 	    			 this.errorMessage += "<br>Study not updated";

    			}
    			else if(dataType.equals("surface")) {
   		         	this.errorMessage = "Menu config error: the zip file contains less Single Network folders than are contained" +
   	       				 " in the volume data. The missing folder in the zip file is: " + missingFolderName;
	    			this.errorMessage += "<br>Study not updated";


    			}
    		}
    		LOGGER.trace(loggerId + "validateNetworkFoldersConfig()...exit.");
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
    			if(!isUpdate) {
    				this.errorMessage += "Study not created";
    			}
    			else {
    				this.errorMessage += "Study not updated";

    			}
    			break;
    		}
    	}
    	
		isValid = validateCombinedClustersConfig(isUpdate);
		isValid = validateOverlappingNetworksConfig(isUpdate);
 	
    	return isValid;
    }
	
	/**
	 * Validates the files contained in the overlapping_networks folder
	 * @param isUpdate - boolean indicating if this is part of a study update
	 * @return isValid - boolean
	 */
	protected boolean validateOverlappingNetworksConfig(boolean isUpdate) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateOverlappingNetworksConfig()...invoked.");
		
		boolean isValid = true;
		
    	if(this.menuEntry.contains("overlapping")) {
    		if(!this.overlappingNetworksFolderExists) {
    			isValid = false;
    			if(!isUpdate) {
	    			this.errorMessage = "Integration Zone was selected as an available network type, " +
	    		           "but the 'overlapping_networks' folder does not exist in the uploaded zip file." +
	    				   "<br>Study not created";
    			}
	    		else {
	    			this.errorMessage = "Integration Zone was selected as an available network type, " +
		    		           "but the 'overlapping_networks' folder does not exist in the uploaded zip file." +
		    				   "<br>Study not updated";
	    		}
    			this.errorEncountered = true;    			
    			return isValid;
    		}
    	}
    	
    	
    	if(!this.menuEntry.contains("overlapping")) {
    		if(this.overlappingNetworksFolderExists) {
    			isValid = false;
    			if(!isUpdate) {
	    			this.errorMessage = "Integration Zone was not selected as an available network type, " +
	    		           "but the 'overlapping_networks' folder does exist in the uploaded zip file." +
	    				   "<br>Study not created";
    			}
	    		else {
	    			this.errorMessage = "Integration Zone was not selected as an available network type, " +
		    		           "but the 'overlapping_networks' folder does exist in the uploaded zip file." +
		    				   "<br>Study not updated";
	    		}
    			this.errorEncountered = true;    			
    			return isValid;
    		}
    	}			

		LOGGER.trace(loggerId + "validateOverlappingNetworksConfig()...exit.");
		return isValid;
	}

	/**
	 * Checks that each folder holding the data for a single network (Aud, CO, etc.) has a .nii
	 * and .png file representing every threshold from 1% to 100% in increments of 1.
	 * 
	 * @param dataType - String representing surface or volume data
	 * @return success - boolean indicating if all files were found
	 */
	protected boolean validateThresholdFiles(String dataType, boolean isUpdate) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateThresholdFiles()...invoked.");
		
		boolean success = true;
		
	    ArrayList<String> subDirs = new ArrayList<String>();
	    String targetRootPath = this.absoluteStudyFolder + dataType + "/";	
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(targetRootPath))) {
	        for (Path path : stream) {
	            if (Files.isDirectory(path)) {
	            	if(!path.endsWith("combined_clusters") && !path.endsWith("overlapping_networks")) {
	            		subDirs.add(path.toString());
	            	}
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
	   String shortSubDirName = null;
	   int slashIndex = 0;
	   String baseThreshold = "0.0";
	   String baseThresholdSuffix = null;
	   String baseThresholdSuffixPrecision2 = null;
	   String targetThreshold = null;
	   String targetThresholdPrecision2 = null;
	   boolean precision2NameDetected = false;
	   int currentThreshold = -1;
	   boolean tryPrecision2 = false;
	   boolean niiThresholdFound = false;
	   boolean pngThresholdFound = false;
	   
	   while(dirIt.hasNext()) {
		    subDirName = dirIt.next();
		    slashIndex = subDirName.lastIndexOf("/");
		    shortSubDirName = subDirName.substring(slashIndex);
		    shortSubDirName = dataType + shortSubDirName;
		    
	        if(subDirName.endsWith("/zips")) {
	    	   continue;
	        }
   		    
		    File subDir = new File(subDirName);
		    fileNamesArray = subDir.list();
		    //fileNames = new ArrayList<String>(Arrays.asList(fileNamesArray));
		    //fileNamesIt = fileNames.iterator();
		    //String fileName = null;
		    
			File targetDirectory = new File(subDirName);
			String currentFileName = null;
			File currentFile = null;
			baseThreshold = "0.0";
			targetThreshold = null;
			currentThreshold = 0;
			tryPrecision2 = false;
			
			LOGGER.trace(loggerId + "validateThresholdFiles()...directory=" + subDir);
			LOGGER.trace(loggerId + "validateThresholdFiles()...checking files, count=" + fileNamesArray.length);

			
			while(currentThreshold < 100) {
				
				niiThresholdFound = false;
				pngThresholdFound = false;
				tryPrecision2 = false;
				targetThresholdPrecision2 = "-1";
				
				currentThreshold++;
				
				if(currentThreshold == 100) {
					targetThreshold = "1.0.";
					targetThresholdPrecision2 = "1.00.";
					tryPrecision2 = true;
				}
				else {
					if(currentThreshold == 10) {
						baseThreshold = "0.";
					}
					if((currentThreshold % 10)==0) {
						tryPrecision2 = true;
						baseThresholdSuffix = currentThreshold/10 + ".";
						baseThresholdSuffixPrecision2 = currentThreshold/10 + "0.";
					}
					else {
						baseThresholdSuffix = currentThreshold + ".";
					}
					targetThreshold = baseThreshold + baseThresholdSuffix;
					
					if(tryPrecision2) {
						targetThresholdPrecision2 = baseThreshold + baseThresholdSuffixPrecision2;
					}
				}
			
				for(int i=0;i<fileNamesArray.length;i++) {					
					currentFileName = fileNamesArray[i];
					currentFile = new File(currentFileName);
					
					if(currentThreshold==1) {
						if(!currentFileName.endsWith(".nii") && !currentFileName.endsWith(".png")) {
							this.errorEncountered = true;
							String errorFile = currentFileName;
							this.errorMessage = "Unexpected file encountered<br>directory:<br>" + shortSubDirName;
							this.errorMessage += "<br>file name:<br>" + errorFile;
							LOGGER.trace(loggerId + errorMessage);
							return false;	
						}
					}

					if(currentFile.isDirectory()) {
						this.errorEncountered = true;
						this.errorMessage = "Unexpected directory encountered:" + shortSubDirName;
						LOGGER.trace(loggerId + errorMessage);
						return false;		
					}
					
					if(currentFileName.contains(targetThreshold)) {
						if(currentFileName.endsWith(".nii")) {
							niiThresholdFound = true;
						}
						if(currentFileName.endsWith(".png")) {
							pngThresholdFound = true;
						}
					}
					if(tryPrecision2) {
						if(currentFileName.contains(targetThresholdPrecision2)) {
							int index = currentFileName.lastIndexOf("0");
							StringBuilder sb = new StringBuilder(currentFileName);
							sb.deleteCharAt(index);
							String oldFileName = subDirName + "/" + currentFileName;
							String newFileName = subDirName + "/" + sb.toString();
							boolean renameSuccess = this.renamePrecision2File(oldFileName, newFileName);
							
							if(!renameSuccess) {
								this.errorEncountered = true;
								this.errorMessage = "Unable to rename file with precision2 notation:<br>" + currentFileName;
								this.errorMessage += "<br>Unable to create study";
								LOGGER.trace(loggerId + errorMessage);
								return false;
							}
							
							if(currentFileName.endsWith(".nii")) {
								niiThresholdFound = true;
							}
							if(currentFileName.endsWith(".png")) {
								pngThresholdFound = true;
							}
						}
					}
					if(niiThresholdFound && pngThresholdFound) {
						break;
					}
				}
			    if(!niiThresholdFound || !pngThresholdFound) {
			    	String message = "Threshold file missing.<br>Directory: " + shortSubDirName;
			    	message += "<br>Threshold: " + targetThreshold;
			    	message += "<br>Check both .nii and .png files for threshold.";
			    	message += "<br>Names must follow standard naming conventions";
			    	message += "<br>Download surface.zip for example data.";
		    		if(!isUpdate) {
		    			this.errorMessage += "<br><br>Study not created";
		    		}
		    		else {
		    			this.errorMessage += "<br><br>Study not updated";
		    		}
			    	this.errorMessage = message;
			    	this.errorEncountered = true;
					LOGGER.trace(loggerId + errorMessage);
			    	return false;
			    }
			}
			fileNamesArray = subDir.list();
			LOGGER.trace(loggerId + "validateThresholdFiles()...finished checking directory=" + subDir);
			LOGGER.trace(loggerId + "validateThresholdFiles()...file count=" + fileNamesArray.length);
	   }
	   
		LOGGER.trace(loggerId + "validateThresholdFiles()...exit.");
		return success;
	}
	
	/**
	 * Validates that there is only 1 top level folder in the zip file. This top level
	 * folder must be named either 'surface' or 'volume'.
	 * 
	 * @param dataType - String denoting surface or volume data
	 * @return isValid - boolean
	 */
	protected boolean validateZipFileTopLevelFolder(String dataType) {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "validateTopLevelFolder()...invoked.");
		
		boolean isValid = true;
		
		ZipFile zipFile = null;
		
		try {
			if(dataType.equals("surface")) {
				zipFile = new ZipFile(this.surfaceZipFilePath);
			}
			else if(dataType.equals("volume")) {
				zipFile = new ZipFile(this.volumeZipFilePath);
			}	
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
			DiagnosticsReporter.createDiagnosticsEntry(e);
			this.errorEncountered = true;
			this.errorMessage = "Unable to examine entries in zip file";
			return false;
		}
					
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if(!name.matches("\\S+/\\S+")){ //it's a top level folder
            	if(name.equals("./")) { //some zip utils add a ./ folder that only exists inside the zip file
            		continue;
            	}
                if(!name.equals(dataType + "/")) {
                	this.errorEncountered = true;
                	this.errorMessage = "Invalid top level folder or file<br>encountered in zip file:<br>" + name;
                	isValid = false;     
                	break;
                }
            }
        }

		return isValid;
	}

}
