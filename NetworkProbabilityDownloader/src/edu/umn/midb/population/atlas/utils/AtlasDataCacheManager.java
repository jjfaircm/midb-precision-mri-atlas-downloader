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
import edu.umn.midb.population.atlas.security.TokenManager;
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
	private Hashtable<String, ArrayList<String>> base64NetworkImageStrings = new Hashtable<String, ArrayList<String>>();
	//private Hashtable<String, ArrayList<byte[]>> binaryNetworkImageBuffers = new Hashtable<String, ArrayList<byte[]>>();
	private Hashtable<String, ArrayList<String>> imagePathNames = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, NetworkMapData> networkMapDataCache = new Hashtable<String, NetworkMapData>();
	private byte[] allDataZipBuffer = null;
	private ArrayList<String> menuStudyNames = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> menuChoicesMap = new Hashtable<String, ArrayList<String>>();
	private ArrayList<String> summaryStudyNames = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> summaryEntriesMap = new Hashtable<String, ArrayList<String>>();
	private ArrayList<String> networkFolderNamesConfig = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> singleNetworkFolderNamesMap = new Hashtable<String, ArrayList<String>>();

	private static final String MENU_CONFIG_PATH = "/midb/menu.conf";
	private static final String ACL_CONFIG_PATH = "/midb/acl.conf";
	private static final String SETTINGS_CONFIG_PATH = "/midb/settings.conf";
	private static final String KEY_CONFIG_PATH = "/midb/key.conf";
	private static final String SUMMARY_CONFIG_PATH = "/midb/summary.conf";
	private static final String NETWORK_FOLDERS_CONFIG_PATH = "/midb/network_folder_names.conf";
	private static final String NEW_LINE = "\n";
	private String localHostName = null;


	private ArrayList<String> privilegedIPs = new ArrayList<String>();

	private final Object menuLock = new Object();
	private final Object configLock = new Object();

	
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
			instance.loadMenuConfig();
			instance.loadACL();
			instance.loadSummaryConfig();
			instance.loadNetworkFolderNamesConfig();
			//instance.loadSettingsConfig();
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
		File file = new File(ACL_CONFIG_PATH);
		
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

		File file = new File(MENU_CONFIG_PATH);
		boolean menuEntryNamePending = true;
		boolean menuSubEntriesPending = false;
		String menuStudyName = null;
		String menuSubEntry = null;
		ArrayList<String> subMenuEntries = null;
		
		
		
		if(!file.exists()) {
			LOGGER.fatal("Config file not found:" + MENU_CONFIG_PATH);
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
	
	private void loadNetworkFolderNamesConfig() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadNetworkFolderNamesConfig()...invoked.");

		File file = new File(NETWORK_FOLDERS_CONFIG_PATH);
		ArrayList<String> currentConfig = null;
		int equalsIndex = -1;
		int closeParenIndex = -1;
		String studyNameKey = null;
		int i = 0;
		
		if(!file.exists()) {
			return;
		}
	
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				outputLine = outputLine.trim();
				if(outputLine.trim().length() < 3) {
					continue;
				}
				if(outputLine.startsWith("#")) {
					continue;
				}
				
				if(outputLine.contains("id=")) {
					currentConfig = new ArrayList<String>();
					equalsIndex = outputLine.indexOf("=");
					closeParenIndex = outputLine.indexOf(")");
					studyNameKey = outputLine.substring(equalsIndex+1, closeParenIndex).trim();
					continue;
				}
				
				if(outputLine.equals("END NETWORK FOLDERS ENTRY")) {
					this.singleNetworkFolderNamesMap.put(studyNameKey, currentConfig);
					continue;
				}
				currentConfig.add(outputLine.trim());
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "loadNetworkFolderNamesConfig()...exit.");	
		
	}
	
	
	private void loadNetworkFolderNamesConfigOld() {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadNetworkFolderNamesConfig()...invoked.");

		File file = new File(NETWORK_FOLDERS_CONFIG_PATH);
		
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				if(outputLine.trim().length() < 3) {
					continue;
				}
				if(outputLine.startsWith("#")) {
					continue;
				}
				if(outputLine.contains("END NETWORK FOLDERS ENTRY")) {
					outputLine += "&&";
				}
				this.networkFolderNamesConfig.add(outputLine.trim() + NEW_LINE);
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadNetworkFolderNamesConfig()...exit.");
	
	
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
		// we only cache abcd template matching study data
		if(networkNamePath.contains("abcd_template_matching")) {
			LOGGER.info(loggerId + "loadBase64ImagePathStrings()...adding base564ImageStrings array to cache, key=" + networkNamePath);
			this.base64NetworkImageStrings.put(networkNamePath, base64ImageStrings);
			//this.binaryNetworkImageBuffers.put(networkNamePath, imageByteBuffers);
		}
		LOGGER.trace(loggerId + "loadBase64ImagePathStrings()...exit.");
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
			//LOGGER.trace(loggerId + imagePathNames);
			this.imagePathNames.put(networkNamePath, imagePathNames);
			if(networkNamePath.contains("abcd_template_matching")) {
				this.loadBase64ImagePathStrings(networkNamePath);
			}
		}
		
		LOGGER.trace(loggerId + "getImagePathNames()...exit.");
		return imagePathNames;
	}
	
	public ArrayList<String> getMenuStudyNames() {
		synchronized(configLock) {
			return this.menuStudyNames;
		}
	}
	
	public Hashtable<String, ArrayList<String>> getMenuOptionsMap() {
		synchronized(configLock) {
			return this.menuChoicesMap;
		}
	}
	
	public ArrayList<String> getNeuralNetworkFolderNamesConfig() {
		
		return this.networkFolderNamesConfig;
	}
	

	public ArrayList<String> getPrivilegedList() {
		return this.privilegedIPs;
	}
	
	private void addNetworkMapImage(String networkPath, ArrayList<String> imagePathNames) {
		
		String probabilityMapImagePath = imagePathNames.remove(0);
		byte[] fileBytes = DirectoryAccessor.getFileBytes(probabilityMapImagePath);
		String base64EncodedString = Base64.getEncoder().encodeToString(fileBytes);
		String networkMapNifiFilePath = DirectoryAccessor.getNetworkMapNiiFilePath(networkPath);
		NetworkMapData nmd = new NetworkMapData(networkPath, base64EncodedString, networkMapNifiFilePath);
        this.networkMapDataCache.put(networkPath, nmd);
	}
	
	public NetworkMapData getNetworkMapData(String networkName) {
		return this.networkMapDataCache.get(networkName);
	}
	
	public void reloadNetworkFoldersConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadNetworkFoldersConfig()...invoked.");
		
			this.networkFolderNamesConfig = new ArrayList<String>();
			loadNetworkFolderNamesConfig();
			LOGGER.trace(loggerId + "reloadNetworkFoldersConfig()...exit.");
		}
	}
	
	public void reloadConfigs() {
		synchronized(configLock) {
			this.reloadMenuConfig();
			this.reloadSummaryConfig();
			this.reloadNetworkFoldersConfig();
		}
	}
	
	public void reloadMenuConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadMenuConfig()...invoked.");
		
			this.menuStudyNames = new ArrayList<String>();
			this.menuChoicesMap = new Hashtable<String, ArrayList<String>>();
			loadMenuConfig();
			LOGGER.trace(loggerId + "reloadMenuConfig()...exit.");
		}
	}
	
	public void reloadSummaryConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadSummaryConfig()...invoked.");
		
			this.summaryStudyNames = new ArrayList<String>();
			this.summaryEntriesMap = new Hashtable<String, ArrayList<String>>();
			loadSummaryConfig();
			LOGGER.trace(loggerId + "reloadSummaryConfig()...exit.");
		}
	}
	
	public void loadSummaryConfig() {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadSummaryConfig()...invoked.");

		File file = new File(SUMMARY_CONFIG_PATH);
		boolean summaryEntryNamePending = true;
		boolean summarySubEntriesPending = false;
		String summaryStudyName = null;
		String summarySubEntry = null;
		ArrayList<String> summarySubEntries = null;
		int beginIndex = 0;
		int endIndex = 0;
		
		
		if(!file.exists()) {
			LOGGER.fatal("Config file not found:" + SUMMARY_CONFIG_PATH);
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null) {
				if(outputLine.trim().length() == 0) {
					continue;
				}
				if(outputLine.contains("BEGIN SUMMARY")) {
					summaryEntryNamePending = true;
				}
				if(outputLine.contains("END SUMMARY")) {
					summarySubEntriesPending = false;
					continue;
				}
				
				if(summaryEntryNamePending) {
					beginIndex = outputLine.indexOf("=");
					endIndex = outputLine.indexOf(")");
					summaryStudyName = outputLine.substring(beginIndex+1, endIndex);
					this.summaryStudyNames.add(summaryStudyName);
					summaryEntryNamePending = false;
					summarySubEntriesPending = true;
					summarySubEntries = new ArrayList<String>();
					this.summaryEntriesMap.put(summaryStudyName, summarySubEntries);
					continue;
				}
				else if(summarySubEntriesPending) {
					summarySubEntry = outputLine;
					summarySubEntries.add(summarySubEntry);
				}
				else if(outputLine.contains("END SUMMARY")) {
					summarySubEntriesPending = false;
				}
			}
			
			Enumeration<String> keys = this.summaryEntriesMap.keys();
			String currentKey = null;
			ArrayList<String> summarySubChoices = null;
			
			while(keys.hasMoreElements()) {
				currentKey = keys.nextElement();
				summarySubChoices = this.summaryEntriesMap.get(currentKey);
				//LOGGER.trace(loggerId + "summaryDetails->>" + currentKey + ":" + summarySubChoices);
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadSummaryConfig()...exit.");
	}
	
	public ArrayList<String> getSummaryStudyNames() {
		return this.summaryStudyNames;
	}
	
	public Hashtable<String, ArrayList<String>> getSummaryEntriesMap() {
		return this.summaryEntriesMap;
	}

	public String getLocalHostName() {
		return localHostName;
	}

	public void setLocalHostName(String localHostName) {
		this.localHostName = localHostName;
	}
	
	public void loadKeyFromFile() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadKeyFromFile()...invoked.");

		boolean shouldContinue = true;
		String key = null;
		String[] keyArray = null;
		
		File file = new File(KEY_CONFIG_PATH);
		
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null && shouldContinue) {
				
				if(outputLine.trim().length() == 0) {
					continue;
				}
				if(outputLine.startsWith("#")) {
					continue;
				}
				if(outputLine.contains("key")) {
					keyArray = outputLine.split("=");
					key = keyArray[1];
					shouldContinue = false;
				}
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		EmailNotifier.setKey(key);
		
		LOGGER.trace(loggerId + "loadKeyFromFile()...exit.");
	
	}
	
	public void loadSettingsConfig() {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadSettingsConfig()...invoked.");

		String concatenatedUP = null;
		String[] upArray = null;
		boolean shouldContinue = true;
		
		int count = 0;
		File file = new File(SETTINGS_CONFIG_PATH);
		
		if(!file.exists()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			while ((outputLine = br.readLine()) != null && shouldContinue) {
				
				if(outputLine.trim().length() == 0) {
					continue;
				}
				if(outputLine.startsWith("#")) {
					continue;
				}

				if(outputLine.contains("MIDB_SERIALIZATION")) {
					String[] rawArray = outputLine.trim().split("=");
					concatenatedUP = rawArray[1];
					upArray = concatenatedUP.split("::");
					String sender = upArray[0];
					String password = upArray[1];
					EmailNotifier.setPassword(password);
					EmailNotifier.setSender(sender);
				}
				else if(outputLine.contains("MIDB_ROOT")) {
					String[] rawArray = outputLine.trim().split("=");
					String key = rawArray[1];
					EmailNotifier.setKey(key);
					TokenManager.setKey(key);
				}
				else if(outputLine.contains("MIDB_VERSION")) {
					String[] rawArray = outputLine.trim().split("=");
					String recipient = rawArray[1];
					EmailNotifier.setRecipient(recipient);
				}
				else if(outputLine.contains("MIDB_MRI")) {
					String[] rawArray = outputLine.trim().split("=");
					String password = rawArray[1];
					TokenManager.setPassword(password);
				}
				
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error("Unable to process settings.conf");
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadSettingsConfig()...exit.");
	}
	
	public ArrayList<String> getNeuralNetworkFolderNamesConfig(String studyName) {
		return this.singleNetworkFolderNamesMap.get(studyName);
	}
	
}
