package edu.umn.midb.population.atlas.security;

import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.utils.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.utils.Utils;
import logs.ThreadLocalLogTracker;


public class TokenManager {

	private static String password = null;
	private static String encPassword = null;
	private static String key = null;
	private String currentToken = null;
	private long expirationTime = 0;
	private boolean tokenExpired = false;
	private boolean accessDenied = false;
	private static Logger LOGGER = LogManager.getLogger(TokenManager.class);
	private static ArrayList<String> privilegedList = AtlasDataCacheManager.getInstance().getPrivilegedList();
	
	static {
		LOGGER.trace(privilegedList);
	}
	
	public TokenManager() {
		this.generateToken();
	}
	
	public void generateToken() {
			
		    int leftLimit = 97; // letter 'a'
		    int rightLimit = 122; // letter 'z'
		    int targetStringLength = 10;
		    Random random = new Random();

		    String token = random.ints(leftLimit, rightLimit + 1)
		      .limit(targetStringLength)
		      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		      .toString();

		    this.currentToken = token;
		    this.expirationTime = System.currentTimeMillis() + 30000;
	}
	
	public String getToken() {
		return this.currentToken;
	}
	
	
	
	public boolean validateToken(String token, String passwordParm, String ipAddress) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "validateToken()...invoked, ipAddress=" + ipAddress);

		boolean isValid = false;
		
		String localHostName = AtlasDataCacheManager.getInstance().getLocalHostName();
		if(localHostName.contains("JAMESs-MacBook-Pro")) {
			if(ipAddress.equals("127.0.0.1")) {
				isValid = true;
			}
		}
		
		else if(!this.privilegedList.contains(ipAddress)) {
			LOGGER.trace(loggerId + "validateToken()...ipAddress not in acl.conf.");
			this.accessDenied = true;
			return false;
		}
		
		if(this.currentToken==null) {
			return false;
		}
		
		isValid = false;
		
		if(this.currentToken.contentEquals(token)) {
			if(System.currentTimeMillis() < this.expirationTime) {
				if(passwordParm.contentEquals(password)) {
					isValid = true;
					this.currentToken = null;
				}
				else {
					LOGGER.trace(loggerId + "validateToken()...incorrect password:" + passwordParm);
				}
			}
			else {
				this.tokenExpired = true;
			}
		}
		LOGGER.trace(loggerId + "validateToken()...exit.");
		return isValid;
	}
	
	public boolean isTokenExpired() {
		return this.tokenExpired;
	}
	
	public boolean isAccessDenied() {
		return this.accessDenied;
	}
	
	public static void setKey(String keyString) {
		key = keyString;
		if(password==null && encPassword != null) {
			password = Utils.convertJcpyt(encPassword, key);
		}
	}
	
	public static void setPassword(String encryptedPassword) {
		encPassword = encryptedPassword;
		
		if(key != null) {
			password = Utils.convertJcpyt(encryptedPassword, key);
		}
	}
}
