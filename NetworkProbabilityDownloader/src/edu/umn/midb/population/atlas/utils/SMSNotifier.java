package edu.umn.midb.population.atlas.utils;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.Account.Status;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.TwiMLException;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import logs.ThreadLocalLogTracker;

import java.io.IOException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	//set true to disable download SMS notifications
	private static boolean NOTIFY_DOWNLOADS = false;
	//set true in midb_app.properties to disable all SMS notifications
	private static boolean DISABLE_SMS_NOTIFICATIONS = false;
	//Adding opt-out message to satisfy SMS delivery requirements in twilio
	private static String OPT_OUT_MSG = "::::Reply STOP to opt out";
	private static String TEXTBELT_REPLY_HOOK = "https://midbatlas.io/action=sms_reply";
	
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
		if(!TO_PHONE_NUMBER.startsWith("+1")) {
			if(TO_PHONE_NUMBER.startsWith("1")) {
				TO_PHONE_NUMBER = "+" + TO_PHONE_NUMBER;
			}
			else {
				TO_PHONE_NUMBER = "+1" + TO_PHONE_NUMBER;
			}
		}
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
		if(!FROM_PHONE_NUMBER.startsWith("+1")) {
			if(FROM_PHONE_NUMBER.startsWith("1")) {
				FROM_PHONE_NUMBER = "+" + FROM_PHONE_NUMBER;
			}
			else {
				FROM_PHONE_NUMBER = "+1" + FROM_PHONE_NUMBER;
			}
		}
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
	
	/**
	 * Sets the sendMode which indicates whether the twilio or textBelt service should be used
	 * to send SMS notifications.
	 * 
	 * @param mode - String
	 */
	public static void setSendMode(String mode) {
		LOGGER.trace(LOGGER_ID + "setSendMode()...invoked");
		SEND_MODE = mode;
		LOGGER.trace(LOGGER_ID + "setSendMode()...exit");
	}
	
	/**
	 * Sets the TEXT_BELT_KEY which is the api key required by textBelt
	 * 
	 * @param encTextBeltKey - String
	 */
	public static void setTextBeltKey(String encTextBeltKey) {
		LOGGER.trace(LOGGER_ID + "setTextBeltKey()...invoked");
		TEXT_BELT_KEY = Utils.convertJcpyt(encTextBeltKey, ENCRYPTION_KEY);
		LOGGER.trace(LOGGER_ID + "setTextBeltKey()...exit");
	}
	
	/**
	 * Sends an SMS notification
	 * @param textMessage - String
	 * @param invokerClassName - String
	 */
	public static synchronized void sendNotification(String textMessage, String invokerClassName) {
		
		LOGGER.trace(LOGGER_ID + "sendNotification()...invoked, invoker=" + invokerClassName);
		
		textMessage = textMessage + OPT_OUT_MSG;

		if(DISABLE_SMS_NOTIFICATIONS) {
			LOGGER.trace(textMessage);
			LOGGER.trace(LOGGER_ID + "sendNotification()...notifications disabled, exit");
			return;
		}
		if(SEND_MODE.equalsIgnoreCase("TWILIO")) {
			sendViaTwilio(textMessage, invokerClassName);
		}
		else if(SEND_MODE.equalsIgnoreCase("TEXT_BELT")) {
			sendViaTextBelt(textMessage, invokerClassName);
		}

		//satisfy message throttling limitation imposed by twilio for 'sole proprietor' account
		if(!textMessage.contains("FILE_DOWNLOAD")) {
			Utils.pause(2000);
		}
		LOGGER.trace(LOGGER_ID + "sendNotification()...exit");
	}
	
	/**
	 * Sends an SMS notification via the twilio service (visit https://twilio.com)
	 * @param textMessage - String
	 * @param invokerClassName - String
	 */
	private static void sendViaTwilio(String textMessage, String invokerClassName) {

		LOGGER.trace(LOGGER_ID + "sendViaTwilio()...invoked, invoker=" + invokerClassName);
		
		try {
			if(AUTH_TOKEN == null) {
				LOGGER.trace("Exiting because AUTH_TOKEN is null");
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
		LOGGER.trace(LOGGER_ID + "sendViaTwilio()...exit, invoker=" + invokerClassName);
	}
	
	/**
	 * Sends an SMS notification via the textBelt visit (see textbelt.com)
	 * @param message - String
	 * @param invokerClassName - String
	 */
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
				    new BasicNameValuePair("key", TEXT_BELT_KEY),
				    new BasicNameValuePair("replyWebhookUrl", TEXTBELT_REPLY_HOOK)
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
	
	/**
	 * Sets the boolean indicating if fileDownload events should trigger an SMS notification.
	 * 
	 * @param shouldNotify - boolean
	 */
	public static void setDownloadNotificationMode(boolean shouldNotify) {
		NOTIFY_DOWNLOADS = shouldNotify;
	}
	
	/**
	 * Returns a boolean indicating if file download events should trigger an SMS notification.
	 * 
	 * @return NOTIFY_DOWNLOADS - boolean
	 */
	public static boolean shouldNotifyDownloads() {
		return NOTIFY_DOWNLOADS;
	}
	
	public static void disableSMSNotifications(boolean disableFlag) {
		DISABLE_SMS_NOTIFICATIONS = disableFlag;
	}
	
    /**
     * This fulfills a requirement by twilio and SMS standards that require the ability for
     * notification recipients to reply with 'STOP' to prevent further messaging. This will
     * actually never happen in this application since we only send notifications to the registered
     * developer telephone number (which is specified in midb_app.properties in the /midb 
     * folder).
     * @param request - HttpServletRequest
     * @param response - HttpServletResponse
     */
	public static void handleSMSReceived(HttpServletRequest request, HttpServletResponse response) {
	    
		//If we were to receive requests coming from different telephone numbers, 
		//we could get the telephone number of the sender and the received message by
		//examining the following query parameters:
		//request.getParameter("From")  this is the sender's telephone number
		//request.getParameter("Body")  this is the message
		LOGGER.trace(LOGGER_ID + "handleSMSReceived()...invoked.");
		String message = "Confirmed!";
		Body messageBody = new Body.Builder(message).build();
		com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message.Builder().body(messageBody).build();
	    MessagingResponse twiml = new MessagingResponse.Builder().message(sms).build();

	    response.setContentType("application/xml");
	    
	    try {
	    	response.getWriter().print(twiml.toXml());
	    }
	    catch(TwiMLException tE) {
	    	LOGGER.error(tE.getMessage(), tE);
	    }
	    catch(IOException ioE) {
	    	LOGGER.error(ioE.getMessage(), ioE);
	    }
		LOGGER.trace(LOGGER_ID + "handleSMSReceived()...exit.");
	}


}
