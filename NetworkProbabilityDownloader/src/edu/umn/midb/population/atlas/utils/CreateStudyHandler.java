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
import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;

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

	
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);

	
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
	
	public boolean completeStudyDeploy() throws IOException {
		
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
		return true;
	}
	
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
	
	private String createLinkEntry(String summaryLine) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "createLinkEntry()...invoked.");

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
		
		return linkEntry;
		
	}
	
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
	
	/*
	public boolean deployStudy() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor invoked.");
				
		boolean successIndicator = false;
		
		String fileName = null;
		
		try {
			createPath(this.absoluteStudyFolder);
		
			for (Part part : request.getParts()) {
				fileName = part.getSubmittedFileName();
				if(fileName.toLowerCase().contains("surface")) {
					this.zipFile_surface = absoluteStudyFolder + fileName;
				}
				else if(fileName.toLowerCase().contains("volume")) {
					this.zipFile_volume = absoluteStudyFolder + fileName;
				}
				
			    part.write(absoluteStudyFolder + fileName);
			}
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
			//throw a bids fatal e
		}
		
		if(this.zipFile_surface != null) {
			unzipFolder(zipFile_surface, this.absoluteStudyFolder);
		}
		if(this.zipFile_volume != null) {
			unzipFolder(zipFile_volume, this.absoluteStudyFolder);
		}
		
		updateMenuConfig();
		updateSummaryConfig();
		
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor exit.");

		return successIndicator;
	}
	
	*/
	
	public static void createPath(String absolutePath) throws IOException {
		
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
	
	private void unzipFileOld(String filePathAndName) throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "unzipFiles()...invoked.");
		
        File destDir = new File(this.absoluteStudyFolder);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(filePathAndName));
        ZipEntry zipEntry = zis.getNextEntry();
        boolean isDirectoryEntry = false;
        
        while (zipEntry != null) {
        	File destinationPath = new File(this.absoluteStudyFolder);
        	File newFile = newFile(destinationPath, zipEntry);
        	
        	if (zipEntry.isDirectory()) {
        		isDirectoryEntry = true;
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
        	}
        	else {
        		// fix for Windows-created archives where parent directories do not
        		// have a corresponding entry in the zip file
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                
        	}
             // write file content
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        	zipEntry = zis.getNextEntry();
       }
       zis.close();
	   LOGGER.trace(loggerId + "unzipFiles()...exit.");
	}
	
	public void unzipFolderOrig(String zipFilePathAndName, String destinationPath) throws BIDS_FatalException {

	    String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "unzipFolder() invoked. zip folder/name=" + zipFilePathAndName);

		if(!destinationPath.endsWith("/") ) {
			destinationPath += "/";
		}
		File dir = new File(destinationPath);
        FileInputStream fis = null;
        ZipInputStream zis = null;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        
        try {
        	fis = new FileInputStream(zipFilePathAndName);
            zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            
            while(ze != null){
                String fileName = ze.getName();
                if(fileName.contains(".class") || fileName.contains(".zip")
                   || fileName.contains(".DS_Store") || fileName.contains("__MACOSX/")) {
                	ze = zis.getNextEntry();
                	continue;
                }
                
                File newFile = new File(destinationPath + fileName);
                
                if(ze.isDirectory()) {
                	if(!newFile.mkdirs()) {
                		throw new IOException("Unable to make director:" + newFile.getAbsolutePath());
                	}
                	else {
                		ze = zis.getNextEntry();
                		continue;
                	}
                }
                
        		//LOGGER.trace(loggerId + "unzipFolder() extracting " + destinationPath + fileName);
                //create directories for sub directories in zip
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.close();
            File zipFile = new File(zipFilePathAndName);
            zipFile.delete();
        }
        catch(Exception e) {
    		LOGGER.trace(loggerId + "unzipFolder() caught exception.");
    		String errorMessage = e.getMessage();
    		LOGGER.error(errorMessage, e);
    		if(errorMessage.contains("invalid entry size (expected 997080 but got 997348 bytes)")) {
    			LOGGER.error(loggerId + "Probable invalid error, continuing...");
    			this.appContext.setZipFileUnpackError(true);
    			return;
    		}
    		removeStudyFolder();
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
    		BIDS_FatalException bfE = new BIDS_FatalException(e.getMessage(), ste);
    		throw bfE;
        }
		
		LOGGER.trace(loggerId + "unzipFolder() exit.");
	
	}
	
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
					entryLine = createLinkEntry(entryLine);
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
	
    public void unzipFolder(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
        	String entryName = entry.getName();
            if(entryName.contains(".class") || entryName.contains(".zip")
                    || entryName.contains(".DS_Store") || entryName.contains("__MACOSX/")) {
                 	entry = zipIn.getNextEntry();
                 	continue;
            }
     
            String filePath = destDirectory + File.separator + entryName;
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        
        File zipFile = new File(zipFilePath);
        zipFile.delete();
    }
    
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
    
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
    			this.appContext.setFolderConfigurationError(true);
    			this.appContext.setCreateStudyErrorMessage(errorMessage);
    			return isValid;
    		}
    	}
    	
    	if(this.menuEntry.contains("overlapping")) {
    		if(!this.overlappingNetworksFolderExists) {
    			isValid = false;
    			String errorMessage = "Integration Zone was selected as an available network type, " +
    		           "but the 'overlapping_networks' folder does not exist in the uploaded zip file." +
    					"<br>Study not created";
    			this.appContext.setFolderConfigurationError(true);
    			this.appContext.setCreateStudyErrorMessage(errorMessage);
    			return isValid;
    		}
    	}
    	
    	if(this.allFoldersList.size() != this.foldersList.size()) {
    		isValid = false;
    		String errorMessage = "Folder config error: " +
    	           "there is a mismatch between folders contained in the " +
    			   "zip file versus files listed in folders.txt.  It is possible " +
    	           "that the zip file contains an extraneous folder such as 'New folder' " +
    			   " or some other extraneous folder. <br>Study not created";
			this.appContext.setFolderConfigurationError(true);
			this.appContext.setCreateStudyErrorMessage(errorMessage);
			return isValid;
    	}
    	
    	Iterator<String> configFolderNamesIt = this.foldersList.iterator();
    	String aFolderName = null;
    	
    	while(configFolderNamesIt.hasNext()) {
    		aFolderName = configFolderNamesIt.next();
    		if(aFolderName.contains(" ")) {
    			isValid = false;
    			this.appContext.setCreateStudyHasError(true);
    			String message = TEMPLATE_FOLDERS_SPACE_ERROR_MESSAGE;
    			message = message.replace(REPLACE_FOLDER_NAME, aFolderName);
    			this.appContext.setFolderConfigurationError(true);
    			this.appContext.setCreateStudyErrorMessage(message);
    			break;
    		}
    		if(!this.allFoldersList.contains(aFolderName)) {
    			isValid = false;
    			this.appContext.setCreateStudyHasError(true);
    			String message = TEMPLATE_FOLDERS_ERROR_MESSAGE;
    			message = message.replace(REPLACE_FOLDER_NAME, aFolderName);
    			this.appContext.setFolderConfigurationError(true);
    			this.appContext.setCreateStudyErrorMessage(message);
    			break;
    		}
   
    	}
    	return isValid;
    }
    
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
    
    private void runSystemUnzipCommandOld(String absoluteZipFolderPath) throws IOException {
		String loggerId = this.appContext.getLoggerId();
		LOGGER.trace(loggerId + "runSystemUnzipCommand()...invoked.");

    	int returnCode = -1;
    	String baseCommand = "/usr/bin/unzip ";
    	String commandToExecute = baseCommand + absoluteZipFolderPath;
    	Runtime runtime = Runtime.getRuntime();
    	Process process = null;
    	BufferedReader br = null;
    	String outputLine = null;
    	String completeOutput = "";
    	
    	try {
    		LOGGER.trace(loggerId + "command=" + commandToExecute);
    		process = runtime.exec(commandToExecute);
    	
    		returnCode = process.waitFor();
    		LOGGER.trace(loggerId + "returnCode=" + returnCode);

    		br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		
			while ((outputLine = br.readLine()) != null) {
    			LOGGER.trace(outputLine);
    			completeOutput += outputLine;
    		}
			if(returnCode != 0) {
	    		String message = completeOutput;
	    		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	    		BIDS_FatalException bfE = new BIDS_FatalException(message, ste);
	    		throw bfE;
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
    	
    	
		LOGGER.trace(loggerId + "runSystemUnzipCommand()...exit.");

    }
    
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
				if(lineCount==2 && isSurface) {
					if(!stdInLine.trim().contentEquals("creating: surface/")) {
						this.appContext.setZipFormatError(true);
						LOGGER.trace(loggerId + "setZipFormatError=" + true);
						zipFormatErrorExists = true;
						success = false;
						errorMessage = "surface.zip format error: top level directory is not surface/";
						this.appContext.setZipFormatErrorMessage(errorMessage);
						this.appContext.setCreateStudyErrorMessage(errorMessage);
					}
				}
				if(lineCount==2 && isVolume) {
					if(!stdInLine.trim().contentEquals("creating: volume/")) {
						this.appContext.setZipFormatError(true);
						errorMessage = "volume.zip format error: top level directory is not volume/";
						LOGGER.trace(loggerId + "setZipFormatError=" + true);
						success = false;
						zipFormatErrorExists = true;
						this.appContext.setZipFormatErrorMessage(errorMessage);
						this.appContext.setCreateStudyErrorMessage(errorMessage);
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
				String message = this.studyFolder + ": bad unzip return code for fileName=" + fileName;
				EmailNotifier.sendEmailNotification(errorMessage + "\n" + errorDetails);
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
    
    public String getStudyName() {
    	return this.studyFolder;
    }
    
}
