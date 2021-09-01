package edu.umn.midb.population.atlas.utils;

import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class TokenManager {

	private String currentToken = null;
	private long expirationTime = 0;
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
	
	
	
	public boolean validateToken(String token, String password, String ipAddress) {
		boolean isValid = false;
		
		if(!this.privilegedList.contains(ipAddress)) {
			return false;
		}
		
		if(token.contentEquals(token)) {
			if(System.currentTimeMillis() < this.expirationTime) {
				if(password.contentEquals("wrinkledeggs")) {
					isValid = true;
					this.currentToken = "";
				}
			}
		}
		
		return isValid;
	}
}
