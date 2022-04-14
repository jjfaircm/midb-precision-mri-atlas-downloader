package edu.umn.midb.population.atlas.data.access;

/**
 * 
 * Superclass for the different types of ..Record classes which encapsulate data
 * retrieved from tables in MYSQL. The subclasses and related tables are:
 * <ul>
 * <li>{@link AdminAccessRecord} : admin_access</li>
 * <li>{@link EmailAddressRecord} : email_addresses</li>
 * <li>{@link FileDownloadRecord} : file_downloads</li>
 * <li>{@link WebHitRecord} : web_hits</li>
 * </ul>
 * 
 * @author jjfair
 *
 */
public class BaseRecord {

	protected String createDate = null;
	protected String ipAddress = null;
	protected String city = "UNKNOWN";
	protected String state = "UNKNOWN";
	protected String country = "UNKNOWN";
	protected String latitude = "UNKNOWN";
	protected String longitude = "UNKNOWN";

	/**
	 * Returns the latitude associated with the ipAddress contained in the record.
	 * 
	 * @return latitude - String
	 */
	public String getLatitude() {
		return latitude;
	}
	
	/**
	 * Sets the latitude associated with the ipAddress contained in the record.
	 * 
	 * @param latitude - String
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * Returns the longitude associated with the ipAddress contained in the record.
	 * 
	 * @return longitude - String
	 */
	public String getLongitude() {
		return longitude;
	}
	
	/**
	 * Sets the longitude associated with the ipAddress contained in the record.
	 * 
	 * @param longitude - String
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * Returns the datetime the record was created in a MYSQL record
	 * 
	 * @return createDate - datetime String
	 */
	public String getCreateDate() {
		return createDate;
	}
	
	/**
	 * Sets the create date that was retrieved from the database table
	 * @param createDate - String
	 */
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	
	/**
	 * Returns the ipAddress associated with the record
	 * 
	 * @return ipAddress - String
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	
	/**
	 * Sets the ipAddress of the requestor associated with this record
	 * 
	 * @param ipAddress - String
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	/**
	 * Returns the city associated with the ipAddress contained in the record
	 * 
	 * @return city - String
	 */
	public String getCity() {
		return city;
	}
	
	/**
	 * Sets the city associated with the ipAddress contained in the record
	 * 
	 * @param city - String
	 */
	public void setCity(String city) {
		this.city = city;
	}
	
	/**
	 * 
	 * Returns the state/region associated with the ipAddress contained in the record
	 * 
	 * @return state - String
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * Sets the state (or region) associated with the ipAddress contained in the record
	 * 
	 * @param state - String
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * 
	 * Returns the country associated with the ipAddress contained in the record
	 * 
	 * @return country - String
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * 
	 * Sets the country associated with the ipAddress contained in the record
	 * 
	 * @param country - String
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * 
	 * Returns a concatenation of the city::state::country. This is used as a title for
	 * a location as depicted in a google map.
	 * 
	 * @return location - String
	 */
	public String getLocationName() {
		return this.city + "::" + this.state + "::" + this.country;
	}
}
