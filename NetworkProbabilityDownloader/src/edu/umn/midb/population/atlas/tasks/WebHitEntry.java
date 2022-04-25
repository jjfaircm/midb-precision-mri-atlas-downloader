package edu.umn.midb.population.atlas.tasks;


/**
 * Encapsulates the data needed to insert a record into the web_hits table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class WebHitEntry extends TaskEntry {
	
	private String timestampId = null;
	private String userAgent = null;
	
	/**
	 * 
	 * Default constructor
	 * 
	 */
	public WebHitEntry() {
		this.subclassName = "WebHitEntry";
	}
	
	/**
	 * 
	 * Returns a timestamp String that serves as a unique id.
	 * 
	 * @return timestampId
	 */
	public String getId() {
		return timestampId;
	}
	
	/**
	 * 
	 * Returns the type of browser (user-agent) that the remote requestor used.
	 * 
	 * @return userAgent - String
	 */
	public String getUserAgent() {
		return userAgent;
	}
	
	/**
	 * Sets the timestampId
	 * 
	 * @param id - String
	 */
	public void setId(String id) {
		this.timestampId = id;
	}
	
	/**
	 * Sets the type of browser (user-agent) that the requestor used.
	 * 
	 * @param userAgent - String
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	

}
