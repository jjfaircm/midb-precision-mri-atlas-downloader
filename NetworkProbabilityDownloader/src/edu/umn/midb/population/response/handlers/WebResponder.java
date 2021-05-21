package edu.umn.midb.population.response.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.NetworkMapData;
import logs.ThreadLocalLogTracker;


/**
 * The WebResponder is responsible for sending responses for the various requests that
 * come to the {@link NetworkProbabilityDownloader}. It will answer the following requests:
 * <ul>
 * <li>retrieve the list  of neural network types
 * <li>retrieve the .png images associated with the probabilistic thresholds for a given network type
 * <li>retrieve the NII file for a selected threshold
 * </ul>
 * @author jjfair
 *
 */
public class WebResponder {
	
	private static final String DELIMITER = ":@:";
	private static final String DELIMITER_NEURAL_NAMES = "!@!";
	private static final String DELIMITER_NETWORK_MAP_DATA = "$@$";
	private static final String DELIMITER_NETWORK_MAP_ITEMS = "&@&";


	private static final Logger LOGGER = LogManager.getLogger(WebResponder.class);
	
	/**
	 * Sends the requested NII file related to a selected probabilistic threshold.
	 * 
	 * @param response The current HttpServletResponse object
	 * @param fileBinaryBuffer binaryBuffer containing the requested NII file
	 * @param fileName String representing the name of the requested NII file to download
	 */
	public static void sendFileDownloadResponse(HttpServletResponse response, byte[] fileBinaryBuffer, String fileName) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendFileDownloadResponse()...invoked.");
				
		try {
			response.setContentType("image/nii");
			response.setHeader("X-Content-Type-Options", "nosniff");
			String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", fileName);
            response.setHeader(headerKey, headerValue);
			response.getOutputStream().write(fileBinaryBuffer);
			response.getOutputStream().flush();
		}
		catch(IOException ioE) {
			LOGGER.error(ioE.getLocalizedMessage(), ioE);
		}
		
		LOGGER.trace(loggerId + "sendFileDownloadResponse()...exit.");

	}
	
	/**
	 * Builds a JsonArray representing a list of the different neural network types.
	 * 
	 * @param networkTypes ArrayList of the different neural network types
	 * 
	 * @return JsonArray A json representation of the neural network types
	 */
	public static JsonArray buildNeuralNetworkNamesResponse(ArrayList<String> networkTypes) {
		
		  String loggerId = ThreadLocalLogTracker.get();
		  LOGGER.trace(loggerId + "sendNeuralNetworkNamesResponse()...invoked.");

	      JsonArray jsonArray1 = new Gson().toJsonTree(networkTypes).getAsJsonArray();
		  LOGGER.trace(loggerId + "sendNeuralNetworkNamesResponse()...exit.");

	      return jsonArray1;
	}
	
	/**
	 * Sends the .png files associated with the different probabilistic threhsolds for a selected
	 * neural network.  The files are sent as a list of base64 encoded strings.
	 * 
	 * @param response The current HttpServletResponse object
	 * @param filePaths ArrayList of all the .png file names representing the different probabilistic thresholds
	 * @param imageBase64Strings ArrayList of the .png files in base64 encoded format
	 * @param neuralNetworkNames ArrayList of the neural network names
	 */
	public static void sendThresholdImagesResponse(HttpServletResponse response, ArrayList<String> filePaths, ArrayList<String> imageBase64Strings,
			                                       ArrayList<String> neuralNetworkNames, NetworkMapData networkMapData) {
		
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "sendThresholdImagesResponse()...invoked.");
		 
		 LOGGER.trace(filePaths);
		 
		 JsonArray jsonArray = null;
		 
		 if(neuralNetworkNames != null) {
			 jsonArray = buildNeuralNetworkNamesResponse(neuralNetworkNames);
		 }

		String base64ImageStringsCleaned = imageBase64Strings.toString();
		long preClean = System.currentTimeMillis();
		int b64StringSize = base64ImageStringsCleaned.length();
		//base64ImageStringsCleaned = base64ImageStringsCleaned.substring(1, b64StringSize);
		base64ImageStringsCleaned = base64ImageStringsCleaned.replace("[", "");
		base64ImageStringsCleaned = base64ImageStringsCleaned.replace("]", "");
		long postClean = System.currentTimeMillis();
		long cleanTime = postClean-preClean;
		
		String filePathsCleaned = filePaths.toString();
		
		int filePathsSize = filePathsCleaned.length();
		filePathsCleaned = filePathsCleaned.substring(1, filePathsSize);
		
		String responseString = null;
		
		if(networkMapData == null) {
			responseString = jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned;
		}
		else {
			String networkMapImagePNG = networkMapData.getNetworkMapImage_Base64_String();
			String networkMapImageNIIPath = networkMapData.getCorrespondingNiftiFilePathName();
			responseString = jsonArray + DELIMITER_NEURAL_NAMES + networkMapImagePNG + 
					         DELIMITER_NETWORK_MAP_ITEMS +  networkMapImageNIIPath + DELIMITER_NETWORK_MAP_DATA +
					         base64ImageStringsCleaned + DELIMITER + filePathsCleaned;
			System.out.println(filePaths.get(0));
		}
						
		try {
	      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
		  response.getWriter().println(responseString);
	      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		 LOGGER.trace(loggerId + "sendThresholdImagesResponse()...exit.");
	}
	
	/**
	 * Utilitarian method for testing the download functionality.
	 * 
	 * @param resp The current HttpServletResponse object
	 * @param req The current HttpServletRequest object
	 */
	public static void sendTestFile(HttpServletResponse resp, HttpServletRequest req) {
		
		 resp.setContentType("image/png");
	        resp.setHeader("Content-disposition", "attachment; filename=DCAN.png");
	 
	        try(InputStream in = req.getServletContext().getResourceAsStream("/WEB-INF/DCAN.png");
	          OutputStream out = resp.getOutputStream()) {
	 
	            byte[] buffer = new byte[1048];
	        
	            int numBytesRead;
	            while ((numBytesRead = in.read(buffer)) > 0) {
	                out.write(buffer, 0, numBytesRead);
	            }
	        }
		catch(Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}
	}
}
