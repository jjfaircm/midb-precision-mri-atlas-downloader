package edu.umn.midb.population.atlas.data.access;

/**
 * Encapsulates data retrieved from the web_hits table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class WebHitRecord extends BaseRecord {
	
	private String hitCount = null;
	
	/**
	 * Returns the hitCount associated with this web hit. For example, the 100th visitor to
	 * the website.
	 * 
	 * @return hitCount - String
	 */
	public String getHitCount() {
		return hitCount;
	}
	
	/**
	 * Sets the hitCount as a String.
	 * 
	 * @param hitCount - String
	 */
	public void setHitCount(String hitCount) {
		this.hitCount = hitCount;
	}
	
}
