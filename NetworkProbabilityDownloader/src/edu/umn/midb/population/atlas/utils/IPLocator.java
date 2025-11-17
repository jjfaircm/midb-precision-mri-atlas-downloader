package edu.umn.midb.population.atlas.utils;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;

import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.tasks.TaskEntry;

/**
 * Utility for using the geolite.info web service for retrieving geo-location data for
 * an ip address. See maxmind.com for further information.
 * 
 * 
 * @author jjfair
 *
 */
public class IPLocator {
	
	private static Logger LOGGER = LogManager.getLogger(IPLocator.class);
	private static String LOGGER_ID = " ::LOGGERID=IPLocator:: ";
	private static String KEY = null;
	private static int ACCOUNT_ID = 0;
	private static String LICENSE_KEY = null;
	
	
	/**
	 * Initializes the {@link #ACCOUNT_ID} and {@link #LICENSE_KEY} that are necessary
	 * for using the geolite web service.
	 * 
	 * @param key - String
	 * @param encAccountId - encrypted account id
	 * @param encToken - encrypted license key
	 */
	public static void initAuthentication(String key, String encAccountId, String encToken) {
		LOGGER.trace(LOGGER_ID + "initAuthentication()...invoked");
		KEY = key;
		String accountIdString = Utils.convertJcpyt(encAccountId, KEY);
		ACCOUNT_ID = Integer.parseInt(accountIdString);
		LICENSE_KEY = Utils.convertJcpyt(encToken, KEY);
		LOGGER.trace(LOGGER_ID + "initAuthentication()...exit");
	}
	
	public static void locateIP(TaskEntry tEntry) {
	//public static void locateIP(String ipAddressString) {
		
		LOGGER.trace(LOGGER_ID + "locateIP()...invoked");
		String ipAddressString = tEntry.getRequestorIPAddress();
				
		try {
			
			//for dev environment
			if(ipAddressString.equals("127.0.0.1")) {
				ipAddressString = "68.63.215.250";
				tEntry.setRequestorIPAddress("68.63.215.250");
			}

			InetAddress ipAddress = InetAddress.getByName(ipAddressString);
		
			WebServiceClient client = 
					new WebServiceClient.Builder(ACCOUNT_ID, LICENSE_KEY).host("geolite.info").build();
			CityResponse cityResponse = client.city(ipAddress);
			String cityName = cityResponse.getCity().getName();
			
			
			Subdivision subDiv = cityResponse.getLeastSpecificSubdivision();
			String subDivName = subDiv.getName();
			Country country = cityResponse.getCountry();
			Postal postal = cityResponse.getPostal();
			postal.toString();
			Location location = cityResponse.getLocation();
			double dLatitude = location.getLatitude();
			String sLatitude = dLatitude + "";
			double dLongitude = location.getLongitude();
			String sLongitude = dLongitude + "";
			String countryName = country.getName();
			tEntry.setCity(cityName);
			tEntry.setState(subDivName);
			tEntry.setCountry(countryName);
			tEntry.setLatitude(sLatitude);
			tEntry.setLongitude(sLongitude);
		}
		catch(Exception e) {
			LOGGER.error(LOGGER_ID + "locateIP()...error calling geoLite IP Locator web service");
			LOGGER.error(LOGGER_ID + "ipAddressString=" + ipAddressString);
			LOGGER.error(e.getMessage(), e);
			DiagnosticsReporter.createDiagnosticsEntry(e);
		}
		LOGGER.trace(LOGGER_ID + "locateIP()...exit");
	}

}
