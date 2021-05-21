package edu.umn.midb.population.atlas.data.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import logs.ThreadLocalLogTracker;

/**
 * DirectoryAccessor provides the functionality for accessing the directories containing the .png files
 * and NII files for the various neural network types.  The root folder for the data is the
 * 'midb/networks_small_package-compressed' folder under the system root folder.  Each neural network
 * type has its own folder which is a subfolder of the data root folder.  For example, there is a
 * '/midb/networks_small_package-compressed/Aud' folder. 
 * 
 * @author jjfair
 *
 */
public class DirectoryAccessor {
	
	private static Logger LOGGER = LogManager.getLogger(DirectoryAccessor.class);
	
	/**
	 * Returns a list of all the neural network types.
	 * @param rootDirectory The absolute path of the data root
	 * @return ArrayList Contains all the names of the neural network folders
	 */
	public static ArrayList<String> getNeuralNetworkNames(String rootDirectory) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getNeuralNetworkNames()...invoked.");
		
		File[] directories = new File(rootDirectory).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		    	
		    	if(file.getName().contains("combined")) {
		    		return false;
		    	}
		    	if(file.getName().contains("overlapping")) {
		    		return false;
		    	}
		    	
		        return (file.isDirectory()) & !file.isHidden();
		    }
		});
	
	   Arrays.parallelSort(directories);
	   String shortName;
	   	

	    ArrayList<String> neuralNetworkTypes = new ArrayList<String>();
	    
		for(File file : directories) {
			shortName = file.getName();
			neuralNetworkTypes.add(shortName);
		}

		LOGGER.trace(loggerId + "getNeuralNetworkNames()...exit.");

		return neuralNetworkTypes;
	}
	
	/**
	 * Returns the absolute path and image file name for each and every .png file associated with
	 * each probabilistic threshold for a given neural network type.
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
		    	
		    	
		    	if(fraction_GTE_3_DEC) {
		    		return false;
		    	}
	    	
	    		return true;
		    }
		});
		
		Arrays.sort(directories);
		LOGGER.trace(directories);
		
		for(int i=0; i<directories.length; i++ ) {
			aFile = directories[i];
			anImagePath = aFile.getAbsolutePath();
		
			if(anImagePath.contains("_network_probability") || anImagePath.contains("_network_probability")) {
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
	
	public static String getNetworkMapNiiFilePath(String networkPath) {
		
		File[] directories = new File(networkPath).listFiles(new FileFilter() {
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
	 * Returns the binary byte buffer for the specified .png file.
	 * 
	 * @param filePath  The absolute path and file name of the .png file specified
	 * 
	 * @return byte[] A byte array containing the binary data of the .png file 
	 */
	public static byte[] getFileBytes(String filePath) {
		
		String loggerId = ThreadLocalLogTracker.get();
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
		
		String fractionPortion = thresholdName.substring(beginIndex, endIndex);
		
		if(fractionPortion.length()>=3) {
			is_GTE_3_DEC = true;
		}

		return is_GTE_3_DEC;
	}

}
