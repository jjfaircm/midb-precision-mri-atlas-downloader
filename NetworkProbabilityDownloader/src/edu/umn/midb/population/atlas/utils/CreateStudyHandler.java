package edu.umn.midb.population.atlas.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;

/**
 * 
 * Creates a new study. This task requires creating a folder under the /midb/studies
 * root folder. The new folder will map to the name of the study. For example, the
 * 'ABCD - Template Matching' study has a root folder of: /midb/studies/abcd_template_matching 
 * The image files and .nii files associated with the study are stored in either the
 * 'surface' or 'volume' folder under the study root folder.
 * 
 * After the zip file containing the image (.png) files and .nii files are extracted,
 * then various configuration files are updated so that entries for the new study can
 * be inserted. The configuration files are all stored in the /midb root folder. There
 * are 3 of them:
 * <ul>
 * <li>menu.conf</li>
 * <li>summary.conf</li>
 * <li>network_folder_names.conf</li>
 * </ul>
 * 
 *<p>
 *
 * These configuration files are sent to the client browser so that the client can
 * dynamically build the study menu. When a new study is added no changes have to
 * be made to the html interface or javascript code since everything is handled dynamically
 * when the client calls the javascript function named getMenuData. 
 * <p>
 * With a good internet connection, it typically takes about ten minutes to add a study
 * if the study contains only surface data. If the study also contains volume data then
 * the process would take about twenty minutes. The study is added by logging in to the
 * Admin Console on the browser. There is a link in the Admin Console to download the 
 * instructions for adding a study.
 * 
 * @author jjfair
 *
 */
public class CreateStudyHandler {
	
	private static final String ROOT_DESTINATION_PATH = "/midb/studies/";
	private static final String MENU_CONFIG_FILE = "/midb/menu.conf";
	private static final String SUMMARY_CONFIG_FILE = "/midb/summary.conf";
	private static final String SUMMARY_ENTRY_FILE = "summary.txt";
	private static final String NETWORK_FOLDER_NAMES_CONFIG_FILE = "/midb/network_folder_names.conf";
	private static final String NETWORK_FOLDER_NAMES_ENTRY_FILE = "folders.txt";

