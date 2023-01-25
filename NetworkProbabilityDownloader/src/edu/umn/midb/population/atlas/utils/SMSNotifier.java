package edu.umn.midb.population.atlas.utils;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.Account.Status;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import logs.ThreadLocalLogTracker;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Arrays;


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
	//NOTE: The phone numbers must always begin with a '+' sign, followed
	//      by the country code.
	private static  String TO_PHONE_NUMBER = null;
	private static  String FROM_PHONE_NUMBER = null;
	
	private static  String ACCOUNT_SID_ENC = null;
	private static  String AUTH_TOKEN_ENC =  null;
	private static  String TO_PHONE_NUMBER_ENC = null;
	private static  String FROM_PHONE_NUMBER_ENC = null;
	private static  String TEXT_BELT_KEY = null;
	private static  String SEND_MODE = "TWILIO";
	
	private static String localHostName = null;
	
	public static String ENCRYPTION_KEY = null;
	
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
	 * Sets the ACCOUNT_SID which represents the twilio account used to send
	 * SMS notifications.
	 * 
	 * @param accSidE - the encrypted account sid (type is String)
	 */
	public static void setAccountSIDE(String accSidE) {
		LOGGER.trace(LOGGER_ID + "setAccountSIDE()...invoked");
		ACCOUNT_SID_ENC = accSidE;
		ACCOUNT_SID = Utils.convertJcpyt(ACCOUNT_SID_ENC, ENCRYPTION_KEY);
		LOGGER.trace(LOGGER_ID + "setAccountSIDE()...exit");
	}
	
	/**
	 * 
	 * Sets the AUTH_TOKEN used to authenticate with the twilio.com service
	 * 
	 * @param authTokenE - the encrypted authorization token
	 */
	public static void setAuthTokenE(String authTokenE) {
		LOGGER.trace(LOGGER_ID + "setAuthTokenE()...invoked");
		AUTH_TOKEN_ENC = authTokenE;
		AUTH_TOKEN = Utils.convertJcpyt(AUTH_TOKEN_ENC, ENCRYPTION_KEY);
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
		TO_PHONE_NUMBER = Utils.convertJcpyt(TO_PHONE_NUMBER_ENC, ENCRYPTION_KEY);
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
		FROM_PHONE_NUMBER = Utils.convertJcpyt(FROM_PHONE_NUMBER_ENC, ENCRYPTION_KEY);
		LOGGER.trace(LOGGER_ID + "setFromNumberE()...exit");
	}
	
	/**
	 * Sets the key used for encryption
	 * 
	 * @param key - String
	 */
	public static void setEncryptionKey(String key) {
		LOGGER.trace(LOGGER_ID + "setKey()...invoked");
		ENCRYPTION_KEY = key;
		LOGGER.trace(LOGGER_ID + "setKey()...exit");
	}
	
	public static void setSendMode(String mode) {
		LOGGER.trace(LOGGER_ID + "setSendMode()...invoked");
		SEND_MODE = mode;
		LOGGER.trace(LOGGER_ID + "setSendMode()...exit");
	}
	
	public static void setTextBeltKey(String encTextBeltKey) {
		LOGGER.trace(LOGGER_ID + "setTextBeltKey()...invoked");
		TEXT_BELT_KEY = Utils.convertJcpyt(encTextBeltKey, ENCRYPTION_KEY);
		LOGGER.trace(LOGGER_ID + "setTextBeltKey()...exit");
	}
	
	
	public static synchronized void sendNotification(String textMessage, String invokerClassName) {
		
		String domainName = NetworkProbabilityDownloader.getDomainName();
		if(domainName.contains("localhost") || domainName.contains("MacBook")) {
			LOGGER.trace(LOGGER_ID + "sendNotification()...not sending SMS because localhost is JAMESs-MacBook");
			return;
		}
		
		if(SEND_MODE.equalsIgnoreCase("TWILIO")) {
			sendViaTwilio(textMessage, invokerClassName);
		}
		else if(SEND_MODE.equalsIgnoreCase("TEXT_BELT")) {
			sendViaTextBelt(textMessage, invokerClassName);
		}

	}
	
	private static void sendViaTwilio(String textMessage, String invokerClassName) {

		LOGGER.trace(LOGGER_ID + "sendViaTwilio()...invoked, invoker=" + invokerClassName);
		
		try {
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
		}
		catch(ApiException e) {
			LOGGER.error(e.getMessage(), e);
			DiagnosticsReporter.createDiagnosticsEntry(e, false);
		}
		LOGGER.trace(LOGGER_ID + "sendViaTwilio()...exit");
	
	}
	
	private static void sendViaTextBelt(String message, String invokerClassName) {
		String loggerId = ThreadLocalLogTracker.get();
		if(loggerId == null) {
			loggerId = LOGGER_ID;
		}
		LOGGER.trace(loggerId + "sendViaTextBelt()...invoked.");
		
		LOGGER.trace(LOGGER_ID + "sendViaTextBelt()...invoked, invoker=" + invokerClassName);
		
		try {
			final NameValuePair[] data = {
				    new BasicNameValuePair("phone", TO_PHONE_NUMBER),
				    new BasicNameValuePair("message", message),
				    new BasicNameValuePair("key", TEXT_BELT_KEY)
				};
				HttpClient httpClient = HttpClients.createMinimal();
				HttpPost httpPost = new HttpPost("https://textbelt.com/text");
				httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(data)));
				HttpResponse httpResponse = httpClient.execute(httpPost);
	
				String responseString = EntityUtils.toString(httpResponse.getEntity());
				LOGGER.trace(LOGGER_ID + "responseString=" + responseString);
				
				JSONParser jParser = new JSONParser();
				JSONObject jsonResponeObject = (JSONObject) jParser.parse(responseString);
				String quotaRemaining = jsonResponeObject.get("quotaRemaining").toString();
				LOGGER.trace(loggerId + "sendNotification()...quotaRemaining=" + quotaRemaining);
				
				/*
				 * {"success":true,"textId":"356711651183367431","quotaRemaining":0}
				 */
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
			DiagnosticsReporter.createDiagnosticsEntry(e, false);
		}
		LOGGER.trace(loggerId + "sendViaTextBelt()...exit, invoker=" + invokerClassName);
	}


}
