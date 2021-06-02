package edu.umn.midb.population.atlas.utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.data.access.DirectoryAccessor;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import logs.ThreadLocalLogTracker;

/**
 * Stores and manages various caches of .png files associated with the probabilistic thresholds for all
 * the different neural network types.  This prevents a scenario where multiple simultaneous requests
 * from different web clients would have to construct their own individual buffers of image data.
 * Additionally, a performance benefit is achieved since the base64 encoded data is created only once
 * for any given neural network type. After the first request for a neural network type, subsequent requests
 * are fulfilled by retrieving the data from the cache.
 * 
 * @author jjfair
 *
 */
public class AtlasDataCacheManager {
	
	//the Singleton instance
	private static AtlasDataCacheManager instance = null;
	private static Logger LOGGER = null;
	//contains collection of base64NetworkImageStrings keyed by the different 
	//neural network types
	private ArrayList<String> neuralNetworkNames = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> base64NetworkImageStrings = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, ArrayList<byte[]>> binaryNetworkImageBuffers = new Hashtable<String, ArrayList<byte[]>>();
	private Hashtable<String, ArrayList<String>> imagePathNames = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> neuralNetworkNamesMap = new Hashtable<String, String>();
	private Hashtable<String, NetworkMapData> networkMapDataCache = new Hashtable<String, NetworkMapData>();
	private byte[] allDataZipBuffer = null;

	
	static {
	      LOGGER = LogManager.getLogger(AtlasDataCacheManager.class);
	}
	
	/**
	 * Returns the Singleton instance for the class.
	 * 
	 * @return AtlasDataCacheManager
	 */
	public static synchronized AtlasDataCacheManager getInstance() {
		
		if(instance==null) {
			instance = new AtlasDataCacheManager();
			instance.loadDefaultGlobalData(NetworkProbabilityDownloader.ROOT_PATH);
			instance.loadNeuralNetworkNamesMap();
		}
		return instance;
	}
	
