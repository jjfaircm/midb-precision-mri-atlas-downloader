package edu.umn.midb.population.atlas.data.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.menu.NetworkMapData;
import edu.umn.midb.population.atlas.security.TokenManager;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.IPInfoRequestor;
import edu.umn.midb.population.atlas.utils.IPLocator;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import edu.umn.midb.population.atlas.utils.Utils;
import logs.ThreadLocalLogTracker;

/**
 * Stores and manages various caches of .png files associated with the probabilistic thresholds for all
 * the different neural network types.  This prevents a scenario where multiple simultaneous requests
 * from different web clients would have to construct their own individual buffers of image data.
 * Additionally, a performance benefit is achieved since the base64 encoded data is created only once
 * for any given neural network type. After the first request for a neural network type, subsequent requests
 * are fulfilled by retrieving the data from the cache.
 * 
 * Additionally, this class loads the menu configuration entries from the menu.conf, netork_folder_names.conf,
 * and the summary.conf files. These files provide the data needed for the javascript component in the browser
 * client to dynamically build the study menu that is displayed.
 * 
 * @author jjfair
 *
 */
public class AtlasDataCacheManager {
	
	//the Singleton instance
	private static AtlasDataCacheManager instance = null;
	private static Logger LOGGER = null;
	private static String DEFAULT_LOGGER_ID = " ::LOGGERID=AtlasDataCacheManager_Init:: ";
	private static final String MENU_CONFIG_PATH = "/midb/menu.conf";
	private static final String ACL_CONFIG_PATH = "/midb/acl.conf";
	private static final String SUMMARY_CONFIG_PATH = "/midb/summary.conf";
	private static final String NETWORK_FOLDERS_CONFIG_PATH = "/midb/network_folder_names.conf";
	private static final String NEW_LINE = "\n";
	
	//contains collection of base64NetworkImageStrings keyed by the different 
	//neural network types
	private Hashtable<String, ArrayList<String>> base64NetworkImageStringsCache = new Hashtable<String, ArrayList<String>>();
	//private Hashtable<String, ArrayList<byte[]>> binaryNetworkImageBuffers = new Hashtable<String, ArrayList<byte[]>>();
	private Hashtable<String, ArrayList<String>> imagePathNamesCache = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, NetworkMapData> networkMapDataCache = new Hashtable<String, NetworkMapData>();
	//the menuStudyName is actually the 2nd line in a menuEntry which are stored in the menu.conf file
	//the menuStudyName contains the menuDisplayName, the menuId, and the types of data contained such as surface, volume, or surface_volume
	//here is an example of the complete menuStudyName:
	// ABCD - Template Matching (abcd_template_matching) (surface)
	private ArrayList<String> menuStudyComplexNames = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> menuChoicesCache = new Hashtable<String, ArrayList<String>>();
	private ArrayList<String> summaryStudyNames = new ArrayList<String>();
	private ArrayList<String> menuStudyIds = new ArrayList<String>();
	private Hashtable<String, ArrayList<String>> summaryEntriesCache = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, ArrayList<String>> singleNetworkFolderNamesCache = new Hashtable<String, ArrayList<String>>();


	private String localHostName = null;

	private String key = null;
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
	 * Hide constructor to enforce Singleton pattern.
	 * 
	 */
	private AtlasDataCacheManager() {
		
	}
	
	/**
	 * Creates and caches a {@link NetworkMapData} instance for a given network path. This stores
	 * the base64-encoded data for the .png image file of the probabilityMap image, the absolute
	 * path to the dscalar.nii file, and the network name which also is an absolute path representing
	 * the folder on the server holding the data.
	 * 
	 * @param networkPath - String
	 * @param imagePathNames - ArrayList
	 */
	private void addNetworkMapImage(String networkPath, ArrayList<String> imagePathNames) {
		
		String probabilityMapImagePath = imagePathNames.remove(0);
		byte[] fileBytes = DirectoryAccessor.getFileBytes(probabilityMapImagePath, null);
		String base64EncodedString = Base64.getEncoder().encodeToString(fileBytes);
		String networkMapNifiFilePath = DirectoryAccessor.getNetworkMapNiiFilePath(networkPath);
		NetworkMapData nmd = new NetworkMapData(networkPath, base64EncodedString, networkMapNifiFilePath);
        this.networkMapDataCache.put(networkPath, nmd);
	}
	
