package edu.umn.midb.population.atlas.webresponses;

/**
 * 
 * Encapsulates response data that is sent to the client when validating admin console
 * authentication. The url links to the WEB_HITS_MAP and FILE_DOWNLOADS_MAP are included
 * in the response. This object is converted to a JSON string before being sent to the
 * client.
 * 
 * @author jjfair
 *
 */
public class AdminValidationResponse {

	private String validationMessage = null;
	private String webHitsMapURL = null;
	private String downloadsMapURL = null;
	
	
	/**
	 * Returns the embed link for the google map that depicts all the ip address locations
	 * of requests to download one of the .nii files.
	 * 
	 * @return downloadsMapURL - String
	 */
	public String getDownloadsMapURL() {
		return downloadsMapURL;
	}
	
	/**
	 * Returns the response message that is sent to the client.
	 * 
	 * @return validationMessage - String
	 */
	public String getValidationMessage() {
		return validationMessage;
	}
	
	/**
	 * Returns the embed url of the google map that depicts the locations of all the 
	 * ip addresses of clients that have visited the website. 
	 * 
	 * @return webHitsMapURL - String
	 */
	public String getWebHitsMapURL() {
		return webHitsMapURL;
	}
	
	/**
	 * Sets the url for embedding the google map that shows all locations from which a
	 * file download was requested.
	 * 
	 * @param downloadsMapURL - String
	 */
	public void setDownloadsMapURL(String downloadsMapURL) {
		this.downloadsMapURL = downloadsMapURL;
	}


	/**
	 * Sets the message that is sent to the client when they attempt to login to the admin
	 * console.
	 * 
	 * @param validationResponse - String
	 */
	public void setValidationMessage(String validationResponse) {
		this.validationMessage = validationResponse;
	}


	/**
	 * 
	 * Sets the url used to embed the WEB_HITS_MAP in the website.
	 * 
	 * @param webHitsMapURL - String
	 */
	public void setWebHitsMapURL(String webHitsMapURL) {
		this.webHitsMapURL = webHitsMapURL;
	}
	
	
}
