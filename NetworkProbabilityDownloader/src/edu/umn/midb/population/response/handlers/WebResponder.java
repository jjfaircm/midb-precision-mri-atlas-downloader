package edu.umn.midb.population.response.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.http.Cookie;
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
import edu.umn.midb.population.atlas.data.access.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.data.access.DBManager;
import edu.umn.midb.population.atlas.data.access.EmailAddressRecord;
import edu.umn.midb.population.atlas.data.access.FileDownloadRecord;
import edu.umn.midb.population.atlas.data.access.WebHitRecord;
import edu.umn.midb.population.atlas.menu.Menu;
import edu.umn.midb.population.atlas.menu.MenuEntry;
import edu.umn.midb.population.atlas.menu.NetworkMapData;
import edu.umn.midb.population.atlas.menu.SingleNetworkFoldersConfig;
import edu.umn.midb.population.atlas.menu.StudySummary;
import edu.umn.midb.population.atlas.security.TokenManager;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.study.handlers.CreateStudyHandler;
import edu.umn.midb.population.atlas.study.handlers.UpdateStudyHandler;
import edu.umn.midb.population.atlas.tasks.AdminAccessEntry;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import edu.umn.midb.population.atlas.utils.Utils;
import edu.umn.midb.population.atlas.webresponses.AdminValidationResponse;
import edu.umn.midb.population.atlas.webresponses.UpdateMapUrlsResponse;
import logs.ThreadLocalLogTracker;

//import javax.json.*;


