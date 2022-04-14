package edu.umn.midb.population.response.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
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
import edu.umn.midb.population.atlas.data.access.AdminAccessRecord;
import edu.umn.midb.population.atlas.data.access.DBManager;
import edu.umn.midb.population.atlas.data.access.EmailAddressRecord;
import edu.umn.midb.population.atlas.data.access.FileDownloadRecord;
import edu.umn.midb.population.atlas.data.access.WebHitRecord;
import edu.umn.midb.population.atlas.menu.Menu;
import edu.umn.midb.population.atlas.menu.MenuEntry;
import edu.umn.midb.population.atlas.menu.SingleNetworkFoldersConfig;
import edu.umn.midb.population.atlas.menu.StudySummary;
import edu.umn.midb.population.atlas.security.TokenManager;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.tasks.AdminAccessEntry;
import edu.umn.midb.population.atlas.utils.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.utils.CreateStudyHandler;
import edu.umn.midb.population.atlas.utils.NetworkMapData;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import edu.umn.midb.population.atlas.utils.Utils;
import logs.ThreadLocalLogTracker;

//import javax.json.*;


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
	
	public static void sendAddStudyResponse(ApplicationContext appContext, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendAddStudyResponse()...invoked.");
		String studyName = appContext.getCreateStudyHandler().getStudyName();
		String responseString = "Study successfully created:<br>";
		responseString += studyName;
		responseString += "<br>Please refresh page<br>to view new menu.";
		CreateStudyHandler csHandler = appContext.getCreateStudyHandler();	
		
		
		if(csHandler.createStudyHasError()) {
				responseString = csHandler.getCreateStudyErrorMessage();
		}

		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  LOGGER.trace(loggerId + "sendCreateStudyResponse()...response=" + responseString);

			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		
		LOGGER.trace(loggerId + "sendAddStudyResponse()...exit.");

	}
	
	
	public static void sendMenuDataResponse(HttpServletResponse response, ApplicationContext appContext) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendMenuDataResponse()...invoked.");

		
		String menuResponse = "";
		String summaryData = "";
		
		summaryData = buildSummaryResponse();
		
		menuResponse = summaryData + "&&&";
		
        ArrayList<String> menuStudyNames = AtlasDataCacheManager.getInstance().getMenuStudyNames();
        Hashtable<String, ArrayList<String>> menuSubOptionsMap = AtlasDataCacheManager.getInstance().getMenuOptionsMap();
		
        //buildMenuData(menuStudyNames, menuSubOptionsMap);
        menuResponse += buildMenuResponse(menuStudyNames, menuSubOptionsMap);
		
		String token = appContext.getTokenManager().getToken();
		
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
	
	
	public static void sendNetworkFolderNamesConfigResponse(HttpServletResponse response) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendNetworkFolderNamesConfigResponse()...invoked.");

		ArrayList<String> configLines = AtlasDataCacheManager.getInstance().getNeuralNetworkFolderNamesConfig();
		String responseString = buildNetworkFoldersConfigResponse(configLines);
	
		/*
		if(responseString.endsWith("\n")) {
			int endIndex = responseString.lastIndexOf("\n");
			responseString = responseString.substring(0, endIndex);
		}
		
		if(responseString.endsWith("&&")) {
			int endIndex = responseString.lastIndexOf("&&");
			responseString = responseString.substring(0, endIndex);
		}
	    */
		
		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }

		LOGGER.trace(loggerId + "sendNetworkFolderNamesConfigResponse()...exit.");
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
	
	/*
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
	
	*/
	
	protected static String buildMenuResponse(ArrayList<String> menuStudyNames, Hashtable<String, ArrayList<String>> menuSubOptionsMap) {
		   String loggerId = ThreadLocalLogTracker.get();
		   LOGGER.trace(loggerId + "buildMenuResponse()...invoked.");
		   	   
		   Gson gson = new Gson();
		   Menu menu = new Menu();
		   
		   ArrayList<String> subMenuOptions = null;
		   Iterator<String> studyNamesIt = menuStudyNames.iterator();
		   String currentStudyName = null;
		   MenuEntry menuEntry = null;
		   String id = null;
		   String displayName = null;
		   String dataType= null;
		   
		   int openParenIndex = -1;
		   int closeParenIndex = -1;
		   
		   
		   while(studyNamesIt.hasNext()) {
		    	
		    	currentStudyName = studyNamesIt.next();
		    	
		    	openParenIndex = currentStudyName.indexOf("(");
		    	closeParenIndex = currentStudyName.indexOf(")");
		    	displayName = currentStudyName.substring(0, openParenIndex).trim();
		    	id = currentStudyName.substring(openParenIndex+1, closeParenIndex);
		    	
		    	openParenIndex = currentStudyName.lastIndexOf("(");
		    	closeParenIndex = currentStudyName.lastIndexOf(")");
		    	dataType = currentStudyName.substring(openParenIndex+1, closeParenIndex);

		    	
		    	menuEntry = new MenuEntry();
		    	
		    	menuEntry.setDisplayName(displayName);
		    	menuEntry.setId(id);
		    	menuEntry.setDataType(dataType);
		    	
		    	subMenuOptions = menuSubOptionsMap.get(currentStudyName);
		    	String[] subOptionsArray = new String[subMenuOptions.size()];
		    	subOptionsArray = subMenuOptions.toArray(subOptionsArray);
		    	menuEntry.setSubOptions(subOptionsArray);
		    	menu.addMenuEntry(menuEntry);
		    }
		   
		    String jsonString = gson.toJson(menu);
			LOGGER.trace(loggerId + "buildMenuResponse()...exit.");
		    return jsonString;
	}
	
	protected static String buildNetworkFoldersConfigResponse(ArrayList<String> configLines) {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "buildNetworkFoldersConfigResponse()...invoked.");
		
		String folderNamesConfigJSON = null;
		ArrayList<SingleNetworkFoldersConfig> folderNamesConfigList = new ArrayList<SingleNetworkFoldersConfig>();
		ArrayList<String> studyNames = AtlasDataCacheManager.getInstance().getSummaryStudyNames();
		
		Iterator<String> studyNamesIt = studyNames.iterator();
		String studyName = null;
		SingleNetworkFoldersConfig foldersConfig = null;
		ArrayList<String> folderNamesList = null;
		
		while(studyNamesIt.hasNext()) {
			//object representing folder names config for a specific study
			foldersConfig = new SingleNetworkFoldersConfig();
			studyName = studyNamesIt.next();
			foldersConfig.setId(studyName);
			//folderNamesList contains config for the current study name
			folderNamesList = AtlasDataCacheManager.getInstance().getNeuralNetworkFolderNamesConfig(studyName);
			foldersConfig.setFolderNamesConfig(folderNamesList);
			//folderNamesConfigList contains 1 folders config for each study
			folderNamesConfigList.add(foldersConfig);
		}
		
		Gson gson = new Gson();
		folderNamesConfigJSON = gson.toJson(folderNamesConfigList);
		LOGGER.trace(loggerId + "buildNetworkFoldersConfigResponse()...exit.");
		
		return folderNamesConfigJSON;
	}
	
	protected static String buildSummaryResponse() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "buildSummaryResponse()...invoked.");
		
		String summaryJSON = null;
		ArrayList<StudySummary> summaryList = new ArrayList<StudySummary>();
		ArrayList<String> studyNames = AtlasDataCacheManager.getInstance().getSummaryStudyNames();
		Hashtable<String, ArrayList<String>> summarySubEntriesMap = AtlasDataCacheManager.getInstance().getSummaryEntriesMap();
		
		Iterator<String> studyNamesIt = studyNames.iterator();
		String studyName = null;
		ArrayList<String> summarySubEntries = null;
		Iterator<String> summaryEntriesIt = null;
		String summaryEntry = null;
		StudySummary studySummary = null;
		
		while(studyNamesIt.hasNext()) {
			studySummary = new StudySummary();
			studyName = studyNamesIt.next();
			studySummary.setId(studyName);
			summarySubEntries = summarySubEntriesMap.get(studyName);
			studySummary.setSummaryEntries(summarySubEntries);
			summaryList.add(studySummary);
		}
		
		Gson gson = new Gson();
	    summaryJSON = gson.toJson(summaryList);
		LOGGER.trace(loggerId + "buildSummaryResponse()...exit.");		
		return summaryJSON;
	}
	
	protected static String buildFileDownloadsResponse(ArrayList<FileDownloadRecord> fileDownloads) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildFileDownloadsResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String fileDownloadsJSON = gson.toJson(fileDownloads);
		 LOGGER.trace(loggerId + "buildFileDownloadsResponse()...exit.");
		 
		 return fileDownloadsJSON;
	}
	
	protected static String buildAdminAccessRecordsResponse(ArrayList<AdminAccessRecord> aaRecords) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildAdminAccessRecordsResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String aaRecordsJSON = gson.toJson(aaRecords);
		 LOGGER.trace(loggerId + "buildAdminAccessRecordsResponse()...exit.");
		 
		 return aaRecordsJSON;
	}
	
	
	protected static String buildEmailAddressesResponse(ArrayList<EmailAddressRecord> emailAddresses) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildEmailAddressesResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String emailAddressesJSON = gson.toJson(emailAddresses);
		 LOGGER.trace(loggerId + "buildEmailAddressesResponse()...exit.");
		 
		 return emailAddressesJSON;
	}
	
	protected static String buildWebHitsResponse(ArrayList<WebHitRecord> webHits) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildWebHitsResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String webHitsJSON = gson.toJson(webHits);
		 LOGGER.trace(loggerId + "buildWebHitsResponse()...exit.");
		 
		 return webHitsJSON;
	}
	
	/**
	 * Sends the .png files associated with the different probabilistic threhsolds for a selected
	 * neural network.  The files are sent as a list of base64 encoded strings.
	 * 
	 * @param response The current HttpServletResponse object
	 * @param filePaths ArrayList of all the .png file names representing the different probabilistic thresholds
	 * @param imageBase64Strings ArrayList of the .png files in base64 encoded format
	 * @param networkMapData {@link NetworkMapData}
	 */
	public static void sendThresholdImagesResponse(HttpServletResponse response, ArrayList<String> filePaths, ArrayList<String> imageBase64Strings,
			                                       NetworkMapData networkMapData) {
		
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "sendThresholdImagesResponse()...invoked.");
		 
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
			responseString = base64ImageStringsCleaned + DELIMITER + filePathsCleaned;
		}
		else {
			String networkMapImagePNG = networkMapData.getNetworkMapImage_Base64_String();
			String networkMapImageNIIPath = networkMapData.getCorrespondingNiftiFilePathName();
			responseString = networkMapImagePNG + 
					         DELIMITER_NETWORK_MAP_ITEMS +  networkMapImageNIIPath + DELIMITER_NETWORK_MAP_DATA +
					         base64ImageStringsCleaned + DELIMITER + filePathsCleaned;
		}
						
		try {
		  response.getWriter().println(responseString);
	      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		 
		 double responseLengthMeg = responseString.length()/(1024*1000d);
		 DecimalFormat df = new DecimalFormat("0.00");
		 String resultMeg = df.format(responseLengthMeg);
		 LOGGER.trace(loggerId + "sendThresholdImagesResponse()...exit, response length meg=" + resultMeg);
	}
	
	/**
	 * Utility method for testing the download functionality.
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
	
	public static void sendAdminAccessDeniedResponse(HttpServletResponse response, ApplicationContext appContext, boolean isFileDownloadRequest) {
		String loggerId = ThreadLocalLogTracker.get();
		int actionCount = appContext.getActionCount();
		LOGGER.trace(loggerId + "sendAdminAccessDeniedResponse()...invoked, actionCount=" + actionCount);
		LOGGER.trace(loggerId + appContext.getActionList());
		
		String responseString = null;
		AdminAccessEntry aaEntry = null;
		
		if(isFileDownloadRequest) {
			String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", "Access_Denied.txt");
            response.setHeader(headerKey, headerValue);
		}
		
		if(appContext.getActionCount()==1) {
			//if this is the first action, the session has timed out because the
			//user has signed in to the admin console, but left the screen idle for
			//more than a half hour
			responseString = "Access denied: Session has expired.<br> Please refresh browser page.";
		}
		else {
			aaEntry = new AdminAccessEntry();
			responseString = "Access denied.";
			boolean invalidIP = appContext.getTokenManager().isInvalidIP();
			if(invalidIP) {
				aaEntry.setValidIP(false);
			}
			
			aaEntry.setAction(appContext.getCurrentAction());
			aaEntry.setRequestorIPAddress(appContext.getRemoteAddress());
			aaEntry.setFormattedTimeStamp(appContext.getCurrentActionFormattedTimestamp());
			aaEntry.setAppContext(appContext);
			aaEntry.setRequest(appContext.getCurrentReguest());
			aaEntry.setResponse(response);	
			TokenManager tokenMgr = appContext.getTokenManager();
			if(!tokenMgr.isValidIP()) {
				aaEntry.setValidIP(false);
			}
			if(!tokenMgr.isValidPassword()) {
				aaEntry.setValidPassword(false);
			}
			
			if(!aaEntry.isValidIP()) {
				String message = "MIDB_APP::::HACK_ATTEMPT:::" + NetworkProbabilityDownloader.getDomainName();
				message += "::::IP_ADDRESS=" + aaEntry.getRequestorIPAddress();
				SMSNotifier.sendNotification(message, "DBManager");
			}
			
			DBManager.getInstance().insertAdminAccessRecord(aaEntry, appContext);
		}
		

		try {
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		
	
	}
	
	public static void sendAdminValidationResponse(HttpServletResponse response, ApplicationContext appContext, String token, String password, String ipAddress) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendAdminValidationResponse()...invoked.");

		boolean isValid = false;
		boolean isValidIP = true;
		boolean isExpired = false;
		boolean isAccessDenied = false;
		TokenManager tokenManager = null;
		tokenManager = appContext.getTokenManager();
		
		String currentAction = appContext.getCurrentAction();
		
		if(tokenManager != null) {
			isValid = tokenManager.validateToken(token, password, ipAddress);
			if(!isValid) {
				if(tokenManager.isTokenExpired()) {
					isExpired = true;
				}
				if(tokenManager.isAccessDenied()) {
					isAccessDenied = true;
					if(tokenManager.isInvalidIP()) {
						isValidIP = false;

					}
				}
			}
			if(!isExpired) {
				AdminAccessEntry aaEntry = new AdminAccessEntry();
				aaEntry.setAction(appContext.getCurrentAction());
				aaEntry.setRequestorIPAddress(appContext.getRemoteAddress());
				aaEntry.setFormattedTimeStamp(appContext.getCurrentActionFormattedTimestamp());
				aaEntry.setAppContext(appContext);
				aaEntry.setRequest(appContext.getCurrentReguest());
				aaEntry.setResponse(response);
				TokenManager tokenMgr = appContext.getTokenManager();
				boolean validPassword = tokenMgr.isValidPassword();
				if(!isValidIP) {
					aaEntry.setValidIP(false);
				}
				if(!validPassword) {
					aaEntry.setValidPassword(false);
				}
				DBManager.getInstance().insertAdminAccessRecord(aaEntry, appContext);
			}

		}
		
		appContext.setAdminActionValidated(isValid);

		String responseString = (isValid) ? "true":"false";
		if(isExpired) {
			responseString += ":expired";
		}
		else if(isAccessDenied) {
			responseString += ":access_denied";
		}
		
		
		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendAdminValidationResponse()...exit.");
	}
	
	public static void sendAdminValidationStatus(ApplicationContext appContext, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendAdminValidationStatus()...invoked.");
		
		boolean isValid = false;
		
		isValid = appContext.isAdminActionValidated();
		
		String responseString = (isValid) ? "true":"false";

		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendAdminValidationStatus()...exit.");
	}
	
	public static void sendRemoveStudyResponse(HttpServletResponse response, String studyFolder) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendRemoveStudyResponse()...invoked.");
		
		String responseString = "Study successfully removed:<br> " + studyFolder
				              + "<br>Please refresh page<br>to see current menu";

		try {
		      //response.getWriter().println(jsonArray + DELIMITER_NEURAL_NAMES + base64ImageStringsCleaned + DELIMITER + filePathsCleaned);
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendRemoveStudyResponse()...exit.");
	}
	
	public static void sendResynchWebHitsResponse(HttpServletResponse response, String responseString) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendResynchWebHitsResponse()...invoked.");

		try {
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendResynchWebHitsResponse()...exit.");
	}
	
	public static void sendUpdateMapURLResponse(HttpServletResponse response, int updatedRowCount, String targetMap) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendUpdateMapURLResponse()...invoked");

		String responseString = null;

		if(updatedRowCount==1) {
			responseString = "Successfully updated " + targetMap + " url";
		}
		else {
			responseString = "Unable to update " + targetMap + " url";
		}
		
		try {
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		
		LOGGER.trace(loggerId + "sendUpdateMapURLResponse()...exit");
	}

	
	public static void sendUploadFileResponse(HttpServletResponse response, String fileName) {

		String loggerId = ThreadLocalLogTracker.get();
		String responseString = "File successfully uploaded: " + fileName;
		LOGGER.trace(loggerId + "sendUploadFileResponse()...invoked, response=" + responseString);

		try {
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }

		LOGGER.trace(loggerId + "sendUploadFileResponse()...exit.");
	}
	
	public static void sendFileDownloadRecordsResponse(HttpServletResponse response, ApplicationContext appContext, ArrayList<FileDownloadRecord> fileDownloads) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendFileDownloadRecordsResponse()...invoked");
		String jsonResponse = buildFileDownloadsResponse(fileDownloads);

		try {
			  response.getWriter().println(jsonResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendFileDownloadRecordsResponse()...exit");
	}
	
	public static void sendAdminAccessRecordsResponse(HttpServletResponse response, ApplicationContext appContext, ArrayList<AdminAccessRecord> aaRecords) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendEmailAddressesResponse()...invoked");
		String jsonResponse = buildAdminAccessRecordsResponse(aaRecords);

		try {
			  response.getWriter().println(jsonResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendEmailAddressesResponse()...exit");
	}
	
	public static void sendEmailAddressesResponse(HttpServletResponse response, ApplicationContext appContext, ArrayList<EmailAddressRecord> emailAddresses) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendEmailAddressesResponse()...invoked");
		String jsonResponse = buildEmailAddressesResponse(emailAddresses);

		try {
			  response.getWriter().println(jsonResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendEmailAddressesResponse()...exit");
	}
	
	public static void sendWebHitsResponse(HttpServletResponse response, ApplicationContext appContext, ArrayList<WebHitRecord> webHits) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendWebHitsResponse()...invoked");
		String jsonResponse = buildWebHitsResponse(webHits);

		try {
			  response.getWriter().println(jsonResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendWebHitsResponse()...exit");
	}
	
	public static void sendWebHitsMapURLResponse(HttpServletResponse response, ApplicationContext appContext, String mapURL) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendWebHitsMapURLResponse()...invoked");
		
		try {
			  response.getWriter().println(mapURL);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendWebHitsMapURLResponse()...exit");		
	}

}
