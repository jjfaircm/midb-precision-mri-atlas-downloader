package edu.umn.midb.population.atlas.menu;

/**
 * Encapsulates the data relevant to a network probabilistic map. This includes the
 * neural network name, the base64 encoding of the .png image file, and the absolute path to the .dscalar.nii file.
 * 
 * @author jjfair
 *
 */
public class NetworkMapData { 
	
	private String networkName = null;
	private String networkMapImage_Base64_String = null;
	private String niftiFilePathName = null;
	
	/**
	 * Constructor
	 * @param networkName - String which is the type of neural network (Aud, CO, etc.)
	 * @param b64String - base64 encoded String of the .png file image
	 * @param niftiPath - absolute path to the .nii file
	 */
	public NetworkMapData(String networkName, String b64String, String niftiPath) {
		this.networkName = networkName;
		this.networkMapImage_Base64_String = b64String;
		this.niftiFilePathName = niftiPath;
	}
	
	/**
	 * Returns the the absolute file path of the .nii file corresponding to the .png file
	 * that displays in the client browser
	 * 
	 * @return niftiFilePathName - String
	 */
	public String getCorrespondingNiftiFilePathName() {
		return niftiFilePathName;
	}
	
	/**
	 * Returns the  base64 encoded String of the .png image file
	 * 
	 * @return networkMapImage_Base64_String - String
	 */
	public String getNetworkMapImage_Base64_String() {
		return networkMapImage_Base64_String;
	}
	
	/**
	 * Returns the name of the neural network that this data maps to to.
	 * 
	 * @return networkName - String
	 */
	public String getNetworkName() {
		return networkName;
	}
	
	/**
	 * 
	 * Sets the absolute file to the .nii file that this data represents.
	 * 
	 * @param niftiFilePathName - String
	 */
	public void setCorrespondingNiftiFilePathName(String niftiFilePathName) {
		this.niftiFilePathName = niftiFilePathName;
	}
	
	/**
	 * 
	 * Sets the base64 encoded String of the .png image file.
	 * 
	 * @param networkMapImage_Base64_String - String
	 */
	public void setNetworkMapImage_Base64_String(String networkMapImage_Base64_String) {
		this.networkMapImage_Base64_String = networkMapImage_Base64_String;
	}
	
	/**
	 * Sets the neural network name that this data maps to.
	 * 
	 * @param networkName - String
	 */
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

}
