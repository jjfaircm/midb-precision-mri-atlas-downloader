package edu.umn.midb.population.atlas.tasks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;

/**
 * Superclass of the 3 different types of TaskEntry that represent entries that must
 * be added to the database. The three subclasses and the related MYSQL tables are:
 * 
 * <ul>
 * <li>{@link FileDownloadEntry} : file_downloads</li>
 * <li>{@link EmailAddressEntry} : email_addresses</li>
 * <li>{@link WebHitEntry} : web_hits</li>
 * </ul>
 * 
 * 
 * @author jjfair
 *
 */
public class TaskEntry {
	
	private ApplicationContext appContext = null;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;
	private String requestorIPAddress = null;
	private String formattedTimeStamp = null;
	private String subclassName = null;
	private String city = "unknown";
	private String state = "unknown";
	private String country = "unknown";
	private String latitude = "unknown";
	private String longitude = "unknown";
	private boolean shutdownTrigger = false;

	
	/**
	 * Returns the {@link ApplicationContext} of the client session that generated the
	 * TaskEntry.
	 * 
	 * 
	 * @return appContext - {@link ApplicationContext}
	 */
	public ApplicationContext getAppContext() {
		return appContext;
	}
	
	/**
	 * Returns the city of the geo-location associated with the requestorIPAddress.
	 * 
	 * @return city - String
	 */
	public String getCity() {
		return city;
	}
	
	/**
	 * Gets the country of the geo-location associated with the ipAddress of the requestor
	 * that generated the TaskEntry.
	 * 
	 * 
	 * @return country - String
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * 
	 * Formatted timestamp indicating the date and time that the TaskEntry was created.
	 * 
	 * @return formattedTimeStamp - String
	 */
	public String getFormattedTimeStamp() {
		return formattedTimeStamp;
	}

	/**
	 * Gets the latitude of the geo-location associated with the ipAddress of the requestor
	 * that generated the TaskEntry.
	 * 
	 * 
	 * @return latitude - String
	 */
	public String getLatitude() {
		return latitude;
	}
	
	/**
	 * Gets the longitude of the geo-location associated with the ipAddress of the requestor
	 * that generated the TaskEntry.
	 * 
	 * 
	 * @return longitude - String
	 */
	public String getLongitude() {
		return longitude;
	}
	
	/**
	 * 
	 * instance of HttpServletRequest associated with the requestor that generated
	 * the TaskEntry
	 * 
	 * @return request - HttpServletRequest
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	
	/**
	 * 
	 * Returns the requestorIPAddress of the remote client.
	 * 
	 * @return requestorIPAddress - String
	 */
	public String getRequestorIPAddress() {
		return requestorIPAddress;
	}
	
	/**
	 * 
	 * Returns the HttpServletResponse associated with the HttpServletRequest that was
	 * generated by the servlet container.
	 * 
	 * @return response - HttpServletResponse
	 */
	public HttpServletResponse getResponse() {
		return response;
	}
	
	/**
	 * Returns the state (or region) of the geo-location associated with the requestorIPAddress.
	 * 
	 * @return state - String
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * Sets the name of the inheriting subclass which is used to determine how to process
	 * the TaskEntry.
	 * 
	 * @return subclassName - String
	 */
	public String getSubclassName() {
		return subclassName;
	}
	
	/**
	 * 
	 * Returns a boolean indicating if the TaskEntry is meant to signal shutdown whereby
	 * the receiving thread should gracefully terminate.
	 * 
	 * @return shutdownTrigger - boolean indicating the servlet container is shutting down.
	 */
	public boolean isShutdownTrigger() {
		return shutdownTrigger;
	}
	
	/**
	 * Sets the {@link ApplicationContext} of the client session that generated the TaskEntry
	 * 
	 * 
	 * @param appContext - {@link ApplicationContext}
	 */
	public void setAppContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	
	/**
	 * 
	 * Sets the city of the geo-location associated with the requestorIPAddress.
	 * 
	 * @param city - String
	 */
	public void setCity(String city) {
		if(city != null) {
			this.city = city;
		}
	}
	
	/**
	 * Sets the country of the geo-location associated with the ipAddress of the requestor
	 * that generated the TaskEntry.
	 * 
	 * @param country - String
	 */
	public void setCountry(String country) {
		if(country != null) {
			this.country = country;
		}
	}
	
	/**
	 * 
	 * Sets the formattedTimeStamp indicating the date and time that the TaskEntry
	 * was created.
	 * 
	 * @param formattedTimeStamp - String
	 */
	public void setFormattedTimeStamp(String formattedTimeStamp) {
		this.formattedTimeStamp = formattedTimeStamp;
	}
	
	/**
	 * Sets the latitude of the geo-location associated with the ipAddress of the requestor
	 * that generated the TaskEntry.
	 * 
	 * @param latitude - String
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * Sets the longitude of the geo-location associated with the ipAddress of the requestor
	 * that generated the TaskEntry.
	 * 
	 * @param longitude - String
	 */

	
	
	
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * 
	 * Sets the HttpServletRequest associated with the request that came into
	 * the {@link NetworkProbabilityDownloader} servlet.
	 * 
	 * @param request - HttpServletRequest
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	/**
	 * 
	 * Sets the requestorIPAddress of the remote client.
	 * 
	 * @param requestorIPAddress - String
	 */
	public void setRequestorIPAddress(String requestorIPAddress) {
		this.requestorIPAddress = requestorIPAddress;
	}

	/**
	 * 
	 * Sets the HttpServletResponse instance generated by the Servlet Container (i.e. tomcat) that
	 * is associated with the HttpServletRequest.
	 * 
	 * @param response - HttpServletResponse
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * Sets the shutdownTrigger which determines if the TaskEntry is meant to signal that
	 * the Servlet Container is in shutdown mode.
	 * 
	 * @param shutdownTrigger - boolean
	 */
	public void setShutdownTrigger(boolean shutdownTrigger) {
		this.shutdownTrigger = shutdownTrigger;
	}

	/**
	 * Sets the state (or region) of the geo-location associated with the requestorIPAddress.
	 * 
	 * 
	 * @param state - String
	 */
	public void setState(String state) {
		if(state != null) {
			this.state = state;
		}
	}

	/**
	 * Sets the name of the inheriting subclass (such as WebHitEntry or ...).
	 * 
	 * @param subclassName - String
	 */
	public void setSubclassName(String subclassName) {
		this.subclassName = subclassName;
	}

}
