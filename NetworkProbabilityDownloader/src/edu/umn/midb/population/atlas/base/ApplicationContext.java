package edu.umn.midb.population.atlas.base;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import edu.umn.midb.population.atlas.security.TokenManager;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.CreateStudyHandler;

/**
 * Contains references to any objects that must be tracked for the life of the session.
 * 
 * @author jjfair
 *
 */
public class ApplicationContext { 
	
	private static final String LOGGER_ID_PREFIX = "::LOGGERID=";
	private static final String NEW_LINE = "\n";
    private static final DateTimeFormatter DT_FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_FORMATTER_FOR_ID = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	private String loggerId;
	private TokenManager tokenManager = null;
	private boolean adminActionValidated = false;
	private CreateStudyHandler createStudyHandler = null;
	private ArrayList<String> queryStringChain = new ArrayList<String>();
	private String id = "";
	private String remoteAddress = null;
	private boolean emailAddressAlreadyTracked = false;
	private String sessionId = "NOT_SET";
	private String localHostName = "";
	private int actionCount = 0;
	private String currentAction = null;
	private ArrayList<String> actionList = new ArrayList<String>();
	private String currentActionFormattedTimestamp = null;
	private HttpServletRequest currentReguest = null;
	
	/**
	 * Default constructor
	 */
	public ApplicationContext() {
		this.queryStringChain.add("QUERY STRING HISTORY CHAIN" + NEW_LINE);
	}

	/**
	 * Returns the encapsulated {@link CreateStudyHandler}. If not handler has been set
	 * then null is returned.
	 * 
	 * @return createStudyHandler - {@link CreateStudyHandler}
	 */
	public CreateStudyHandler getCreateStudyHandler() {
		return createStudyHandler;
	}

	/**
	 * Sets the {@link CreateStudyHandler}
	 * 
	 * @param createStudyHandler - {@link CreateStudyHandler}
	 */
	public void setCreateStudyHandler(CreateStudyHandler createStudyHandler) {
		this.createStudyHandler = createStudyHandler;
	}

	/**
	 * Returns the loggerId associated with the current http session.  The loggerId may be
	 * used to identify all log entries associated with a specific user session, and includes
	 * the ip address of the requestor
	 * 
	 * @return String representing the current session's logger id
	 */
	public String getLoggerId() {
		return loggerId;
	}

	/**
	 * Sets the loggerId for the current session.
	 * 
	 * @param sessionId - String representing the current session's logger id
	 * @param ipAddress - String representing ip address of the remote requestor
	 */
	public void setLoggerId(String sessionId, String ipAddress) {
		
		String abbreviatedSessionId = sessionId.substring(0,7);
		this.loggerId = LOGGER_ID_PREFIX + ipAddress + "__" + abbreviatedSessionId + ":: ";
	}
	
	/**
	 * Returns the {@link TokenManager} 
	 * 
	 * @return tokenManager - {@link TokenManager}
	 */
	public TokenManager getTokenManager() {
		return this.tokenManager;
	}
	
	/**
	 * Sets the {@link TokenManager} for the session.
	 * 
	 * @param tokenManager - {@link TokenManager}
	 */
	public void setTokenManager(TokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}

	/**
	 * Returns a boolean indicating if the current session has been validated for
	 * admin access.
	 * 
	 * @return adminActionValidated - boolean
	 */
	public boolean isAdminActionValidated() {
		return adminActionValidated;
	}

	/**
	 * Sets a boolean indicating if admin access has been validated
	 * 
	 * @param adminActionValidated - boolean
	 */
	public void setAdminActionValidated(boolean adminActionValidated) {
		this.adminActionValidated = adminActionValidated;
	}
	