/**
 * The WebResponder is responsible for sending responses for the various requests that
 * come to the {@link NetworkProbabilityDownloader}. It will answer the following requests, among others:
 * <ul>
 * <li>get menu data which will return the menu and sub-menu options to build the study menu
 * <li>get threshold images associated with the probabilistic thresholds for a given network type
 * <li>download an NII file for a selected threshold
 * <li>send the response after a study had been added
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
	 * Sends the response for the add study action.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param response - HttpServletReponse
	 */
	public static void sendAddStudyResponse(ApplicationContext appContext, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendAddStudyResponse()...invoked.");
		String studyName = appContext.getCreateStudyHandler().getStudyName();
		String responseString = "Study successfully created:<br>";
		responseString += studyName;
		responseString += "<br>Please refresh page to view new menu.";
		CreateStudyHandler csHandler = appContext.getCreateStudyHandler();	
		
		
		if(csHandler.isErrorEncountered()) {
				responseString = csHandler.getErrorMessage();
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
	
	
	/**
	 * Sends the menu configuration data and summary configuration data to the client so the
	 * study menu in the browser can be dynamically built. The data is sent as json objects.
	 * 
	 * @param response - HttpServletRequest
	 * @param appContext - {@link ApplicationContext}
	 */
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
	
	/**
	 * Sends the network folder names configuration as a json object. This configuration has
	 * an entry for each study and each entry specifies the display names and related folder
	 * names of the available single networks data for a given study (such as Default Mode Network - DMN);
	 * 
	 * @param response - HttpServletResponse
	 */
	public static void sendNetworkFolderNamesConfigResponse(HttpServletResponse response) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendNetworkFolderNamesConfigResponse()...invoked.");

		String responseString = buildNetworkFoldersConfigResponse();
	
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
	 * Sends the binary buffer of the requested file to be downloaded.
	 * 
	 * @param response - The current HttpServletResponse object
	 * @param fileBinaryBuffer - binaryBuffer containing the requested NII file
	 * @param fileName - String representing the name of the requested NII file to download
	 * @param selectedStudy - String representing what study the file is a part of
	 */
	public static void sendFileDownloadResponse(HttpServletResponse response, byte[] fileBinaryBuffer, String fileName, String selectedStudy) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendFileDownloadResponse()...invoked.");
		
		
		if(fileName.contains("surface.zip")) {
			//this allows client javascript to detect when download is complete
			Cookie ck = new Cookie("np_download_name", "surface.zip");
			ck.setMaxAge(900);
			ck.setHttpOnly(false);
			ck.setPath("/");
			response.addCookie(ck);
		}
		
		String studyNamePrefix = null;
		
		if(selectedStudy != null) {
			studyNamePrefix = selectedStudy + "_";
		}
		
		// admin files like sample_files.zip or add_a_study.docx do not have an
		// associated study
		if(selectedStudy != null && !fileName.startsWith(studyNamePrefix)) {
			fileName = studyNamePrefix + fileName;
		}
		
		
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
	 * Converts the menu configuration into an ArrayList of {@link MenuEntry} objects
	 * and then loads them into a {@link Menu} object. The Menu object is then converted
	 * to a json object.
	 * 
	 * @param menuStudyNames - ArrayList of the study names that are in the menu
	 * @param menuSubOptionsMap - A Hashtable of the subOptions for each study
	 * @return jsonString - String
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
	
	
	/**
	 * Builds the network folders configuration for each menu into a json object.
	 * 
	 * 
	 * @return folderNamesConfigJSON - String
	 */
	protected static String buildNetworkFoldersConfigResponse() {

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
	
	/**
	 * Builds all the summary configurations for each study into a json object.
	 * 
	 * @return  summaryJSON - String
	 */
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
	
	/**
	 * Builds the file download records from the database into a json object.
	 * 
	 * @param fileDownloads - {@link FileDownloadRecord}
	 * 
	 * @return fileDownloadsJSON - json object
	 */
	protected static String buildFileDownloadsResponse(ArrayList<FileDownloadRecord> fileDownloads) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildFileDownloadsResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String fileDownloadsJSON = gson.toJson(fileDownloads);
		 LOGGER.trace(loggerId + "buildFileDownloadsResponse()...exit.");
		 
		 return fileDownloadsJSON;
	}
	
	/**
	 * Builds the admin validation response as a json object.
	 * 
	 * @param validationMessage - String
	 * @return jsonResponse - String
	 */
	protected static String buildAdminValidationResponse(String validationMessage) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildAdminValidationResponse()...invoked.");

		 AdminValidationResponse avr = new AdminValidationResponse();
		 avr.setValidationMessage(validationMessage);
		 
		 String webHitsMapURL = "/HTML/map_error.html";
		 String downloadHitsMapURL = "/HTML/map_error.html";
		 
		 try {
			 webHitsMapURL = DBManager.getInstance().getWebHitsMapURL();
			 downloadHitsMapURL = DBManager.getInstance().getDownloadHitsMapURL();
		 }
		 catch(SQLException sqlE) {
			 LOGGER.error(loggerId + "buildAdminValidationResponse()....unable to retrieve webHitsMapURL.");
		 }
		 
		 avr.setWebHitsMapURL(webHitsMapURL);
		 avr.setDownloadsMapURL(downloadHitsMapURL);
		 
		 Gson gson = new Gson();
		 String jsonResponse = gson.toJson(avr);
		
		 LOGGER.trace(loggerId + "buildAdminValidationResponse()...exit.");
		 return jsonResponse;

	}
	
	/**
	 * Converts the admin access records retrieved from the database into a json object.
	 * 
	 * @param aaRecords - ArrayLst of {@link AdminAccessRecord}
	 * @return aaRecordsJSON - json representation of the admin access records
	 */
	protected static String buildAdminAccessRecordsResponse(ArrayList<AdminAccessRecord> aaRecords) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildAdminAccessRecordsResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String aaRecordsJSON = gson.toJson(aaRecords);
		 LOGGER.trace(loggerId + "buildAdminAccessRecordsResponse()...exit.");
		 
		 return aaRecordsJSON;
	}
	
	
	/**
	 * Converts the email addresses retrieved from the database into a json object.
	 * 
	 * @param emailAddresses - ArrayList of {@link EmailAddressRecord}
	 * @return emailAddressesJSON - json object
	 */
	protected static String buildEmailAddressesResponse(ArrayList<EmailAddressRecord> emailAddresses) {
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildEmailAddressesResponse()...invoked.");
		 
		 Gson gson = new Gson();
		 String emailAddressesJSON = gson.toJson(emailAddresses);
		 LOGGER.trace(loggerId + "buildEmailAddressesResponse()...exit.");
		 
		 return emailAddressesJSON;
	}
	
	/**
	 * Creates a new {@link UpdateMapUrlsResponse} and converts it to a json object.
	 * 
	 * 
	 * @param messageToDisplay - String
	 * @param targetMap - Either WEB_HITS_MAP or FILE_DOWNLOADS_MAP
	 * @return jsonResponse - String
	 * @throws SQLException - unhandled exception
	 */
	protected static String buildUpdateMapUrlResponse(String messageToDisplay, String targetMap) throws SQLException {
		

		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "buildUpdateMapUrlResponse()...invoked.");

		 UpdateMapUrlsResponse uwhResponse = new UpdateMapUrlsResponse();
		 uwhResponse.setMessage(messageToDisplay);
		 
		 String webHitsMapURL = DBManager.getInstance().getWebHitsMapURL();
		 String downloadsMapURL = DBManager.getInstance().getDownloadHitsMapURL();
		  
		 uwhResponse.setWebHitsMapURL(webHitsMapURL);
		 uwhResponse.setDownloadsMapURL(downloadsMapURL);
		 
		 Gson gson = new Gson();
		 String jsonResponse = gson.toJson(uwhResponse);
		
		 LOGGER.trace(loggerId + "buildUpdateMapUrlResponse()...exit.");
		 return jsonResponse;		
	}
	
	/**
	 * Builds the records from the web_hits database table into a json object. 
	 * 
	 * 
	 * @param webHits {@link WebHitRecord}
	 * @return webHitsJSON - String
	 */
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
	 * @param response - The current HttpServletResponse object
	 * @param filePaths - ArrayList of all the .png file names representing the different probabilistic thresholds
	 * @param imageBase64Strings - ArrayList of the .png files in base64 encoded format
	 * @param networkMapData - {@link NetworkMapData}
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
	
	/**
	 * Sends a failed admin access response when due to an expired session.
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param isFileDownloadRequest - boolean
	 */
	public static void sendAdminAccessDeniedResponse(HttpServletResponse response, ApplicationContext appContext, boolean isFileDownloadRequest) {
		String loggerId = ThreadLocalLogTracker.get();
		int actionCount = appContext.getActionCount();
		LOGGER.trace(loggerId + "sendAdminAccessDeniedResponse()...invoked, actionCount=" + actionCount);
		LOGGER.trace(loggerId + appContext.getActionList());
		
		String responseString = null;
		AdminAccessEntry aaEntry = null;
		
		if(isFileDownloadRequest) {
			String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", "Session_Expired.txt");
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
			//even if the actionCount>1 the user may still just try again after first alert
			//so just repeat same message
			responseString = "Access denied: Session has expired.<br> Please refresh browser page.";
			aaEntry.setAction(appContext.getCurrentAction());
			aaEntry.setRequestorIPAddress(appContext.getRemoteAddress());
			aaEntry.setFormattedTimeStamp(appContext.getCurrentActionFormattedTimestamp());
			aaEntry.setAppContext(appContext);
			aaEntry.setRequest(appContext.getCurrentReguest());
			aaEntry.setResponse(response);	
			TokenManager tokenMgr = appContext.getTokenManager();

			if(!tokenMgr.isValidPassword()) {
				aaEntry.setValidPassword(false);
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
	
	/**
	 * Sends the response for an adminValidation request
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param token - the 1-time use token required to login to admin console
	 * @param password - String
	 * @param ipAddress - String
	 */
	public static void sendAdminValidationResponse(HttpServletResponse response, ApplicationContext appContext, String token, String password, String ipAddress) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendAdminValidationResponse()...invoked.");

		boolean isValid = false;
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
		
		String jsonResponse = buildAdminValidationResponse(responseString);
	
		try {
			  response.getWriter().println(jsonResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendAdminValidationResponse()...exit.");
	}
	
	/**
	 * Sends a response indicating if admin console access is still valid (not expired)
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param response - HttpServletResponse
	 */
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
	
	/**
	 * Sends the response for a remove study request.
	 * 
	 * @param response - HttpServletResponse
	 * @param studyFolder - The name of the study folder which serves as the study id.
	 */
	public static void sendRemoveStudyResponse(HttpServletResponse response, String studyFolder) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendRemoveStudyResponse()...invoked.");
		
		String responseString = "Study successfully removed:<br> " + studyFolder
				              + "<br>Please refresh page to see current menu";

		try {
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendRemoveStudyResponse()...exit.");
	}
	
	/**
	 * Sends the response for a resync web hits request.
	 * 
	 * @param response - HttpServletResponse
	 * @param responseString - The response message
	 */
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
	
	
	/**
	 * Sends the response for a request to display available free storage on the server.
	 * 
	 * @param response - HttpServletResponse
	 * @param statsResponse - The response message
	 */
	public static void sendStorageStatsResponse(HttpServletResponse response, String statsResponse) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendStorageStatsResponse()...invoked");
		
		try {
			  response.getWriter().println(statsResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	    }
		LOGGER.trace(loggerId + "sendStorageStatsResponse()...exit");
	}
	
	/**
	 * Sends the response for a request to update a map url (this is the embed link for
	 * displaying a google map).
	 * 
	 * @param response - HttpServletResponse
	 * @param updatedRowCount - This value should be equal to 1
	 * @param targetMap - name of the map that was updated (WEB_HITS_MAP or FILE_DOWNLOADS_MAP)
	 * @param newURL - The new url that was inserted into the database
	 * @throws SQLException - Unhandled exception
	 */
	public static void sendUpdateMapURLResponse(HttpServletResponse response, int updatedRowCount, String targetMap, String newURL) throws SQLException {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendUpdateMapURLResponse()...invoked");

		String responseString = null;

		if(updatedRowCount==1) {
			responseString = "Successfully updated " + targetMap;
		}
		else {
			responseString = "Unable to update " + targetMap;
		}
		
		String jsonResponse = buildUpdateMapUrlResponse(responseString, targetMap);
		
		
		try {
			  response.getWriter().println(jsonResponse);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		
		LOGGER.trace(loggerId + "sendUpdateMapURLResponse()...exit");
	}
	
	/**
	 * Sends the response for updating a study.
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param response - HttpServletResponse
	 * @param updateHandler - {@link UpdateStudyHandler}
	 */
	public static void sendUpdateStudyResponse(ApplicationContext appContext, HttpServletResponse response, UpdateStudyHandler updateHandler) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendUpdateStudyResponse()...invoked");

		String studyId = updateHandler.getStudyId();
		
		String responseString = "Successfully updated study: " + studyId + "<br>" +
		                        "Refresh page to view changes";
		
		if(updateHandler.isErrorEncountered()) {
			responseString = updateHandler.getErrorMessage();
		}
		
		try {
			  response.getWriter().println(responseString);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		
		LOGGER.trace(loggerId + "sendUpdateStudyResponse()...exit");
	}

	
	/**
	 * Sends the response after a file has been uploaded for adding a study.
	 * 
	 * @param response - HttpServletResponse
	 * @param fileName - String
	 */
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
	
	/**
	 * Sends the file download records as a json object
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param fileDownloads - ArrayList of {@link FileDownloadRecord}
	 */
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
	
	/**
	 * 
	 * Sends the response for getAdminAccessRecords request
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param aaRecords - ArrayList of {@link AdminAccessRecord}
	 */
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
	
	/**
	 * Sends the emailAddress records as a json object
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param emailAddresses - ArrayList of {@link EmailAddressRecord}
	 */
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
	
	/**
	 * Sends the encrypted result of a String input by an admin user
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param encryptedText - String
	 */
	public static void sendEncrytpionRequestResponse(HttpServletResponse response, ApplicationContext appContext, String encryptedText) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendEncrytpionRequestResponse()...invoked");

		try {
			  response.getWriter().println(encryptedText);
		      Thread.sleep(1000);
		}
	    catch(Exception e) {
	    	  LOGGER.error(e.getMessage(), e);
	     }
		LOGGER.trace(loggerId + "sendEncrytpionRequestResponse()...exit");
	}
	
	
	
	
	/**
	 * Sends the collection of {@link WebHitRecord}	objects as a json string to the client. 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param webHits - ArrayList of {@link WebHitRecord}
	 */
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
	
	/**
	 * Sends the url for the web hits map.
	 * 
	 * @param response - HttpServletResponse
	 * @param appContext - {@link ApplicationContext}
	 * @param mapURL - String
	 */
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