	/**
	 * Returns an ArrayList of base64 encoded Strings for all the .png files associated with the probabilistic thresholds for a specific neural network.
	 * @param networkNamePath The name of the selected neural network type
	 * 
	 * @return ArrayList of base64 encoded .png image files
	 */
	public ArrayList<String> getBase64ImagePathStrings(String networkNamePath) {
		
		ArrayList<String> targetList = this.base64NetworkImageStringsCache.get(networkNamePath);
		if(targetList == null) {
			targetList = this.loadBase64ImagePathStrings(networkNamePath);
		}
		return targetList;
	}
	
	/**
	 * Returns a list of NII image paths/names for a given neural network. The networkNamePath
	 * represents the absolute path to the desired data. This path includes the study name.
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
		
		ArrayList<String> imagePathNames = this.imagePathNamesCache.get(networkNamePath);
		
		
		if(imagePathNames==null) {
			imagePathNames = DirectoryAccessor.getThresholdImagePaths(networkNamePath);
			if(networkMapImageExists) {
				this.addNetworkMapImage(networkNamePath, imagePathNames);
			}
			LOGGER.info(loggerId + " adding imagePathNames to cache, key=" + networkNamePath);
			LOGGER.trace(loggerId + "DirectoryAccessor.getThresholdImagePaths()...count=" + imagePathNames.size());

			// we cache the names, but only the base64-encoded image data for the abcd study
			this.imagePathNamesCache.put(networkNamePath, imagePathNames);
			if(networkNamePath.contains("abcd_template_matching")) {
				this.loadBase64ImagePathStrings(networkNamePath);
			}
		}
		
		LOGGER.trace(loggerId + "getImagePathNames()...exit.");
		return imagePathNames;
	}
	
	/**
	 * Returns the localHostName on which the web application is running.
	 * 
	 * @return localHostName - String
	 */
	public String getLocalHostName() {
		return localHostName;
	}
	
	
	/**
	 * Returns a map of menu options for each study. The map is keyed by
	 * the studyId and will return all the sub-options for that particular
	 * study in the menu shown in the browser. For example, abcd_template_matching
	 * would be the key and the sub-options would be: combined_clusters, overlapping,
	 * and single.
	 * 
	 * @return menuChoicesMap - A Hashtable of ArrayList of String
	 */
	public Hashtable<String, ArrayList<String>> getMenuOptionsMap() {
		synchronized(configLock) {
			return this.menuChoicesCache;
		}
	}

	/**
	 * Returns the 'complex' names of all configured studies. The complex name contains the
	 * display name, the id, and data type(s). An example is: 
	 * ABCD - Template Matching (abcd_template_matching) (surface)
	 * 
	 * @return menuStudyNames - ArrayList of String
	 */
	public ArrayList<String> getMenuStudyComplexNames() {
		synchronized(configLock) {
			return this.menuStudyComplexNames;
		}
	}
	
	/**
	 * Returns an instance of {@link NetworkMapData} from the networkMapDataCache
	 * keyed by the networkName such as /midb/studies/studyId/surface_or_volume/networkId
	 * 
	 * @param networkName - String
	 * @return networkMapData - {@link NetworkMapData}
	 */
	public NetworkMapData getNetworkMapData(String networkName) {
		return this.networkMapDataCache.get(networkName);
	}
	
	
	/**
	 * Returns the networkFolderNames config entry for a specific study.
	 * 
	 * @param studyName - String
	 * @return nfnConfig - ArrayList of String
	 */
	public ArrayList<String> getNeuralNetworkFolderNamesConfig(String studyName) {
		return this.singleNetworkFolderNamesCache.get(studyName);
	}
	
	public ArrayList<String> getPrivilegedList() {
		return this.privilegedIPs;
	}
	

	/**
	 * Returns the map of summaryEntries keyed by studyId.
	 * 
	 * @return - summaryEntriesMap - Hashtable of ArrayList of String
	 */
	public Hashtable<String, ArrayList<String>> getSummaryEntriesMap() {
		return this.summaryEntriesCache;
	}
	
	/**
	 * Returns the studyIDs for each entry in the /midb/summary.conf file
	 * 
	 * @return summaryStudyNames - ArrayList of String
	 */
	public ArrayList<String> getSummaryStudyNames() {
		return this.summaryStudyNames;
	}
	
