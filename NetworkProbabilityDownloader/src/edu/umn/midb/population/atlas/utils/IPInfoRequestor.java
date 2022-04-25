package edu.umn.midb.population.atlas.utils;

import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.tasks.TaskEntry;
import io.ipinfo.api.IPInfo;
import io.ipinfo.api.IPInfoBuilder;
import io.ipinfo.api.errors.RateLimitedException;
import io.ipinfo.api.model.IPResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for retrieving geo-location data for an ip address. This is a service
 * of ipinfo.io
 * 
 * 
 * @author jjfair
 *
 */
public class IPInfoRequestor {
	
	
	private static final String LOGGER_ID = "::LOGGERID=IPInfoRequestor:: ";
	private static final Logger LOGGER = LogManager.getLogger(IPInfoRequestor.class);
	private static int ERROR_REPORT_COUNT = 0;
	private static int SMS_NOTIFY_COUNT = 0;
	private static String KEY = null;
	private static String TOKEN = null;
	
	
	/**
	 * Retrieves geo-location data for the requestorIPAddress contained in the {@link TaskEntry}
	 * instance. This information is used to set the relevant TaskEntry attributes such as latitude, and
	 * longitude.
	 * 
	 * @param tEntry - {@link TaskEntry}
	 */
	public static void getIPInfo(TaskEntry tEntry) {
		
        IPInfo ipInfo = new IPInfoBuilder()
                .setToken(TOKEN)
                .build();
        
        try {
        	String ipAddressString = tEntry.getRequestorIPAddress();
        	
			if(ipAddressString.equals("127.0.0.1")) {
				ipAddressString = "68.63.215.250";
				// 34.221.125.31   68.63.215.250
				tEntry.setRequestorIPAddress("68.63.215.250");
			}
			
            IPResponse ipResponse = ipInfo.lookupIP(ipAddressString);

            // Print out the hostname
            //System.out.println(response.getHostname());
            String city = ipResponse.getCity();
            String state = ipResponse.getRegion();
            String countryCode = ipResponse.getCountryCode();
            String countryName = CountryNamesResolver.getInstance().getCountryName(countryCode);
            String latitude = ipResponse.getLatitude();
            String longitude = ipResponse.getLongitude();
            tEntry.setCity(city);
            tEntry.setState(state);
            tEntry.setCountry(countryName);
            tEntry.setLatitude(latitude);
            tEntry.setLongitude(longitude);
            
            if(city==null && SMS_NOTIFY_COUNT<4) {
            	String domainName = NetworkProbabilityDownloader.getDomainName();
        		String message = "MIDB_APP_IPINFO_NULL::::" + domainName + "::::IP=" + ipAddressString;
        		SMSNotifier.sendNotification(message, "NetworkProbabilityDownloader");
        		updateNotifyCount();
            }
                        
        } 
        catch (RateLimitedException ex) {
            LOGGER.error(LOGGER_ID + "getIPInfo()...RateLimitException encountered");
            if(ERROR_REPORT_COUNT == 0) {
            	DiagnosticsReporter.createDiagnosticsEntry(ex);
            	updateErrorCount();
            }
        }
        catch(Exception e) {
        	LOGGER.error(LOGGER_ID + "getIPInfo()...error getting ip info.");
        	LOGGER.error(e.getMessage(), e);
            if(ERROR_REPORT_COUNT == 0) {
            	DiagnosticsReporter.createDiagnosticsEntry(e);
            	updateErrorCount();
            }
        }        
	}
	
	/**
	 * Initializes the authorization token necessary for retrieving data through the
	 * web service.
	 * 
	 * @param key - encryption key
	 * @param encToken - encrypted authorization token
	 */
	public static void initAuthentication(String key, String encToken) {
		LOGGER.trace(LOGGER_ID + "initAuthentication()...invoked.");
		KEY = key;
		TOKEN = Utils.convertJcpyt(encToken, KEY);
	}
	
	/**
	 * Updates the ERROR_REPORT_COUNT which is used to prevent redundant invocations
	 * of {@link DiagnosticsReporter#createDiagnosticsEntry(Exception)}
	 * 
	 */
	private static synchronized void updateErrorCount() {
		ERROR_REPORT_COUNT++;
	}
	
	/**
	 * Updates the SMS_NOTIFY_COUNT which is used to prevent redundant invocations
	 * of {@link SMSNotifier#sendNotification(String, String)}
	 * 
	 */
	private static synchronized void updateNotifyCount() {
		SMS_NOTIFY_COUNT++;
	}

}
