package edu.umn.midb.population.atlas.data.access;


/**
 * 
 * Encapsulates the data retrieved from a record in the file_downloads table in MYSQL.
 * 
 * @author jjfair
 *
 */
public class FileDownloadRecord extends BaseRecord {
	
	private String fileName = null;
	private String study = null;
	private String emailAddress = null;
	
	/**
	 * Returns the fileName of the downloaded file (does not contain path information).
	 * 
	 * @return fileName - String
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * 
	 * Sets the fileName of the downloaded file. The path should not be included.
	 * 
	 * @param fileNameWithoutPath - String
	 */
	public void setFileName(String fileNameWithoutPath) {
		this.fileName = fileNameWithoutPath;
	}
	
	/**
	 * 
	 * Returns the name of the study that contains the downloaded file.
	 * 
	 * @return study - String
	 */
	public String getStudy() {
		return study;
	}
	
	/**
	 * 
	 * Sets the study name that contains the downloaded file.
	 * 
	 * @param study - String
	 */
	public void setStudy(String study) {
		this.study = study;
	}
	
	/**
	 * 
	 * Returns the email address of the user that downloaded the file. If the email address
	 * was not provided, then 'unknown' is returned.
	 * 
	 * @return emailAddress - String
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	
	/**
	 * 
	 * Sets the email address of the user that downloaded the file.
	 * 
	 * @param emailAddress - String
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
