package edu.umn.midb.population.atlas.base;

import java.sql.Timestamp;
import java.util.ArrayList;

import edu.umn.midb.population.atlas.security.TokenManager;
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
	private String loggerId;
	private TokenManager tokenManager = null;
	private boolean adminActionValidated = false;
	private CreateStudyHandler createStudyHandler = null;
	private ArrayList<String> queryStringChain = new ArrayList<String>();
	private String id = "";
	private boolean zipFileUnpackError = false;
	private boolean folderConfigurationError = false;
	private String createStudyErrorMessage = null;
	private boolean createStudyHasError = false;
	private String remoteAddress = null;
	private boolean zipFormatError = false;
	private String zipFormatErrorMessage = null;
	
	public boolean isZipFileUnpackError() {
		return zipFileUnpackError;
	}

	public void setZipFileUnpackError(boolean zipFileUnpackError) {
		this.zipFileUnpackError = zipFileUnpackError;
	}

	public ApplicationContext() {
		this.queryStringChain.add("QUERY STRING HISTORY CHAIN" + NEW_LINE);
	}

	public CreateStudyHandler getCreateStudyHandler() {
		return createStudyHandler;
	}

	public void setCreateStudyHandler(CreateStudyHandler createStudyHandler) {
		this.createStudyHandler = createStudyHandler;
	}

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
	
	public void addQueryStringToHistoryChain(String queryString) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String entryString = queryString.replace("&", "...");
		entryString += "::";
		entryString += NEW_LINE;
		entryString = timestamp + "  " + entryString;
		this.queryStringChain.add(entryString);
	}
	
	public String getQueryStringHistory() {
		//in case the user has multiple errors during 1 session, we only want
		//the chain to include the last id (the id is the sessionId + timestamp)
		this.queryStringChain.add(this.id);
		int length = this.queryStringChain.size();
		String returnString = this.queryStringChain.toString();
		this.queryStringChain.remove(length-1);
		return returnString;
	}
	
	public void setId(String id) {
		String idString = "INCIDENT_ID=" + id + NEW_LINE;
		//this.queryStringChain.add(idString);
		this.id = idString;
	}

	public boolean createStudyHasError() {
		return createStudyHasError;
	}

	public void setCreateStudyHasError(boolean createStudyHasError) {
		this.createStudyHasError = createStudyHasError;
	}

	public String getCreateStudyErrorMessage() {
		return createStudyErrorMessage;
	}

	public void setCreateStudyErrorMessage(String createFolderErrorMessage) {
		this.createStudyErrorMessage = createFolderErrorMessage;
	}
	
	public void setRemoteAddress(String remoteAddr) {
		String ipString = "ipAddress=" + remoteAddr + NEW_LINE;
		this.queryStringChain.add(ipString);
		this.remoteAddress = remoteAddr;
	}

	public boolean isFolderConfigurationError() {
		return folderConfigurationError;
	}

	public void setFolderConfigurationError(boolean folderConfigurationError) {
		this.folderConfigurationError = folderConfigurationError;
	}
	
	public void clearErrors() {
		this.createStudyErrorMessage = null;
		this.zipFormatErrorMessage = null;
		this.createStudyHasError = false;
		this.folderConfigurationError = false;
		this.zipFileUnpackError = false;
		this.zipFormatError = false;
	}

	public boolean isZipFormatError() {
		return zipFormatError;
	}

	public void setZipFormatError(boolean zipFormatError) {
		this.zipFormatError = zipFormatError;
	}

	public String getZipFormatErrorMessage() {
		return zipFormatErrorMessage;
	}

	public void setZipFormatErrorMessage(String zipFormatErrorMessage) {
		this.zipFormatErrorMessage = zipFormatErrorMessage;
	}

}
