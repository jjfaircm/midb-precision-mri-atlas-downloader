package edu.umn.midb.population.atlas.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;

public class CreateStudyHandler {
	
	private static final String ROOT_DESTINATION_PATH = "/midb/studies/";
	private static final String MENU_CONFIG_FILE = "/midb/menu.conf";
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String menuEntry;
	private String studyFolder;
	private String availableDataTypes = null;
	private String zipFile_surface = null;
	private String zipFile_volume = null;

	
	private static final Logger LOGGER = LogManager.getLogger(CreateStudyHandler.class);

	
	public CreateStudyHandler(HttpServletRequest request, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor invoked.");

		this.request = request;
		this.response = response;
		this.studyFolder = ROOT_DESTINATION_PATH + request.getParameter("studyFolderName") + File.separator;
		this.menuEntry = request.getParameter("menuEntry");
		this.availableDataTypes = request.getParameter("availableDataTypes");
		
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor exit.");

	}
	
	public boolean deployStudy() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor invoked.");
				
		boolean successIndicator = false;
		
		String fileName = null;
		
		try {
			createPath(this.studyFolder);
		
			for (Part part : request.getParts()) {
				fileName = part.getSubmittedFileName();
				if(fileName.toLowerCase().contains("surface")) {
					this.zipFile_surface = studyFolder + File.separator + fileName;
				}
				else if(fileName.toLowerCase().contains("volume")) {
					this.zipFile_volume = studyFolder + File.separator + fileName;
				}
				
			    part.write(studyFolder + File.separator + fileName);
			}
		}
		catch(Exception e) {
			LOGGER.error(loggerId + e.getMessage(), e);
			//throw a bids fatal e
		}
		
		if(this.zipFile_surface != null) {
			unzipFolder(zipFile_surface, this.studyFolder);
		}
		if(this.zipFile_volume != null) {
			unzipFolder(zipFile_volume, this.studyFolder);
		}
		
		updateMenuConfig();
		
		LOGGER.trace(loggerId + "CreateStudyHandler()...constructor exit.");

		return successIndicator;
	}
	
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
	
	private void updateMenuConfig() throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");
		
	    FileWriter fw = new FileWriter(MENU_CONFIG_FILE, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.newLine();
	    bw.write(menuEntry);
	    bw.newLine();
	    bw.close();
	    AtlasDataCacheManager.getInstance().reloadMenuConfig();
	    
		LOGGER.trace(loggerId + "updateMenuConfig()...invoked.");
	}
	
	private void unzipFileOld(String filePathAndName) throws IOException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "unzipFiles()...invoked.");
		
        File destDir = new File(this.studyFolder);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(filePathAndName));
        ZipEntry zipEntry = zis.getNextEntry();
        boolean isDirectoryEntry = false;
        
        while (zipEntry != null) {
        	File destinationPath = new File(this.studyFolder);
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
	   LOGGER.trace(loggerId + "unzipFiles()...exit.");
	}
	
	public static void unzipFolder(String zipFilePathAndName, String destinationPath) {

	    String loggerId = ThreadLocalLogTracker.get();
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
            File zipFile = new File(zipFilePathAndName);
            zipFile.delete();
        }
        catch(Exception e) {
    		LOGGER.trace(loggerId + "unzipFolder() caught exception.");
    		LOGGER.error(e.getMessage(), e);
        }
		
		LOGGER.trace(loggerId + "unzipFolder() exit.");
	
	}
	

}
