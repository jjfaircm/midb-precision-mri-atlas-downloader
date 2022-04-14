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


public class IPInfoRequestor {
	
	
	private static final String LOGGER_ID = "::LOGGERID=IPInfoRequestor:: ";
	private static final Logger LOGGER = LogManager.getLogger(IPInfoRequestor.class);
	private static int ERROR_REPORT_COUNT = 0;
	private static int SMS_NOTIFY_COUNT = 0;
	private static String KEY = null;
	private static String TOKEN = null;
	
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
	
	public static void initAuthentication(String key, String encToken) {
		LOGGER.trace(LOGGER_ID + "initAuthentication()...invoked.");
		KEY = key;
		TOKEN = Utils.convertJcpyt(encToken, KEY);
	}
	
	private static synchronized void updateErrorCount() {
		ERROR_REPORT_COUNT++;
	}
	
	private static synchronized void updateNotifyCount() {
		SMS_NOTIFY_COUNT++;
	}
	
	public static void main(String args[]) {
		
		CountryNamesResolver.getInstance();
		
		TaskEntry tEntry = new TaskEntry();
		tEntry.setRequestorIPAddress("66.249.70.100");
		getIPInfo(tEntry);
		System.out.println(tEntry.getLatitude());

	}

}