	/**
	 * Initializes the {@link SMSNotifier} with the correct account sid, authorization token,
	 * 'from' telephone number, and 'to' telephone number. All the incoming parameters are
	 * encrypted.
	 * 
	 * @param accountSIDE - encrypted account sid
	 * @param authTokenE - encrypted authorization token
	 * @param toPhoneE - encrypted 'to' telephone number that receives the SMS messages
	 * @param fromPhoneE - encrypted 'from' telephone number which is a virtual number from twillio.com
	 * @param textBeltKey - encrypted api key for using textbelt.com service
	 * @param smsMode - String indicating if textbelt or twilio should be used for sms service
	 */
	private void initSMSNotifier(String accountSIDE, String authTokenE, String toPhoneE, String fromPhoneE,
			                     String textBeltKey, String smsMode) {
		LOGGER.trace(DEFAULT_LOGGER_ID + "initSMSNotifier()...invoked");
		
		boolean success = true;
		int successCount = 0;
		
		SMSNotifier.setEncryptionKey(key);
		
		if(accountSIDE != null) {
			SMSNotifier.setAccountSIDE(accountSIDE);
			successCount++;
		}
		if(authTokenE != null) {
			SMSNotifier.setAuthTokenE(authTokenE);
			successCount++;
		}
		if(toPhoneE != null) {
			SMSNotifier.setToNumberE(toPhoneE);
			successCount++;
		}
		if(fromPhoneE != null) {
			SMSNotifier.setFromNumberE(fromPhoneE);
			successCount++;
		}
		if(textBeltKey != null) {
			SMSNotifier.setTextBeltKey(textBeltKey);
		}
		if(smsMode != null) {
			SMSNotifier.setSendMode(smsMode);
		}
		

		LOGGER.trace(DEFAULT_LOGGER_ID + "initSMSNotifier()...exit");

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
	 * @return base64Strings Array of base64-encoded images
	 * 
	 */
	private ArrayList<String> loadBase64ImagePathStrings(String networkNamePath) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadBase64ImagePathStrings()...invoked, networkNamePath=" + networkNamePath);
		ArrayList<String> imagePaths = this.imagePathNamesCache.get(networkNamePath);
		ArrayList<byte[]> imageByteBuffers = new ArrayList<byte[]>();

		Iterator<String> imagePathsIt = imagePaths.iterator();
		String anImagePath = null;
		byte[] imageBuffer = null;
		
		while(imagePathsIt.hasNext()) {
			anImagePath = imagePathsIt.next();
			imageBuffer = DirectoryAccessor.getFileBytes(anImagePath, null);
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
			this.base64NetworkImageStringsCache.put(networkNamePath, base64ImageStrings);
			//this.binaryNetworkImageBuffers.put(networkNamePath, imageByteBuffers);
		}
		LOGGER.trace(loggerId + "loadBase64ImagePathStrings()...exit.");
		return base64ImageStrings;
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
		this.imagePathNamesCache.put(targetDirectory, imagePathNames);
		this.loadBase64ImagePathStrings(targetDirectory);
		LOGGER.trace(loggerId + "loadDefaultGlobalData()...loadBase64ImagePathStrings.size=" + this.base64NetworkImageStringsCache.size());
	}
	
	/**
	 * Loads the study menu entries from the /midb/menu.conf file.
	 * 
	 */
	private void loadMenuConfig() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadMenuConfig()...invoked.");