	/**
	 * Adds a queryString to the query string history. This is useful for debugging
	 * error conditions.
	 * 
	 * @param queryString - String
	 */
	public void addQueryStringToHistoryChain(String queryString) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String entryString = queryString.replace("&", "...");
		entryString += "::";
		entryString += NEW_LINE;
		entryString = timestamp + "  " + entryString;
		this.queryStringChain.add(entryString);
	}
	
	/**
	 * Returns the ArrayList of query string history as a single String.
	 * 
	 * @return queryHistory - String
	 *  
	 */
	public String getQueryStringHistory() {
		//in case the user has multiple errors during 1 session, we only want
		//the chain to include the last id (the id is the sessionId + timestamp)
		this.queryStringChain.add(this.id);
		int length = this.queryStringChain.size();
		String returnString = this.queryStringChain.toString();
		this.queryStringChain.remove(length-1);
		return returnString;
	}
	
	
	/**
	 * Constructs and returns a String to use as an incident id which is used for
	 * error reporting.
	 * 
	 * @param id - String
	 */
	public void setIncidenceId(String id) {
		String idString = "INCIDENT_ID=" + id + NEW_LINE;
		this.id = idString;
	}

	/**
	 * Sets the ip address of the remote requestor
	 * 
	 * @param remoteAddr - String
	 */
	public void setRemoteAddress(String remoteAddr) {
		String ipString = "ipAddress=" + remoteAddr + NEW_LINE;
		this.queryStringChain.add(ipString);
		this.remoteAddress = remoteAddr;
	}

	/**
	 * Returns the ip address of the remote requestor.
	 * 
	 * @return remoteAddress - String
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * Sets a boolean indicating if the application has already received an email address
	 * from the requestor.  This is used in conjunction with the downloading of files where
	 * the application will write the email address to the database if the requestor has not
	 * opted out of subscribing.
	 * 
	 * @return emailAddressAlreadyTracked - boolean
	 */
	public boolean isEmailAddressAlreadyTracked() {
		return emailAddressAlreadyTracked;
	}

	public void setEmailAlreadyTracked(boolean emailAlreadyTracked) {
		this.emailAddressAlreadyTracked = emailAlreadyTracked;
	}

	/**
	 * Returns the session id. This is a combination of the requestor's ip address and
	 * the sessionId created by the servlet container.
	 * 
	 * @return sessionId - String
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the current sessionId as created by the servlet container.
	 * 
	 * @param sessionId - String
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	/**
	 * Get the number of action requests, coming in as requests to the {@link NetworkProbabilityDownloader} servlet, that
	 * have been received. 
	 * 
	 * @return actionCount - int
	 */
	public int getActionCount() {
		return actionCount;
	}

	/**
	 * Increments the actionCount
	 */
	public void incrementActionCount() {
		this.actionCount++;
	}

	/**
	 * Returns the name of the most current action that was received by the {@link NetworkProbabilityDownloader} servlet.
	 * 
	 * @return currentAction - String
	 */
	public String getCurrentAction() {
		return currentAction;
	}

	/**
	 * Sets the last (current) action received by the client.
	 * 
	 * @param currentAction - String
	 */
	public void setCurrentAction(String currentAction) {
		if(this.currentAction != null) {
			this.actionList.add(currentAction);
		}
		this.currentAction = currentAction;
		LocalDateTime localTime = LocalDateTime.now();
		String formattedTS = DT_FORMATTER_1.format(localTime);
		formattedTS = formattedTS.replace(" ", ",");
		this.currentActionFormattedTimestamp = formattedTS;
	}

	/**
	 * Returns the timestamp indicating the time that the current action was requested
	 * 
	 * @return currentActionFormattedTimestamp - String
	 */
	public String getCurrentActionFormattedTimestamp() {
		return currentActionFormattedTimestamp;
	}

	/**
	 * Returns the last request that was received from the client.
	 * 
	 * @return currentReguest - HttpServletRequest
	 * 
	 */
	public HttpServletRequest getCurrentReguest() {
		return currentReguest;
	}

	/**
	 * Sets the current/last request received by the {@link NetworkProbabilityDownloader} servlet.
	 * 
	 * @param currentReguest - HttpServletRequest
	 */
	public void setCurrentReguest(HttpServletRequest currentReguest) {
		this.currentReguest = currentReguest;
	}
	
	/**
	 * Returns the ArrayList of String representing action requests that have been received.
	 * 
	 * @return actionList - ArrayList of String
	 */
	public ArrayList<String> getActionList() {
		return this.actionList;
	}

}