	/**
	 * Loads the default cache of base64 encoded Strings for the combined_clusters neural networks.
	 * This data is loaded at servlet startup when the servlet container is initialized.  The default
	 * request for every web client is always the combined_clusters image data.
	 * 
	 * @param rootPath String representation of the root folder where all the image data resides in the
	 *                 directory structure on the server.
	 */
	private void loadDefaultGlobalData(String rootPath) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadDefaultGlobalData()...invoked.");
		String defaultNetworkName = NetworkProbabilityDownloader.DEFAULT_NEURAL_NETWORK;
		String targetDirectory = rootPath + defaultNetworkName;
		ArrayList<String> imagePathNames = DirectoryAccessor.getThresholdImagePaths(targetDirectory);
		this.imagePathNames.put(targetDirectory, imagePathNames);
		this.loadBase64ImagePathStrings(targetDirectory);
		LOGGER.trace(loggerId + "loadDefaultGlobalData()...loadBase64ImagePathStrings.size=" + this.base64NetworkImageStrings.size());
	}
	
	private void loadNeuralNetworkNamesMap() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadNeuralNetworkNamesMap()...invoked.");

		this.neuralNetworkNamesMap.put("Auditory Network", "Aud");
		this.neuralNetworkNames.add("Auditory Network");
		this.neuralNetworkNamesMap.put("Cingulo-Opercular Network", "CO");
		this.neuralNetworkNames.add("Cingulo-Opercular Network");
		this.neuralNetworkNamesMap.put("Dorsal Attention Network", "DAN");
		this.neuralNetworkNames.add("Dorsal Attention Network");
		this.neuralNetworkNamesMap.put("Default Mode Network", "DMN");
		this.neuralNetworkNames.add("Default Mode Network");
		this.neuralNetworkNamesMap.put("Frontoparietal Network", "FP");
		this.neuralNetworkNames.add("Frontoparietal Network");
		this.neuralNetworkNamesMap.put("Medial Temporal Network", "MTL");
		this.neuralNetworkNames.add("Medial Temporal Network");
		this.neuralNetworkNamesMap.put("Parietal Medial Network", "PMN");
		this.neuralNetworkNames.add("Parietal Medial Network");
		this.neuralNetworkNamesMap.put("Parietal Occipital Network", "PON");
		this.neuralNetworkNames.add("Parietal Occipital Network");
		this.neuralNetworkNamesMap.put("Salience Network", "Sal");
		this.neuralNetworkNames.add("Salience Network");
		this.neuralNetworkNamesMap.put("Dorsal Sensorimotor Network", "SMd");
		this.neuralNetworkNames.add("Dorsal Sensorimotor Network");
		this.neuralNetworkNamesMap.put("Lateral Sensorimotor Network", "SMl");
		this.neuralNetworkNames.add("Lateral Sensorimotor Network");
		this.neuralNetworkNamesMap.put("Temporal Pole Network", "Tpole");
		this.neuralNetworkNames.add("Temporal Pole Network");
		this.neuralNetworkNamesMap.put("Ventral Attention Network", "VAN");
		this.neuralNetworkNames.add("Ventral Attention Network");
		this.neuralNetworkNamesMap.put("Visual Network", "Vis");
		this.neuralNetworkNames.add("Visual Network");
		
		LOGGER.info(this.neuralNetworkNames);
			
		//we now add 2 more names that are not included in the neuralNetworkNames because
		//they are not 'Single' network names
		this.neuralNetworkNamesMap.put("combined_clusters", "combined_clusters");
		this.neuralNetworkNamesMap.put("overlapping", "Number_of_overlapping_networks_Compressed");
		
		LOGGER.trace(loggerId + "loadNeuralNetworkNamesMap()...exit."); 
	}
	
	/**
	 * Creates a cache of base64 encoded Strings for all the probabilistic thresholds for the given neural network.
	 * 
	 * @param networkNamePath Name of the selected neural network type.
	 * 
	 */
	private void loadBase64ImagePathStrings(String networkNamePath) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadBase64ImagePathStrings()...invoked, networkNamePath=" + networkNamePath);
		ArrayList<String> imagePaths = this.imagePathNames.get(networkNamePath);
		ArrayList<byte[]> imageByteBuffers = new ArrayList<byte[]>();

		Iterator<String> imagePathsIt = imagePaths.iterator();
		String anImagePath = null;
		byte[] imageBuffer = null;
		
		while(imagePathsIt.hasNext()) {
			anImagePath = imagePathsIt.next();
			imageBuffer = DirectoryAccessor.getFileBytes(anImagePath);
			imageByteBuffers.add(imageBuffer);
		}

		Iterator<byte[]> bufferIt = imageByteBuffers.iterator();

		ArrayList<String> base64ImageStrings = new ArrayList<String>();
		String encodedString = null;

		while(bufferIt.hasNext()) {
			imageBuffer = bufferIt.next();
			encodedString = Base64.getEncoder().encodeToString(imageBuffer);
			base64ImageStrings.add(encodedString);
		}
		LOGGER.info(loggerId + "loadBase64ImagePathStrings()...adding base564ImageStrings array to cache, key=" + networkNamePath);
		LOGGER.trace(loggerId + "loadBase64ImagePathStrings()...exit.");
		this.base64NetworkImageStrings.put(networkNamePath, base64ImageStrings);
		this.binaryNetworkImageBuffers.put(networkNamePath, imageByteBuffers);
	}
	
	/**
	 * 
	 */
	public byte[] getAllDataZipBuffer() {
		return this.allDataZipBuffer;
	}
	
	/**
	 * Returns an ArrayList of base64 encoded Strings for all the .png files associated with the probabilistic thresholds for a specific neural network
	 * @param networkNamePath The name of the selected neural network type
	 * 
	 * @return ArrayList of base64 encoded .png image files
	 */
	public ArrayList<String> getBase64ImagePathStrings(String networkNamePath) {
		
		return this.base64NetworkImageStrings.get(networkNamePath);
	}
	
	/**
	 * Returns a list of NII image paths/names for a given neural network.
	 * 
	 * @param networkNamePath The absolute path to the folder containing the NII files for a neural network type
	 * 
	 * @return ArrayList of NII file names/paths
	 */
	public synchronized ArrayList<String> getImagePathNames(String networkNamePath) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getImagePathNames()...invoked.");
		//if it's null in the cache then load them and then
		//invoke loadBase64ImagePathStrings(...)
		
		boolean isSingleNetworkRequest = true;
		
		if(networkNamePath.equals("combined_clusters") || networkNamePath.equals("overlapping")) {
			isSingleNetworkRequest = false;
		}
		
		ArrayList<String> imagePathNames = this.imagePathNames.get(networkNamePath);
		
		
		if(imagePathNames==null) {
			imagePathNames = DirectoryAccessor.getThresholdImagePaths(networkNamePath);
			if(isSingleNetworkRequest) {
				this.addNetworkMapImage(networkNamePath, imagePathNames);
			}
			LOGGER.info(loggerId + " adding imagePathNames to cache, key=" + networkNamePath);
			LOGGER.trace(loggerId + "DirectoryAccessor.getThresholdImagePaths()...count=" + imagePathNames.size());
			LOGGER.trace(loggerId + imagePathNames);
			this.imagePathNames.put(networkNamePath, imagePathNames);
			this.loadBase64ImagePathStrings(networkNamePath);
		}
		
		LOGGER.trace(loggerId + "getImagePathNames()...exit.");
		return imagePathNames;
	}
	
	/**
	 * Returns an ArrayList of Strings for the available neural network types.
	 * 
	 * @return ArrayList containing the neural network types.
	 * 
	 */
	public ArrayList<String> getNeuralNetworkNames() {
		
		return this.neuralNetworkNames;
	}
	
	public String getNetworkPathName(String networkName) {
		return this.neuralNetworkNamesMap.get(networkName);
	}
	

	private void addNetworkMapImage(String networkPath, ArrayList<String> imagePathNames) {
		
		String probabilityMapImagePath = imagePathNames.remove(0);
		byte[] fileBytes = DirectoryAccessor.getFileBytes(probabilityMapImagePath);
		String base64EncodedString = Base64.getEncoder().encodeToString(fileBytes);
		String networkMapNifiFilePath = DirectoryAccessor.getNetworkMapNiiFilePath(networkPath);
		int index = networkPath.lastIndexOf("/");
		String networkName = networkPath.substring(index+1);
		NetworkMapData nmd = new NetworkMapData(networkName, base64EncodedString, networkMapNifiFilePath);
        this.networkMapDataCache.put(networkName, nmd);
	}
	
	public NetworkMapData getNetworkMapData(String networkName) {
		return this.networkMapDataCache.get(networkName);
	}
}
