package edu.umn.midb.population.atlas.base;

import edu.umn.midb.population.atlas.utils.TokenManager;

/**
 * Contains references to any objects that must be tracked for the life of the session.
 * 
 * @author jjfair
 *
 */
public class ApplicationContext { 
	
	private static final String LOGGER_ID_PREFIX = "::LOGGERID=";
	private String loggerId;
	private TokenManager tokenManager = null;
	private boolean adminActionValidated = false;

	/**
	 * Returns the loggerId associated with the current http session.  The loggerId may be
	 * used to identify all log entries associated with a specific user session.
	 * 
	 * @return String representing the current session's logger id
	 */
	public String getLoggerId() {
		return loggerId;
	}

	/**
	 * Sets the loggerId for the current session.
	 * 
	 * @param sessionId String representing the current session's logger id
	 */
	public void setLoggerId(String sessionId) {
		
		String abbreviatedSessionId = sessionId.substring(0,7);
		this.loggerId = LOGGER_ID_PREFIX + abbreviatedSessionId + ":: ";
	}
	
	public TokenManager getTokenManager() {
		return this.tokenManager;
	}
	
	public void setTokenManager(TokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}

	public boolean isAdminActionValidated() {
		return adminActionValidated;
	}

	public void setAdminActionValidated(boolean adminActionValidated) {
		this.adminActionValidated = adminActionValidated;
	}

}