	private static final String TEMPLATE_BEGIN_SUMMARY = "BEGIN SUMMARY (ID=${studyFolderName})";
	private static final String REPLACE_STUDY_FOLDER_NAME = "${studyFolderName}";
	private static final String TEMPLATE_END_SUMMARY = "END SUMMARY";
	private static final String TEMPLATE_BEGIN_NETWORK_FOLDER_ENTRY = "NETWORK FOLDERS ENTRY (id=${studyFolderName})";
	private static final String TEMPLATE_END_NETWORK_FOLDER_ENTRY = "END NETWORK FOLDERS ENTRY";
	private static final String TEMPLATE_FOLDERS_ERROR_MESSAGE = "folder.txt error: folder ${folderName} not found in zip file. Study not created.";
	private static final String TEMPLATE_FOLDERS_SPACE_ERROR_MESSAGE = "folder.txt error: folder ${folderName} contains spaces in the name." 
			                            + " Folders with spaces in name not allowed. Study not created.";
	private static final String TEMPLATE_LINK = "<a href=\"${url}/\" target=\"_blank\" style=\"font-style: italic; color:DarkBlue;\">${linkText}</a>";
	private static final String REPLACE_FOLDER_NAME = "${folderName}";
	private static final String TEMPLATE_ZIP_COMMAND = "/usr/bin/zip -r zips/${folderName}.zip ${folderName} -x \"*.DS_Store\" -x \"__MACOSX\" -x \"*.png\"";
    private static final int BUFFER_SIZE = 4096;

	
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);
	/**
	 * Cretes a a new folder required by the new study.
	 * 
	 * @param absolutePath - String
	 * @throws IOException - unhandled exception
	 */
	private static void createPath(String absolutePath) throws IOException {
		
		File targetDirectory = new File(absolutePath);
		boolean successIndicator = false;
		
		if(!targetDirectory.exists()) {
			successIndicator = targetDirectory.mkdirs();
			if(!successIndicator) {
				throw new IOException("Unable to create folder:" + absolutePath);
			}
		}
	}
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}
	private ApplicationContext appContext = null;
	private String menuEntry;
	private String absoluteStudyFolder;
	private String studyFolder;
	private String availableDataTypes = null;
	private String zipFile_surface = null;
	private String zipFile_volume = null;
	private String summaryTextFile = null;
	private String foldersTextFile = null;
	private boolean combinedClustersFolderExists = false;


	private boolean overlappingNetworksFolderExists = false;
	//foldersList represents the folders listed in the uploaded folders.txt file
	private ArrayList<String> foldersList = new ArrayList<String>();
	
	//allFoldersList is the list of folders under the surface folder
	//NOTE: the list of folders under volume should match those under the surface folder
	private ArrayList<String> allFoldersList = new ArrayList<String>();
	private ArrayList<String> summaryEntryLines = new ArrayList<String>();
	
	private ArrayList<String> networkFoldersEntryLines = new ArrayList<String>();

	private String createStudyErrorMessage = null;
	
	private boolean createStudyHasError = false;
	
	/**
	 * 
	 * Hides the default constructor.
	 * 
	 */
	private CreateStudyHandler() {
		
	}
	
	/**
	 * Public constructor. The instance is stored in the {@link ApplicationContext} since it must
	 * persist across multiple file uploads for files that are are required to create a new study.
	 * 
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param request - HttpServletRequest
	 * @param response - HttpServletResponse
	 * @throws IOException - Unhandled exception
	 */
	public CreateStudyHandler(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor invoked.");

		//this.request = request;
		//this.response = response;
		this.appContext = appContext;
		this.studyFolder = request.getParameter("studyFolderName");
		this.absoluteStudyFolder = ROOT_DESTINATION_PATH + studyFolder + File.separator;
		this.menuEntry = request.getParameter("menuEntry");
		this.availableDataTypes = request.getParameter("availableDataTypes");
		this.summaryTextFile = this.absoluteStudyFolder + "summary.txt";
		this.foldersTextFile = this.absoluteStudyFolder + "folders.txt";
		
		createPath(this.absoluteStudyFolder);
		
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor exit.");

	}
	
	/**
     * Clears the error indicators.
     * 
     */
	public void clearErrors() {
		this.setCreateStudyErrorMessage(null);
		this.setCreateStudyHasError(false);
	}
	
	/**
	 * Informs the handler to complete the deploy process since all files have
	 * been uploaded.
	 * 
	 * @return succes - boolean
	 * @throws IOException - unhandled exception
	 */
	public boolean completeStudyDeploy() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "completeStudyDeploy()...invoked.");

		boolean success = true;
		
		if(this.zipFile_surface != null) {
			//unzipFolder(zipFile_surface, this.absoluteStudyFolder);
			success = runSystemUnzipCommand(this.zipFile_surface);
			if(!success) {
				removeStudyFolder();
				return false;
			}
            File zipFile = new File(this.zipFile_surface);
            zipFile.delete();
		}
		if(this.zipFile_volume != null && success) {
			//unzipFolder(zipFile_volume, this.absoluteStudyFolder);
			success = runSystemUnzipCommand(this.zipFile_volume);
			if(!success) {
				removeStudyFolder();
				return false;
			}
            File zipFile = new File(this.zipFile_volume);
            zipFile.delete();
		}
		this.createNetworkFolderConfigEntry();
		
		if(this.zipFile_surface != null) {
			success = this.validateNetworkFoldersConfig("surface");
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
		}

		if(this.zipFile_volume != null) {
			success = this.validateNetworkFoldersConfig("volume");
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
		}
	
		createSubFolderZips();
		updateMenuConfig();
		updateSummaryConfig();
		updateNetworkFolderNamesConfig();
		createConfigBackup();
		
		AtlasDataCacheManager.getInstance().reloadConfigs();
		LOGGER.trace(loggerId + "completeStudyDeploy()...exit.");
		return true;
	}
	

	/**
	 * 
	 * Backs up the config files before creating the entries for the new study.
	 * This is done in case a rollback is necessary.
	 * 
	 */
	private void createConfigBackup() {
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
	 * Creates an entry for the Single Network folders configuration for the new study.
	 * This creates an entry in the network_folder_names.conf file.
	 * 
	 */
	private void createNetworkFolderConfigEntry() {

		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createNetworkFolderConfigEntry()...invoked.");

		File file = new File(this.foldersTextFile);
		boolean shouldContinue = true;
		
		String folderName = null;
		String entryLine = TEMPLATE_BEGIN_NETWORK_FOLDER_ENTRY.replace(REPLACE_STUDY_FOLDER_NAME, this.studyFolder);
		this.networkFoldersEntryLines.add(entryLine);
		int beginIndex = 0;
				
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			while ((entryLine = br.readLine()) != null) {
				entryLine = entryLine.trim();
				if(entryLine.length()<3) {
					continue;
				}
				if(entryLine.startsWith("#")) {
					continue;
				}
				
				beginIndex = entryLine.indexOf("=");
				folderName = entryLine.substring(beginIndex+1).trim();
				this.foldersList.add(folderName);
				this.networkFoldersEntryLines.add(entryLine);
			}
			this.networkFoldersEntryLines.add(TEMPLATE_END_NETWORK_FOLDER_ENTRY);
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "createNetworkFolderConfigEntry()...exit.");
	}
	
	/**
	 * Returns a boolean indicating if the study creation encountered an error
	 * condition. Used by the {@link WebResponder#sendAddStudyResponse(ApplicationContext, HttpServletResponse)}
	 * method.
	 * 
	 * @return createStudyHasError - boolean
	 */
	public boolean createStudyHasError() {
		return createStudyHasError;
	}
	
	/**
	 * Creates zip files for the various types of available network data. This is done
	 * so that the user can download the files for all 'thresholds' if desired.
	 * 
	 */
	private void createSubFolderZips() {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createSubFolderZips()...invoked.");
		
		long startTime = System.currentTimeMillis();
		
		Iterator<String> foldersIt = this.allFoldersList.iterator();
		String currentFolder = null;
		String surfaceRootFolder = this.absoluteStudyFolder + "surface";
		String volumeRootFolder = this.absoluteStudyFolder + "volume";
		int count = 0;

		
		while(foldersIt.hasNext()) {
			count++;
			currentFolder = foldersIt.next();
			
			if(this.zipFile_surface != null) {
				if(count==1) {
					String zipDirPath = surfaceRootFolder + File.separator + "zips";
					File zipDirectory = new File(zipDirPath);
					zipDirectory.mkdir();
				}
				runSystemZipFolderCommand(currentFolder, surfaceRootFolder);
			}
			if(this.zipFile_volume != null) {
				if(count==1) {
					String zipDirPath = volumeRootFolder + File.separator + "zips";
					File zipDirectory = new File(zipDirPath);
					zipDirectory.mkdir();
				}
				runSystemZipFolderCommand(currentFolder, volumeRootFolder);
			}
			
		}
		long finishTime = System.currentTimeMillis();
		long elapsedTime = finishTime - startTime;
		long elapsedSeconds = elapsedTime/1000;
		LOGGER.trace(loggerId + "createSubFolderZips()...exit. elapsedSeconds=" + elapsedSeconds);
	}
	
	/**
	 * Inserts an entry fpr the new study in the /midb/summary.conf file. The entry
	 * represents lines that are stored as list entries that are displayed under the
	 * main image panel in the browser.
	 * 
	 */
	private void createSummaryEntry() {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createSummaryEntry()...invoked.");

		File file = new File(this.absoluteStudyFolder + SUMMARY_ENTRY_FILE);
		boolean shouldContinue = true;
		
		String entryLine = TEMPLATE_BEGIN_SUMMARY.replace(REPLACE_STUDY_FOLDER_NAME, this.studyFolder);
		this.summaryEntryLines.add(entryLine);
				
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			while ((entryLine = br.readLine()) != null) {
				if(entryLine.contains("[") && entryLine.contains("]")) {
					entryLine = createUrlLinkEntry(entryLine);
				}
				this.summaryEntryLines.add(entryLine);
			}
			this.summaryEntryLines.add(TEMPLATE_END_SUMMARY);
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "createSummaryEntry()...exit.");
	
	}
	
	/**
	 * 
	 * If a line in the summary entry contains a link, this method generates the
	 * appropriate html anchor tag.
	 * 
	 * @param summaryLine - String
	 * @return anchorTag - String
	 */
	private String createUrlLinkEntry(String summaryLine) {
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
	 * Returns an encountered error condition message.
	 * 
	 * @return createStudyErrorMessage - String
	 */
	public String getCreateStudyErrorMessage() {
		return createStudyErrorMessage;
	}
	
	/**
     * Returns the name of the new study created. Note that it returns the name of the
     * root folder rather than the display name of the new study. The display name may
     * be 'ABCD - Template Matching' and the folder name would be abcd_template_matching.
     * 
     * @return studyFolder - String
     */
    public String getStudyName() {
    	return this.studyFolder;
    }
	
	/**
	 * Removes the study folder for the new study. This is invoked if the creation
	 * encounters an error condition and must be aborted.
	 * 
	 */
	private void removeStudyFolder() {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "removeStudyFolder()...invoked.");
		
		try {
			File targetDirectory = new File(this.absoluteStudyFolder);
			FileUtils.deleteDirectory(targetDirectory);
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "removeStudyFolder()...exit.");
	}
	
	/**
     * 
     * Executes the 'unzip' command as a bash command.
     * 
     * @param absoluteZipPath - String
     * @return success - boolean
     * @throws IOException - unhandled exception
     * @throws BIDS_FatalException - unhandled exception
     */
    private boolean runSystemUnzipCommand(String absoluteZipPath) throws IOException, BIDS_FatalException {

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
						this.setCreateStudyHasError(true);
						LOGGER.trace(loggerId + "setZipFormatError=" + true);
						zipFormatErrorExists = true;
						success = false;
						errorMessage = "surface.zip format error: top level directory is not surface/";
						this.setCreateStudyErrorMessage(errorMessage);
						this.setCreateStudyHasError(true);
					}
				}
				if(lineCount==2 && isVolume) {
					if(!stdInLine.trim().contentEquals("creating: volume/")) {
						this.setCreateStudyHasError(true);
						errorMessage = "volume.zip format error: top level directory is not volume/";
						LOGGER.trace(loggerId + "setZipFormatError=" + true);
						success = false;
						zipFormatErrorExists = true;
						this.setCreateStudyErrorMessage(errorMessage);
						this.setCreateStudyHasError(true);
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
    		throw ioE;
    	}
    	catch(InterruptedException iE) {
    		LOGGER.trace(loggerId + "runSystemUnzipCommand()...error enountered");
    		LOGGER.trace(iE.getMessage(), iE);
    		String message = iE.getMessage();
    		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
    		BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
    		throw bfE;
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
	 */
    private void runSystemZipFolderCommand(String folderName, String workingDirectoryPath) {
    	
    	String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "runSystemZipFolderCommand()...invoked.");

    	int returnCode = -1;
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
				
				String message = this.studyFolder + ": unable to create zip file for folder=" + folderName;
				EmailNotifier.sendEmailNotification(message + "\n" + errorDetails);
			}
    	}
    	catch(IOException ioE) {
    		LOGGER.error(loggerId + "Unable to create zip files for " + workingDirectoryPath + folderName);
    		LOGGER.error(ioE.getMessage(), ioE);
    	}
    	catch(InterruptedException iE) {
    		LOGGER.error(loggerId + "Unable to create zip files for " + workingDirectoryPath + folderName);
    		LOGGER.error(iE.getMessage(), iE);
    	}
    	
    }
    
    /**
	 * Sets the message relating to an encountered error condition.
	 * 
	 * @param createStudyErrorMessage - String
	 */
	private void setCreateStudyErrorMessage(String createStudyErrorMessage) {
		this.createStudyErrorMessage = createStudyErrorMessage;
	}
    
    /**
	 * Sets the boolean indicating that an error was encountered.
	 * 
	 * @param createStudyHasError - boolean
	 */
	public void setCreateStudyHasError(boolean createStudyHasError) {
		this.createStudyHasError = createStudyHasError;
	}
    
    /**
	 * Add an entry for the new study in the /midb/menu.conf file
	 * 
	 * @throws IOException - Unhandled exception
	 */
	private void updateMenuConfig() throws IOException {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");
		
		FileWriter fw = new FileWriter(MENU_CONFIG_FILE, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(menuEntry);
	    bw.newLine();
	    bw.close();
	    //AtlasDataCacheManager.getInstance().reloadMenuConfig();
	    
		LOGGER.trace(loggerId + "updateMenuConfig()...exit.");
	}

	/**
	 * 
	 * Adds an entry for the new study in the /midb/network_folder_names.conf file.
	 * 
	 * @throws IOException - Unhandled exception
	 */
	private void updateNetworkFolderNamesConfig() throws IOException {
		
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "updateNetworkFolderNamesConfig()...invoked.");
		
		PrintWriter pw = new PrintWriter(new FileWriter(NETWORK_FOLDER_NAMES_CONFIG_FILE, true));
	    
	    Iterator<String> networkFolderLinesIt = this.networkFoldersEntryLines.iterator();
	    pw.println();
	    
	    while(networkFolderLinesIt.hasNext()) {
	    	pw.println(networkFolderLinesIt.next());
	    }
	    pw.close();
	    //AtlasDataCacheManager.getInstance().reloadSummaryConfig();
        File networkFoldersFile = new File(this.foldersTextFile);
        networkFoldersFile.delete();

		LOGGER.trace(loggerId + "updateNetworkFolderNamesConfig()...exit.");

	}

	/**
	 * Creates an entry for the new study in the /midb/summary.conf file.
	 * 
	 * 
	 * @throws IOException - Unhandled exception
	 */
	private void updateSummaryConfig() throws IOException {
		
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "updateSummaryConfig()...invoked.");
		
		createSummaryEntry();

		PrintWriter pw = new PrintWriter(new FileWriter(SUMMARY_CONFIG_FILE, true));
	    
	    Iterator<String> summaryLinesIt = this.summaryEntryLines.iterator();
	    pw.println();
	    
	    while(summaryLinesIt.hasNext()) {
	    	pw.println(summaryLinesIt.next());
	    }
	    pw.close();
	    //AtlasDataCacheManager.getInstance().reloadSummaryConfig();
	    
        File summaryFile = new File(this.summaryTextFile);
        summaryFile.delete();

		LOGGER.trace(loggerId + "updateSummaryConfig()...exit.");

	}

	/**
	 * Uploads a file required by the new study.
	 * 
	 * @param request - HttpServletRequest
	 * @param fileSize - size of the file
	 * @return fileName - String
	 * @throws IOException - Unhandled exception
	 */
	public String uploadFile(HttpServletRequest request, long fileSize) throws IOException {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "uploadFile()...invoked.");
		
		boolean isSurfaceZip = false;
		boolean isVolumeZip = false;

		long uploadedZipSize = 0;

		String fileName = null;

		try {
		
			for (Part part : request.getParts()) {
				fileName = part.getSubmittedFileName();
				if(fileName.toLowerCase().contains("surface")) {
					isSurfaceZip = true;
					this.zipFile_surface = absoluteStudyFolder + fileName;
				}
				else if(fileName.toLowerCase().contains("volume")) {
					isVolumeZip = true;
					this.zipFile_volume = absoluteStudyFolder + fileName;
				}
				
			    part.write(absoluteStudyFolder + fileName);
			}
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			BIDS_FatalException bfE = new BIDS_FatalException(e.getMessage(), ste);
			throw bfE;
		}
		
		
		if(isSurfaceZip) {
			File surfaceZipFile = new File(this.zipFile_surface);
			uploadedZipSize = surfaceZipFile.length();
			if(uploadedZipSize != fileSize) {
				String message = "File upload error, received fileSize=" + uploadedZipSize;
				message += " Expected fileSize=" + fileSize;
				StackTraceElement[] ste = Thread.currentThread().getStackTrace();
				BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
				throw bfE;
			}
		}
		else if(isVolumeZip) {
			File volumeZipFile = new File(this.zipFile_volume);
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
		
		LOGGER.info(loggerId + "uploadFile()...file name=" + fileName);
		LOGGER.info(loggerId + "uploadFile()...fileSize parameter=" + fileSize);
		LOGGER.info(loggerId + "uploadFile()...uploaded file size=" + uploadedZipSize);
		
		LOGGER.trace(loggerId + "uploadFile()...exit.");
		return fileName;
	}

	/**
	 * 
	 * Validates that there is not a mismatch between the uploaded folders.txt file
	 * and the folders contained in the uploaded zip file.
	 * 
	 * @param dataType - either 'volume' or 'surface'
	 * @return isValid - boolean
	 */
	private boolean validateNetworkFoldersConfig(String dataType) {
    	
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
    		this.allFoldersList.add(directories[i].getName());
    	}
    	
    	if(this.menuEntry.contains("combined_clusters")) {
    		if(!this.combinedClustersFolderExists) {
    			isValid = false;
    			String errorMessage = "Combined Networks was selected as an available network type, " +
    		           "but the 'combined_clusters' folder does not exist in the uploaded zip file." +
    				   "<br>Study not created";
    			this.setCreateStudyHasError(true);
    			this.setCreateStudyErrorMessage(errorMessage);
    			return isValid;
    		}
    	}
    	
    	if(this.menuEntry.contains("overlapping")) {
    		if(!this.overlappingNetworksFolderExists) {
    			isValid = false;
    			String errorMessage = "Integration Zone was selected as an available network type, " +
    		           "but the 'overlapping_networks' folder does not exist in the uploaded zip file." +
    					"<br>Study not created";
    			this.setCreateStudyHasError(true);
    			this.setCreateStudyErrorMessage(errorMessage);
    			return isValid;
    		}
    	}
    	
    	String errorMessage = null;
    	
    	if(this.allFoldersList.size() != this.foldersList.size()) {
    		isValid = false;
			this.setCreateStudyHasError(true);
    		
    		if(this.allFoldersList.size()>this.foldersList.size()) {
    			
    			Iterator<String> allFoldersListIt = allFoldersList.iterator();
    			String curFolderName = null;
    			String missingFolderName = "undetermined";
    			
    			while(allFoldersListIt.hasNext()) {
    				curFolderName = allFoldersListIt.next();
    				if(!this.foldersList.contains(curFolderName)) {
    					missingFolderName = curFolderName;
    					break;
    				}
    			}
    			
    			errorMessage = "Folder config error: " +
    		         "the zip file contains more Single Network folders than are listed" +
    				 " in the folders.txt file. The missing entry is: " + missingFolderName;
    		}
    		
    		if(this.foldersList.size()>this.allFoldersList.size()) {
    			
    			Iterator<String> foldersListIt = this.foldersList.iterator();
    			String curFolderName = null;
    			String missingFolderName = "undetermined";
     			
    			while(foldersListIt.hasNext()) {
    				curFolderName = foldersListIt.next();
    				if(!this.allFoldersList.contains(curFolderName)) {
    					missingFolderName = curFolderName;
    					break;
    				}
    			}
    			
      			errorMessage = "Folder config error: " +
       		         "the folders.txt file contains more Single Network folders than are contained" +
       				 " in the zip file. The missing folder in the zip file is: " + missingFolderName;
    			
    		}

			this.setCreateStudyErrorMessage(errorMessage);
			return isValid;
    	}
    	
    	Iterator<String> configFolderNamesIt = this.foldersList.iterator();
    	String aFolderName = null;
    	
    	while(configFolderNamesIt.hasNext()) {
    		aFolderName = configFolderNamesIt.next();
    		if(aFolderName.contains(" ")) {
    			isValid = false;
    			this.setCreateStudyHasError(true);
    			String message = TEMPLATE_FOLDERS_SPACE_ERROR_MESSAGE;
    			message = message.replace(REPLACE_FOLDER_NAME, aFolderName);
    			this.setCreateStudyHasError(true);
    			this.setCreateStudyErrorMessage(message);
    			break;
    		}
    	}
    	return isValid;
    }

    
}