		File file = new File(MENU_CONFIG_PATH);
		boolean menuEntryNamePending = true;
		boolean menuSubEntriesPending = false;
		int beginIndex = 0;
		int endIndex = 0;
		String menuStudyId = null;
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
				if(outputLine.contains("MENU ENTRY") && outputLine.contains("ID=")) {
					beginIndex = outputLine.indexOf("=");
					endIndex = outputLine.indexOf(")");
					menuStudyId = outputLine.substring(beginIndex+1, endIndex);
					this.menuStudyIds.add(menuStudyId);
					//the menuId is just the shortId portion of the menuStudyName
					//the menuId is also the name of the subfolder containing the specific study
					//for example, the abcd_template_matching subfolder under the /midb/studies
					//folder contains the study which has an id of abcd_template_matching
					
					//menuEntryName a.k.a. menuStudyName is a complex name which contains the menuDisplayName, 
					//the menuId, and the types of data contained such as surface, volume, or surface_volume
					menuEntryNamePending = true;
					continue;
				}
				if(menuEntryNamePending) {
					//the menuStudyName contains the menuDisplayName, the menuId, and the types of data contained such as surface, volume, or surface_volume
					//here is an example of the complete menuStudyName:
					// ABCD - Template Matching (abcd_template_matching) (surface)
					menuStudyName = outputLine;
					this.menuStudyComplexNames.add(menuStudyName);
					menuEntryNamePending = false;
					menuSubEntriesPending = true;
					subMenuEntries = new ArrayList<String>();
					this.menuChoicesCache.put(menuStudyName, subMenuEntries);
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
			
			Collections.sort(this.menuStudyComplexNames);
			
			Enumeration<String> keys = this.menuChoicesCache.keys();
			String currentKey = null;
			ArrayList<String> subMenuChoices = null;
			
			while(keys.hasMoreElements()) {
				currentKey = keys.nextElement();
				subMenuChoices = this.menuChoicesCache.get(currentKey);
				LOGGER.trace(loggerId + "menuDetails->>" + currentKey + ":" + subMenuChoices);
			}
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadMenuConfig()...exit.");
	}
	
	/**
	 * Loads all the entries from the /midb/network_folder_names.conf.
	 * Each entry is a collection of lines in an ArrayList of String and
	 * stored in a map keyed by study ids.
	 * 
	 */
	private void loadNetworkFolderNamesConfig() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadNetworkFolderNamesConfig()...invoked.");

		File file = new File(NETWORK_FOLDERS_CONFIG_PATH);
		ArrayList<String> currentConfig = null;
		int equalsIndex = -1;
		int closeParenIndex = -1;
		String studyNameKey = null;
		
		if(!file.exists()) {
			LOGGER.fatal("Config file not found:" + NETWORK_FOLDERS_CONFIG_PATH);
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
					this.singleNetworkFolderNamesCache.put(studyNameKey, currentConfig);
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
	

	/**
	 * Loads the entries in the /midb/summary.conf file.
	 * 
	 */
	private void loadSummaryConfig() {

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
					this.summaryEntriesCache.put(summaryStudyName, summarySubEntries);
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
			
			br.close();
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadSummaryConfig()...exit.");
	}

	/**
	 * Reloads all the config entries
	 * 
	 */
	public void reloadConfigs() {
		synchronized(configLock) {
			this.reloadMenuConfig();
			this.reloadSummaryConfig();
			this.reloadNetworkFoldersConfig();
		}
	}
	
	/**
	 * Reloads the entries in the /midb/menu.conf file.
	 * 
	 */
	public void reloadMenuConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadMenuConfig()...invoked.");
		
			this.menuStudyIds = new ArrayList<String>();
			this.menuStudyComplexNames = new ArrayList<String>();
			this.menuChoicesCache = new Hashtable<String, ArrayList<String>>();
			loadMenuConfig();
			LOGGER.trace(loggerId + "reloadMenuConfig()...exit.");
		}
	}
	
	/**
	 * Reloads the entries in the /midb/network_folder_names.conf file.
	 * 
	 */
	public void reloadNetworkFoldersConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadNetworkFoldersConfig()...invoked.");
			loadNetworkFolderNamesConfig();
			LOGGER.trace(loggerId + "reloadNetworkFoldersConfig()...exit.");
		}
	}
	
	/**
	 * Reloads the entries in the /midb/summary.conf file.
	 * 
	 */
	public void reloadSummaryConfig() {
		synchronized(menuLock) {
			String loggerId = ThreadLocalLogTracker.get();
			LOGGER.trace(loggerId + "reloadSummaryConfig()...invoked.");
		
			this.summaryStudyNames = new ArrayList<String>();
			this.summaryEntriesCache = new Hashtable<String, ArrayList<String>>();
			loadSummaryConfig();
			LOGGER.trace(loggerId + "reloadSummaryConfig()...exit.");
		}
	}
	
	/**
	 * Sets the local host name.
	 * @param localHostName - String
	 */
	public void setLocalHostName(String localHostName) {
		this.localHostName = localHostName;
	}
	
	/**
	 * Returns the list of menu study ids.
	 * @return menuStudyIds - ArrayList<String>
	 */
	public ArrayList<String> getMenuStudyIds() {
		return this.menuStudyIds;
	}
	
	
} // end class
