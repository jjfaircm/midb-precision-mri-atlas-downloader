package edu.umn.midb.population.atlas.utils;

public class NetworkMapData {
	
	private String networkName = null;
	private String networkMapImage_Base64_String = null;
	private String niftiFilePathName = null;
	
	public NetworkMapData(String networkName, String b64String, String niftiPath) {
		this.networkName = networkName;
		this.networkMapImage_Base64_String = b64String;
		this.niftiFilePathName = niftiPath;
	}
	
	public String getNetworkName() {
		return networkName;
	}
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}
	public String getNetworkMapImage_Base64_String() {
		return networkMapImage_Base64_String;
	}
	public void setNetworkMapImage_Base64_String(String networkMapImage_Base64_String) {
		this.networkMapImage_Base64_String = networkMapImage_Base64_String;
	}
	public String getCorrespondingNiftiFilePathName() {
		return niftiFilePathName;
	}
	public void setCorrespondingNiftiFilePathName(String niftiFilePathName) {
		this.niftiFilePathName = niftiFilePathName;
	}

}
