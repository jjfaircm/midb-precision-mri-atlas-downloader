package edu.umn.midb.population.atlas.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	//private Hashtable<String, ArrayList<byte[]>> binaryNetworkImageBuffers = new Hashtable<String, ArrayList<byte[]>>();
	private Hashtable<String, ArrayList<String>> imagePathNames = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> neuralNetworkNamesMap = new Hashtable<String, String>();
	private Hashtable<String, NetworkMapData> networkMapDataCache = new Hashtable<String, NetworkMapData>();
	private byte[] allDataZipBuffer = null;
	private ArrayList<String> menuStudyNames = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> menuChoicesMap = new Hashtable<String, ArrayList<String>>();
	private static final String MENU_FILE_PATH = "/midb/menu.conf";
	private static final String ACL_FILE_PATH = "/midb/acl.conf";
	private ArrayList<String> privilegedIPs = new ArrayList<String>();

	private final Object menuLock = new Object();

	
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
			instance.loadDefaultGlobalData(NetworkProbabilityDownloader.DEFAULT_ROOT_PATH);
			instance.loadNeuralNetworkNamesMap();
			instance.loadMenuConfig();
			instance.loadACL();
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
	
	private void loadACL() {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadACL()...invoked.");

		String ipAddress = null;
		File file = new File(ACL_FILE_PATH);
		
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				if(outputLine.trim().length() == 0) {
					continue;
				}
				ipAddress = outputLine.trim();
				this.privilegedIPs.add(ipAddress);
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadMenuConfig()...exit.");
	
	}
	
	private void loadMenuConfig() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadMenuConfig()...invoked.");

		File file = new File(MENU_FILE_PATH);
		boolean menuEntryNamePending = true;
		boolean menuSubEntriesPending = false;
		String menuStudyName = null;
		String menuSubEntry = null;
		ArrayList<String> subMenuEntries = null;
		
		
		
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				if(outputLine.trim().length() == 0) {
					continue;
				}
				if(outputLine.contains("MENU ENTRY")) {
					menuEntryNamePending = true;
					continue;
				}
				if(menuEntryNamePending) {
					menuStudyName = outputLine;
					this.menuStudyNames.add(menuStudyName);
					menuEntryNamePending = false;
					menuSubEntriesPending = true;
					subMenuEntries = new ArrayList<String>();
					this.menuChoicesMap.put(menuStudyName, subMenuEntries);
					continue;
				}
				else if(menuSubEntriesPending) {
					menuSubEntry = outputLine;
					subMenuEntries.add(menuSubEntry);
				}
				else if(outputLine.contains("END MENU")) {
					menuSubEntriesPending = false;
				}
			}
			
			Enumeration<String> keys = this.menuChoicesMap.keys();
			String currentKey = null;
			ArrayList<String> subMenuChoices = null;
			
			while(keys.hasMoreElements()) {
				currentKey = keys.nextElement();
				subMenuChoices = this.menuChoicesMap.get(currentKey);
				LOGGER.trace(loggerId + "menuDetails->>" + currentKey + ":" + subMenuChoices);
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadMenuConfig()...exit.");
	}
	
	private void loadNeuralNetworkNamesMap() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadNeuralNetworkNamesMap()...invoked.");

		// this is a map that associates the names in the browser dropdown to the
		// actual names of the directories that contain the threshold files of the
		// different networks.
		// The neuralNetworkNames arrayList are the names that appear in the dropdown
		// selection in the browser
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
		//they are not 'Single' network names, however they still have directories that 
		//contain threshold image files that may be selected for display in the browser
		this.neuralNetworkNamesMap.put("combined_clusters", "combined_clusters");
		//overlapping maps to integration-zone in browser menu nomenclature
		this.neuralNetworkNamesMap.put("overlapping", "overlapping_networks");
		
		LOGGER.trace(loggerId + "loadNeuralNetworkNamesMap()...exit."); 
	}
	
	/**
	 * Creates a cache of base64 encoded Strings for all the probabilistic thresholds for the given neural network.
	 * The names of the files are stored in an arrayList, and then the base64Strings representing the image bytes
	 * will be created and stored in a corresponding arrayList in the same order as the file names are stored.
	 * Therefore there is a 1-1 correspondence between the file names stored in the imagePaths array list and the
	 * base64 byte arrays stored in the imageByteBuffers arrayList.  In other words, for example, to find the name
	 * of the file represented in imageByteBuffers position 5, you would interrogate position 5 of the imagePathNames.
	 * 
	 * @param networkNamePath Name of the selected neural network type.
	 * 
	 */
	private ArrayList<String> loadBase64ImagePathStrings(String networkNamePath) {
		
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
		// we only cache abcd template matching study data
		if(networkNamePath.contains("abcd_template_matching")) {
			this.base64NetworkImageStrings.put(networkNamePath, base64ImageStrings);
			//this.binaryNetworkImageBuffers.put(networkNamePath, imageByteBuffers);
		}
		
		return base64ImageStrings;
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
		
		ArrayList<String> targetList = this.base64NetworkImageStrings.get(networkNamePath);
		if(targetList == null) {
			targetList = this.loadBase64ImagePathStrings(networkNamePath);
		}
		return targetList;
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
		LOGGER.trace(loggerId + "getImagePathNames()...invoked,networkNamePath=" + networkNamePath);
		//if it's null in the cache then load them and then
		//invoke loadBase64ImagePathStrings(...)
		
		boolean networkMapImageExists = true;
		
		if(networkNamePath.contains("combined_clusters")) {
			networkMapImageExists = false;
		}
		
		ArrayList<String> imagePathNames = this.imagePathNames.get(networkNamePath);
		
		
		if(imagePathNames==null) {
			imagePathNames = DirectoryAccessor.getThresholdImagePaths(networkNamePath);
			if(networkMapImageExists) {
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
	
	public ArrayList<String> getMenuStudyNames() {
		synchronized(menuLock) {
			return this.menuStudyNames;
		}
	}
	
	public Hashtable<String, ArrayList<String>> getMenuOptionsMap() {
		synchronized(menuLock) {
			return this.menuChoicesMap;
		}
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
	
	public ArrayList<String> getPrivilegedList() {
		return this.privilegedIPs;
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
	
	public void reloadMenuConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadMenuConfig()...invoked.");
		
			this.menuStudyNames = new ArrayList<String>();
			menuChoicesMap = new Hashtable<String, ArrayList<String>>();
			loadMenuConfig();
			LOGGER.trace(loggerId + "reloadMenuConfig()...invoked.");
		}
	}
}
