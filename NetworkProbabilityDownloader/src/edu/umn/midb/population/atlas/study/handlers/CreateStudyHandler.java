package edu.umn.midb.population.atlas.study.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.data.access.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import logs.ThreadLocalLogTracker;

/**
 * 
 * Provides functionality for creating a new study. This task requires creating a folder under the /midb/studies
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
public class CreateStudyHandler extends StudyHandler {
	
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
    
    
	private String summaryTextFile = null;
	private String foldersTextFile = null;
	private ArrayList<String> summaryEntryLines = new ArrayList<String>();
	private ArrayList<String> networkFoldersEntryLines = new ArrayList<String>();
	private static final Object MENU_LOCK = new Object();
	
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);
	
	
	
	/**
	 * Creates a new folder required by the new study.
	 * 
	 * @param absolutePath - String
	 * @throws IOException - unhandled exception
	 */
	private static void createPath(String absolutePath) throws IOException {
		
		File targetDirectory = new File(absolutePath);
		
		// the directory could already exist if a user previously tried to add a study
		// with the same name, but closed their browser in the middle of the file uploads
		// we will just rename the existing folder since it is really an orphaned folder
		if(targetDirectory.exists()) {
			int index = absolutePath.lastIndexOf("/");
			String newPathName = absolutePath.substring(0, index) + "_orphan";
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			String tsString = ts.toString();
			tsString = tsString.replace(" ", "__");
			tsString = tsString.replace(":", ".");
			newPathName = newPathName + "_" + tsString;
			File orphanedDirectory = new File(newPathName);
			targetDirectory.renameTo(orphanedDirectory);
		}
		
		boolean successIndicator = false;
		
		if(!targetDirectory.exists()) { 
			successIndicator = targetDirectory.mkdirs();
			if(!successIndicator) {
				throw new IOException("Unable to create folder:" + absolutePath);
			}
		}
	}
	

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
		
		super();
		
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
	 * Informs the handler to complete the deploy process since all files have
	 * been uploaded.
	 * 
	 * @return succes - boolean
	 * @throws IOException - unhandled exception
	 */
	public boolean completeStudyDeploy() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "completeStudyDeploy()...invoked.");

		boolean success = checkSufficientServerStorage();
		
		if(!success) {
			this.errorMessage = "Insufficient storage available.";
			this.errorEncountered = true;
			LOGGER.trace(loggerId + "  Insufficient storage available.");
			LOGGER.trace(loggerId + "completeStudyDeploy()...exit.");
			return success;
		}
		
		if(this.surfaceZipFilePath != null) {
			success = validateZipFileTopLevelFolder("surface");
			if(!success) {
				removeStudyFolder();
				return false;
			}
			
			success = runSystemUnzipCommand(this.surfaceZipFilePath);
			if(!success) {
				removeStudyFolder();
				return false;
			}
		}
		else {
			LOGGER.trace(loggerId + "completeStudyDeploy()...the surfaceZipFilePath was not found. Possible storage problem or upload problem");
		}
		
		if(this.volumeZipFilePath != null && success) {
			success = validateZipFileTopLevelFolder("volume");
			if(!success) {
				removeStudyFolder();
				return false;
			}
			success = runSystemUnzipCommand(this.volumeZipFilePath);
			if(!success) {
				removeStudyFolder();
				return false;
			}
		}
		this.createNetworkFolderConfigEntry();
		
		if(this.surfaceZipFilePath != null) {
			success = this.validateNetworkFoldersConfig("surface", false);
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
			
			success = this.validateDscalarFiles("surface", false);
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
			
			success = this.validateFileNames("surface", false);
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
			
			success = validateThresholdFiles("surface", false);
			if(!success) {
				this.removeStudyFolder();
				return false;
			}
			
		}

		if(this.volumeZipFilePath != null) {
			success = this.validateNetworkFoldersConfig("volume", false);
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
			
			
			success = this.validateDscalarFiles("volume", false);
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
			
			success = this.validateFileNames("volume", false);
			if(!success) {
				this.removeStudyFolder();
				return success;
			}
			
			success = validateThresholdFiles("volume", false);
			if(!success) {
				this.removeStudyFolder();
				return false;
			}
			
		}
	
		synchronized(MENU_LOCK) {
			createConfigBackup();
			updateMenuConfig();
			updateSummaryConfig();
			updateNetworkFolderNamesConfig();
		}
			
		//the remaining work can be completed asynchronously so the admin user
		//does not have to wait for the subfolders to be zipped for potential downloads
		//Refer to AsyncRunner inner class in StudyHandler
		asyncRunner.start();
		
		AtlasDataCacheManager.getInstance().reloadConfigs();
		LOGGER.trace(loggerId + "completeStudyDeploy()...exit.");
		return true;
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
				this.configuredFoldersList.add(folderName);
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
	 * Inserts an entry for the new study in the /midb/summary.conf file. The entry
	 * represents lines that are stored as list entries that are displayed under the
	 * main image panel in the browser.
	 * 
	 */
	private void createSummaryEntry() {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createSummaryEntry()...invoked.");

		File file = new File(this.absoluteStudyFolder + SUMMARY_ENTRY_FILE);
		
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
		LOGGER.trace(loggerId + "removeStudyFolder()...invoked. Folder name=" + this.absoluteStudyFolder);
		
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
        //File networkFoldersFile = new File(this.foldersTextFile);
        //networkFoldersFile.delete();

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
	    
        //File summaryFile = new File(this.summaryTextFile);
        //summaryFile.delete();

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

		long uploadedSize = 0;

		String fileName = null;

		try {
		
			for (Part part : request.getParts()) {
				fileName = part.getSubmittedFileName();
				if(fileName.toLowerCase().contains("surface")) {
					isSurfaceZip = true;
					this.surfaceZipFilePath = absoluteStudyFolder + fileName;
				}
				else if(fileName.toLowerCase().contains("volume")) {
					isVolumeZip = true;
					this.volumeZipFilePath = absoluteStudyFolder + fileName;
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
			File surfaceZipFile = new File(this.surfaceZipFilePath);
			uploadedSize = surfaceZipFile.length();
			if(uploadedSize != fileSize) {
				String message = "File upload error, received fileSize=" + uploadedSize;
				message += " Expected fileSize=" + fileSize;
				StackTraceElement[] ste = Thread.currentThread().getStackTrace();
				BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
				throw bfE;
			}
		}
		else if(isVolumeZip) {
			File volumeZipFile = new File(this.volumeZipFilePath);
			uploadedSize = volumeZipFile.length();
			if(uploadedSize != fileSize) {
				String message = "File upload error for file " + fileName;
				message += ". Target file size=" + fileSize;
				message +=  ". Received fileSize=" + uploadedSize;
				StackTraceElement[] ste = Thread.currentThread().getStackTrace();
				BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
				throw bfE;
			}
		}
		else {
			File textFile = new File(this.absoluteStudyFolder + fileName);
			uploadedSize = textFile.length();
		}
		
		LOGGER.info(loggerId + "uploadFile()...file name=" + fileName);
		LOGGER.info(loggerId + "uploadFile()...fileSize parameter=" + fileSize);
		LOGGER.info(loggerId + "uploadFile()...uploaded file size=" + uploadedSize);
		
		LOGGER.trace(loggerId + "uploadFile()...exit.");
		return fileName;
	}
    
}
