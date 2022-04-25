package edu.umn.midb.population.atlas.tasks;

/**
 * Encapsulates the data necessary to add a record to the file_downloads table in MYSQL.
 * 
 * 
 * @author jjfair
 *
 */
public class FileDownloadEntry extends TaskEntry {

	private String study = null;
	private String neuralNetworkName = null;
	private String filePath = null;
	private String fileName = null;
	private String emailAddress = "UNKNOWN";
	private String id = "NOT SET";

	public FileDownloadEntry() {
		this.subclassName = "FileDownloadEntry";
	}
	
	/**
	 * 
	 * Returns the email of the client that downloaded the file. This information may not
	 * be available if the client chooses not to subscribe.
	 * 
	 * @return emailAddress - String
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	
	/**
	 * Get the file name (not including path) of the downloaded file.
	 * 
	 * @return fileName - String
	 */
	public String getFileName() {
		return fileName;
	}
	

	/**
	 * 
	 * Returns the absolute folder path on the server where the downloaded file is located.
	 * 
	 * @return filePath - String
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Returns the id of this downloaded file action. The id is a timestamp.
	 * 
	 * @return id - String that represents a timestamp
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the name of the neural network name that the downloaded file is associated with.
	 * 
	 * @return neuralNetworkName - String
	 */
	public String getNeuralNetworkName() {
		return neuralNetworkName;
	}
	
	/**
	 * 
	 * Returns the study name that the downloaded file is associated with.
	 * 
	 * @return study - String
	 */
	public String getStudy() {
		return study;
	}
	
	/**
	 * 
	 * Sets the email address of the client who downloaded the file.
	 * 
	 * @param emailAddress - String
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	/**
	 * 
	 * Sets the file name (not including path) of the downloaded file.
	 * 
	 * @param fileName - String
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * 
	 * Sets the absolute file path on the server where the downloaded file is located.
	 * 
	 * @param filePath - String
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Sets the id with a timestamp String.
	 * 
	 * @param id - String representing a timestamp
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * Sets the neuralNetworkName that the downloaded file is associated with.
	 * 
	 * @param neuralNetworkName - String
	 */
	public void setNeuralNetworkName(String neuralNetworkName) {
		this.neuralNetworkName = neuralNetworkName;
	}
	
	/**
	 * 
	 * Sets the study that the downloaded file is associated with.
	 * 
	 * @param study - String
	 */
	public void setStudy(String study) {
		this.study = study;
	}

}
