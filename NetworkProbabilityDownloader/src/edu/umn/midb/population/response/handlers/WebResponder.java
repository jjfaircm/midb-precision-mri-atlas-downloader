package edu.umn.midb.population.response.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.utils.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.utils.NetworkMapData;
import edu.umn.midb.population.atlas.utils.TokenManager;
import edu.umn.midb.population.atlas.utils.Utils;
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
	
	public static void sendCreateMenuResponse(ApplicationContext appContext, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendCreateMenuResponse()...invoked.");

		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println("Study created, please refresh page to view new menu");
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }

		
		LOGGER.trace(loggerId + "sendCreateMenuResponse()...exit.");

	}
	
	
	public static void sendMenuDataResponse(HttpServletResponse response, ApplicationContext appContext) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendMenuDataResponse()...invoked.");

		
		String menuResponse = "";
		
        ArrayList<String> menuStudyNames = AtlasDataCacheManager.getInstance().getMenuStudyNames();
        Hashtable<String, ArrayList<String>> menuSubOptionsMap = AtlasDataCacheManager.getInstance().getMenuOptionsMap();
		menuResponse = buildMenuResponse(menuStudyNames, menuSubOptionsMap);
		
		TokenManager tokenManager = new TokenManager();
		String token = tokenManager.getToken();
		appContext.setTokenManager(tokenManager);
		
		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(token + "&&&" + menuResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }

		LOGGER.trace(loggerId + "sendMenuDataResponse()...exit.");

	}

	
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
	
	public static JsonArray buildMenuResponseOld(ArrayList<String> menuStudyNames, Hashtable<String, ArrayList<String>> menuSubOptionsMap) {
		   String loggerId = ThreadLocalLogTracker.get();
		   LOGGER.trace(loggerId + "buildMenuResponse()...invoked.");

		   JsonArray menuJsonArray = new JsonArray();
		   Iterator<String> studyNamesIt = menuStudyNames.iterator();
		   String currentStudyName = null;
		   JsonPrimitive colonPrimitive = new JsonPrimitive(":");
		   JsonPrimitive doubleColonPrimitive = new JsonPrimitive("::");

		   ArrayList<String> subMenuOptions = null;
		    // loop through your elements
		    while(studyNamesIt.hasNext()) {
		    	currentStudyName = studyNamesIt.next();
	        	JsonPrimitive jsonPrimitiveStudy = new JsonPrimitive(currentStudyName);
		    	menuJsonArray.add(jsonPrimitiveStudy);
		    	subMenuOptions = menuSubOptionsMap.get(currentStudyName);
		    	String anOption = null;
		    	Iterator<String> subMenuOptionsIt = subMenuOptions.iterator();
		        JsonArray submneuOptionsJsonArray = new JsonArray();
		        
		        while(subMenuOptionsIt.hasNext()) {
		        	anOption = subMenuOptionsIt.next();
		        	JsonPrimitive jsonPrimitive = new JsonPrimitive(anOption);
		        	submneuOptionsJsonArray.add(jsonPrimitive);
		        }
		        menuJsonArray.add(colonPrimitive);
		        menuJsonArray.add(submneuOptionsJsonArray);
		        if(studyNamesIt.hasNext()) {
		        	menuJsonArray.add(doubleColonPrimitive);
		        }
		        
		    }
			LOGGER.trace(loggerId + "buildMenuResponse()...menuJsonArray=" + menuJsonArray);
		    return menuJsonArray;
	}
	
	public static String buildMenuResponse(ArrayList<String> menuStudyNames, Hashtable<String, ArrayList<String>> menuSubOptionsMap) {
		   String loggerId = ThreadLocalLogTracker.get();
		   LOGGER.trace(loggerId + "buildMenuResponse()...invoked.");

		   String responseString = "";
		   String colon = ":";
		   String doubleColon = "::";
		   String comma = ",";
		   
		   ArrayList<String> subMenuOptions = null;
		   Iterator<String> studyNamesIt = menuStudyNames.iterator();
		   String currentStudyName = null;

		    while(studyNamesIt.hasNext()) {
		    	currentStudyName = studyNamesIt.next();
		    	responseString += currentStudyName;
		    	responseString += colon;
		    	
		    	subMenuOptions = menuSubOptionsMap.get(currentStudyName);
		    	Iterator<String> subMenuOptionsIt = subMenuOptions.iterator();
		    	String anOption = null;

		        
		    	while(subMenuOptionsIt.hasNext()) {
		        	anOption = subMenuOptionsIt.next();
			    	responseString += anOption;

		        	if(subMenuOptionsIt.hasNext()) {
				    	responseString += comma;
		        	}
		        	else if(studyNamesIt.hasNext()) {
				    	responseString += doubleColon;
		        	}
		    	}
		    }
			LOGGER.trace(loggerId + "buildMenuResponse()...responseString=" + responseString);
			return responseString;
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
		 
		 //LOGGER.trace(filePaths);
		 
		 JsonArray jsonArrayNetworkNames = null;
		 //String menuResponse = null;
		 
		 if(neuralNetworkNames != null) {
			 jsonArrayNetworkNames = buildNeuralNetworkNamesResponse(neuralNetworkNames);
             ArrayList<String> menuStudyNames = AtlasDataCacheManager.getInstance().getMenuStudyNames();
             Hashtable<String, ArrayList<String>> menuSubOptionsMap = AtlasDataCacheManager.getInstance().getMenuOptionsMap();
			 //menuResponse = buildMenuResponse(menuStudyNames, menuSubOptionsMap);
			 //LOGGER.trace(loggerId + "jsonArray follows");
			 //LOGGER.trace(jsonArray);
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
			responseString = jsonArrayNetworkNames + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned;
		}
		else {
			String networkMapImagePNG = networkMapData.getNetworkMapImage_Base64_String();
			String networkMapImageNIIPath = networkMapData.getCorrespondingNiftiFilePathName();
			responseString = jsonArrayNetworkNames + DELIMITER_NEURAL_NAMES + networkMapImagePNG + 
					         DELIMITER_NETWORK_MAP_ITEMS +  networkMapImageNIIPath + DELIMITER_NETWORK_MAP_DATA +
					         base64ImageStringsCleaned + DELIMITER + filePathsCleaned;
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
	
	public static void sendAdminValidationResponse(HttpServletResponse response, ApplicationContext appContext, String token, String password, String ipAddress) {
		
		TokenManager tokenManager = appContext.getTokenManager();
		boolean isValid = tokenManager.validateToken(token, password, ipAddress);
		appContext.setAdminActionValidated(isValid);
		
		String responseString = (isValid) ? "true":"false";
		
		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		
	}
	
	public static void sendRemoveStudyResponse(HttpServletResponse response, String studyFolder) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendRemoveStudyResponse()...invoked.");
		
		String responseString = "Study successfully removed: " + studyFolder
				              + "<br>Please refresh page to see current menu";

		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		 
	}
}
