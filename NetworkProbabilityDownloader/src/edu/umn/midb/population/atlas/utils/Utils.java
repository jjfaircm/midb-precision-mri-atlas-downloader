package edu.umn.midb.population.atlas.utils;

import java.util.Random;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import edu.umn.midb.population.atlas.data.access.DBManager;
import logs.ThreadLocalLogTracker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Encapsulates miscellaneous functionality.
 * 
 * @author jjfair
 *
 */
public class Utils {
	
	private static Logger LOGGER = null;
	private static String LOGGER_ID = " ::LOGGERID=Utils:: ";

	static {
		LOGGER = LogManager.getLogger(Utils.class);
	}

	/**
	 * Returns non-encrypted value of an encrypted String.
	 * 
	 * @param encrypted - String
	 * @param strkey - String representing the encryption key
	 * @return decrypted - String
	 */
	public static String convertJcpyt(String encrypted, String strkey) {
		LOGGER.trace(LOGGER_ID + "convertJcpyt()...invoked");
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(strkey);

		String decrypted = encryptor.decrypt(encrypted);
		LOGGER.trace(LOGGER_ID + "convertJcpyt()...exit");
		return decrypted;
	}
		
	/**
	 * Encrypts a string
	 * 
	 * @param to_encrypt - String to encrypt
	 * @param strkey - encryption key
	 * @return encrypted - String representing the encrypted value
	 */
	public static String encryptJsypt(String to_encrypt, String strkey) {
		LOGGER.trace(LOGGER_ID + "encryptJsypt()...invoked");

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(strkey);
		String encrypted= encryptor.encrypt(to_encrypt);
		LOGGER.trace(LOGGER_ID + "encryptJsypt()...exit");
		return encrypted;
	}
	
	/**
	 * Executes the Thread.sleep(long) method
	 * 
	 * @param pauseMS - long
	 */
	public static void pause(long pauseMS) {
		String loggerId = ThreadLocalLogTracker.get();
		//LOGGER.trace(loggerId + "pause()...invoked.");

		try {
			Thread.sleep(pauseMS);
		}
		catch(InterruptedException iE) {
			LOGGER.trace(loggerId + "pause()...caught exception...");
			LOGGER.error(iE.getMessage(), iE);
		}
		//LOGGER.trace(loggerId + "pause()...exit...");
	}
	
	/**
	 * Parses a setting entry
	 * 
	 * @param entry - String
	 * @return returnArray - String[]
	 */
	public static String[] parseSettingEntry(String entry) {
		
		boolean endsWithEqual = false;
		
		if(entry.endsWith("=")) {
			endsWithEqual = true;
		}
		
		String[] originalArray = entry.split("=");
		String[] returnArray = null;
		
		if(!endsWithEqual &&  originalArray.length==2) {
			returnArray = originalArray;
		}
		
		else {
			int index = entry.indexOf("=");
			String entryKey = entry.substring(0, index);
			String entryValue = entry.substring(index+1);
			returnArray = new String[] {entryKey, entryValue};
		}
		
		return returnArray;
		
	}

}
