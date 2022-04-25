package edu.umn.midb.population.atlas.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.tasks.DownloadTracker;
import logs.ThreadLocalLogTracker;

import java.util.Properties;

/**
 * @deprecated
 * 
 * Utility class for sending email notifications when error conditions are encountered.
 * This is deprecated due to changes in the google mail api which has been switched to
 * using oAuth. The function of notfications is now handled by the {@link SMSNotifier}
 * 
 * @author jjfair
 *
 */
public class EmailNotifier {
	
    static String sender = null;
    static String password = null;
    static String recipient = null;
    
    static String encryptedPassword = null;
    static String encryptedSender = null;
    static String encryptedRecipient = null;
    static String key = null;
    static Properties prop = null;
	private static Logger LOGGER = null;

    static {
		LOGGER = LogManager.getLogger(EmailNotifier.class);
		LOGGER.trace("Setting EMAIL_ARGS");
        prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
    }
    
    /**
     * @deprecated
     * 
     * Kept just for coding sample purposes.
     * 
     * @param notificationText - String
     */
    public static void sendEmailNotification(String notificationText) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendEmailNotification()...invoked.");
		LOGGER.warn(loggerId + "sendEmailNotification()...DISABLED DUE TO GOOGLE ENFORCING oAUTH !!!!!!!!!!!!!!!!!!!!!!1");

    	/*
    	PasswordAuthentication pwdAuth = new PasswordAuthentication(sender, password);
    	 Session session = Session.getInstance(prop,
                 new javax.mail.Authenticator() {
                     protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication(sender, password);
                     }
                 });
    	 
    	    try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(sender));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(recipient)
                );
                message.setSubject("MIDB ERROR NOTIFICATION");
                message.setText(notificationText);

                Transport.send(message);

            } catch (MessagingException e) {
            	LOGGER.trace(loggerId + "Error sending email notification");
            	LOGGER.trace(e.getMessage(), e);
            }
            */
    		LOGGER.trace(loggerId + "sendEmailNotification()...exit.");
    }
    
    public static void setKey(String envKey) {
    	LOGGER.trace("setKey()...invoked");
    	key = envKey;
    	if(password == null && encryptedPassword != null) {
    		password = Utils.convertJcpyt(encryptedPassword, key);
    	}
    	if(sender == null && encryptedSender != null) {
    		sender = Utils.convertJcpyt(encryptedSender, key);
    		sender += "@gmail.com";
    	}
    	if(recipient == null && encryptedRecipient != null) {
    		recipient = Utils.convertJcpyt(encryptedRecipient, key);
    		recipient += "@gmail.com";
    	}
    	LOGGER.trace("setKey()...exit");
    }
    
    public static void setPassword(String encrPassword) {
    	LOGGER.trace("setPassword()...invoked");
    	encryptedPassword = encrPassword;
    	if(key != null) {
    		password = Utils.convertJcpyt(encrPassword, key);
    	}
    	LOGGER.trace("setPassword()...exit");
    }
    
    public static void setRecipient(String encRecipient) {
    	LOGGER.trace("setRecipient()...invoked");
    	encryptedRecipient = encRecipient;
    	if(key != null) {
    		recipient = Utils.convertJcpyt(encRecipient, key);
    		recipient += "@gmail.com";
    	}
    	LOGGER.trace("setRecipient()...exit");
    }
    
    public static void setSender(String encSender) {
    	LOGGER.trace("setSender()...invoked");
    	encryptedSender = encSender;
    	if(key != null) {
    		sender = Utils.convertJcpyt(encSender, key);
    		sender += "@gmail.com";
    	}
    	LOGGER.trace("setSender()...exit");
    }
    
}