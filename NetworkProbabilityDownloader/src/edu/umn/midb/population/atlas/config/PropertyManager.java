package edu.umn.midb.population.atlas.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.data.access.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import edu.umn.midb.population.atlas.utils.Utils;
import logs.LogConfigurator;
import logs.ThreadLocalLogTracker;


/**
 * This class loads all the relevant property files utilized by the application.  It follows the
 * Singleton pattern so there is only one instance of this class for the application and is shared
 * by all sessions.  It loads all the property files when the single instance is constructed.  Each
 * property file is loaded and all the properties are stored in a Hashtable keyed by the name of
 * the application that the specific property file is relevant to.
 * 
 * 
 */
public final class PropertyManager {
	
	/** the singleton instance. */ 
	private static PropertyManager instance;
	private String encryptionKey = null;
	private String applicationPropertiesFile = "/midb/midb_app.properties";
	private Hashtable<String, String> applicationSettings = new Hashtable<String, String>();

	
	private static final String CONFIG_PROPERTIES_FILE_PATH = "/midb/app_config.properties";
	  
	private static Logger LOGGER = null;
	
	  static {
	      //to enable log4j2 initialization tracing/debug
	  	  //System.setProperty("log4j2.debug", "");
	      configureLOG4J2();
	      String loggerId = " ::LOGGERID=PropertyManager:: ";
	      //String loggerId = ThreadLocalLogTracker.get();
	      LOGGER = LogManager.getLogger(PropertyManager.class);
	      LOGGER.info(loggerId + "Class PropertyManager just loaded. Build date = " + NetworkProbabilityDownloader.BUILD_DATE);
	}
	
	
	/**
	 * Private constructor which is never utilized.
	 */
	private PropertyManager() {
		
	}
	
  /**
   * Returns the Singleton instance for the class.
   * 	  
   * @return PropertyManager
   */
  public static synchronized PropertyManager getInstance() {
	    if (instance == null) {
	        instance = new PropertyManager();
	        instance.loadSettingsConfig();
	    }
	    return instance;
    }
	  
  /**
   * Handles the dynamic configuration of log4j at runtime.
   */
  public static void configureLOG4J2() {
	  LogConfigurator.configureLOG4J2();
  }
  
  /**
   * Sets the encryption key
   * @param encryptionKey - String
   */
  public void setEncryptionKey(String encryptionKey) {
	  LOGGER.trace(NetworkProbabilityDownloader.DEFAULT_LOGGER_ID + "setEncryptionKey()...invoked");
	  this.encryptionKey = encryptionKey;
	  LOGGER.trace(NetworkProbabilityDownloader.DEFAULT_LOGGER_ID + "setEncryptionKey()...exit");
  }
  
