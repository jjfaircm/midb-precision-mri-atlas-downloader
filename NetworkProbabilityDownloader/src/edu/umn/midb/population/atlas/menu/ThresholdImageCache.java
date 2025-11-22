package edu.umn.midb.population.atlas.menu;

import java.util.ArrayList;

public class ThresholdImageCache {
	
	private ArrayList<String> base64ImageStrings = null;
	private ArrayList<String> niftiFilePaths = null;
	private NetworkMapData networkMapData = null;
	
	
	public ArrayList<String> getBase64ImageStrings() {
		return base64ImageStrings;
	}
	
	public void setBase64ImageStrings(ArrayList<String> base64ImageStrings) {
		this.base64ImageStrings = base64ImageStrings;
	}
	
	public ArrayList<String> getNiftiFilePaths() {
		return niftiFilePaths;
	}
	
	public void setNiftiFilePaths(ArrayList<String> niftiFilePaths) {
		this.niftiFilePaths = niftiFilePaths;
	}

	public NetworkMapData getNetworkMapData() {
		return networkMapData;
	}

	public void setNetworkMapData(NetworkMapData networkMapData) {
		this.networkMapData = networkMapData;
	}
	
}
