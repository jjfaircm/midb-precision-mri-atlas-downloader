package edu.umn.midb.population.atlas.utils;

/**
 * Encapsulates the available storage stats data.
 * 
 * @author jjfair
 *
 */
public class ServerStorageStats {
	
	private float availableFreeAmount = 0;
	private String unitOfMeasure = "GB";
	private String message = null;
	
	
	/**
	 * Returns the numeric amount of free storage.
	 * 
	 * @return availableFreeAmount - float
	 */
	public float getAmount() {
		return availableFreeAmount;
	}
	
	/**
	 * Set the available free amount
	 * 
	 * @param amount - float
	 */
	public void setAmount(float amount) {
		this.availableFreeAmount = amount;
	}
	
	/**
	 * Returns the unit of measurement, such as Gigabytes
	 * 
	 * @return unitOfMeasure - String
	 */
	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}
	
	/**
	 * Sets the unit of measure, such as gigabytes.
	 * 
	 * @param unitOfMeasure - String
	 */
	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	
	/**
	 * Returns a string that states the number and unit of measure for available free storage.
	 * 
	 * @return message - String
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Sets the message that states the amount and unit of measure of free storage.
	 * 
	 * @param message - String
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
