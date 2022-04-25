package edu.umn.midb.population.atlas.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.Account.Status;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for sending SMS notifications via an account on twillio.com
 * 
 * @author jjfair
 *
 */
public class SMSNotifier  {
	
	private static Logger LOGGER = null;
	private static String LOGGER_ID = " ::LOGGERID=SMSNotifier:: ";

	private static  String ACCOUNT_SID = null;
	private static  String AUTH_TOKEN =  null;
	private static  String TO_PHONE_NUMBER = null;
	private static  String FROM_PHONE_NUMBER = null;
	
	private static  String ACCOUNT_SID_ENC = null;
	private static  String AUTH_TOKEN_ENC =  null;
	private static  String TO_PHONE_NUMBER_ENC = null;
	private static  String FROM_PHONE_NUMBER_ENC = null;
	
	private static String localHostName = null;
	
	public static String KEY = null;
	
	static {
		LOGGER = LogManager.getLogger(SMSNotifier.class);
		
		try {
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    localHostName = addr.getHostName();
		    LOGGER.info("local machine name=" + localHostName);
		}
		catch (UnknownHostException ex) {
		    LOGGER.error(LOGGER_ID + "Hostname can not be resolved");
		    LOGGER.error(ex.getLocalizedMessage(), ex);
		}
	}

	
	/**
	 * 
	 * Sets the ACCOUNT_SID which represents the twillio account used to send
	 * SMS notifications.
	 * 
	 * @param accSidE - the encrypted account sid (type is String)
	 */
	public static void setAccountSIDE(String accSidE) {
		LOGGER.trace(LOGGER_ID + "setAccountSIDE()...invoked");
		ACCOUNT_SID_ENC = accSidE;
		ACCOUNT_SID = Utils.convertJcpyt(ACCOUNT_SID_ENC, KEY);
		LOGGER.trace(LOGGER_ID + "setAccountSIDE()...exit");
	}
	
	/**
	 * 
	 * Sets the AUTH_TOKEN used to authenticate with the twillio.com service
	 * 
	 * @param authTokenE - the encrypted authorization token
	 */
	public static void setAuthTokenE(String authTokenE) {
		LOGGER.trace(LOGGER_ID + "setAuthTokenE()...invoked");
		AUTH_TOKEN_ENC = authTokenE;
		AUTH_TOKEN = Utils.convertJcpyt(AUTH_TOKEN_ENC, KEY);
		LOGGER.trace(LOGGER_ID + "setAuthTokenE()...exit");
	}
	
	/**
	 * 
	 * Sets the telephone number to which SMS notifications are sent
	 * 
	 * @param toNumberE - encrypted 'to' telephone number
	 */
	public static void setToNumberE(String toNumberE) {
		LOGGER.trace(LOGGER_ID + "setToNumberE()...invoked");
		TO_PHONE_NUMBER_ENC = toNumberE;
		TO_PHONE_NUMBER = Utils.convertJcpyt(TO_PHONE_NUMBER_ENC, KEY);
		LOGGER.trace(LOGGER_ID + "setToNumberE()...exit");
	}
	
	/**
	 * Sets the 'from' telephone number used to send SMS notifications.
	 * This number is a virtual telephone number rented from twillio.com.
	 * 
	 * @param fromNumberE - encrypted 'from' telephone number
	 */
	public static void setFromNumberE(String fromNumberE) {
		LOGGER.trace(LOGGER_ID + "setFromNumberE()...invoked");
		FROM_PHONE_NUMBER_ENC = fromNumberE;
		FROM_PHONE_NUMBER = Utils.convertJcpyt(FROM_PHONE_NUMBER_ENC, KEY);
		LOGGER.trace(LOGGER_ID + "setFromNumberE()...exit");
	}
	
	/**
	 * Sets the key used for encryption
	 * 
	 * @param key - String
	 */
	public static void setKey(String key) {
		LOGGER.trace(LOGGER_ID + "setKey()...invoked");
		KEY = key;
		LOGGER.trace(LOGGER_ID + "setKey()...exit");
	}
	
	
	public static void sendNotification(String textMessage, String invokerClassName) {
		LOGGER.trace(LOGGER_ID + "sendNotification()...invoked, invoker=" + invokerClassName);
		String domainName = NetworkProbabilityDownloader.getDomainName();
		if(domainName.contains("localhost") || domainName.contains("JAMESs")) {
			LOGGER.trace(LOGGER_ID + "sendNotification()...not sending SMS because localhost is JAMESs-MacBook");
			return;
		}
		
		if(AUTH_TOKEN == null) {
			return;
		}
		
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(
                new PhoneNumber(TO_PHONE_NUMBER),
                //new com.twilio.type.PhoneNumber("+15005550006"),
                new PhoneNumber(FROM_PHONE_NUMBER),
                textMessage)
            .create();
		LOGGER.trace(LOGGER_ID + "sendNotification()...exit");
	}

	public static void main(String[] args) {
        Twilio.init("X", "X");
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+1X"),
                new com.twilio.type.PhoneNumber("+1X"),
                "Test at 3:06 AM")
            //.setStatusCallback(URI.create("https://midbatlas.io/NetworkProbabilityDownloader/MessageStatusReceiver"))
            .create();

        System.out.println(message.getStatus());
        System.out.println(message.getSid());
        System.out.println(message.getErrorMessage());
    }
	
}