  /**
   * Updates the specified propertyKey with the new value passed in.
   * @param appContext - {@link ApplicationContext}
   * @param propKey - String
   * @param propValue - String
   * @return success - boolean
   */
  public synchronized boolean updateConfigProperty(ApplicationContext appContext, String propKey, String propValue) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "updateConfigProperty()...invoked.");
		
		boolean success = true;
		String valueToStore = null;
		propKey = propKey.trim();
		propValue = propValue.trim();
		
		//if the property is either the 'from' or 'to' telephone number then remove
		//any digits or parenthesis around the area code
		//for example 1-(507)-555-1212 should be 15075551212
		if(propKey.equals("MIDB_TTP") || propKey.equals("MIDB_FTP")) {
			propValue = propValue.replace("-", "");
			propValue = propValue.replace("(", "");
			propValue = propValue.replace(")", "");
            //also make sure number starts with the digit 1
			if(!propValue.startsWith("1")) {
				propValue = "1" + propValue;
			}
		}
		
    	//all values requiring encryption should be encrypted before writing to props file
    	if(!propKey.equalsIgnoreCase("MIDB_SMS_MODE")) {
    		valueToStore = Utils.encryptJsypt(propValue, encryptionKey);
        }
    	
    	try {
	    	File propsFile = new File(applicationPropertiesFile);
	    	BufferedReader br = new BufferedReader(new FileReader(propsFile));
	    	
	    	ArrayList<String> existingProps = new ArrayList<String>();
	    	String propLine = null;
	    	
	    	while ((propLine = br.readLine()) != null) {
	    		if(propLine.startsWith("#")) {
	    			continue;
	    		}
	    		if(propLine.startsWith(propKey)) {
	    			propLine = propKey + "=" + valueToStore;
	    		}
	    		existingProps.add(propLine);
	    	}
	    	br.close();
	    	
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(propsFile));
	    	LocalDateTime localDateTime = LocalDateTime.now();
	    	DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a");
	    	String ldtString = FOMATTER.format(localDateTime);
	    	String updateComment = "#Last updated: " + ldtString;
	    	
	    	Iterator<String> propsIt = existingProps.iterator();
	    	String aProp = null;
	    	bw.write(updateComment);
	    	bw.newLine();
	    	
	    	while(propsIt.hasNext()) {
	    		aProp = propsIt.next();
	    		bw.write(aProp);
	    		bw.newLine();
	    	}
	    	bw.close();
    	}
    	catch(IOException ioE) {
    		LOGGER.error(loggerId + "Error updating /midb/midb_app.properties file.");
    		LOGGER.error(ioE.getMessage(), ioE);
    		appContext.setConfigErrorExists(true);
    		appContext.setConfigError(ioE.getMessage());
    		success = false;
    	}
    	
    	if(success) {
    		switch (propKey) {
    		
    		case  "MIDB_TTP":
    			SMSNotifier.setToNumberE(valueToStore);
    			break;
    		case  "MIDB_FTP":
    			SMSNotifier.setFromNumberE(valueToStore);
    			break;
    		case  "MIDB_TAC":
    			SMSNotifier.setAccountSIDE(valueToStore);
    			break;
    		case  "MIDB_TAU":
    			SMSNotifier.setAuthTokenE(valueToStore);
    			break;
    		case "MIDB_SMS_MODE":
    			SMSNotifier.setSendMode(valueToStore);
    		}
    		appContext.setConfigUpdateMessage("Config property successfully updated and stored.");
    	}
		
		LOGGER.trace(loggerId + "updateConfigProperty()...exit.");
		return success;
  }
  
  /**
   * Returns the current value for the specified configuration key
   * @param key - String
   * @return value - String
   */
  public String getApplicationConfigProperty(String key) {
	  LOGGER.trace(NetworkProbabilityDownloader.DEFAULT_LOGGER_ID + "getApplicationConfigProperty()...invoked");
	  LOGGER.trace(NetworkProbabilityDownloader.DEFAULT_LOGGER_ID + "getApplicationConfigProperty()...exit");
	  return this.applicationSettings.get(key);
  }
  
  /**
	 * Load settings from the /midb/midb_app.properties file. Most properties are
	 * in this file. However, some settings are stored in environment variables
	 * which are stored in the tomcat_service.conf file which are loaded via
	 * the tomcat.service unit (this applies to linux). The settings in the 
	 * environment variables are settings which would very seldom, if ever, be
	 * changed. Settings in the /midb/midb_app.properties are ones that may be
	 * changed via the Admin Console.
	 */
	public void loadSettingsConfig() {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "loadSettingsConfig()...invoked.");

		String concatenatedUP = null;
		String[] upArray = null;
		boolean shouldContinue = true;
		
		File file = new File(applicationPropertiesFile);
		
		if(!file.exists()) {
			return;
		}
		
		String key = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String outputLine = null;
			
			String propName = null;
			int endIndex = 0;
			String equalSign = "=";
			String[] rawArray = null;
			
			while ((outputLine = br.readLine()) != null && shouldContinue) {
				
				outputLine = outputLine.trim();
				
				if(outputLine.length() == 0) {
					continue;
				}
				if(outputLine.startsWith("#")) {
					continue;
				}
				endIndex = outputLine.indexOf(equalSign);
				propName = outputLine.substring(0, endIndex).trim();
				
				rawArray = Utils.parseSettingEntry(outputLine.trim());
				this.applicationSettings.put(propName, rawArray[1]);
		}
			
			br.close();
		}
		catch(Exception e) {
			LOGGER.error("Unable to process settings.conf");
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.trace(loggerId + "loadSettingsConfig()...exit.");
	}
	  
}
