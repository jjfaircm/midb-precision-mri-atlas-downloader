package edu.umn.midb.population.atlas.data.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import logs.ThreadLocalLogTracker;

public class DirectoryAccessor {
	
	private static Logger LOGGER = LogManager.getLogger(DirectoryAccessor.class);
	
	public static ArrayList<String> getNeuralNetworkNames(String rootDirectory) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getNeuralNetworkNames()...invoked.");

		ArrayList<String> neuralNetworkTypes = new ArrayList<String>();
		neuralNetworkTypes.add("visual");
		neuralNetworkTypes.add("audio");
		neuralNetworkTypes.add("combined_clusters");
		LOGGER.trace(loggerId + "getNeuralNetworkNames()...exit.");

		return neuralNetworkTypes;
	}
	
	public static ArrayList<String> getThresholdImagePaths(String fileDirectory) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getThresholdImagePaths()...invoked.");

		ArrayList<String> imagePaths = new ArrayList<String>();
		File[] directories =  null;
		File aFile = null;
		String anImagePath = null;
				
		directories = new File(fileDirectory).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		        return (!file.isDirectory()) & !file.isHidden() & file.getName().contains("thresh") &
		        		 file.getName().contains(".png");
		    }
		});
		
		for(int i=0; i<directories.length; i++ ) {
			aFile = directories[i];
			anImagePath = aFile.getAbsolutePath();
			imagePaths.add(anImagePath);
		}
		
		LOGGER.trace(loggerId + "getThresholdImagePaths()...exit.");
		
		return imagePaths;

	}
	
	public static byte[] getFileBytes(String filePath) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getFileBytes()...invoked.");

		
		byte[] allBytes = null;
		
		try {
			InputStream inputStream = new FileInputStream(filePath);
			allBytes = inputStream.readAllBytes();
			inputStream.close();
		}
		catch(Exception e) {
			throw new BIDS_FatalException(e.getMessage(), e.getStackTrace());
		}
		
		LOGGER.trace(loggerId + "getFileBytes()...exit.");

		return allBytes;

	}

}
