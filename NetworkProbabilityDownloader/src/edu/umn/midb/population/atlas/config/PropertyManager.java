package edu.umn.midb.population.atlas.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import logs.LogConfigurator;


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
	  
	private static Logger LOGGER = null;
	
	  static {
	      //to enable log4j2 initialization tracing/debug
	  	  //System.setProperty("log4j2.debug", "");
	      configureLOG4J2();
	      String loggerId = null;
	      //String loggerId = ThreadLocalLogTracker.get();
	      LOGGER = LogManager.getLogger(PropertyManager.class);
	      LOGGER.info(loggerId + "Class PropertyManager just loaded. Build date = ");
	}
	
	
	//private constructor
	private PropertyManager() {
		
	}
	
	  public static synchronized PropertyManager getInstance() {
		    if (instance == null) {
		        instance = new PropertyManager();
		    }
		    return instance;
    }
	  
	  /**
	   * Handles the dynamic configuration of log4j at runtime.
	   */
	  public static void configureLOG4J2() {
		  LogConfigurator.configureLOG4J2();
	  }
	  
}
