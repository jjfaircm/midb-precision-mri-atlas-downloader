package edu.umn.midb.population.atlas.base;

public class ApplicationContext {
	
	private static final String LOGGER_ID_PREFIX = "::LOGGERID=";
	private String loggerId;

	public String getLoggerId() {
		return loggerId;
	}

	public void setLoggerId(String sessionId) {
		
		String abbreviatedSessionId = sessionId.substring(0,7);
		this.loggerId = LOGGER_ID_PREFIX + abbreviatedSessionId + ":: ";
	}
	
}
