package edu.umn.midb.population.atlas.utils;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.umn.midb.population.atlas.config.files.FileLocator;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Utility for mapping a country code received by {@link IPInfoRequestor#getIPInfo(edu.umn.midb.population.atlas.tasks.TaskEntry)}
 * to a country name. For example a parameter of 'US' will return 'United States'. The codes and mapped
 * country names are stored in a file named country_names.json which is stored in the same package as the
 * {@link FileLocator} class. This class adheres to the Singleton pattern.
 * 
 * @author jjfair
 *
 */
public class CountryNamesResolver {
	
	private JSONObject data = null;
	private static CountryNamesResolver instance = null;
	private static Logger LOGGER = null;
	private static final String LOGGER_ID = " ::LOGGER_ID=CountryNamesResolver:: ";
	private static final String COUNTRY_NAMES_FILE = "country_names.json";
	
	static {
		LOGGER = LogManager.getLogger(CountryNamesResolver.class);
	}
	
	//hide empty constructor
	private CountryNamesResolver() {
		
	}
	
	/**
	 * Returns the Singleton instance.
	 * 
	 * @return instance - {@link CountryNamesResolver}
	 */
	public static synchronized CountryNamesResolver getInstance() {
		if(instance == null) {
			instance = new CountryNamesResolver();
			instance.loadCountryNames();
		}
		return instance;
	}
	
	/**
	 * Loads the mapping of codes to country names from the country_names.json file.
	 * 
	 */
	private void loadCountryNames() {
		LOGGER.trace(LOGGER_ID + "loadCountryNames()...invoked.");
		try {
			
			String filePath = FileLocator.getPath(COUNTRY_NAMES_FILE);

			JSONParser parser = new JSONParser();
			this.data = (JSONObject) parser.parse(
		              new FileReader(filePath));
									
		}
		catch(Exception e) {
			LOGGER.fatal(LOGGER_ID + "loadCountryNames()...failed to load country_names.json");
			LOGGER.fatal(e.getMessage(), e);
			DiagnosticsReporter.createDiagnosticsEntry(e);
		}	
		LOGGER.trace(LOGGER_ID + "loadCountryNames()...exit.");
	}
	
	/**
	 * Returns the full country name mapped to country code parameter.
	 * 
	 * @param countryCode - String
	 * 
	 * @return countryName - String
	 */
	public String getCountryName(String countryCode) {
		if(countryCode == null) {
			return null;
		}
		String countryName = this.data.get(countryCode).toString();
		if(countryName == null) {
			countryName = "unknown";
		}
		return countryName;
	}

}
