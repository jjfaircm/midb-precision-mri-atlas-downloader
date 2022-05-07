package edu.umn.midb.population.atlas.webresponses;

/**
 * Encapsulates the response for a client request of updating the url for the web hits map
 * which is stored in the map_urls table in MYSQL. An instance of this class is converted
 * to a json string which is sent to the client.
 * 
 * @author jjfair
 *
 */
public class UpdateMapUrlsResponse {
	
	String message = null;
	String webHitsMapURL = null;
	String downloadsMapURL = null;
	String targetMap = null;
	
	
	/**
	 * Returns the name of the target map (i.e., WEB_HITS_MAP)
	 * 
	 * @return targetMap String
	 */
	public String getTargetMap() {
		return targetMap;
	}
	
	/**
	 * Sets the targetMap.
	 * 
	 * @param targetMap - String
	 */
	public void setTargetMap(String targetMap) {
		this.targetMap = targetMap;
	}
	
	/**
	 * Gets the message that will be displayed on the client.
	 * 
	 * @return message - String
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Sets the message that will be displayed on the client.
	 * 
	 * @param message - String
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Returns the iframe tag which is used to embed the map in the browser.
	 * 
	 * @return mapURL - String
	 */
	public String getWebHitsMapURL() {
		return webHitsMapURL;
	}
	
	/**
	 * Sets the embed code to be used as an iframe in the client browser.
	 * 
	 * @param mapURL - String
	 */
	public void setDownloadsMapURL(String mapURL) {
		this.downloadsMapURL = mapURL;
	}
	
	/**
	 * Returns the iframe tag which is used to embed the map in the browser.
	 * 
	 * @return mapURL - String
	 */
	public String getDownloadsMapURL() {
		return downloadsMapURL;
	}
	
	/**
	 * Sets the embed code to be used as an iframe in the client browser.
	 * 
	 * @param mapURL - String
	 */
	public void setWebHitsMapURL(String mapURL) {
		this.webHitsMapURL = mapURL;
	}

}
