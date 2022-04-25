package edu.umn.midb.population.atlas.data.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import logs.ThreadLocalLogTracker;

/**
 * DirectoryAccessor provides the functionality for accessing the directories containing the .png files
 * and NII files for the various neural network types.  The root folder for the data is the
 * 'midb/studies/study_name/surface_or_volume/' folder under the system root folder.  Each neural network
 * type has its own folder which is a subfolder of the data root folder.  For example, there is a
 * '/midb/studies/abcd_template_matching/surface/Aud' folder. 
 * 
 * @author jjfair
 *
 */
public class DirectoryAccessor {
	
	private static Logger LOGGER = LogManager.getLogger(DirectoryAccessor.class);
	
	/**
	 * Returns a boolean indicating if the file name should be included in the list of .png files returned
	 * to the client browser.  Only files representing a whole percentile from 1% to 100% are included. Therefore,
	 * a file with the name of 'Aud_thresh0.005.png' would be excluded since it represents a value of 0.5%.
	 * 
	 * @param thresholdName String representing the name of the .png file being interrogated
	 * 
	 * @return boolean indicating if the file name represents a whole percentage value from 1% to 100%
	 */
	private static boolean fraction_GTE_3_DEC(String thresholdName) {
		
		boolean is_GTE_3_DEC = false;
		
		int beginIndex = thresholdName.indexOf(".") + 1;
		int endIndex = thresholdName.lastIndexOf(".");
		
		if(endIndex < beginIndex) {
			LOGGER.error("Error for fraction_GTE_3_DEC()...fileName=" + thresholdName);
		}
		
		String fractionPortion = thresholdName.substring(beginIndex, endIndex);
		
		
		if(fractionPortion.length()>=3) {
			is_GTE_3_DEC = true;
		}

		return is_GTE_3_DEC;
	}
	
	/**
	 * Returns the binary byte buffer for the specified file.
	 * 
	 * @param filePath  The absolute path and file name of the file specified
	 * 
	 * @return byte[] A byte array containing the binary data of the file 
	 */
	public static byte[] getFileBytes(String filePath) {
		
		//String loggerId = ThreadLocalLogTracker.get();
		//LOGGER.trace(loggerId + "getFileBytes()...invoked.");

		
		byte[] allBytes = null;
		
		try {
			InputStream inputStream = new FileInputStream(filePath);
			allBytes = inputStream.readAllBytes();
			inputStream.close();
		}
		catch(Exception e) {
			throw new BIDS_FatalException(e.getMessage(), e.getStackTrace());
		}
		
		//LOGGER.trace(loggerId + "getFileBytes()...exit.");

		return allBytes;

	}
	
	/**
	 * 
	 * Returns the absolute file path of the .dscalar.nii file for a given study and network type
	 * 
	 * @param studyAndNetworkPath - String
	 * @return absolutePath - String
	 */
	public static String getNetworkMapNiiFilePath(String studyAndNetworkPath) {
		
		File[] directories = new File(studyAndNetworkPath).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		    	
		    	if(file.getName().contains(".dscalar.nii")) {
		    		return true;
		    	}
		        return false;
		    }
		});
		
		File aFile = directories[0]; 
		
		String path = aFile.getAbsolutePath();
		return path;
	}
	
	/**
	 * Returns the absolute path and image file name for each and every .png file associated with
	 * each probabilistic threshold for a given study and neural network type.
	 *  
	 * @param fileDirectory String representing the neural network folder that has been selected by the web client
	 * @return ArrayList of String objects
	 */
	public static ArrayList<String> getThresholdImagePaths(String fileDirectory) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getThresholdImagePaths()...invoked. Target directory=" + fileDirectory);

		ArrayList<String> imagePaths = new ArrayList<String>();
		File[] directories =  null;
		File aFile = null;
		String anImagePath = null;
		//String imagePath100 = null;
		
				
		directories = new File(fileDirectory).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		    	
		    	//file name designates 3 or more decimal places
		    	//for example Aud_thresh0.005.png
		    	//we ignore such files because we only care about
		    	//thresholds from 1% to 100% in increments of 1%
		    	//such as 1, 2, 3, etc.
		    	boolean fraction_GTE_3_DEC = false;
		    	
		    	if(file.isDirectory()) {
		    		return false;
		    	}
		    	if(!file.getName().contains(".png")) {
		    		return false;
		    	}
		 
		    	String filePathName = file.getName();
		    	
		    	/*
		    	if(filePathName.contains("_network_probability")) {
		    		return false;
		    	}
		    	*/
		    	
		    	if(!filePathName.contains("_network_probability") && !filePathName.contains("_number_of_nets")) {
		    	     fraction_GTE_3_DEC = fraction_GTE_3_DEC(filePathName);
		    	}
		    	
		    	//ignore files that represent .005 percentages such as Aud_thresh0.005.png
		    	if(fraction_GTE_3_DEC) {
		    		return false;
		    	}
	    	
	    		return true;
		    }
		});
		
		Arrays.sort(directories);

		for(int i=0; i<directories.length; i++ ) {
			aFile = directories[i];
			anImagePath = aFile.getAbsolutePath();
			// the client always expects the network probability map to be first in the array
			// because it is processed differently on the client
			if(anImagePath.contains("_network_probability") || anImagePath.contains("_number_of_nets")) {
				imagePaths.add(0, anImagePath);
			}
			else {
				imagePaths.add(anImagePath);
			}
		}
		LOGGER.trace(loggerId + "getThresholdImagePaths()...imagePaths count=" + imagePaths.size());
		LOGGER.trace(loggerId + "getThresholdImagePaths()...exit.");
		
		return imagePaths;

	}

}
