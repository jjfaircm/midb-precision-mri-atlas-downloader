package edu.umn.midb.population.atlas.servlet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.config.PropertyManager;
import edu.umn.midb.population.atlas.data.access.DirectoryAccessor;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import edu.umn.midb.population.atlas.security.TokenManager;
import edu.umn.midb.population.atlas.tasks.DownloadTracker;
import edu.umn.midb.population.atlas.tasks.HitTracker;
import edu.umn.midb.population.atlas.utils.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.utils.CreateStudyHandler;
import edu.umn.midb.population.atlas.utils.EmailNotifier;
import edu.umn.midb.population.atlas.utils.NetworkMapData;
import edu.umn.midb.population.atlas.utils.RemoveStudyHandler;
import edu.umn.midb.population.atlas.utils.ZipChunkHandler;
import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;

/**
 * 
 * https://gitlab.com/Fair_lab/midb-precision-functional-atas
 * 
 * The servlet that is the entry point for client requests coming from a web browser.
 * The initial client request from the browser will retrieve a list of the different
 * neural network types, such as aud, co, or others.  The client will then select a network
 * type which results in a request for a collection of png images that map to each probabilistic
 * threshold in 1% steps from 1% to 100%.  The client can then send a request to
 * download the NII file related to the selected probabilistic threshold.
 * 
 * @author jjfair   
 *  
 * multi-level menu with bootstrap video
 * https://www.youtube.com/watch?v=jEAeDID1pks
 * 
 * also see:
 * https://www.cssscript.com/multi-level-navigation/
 * 
 * also see:
 * 
 * https://www.jotform.com/blog/multilevel-drop-down-navigation-menus-examples-and-tutorials/

   1)* Move MIDB...
   2)* New main landing page (landing div) : childmind.org
   3)* copyright (robert, me)
   4)* remove funding
   5)* add upload date
   6)* remove acknowledgements
   7) additional copyright images
   8)* TBD:FUTURE:  Choose data sets
         Human Connectome Project
              Combined
              Single
   9)  Add controls to switch from Volume Data / Surface Data
   10) Change menu processing to generic traversal without IDs
   11) Automate dynamic menu building based on config file
   12) Add admin screens to change menu config
   13) reformat tab selections
   14) add new landing screen (main div/layer)
   15) add logging toggle to javascript
   16) add drag/drop and interface for creating new menu entry
   
   
   @MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1024 * 2,
   maxFileSize = 2147483647, 
   maxRequestSize = 1024 * 1024 * 1024 * 5)
   
   
   https://stackoverflow.com/questions/20212851/slice-large-file-into-chunks-and-upload-using-ajax-and-html5-filereader

   https://coderanch.com/t/479604/java/Creating-file-chunks-files-java
 */



@MultipartConfig(location = "/midb/temp")

public class NetworkProbabilityDownloader extends HttpServlet {
	
	/*
	 * 
	 * <!--[if IE 6]>
           <link rel="stylesheet" type="text/css" href="iespecific.css" />
       <![endif]-->
	 * 
	 * https://css-tricks.com/ie-10-specific-styles/
	 * 
	 */
	/*
	https://innovation.umn.edu/developmental-cognition-and-neuroimaging-lab/
	*/
	/*
	     how to change cached password for git in eclipse:
         https://www.programmersought.com/article/70134449534/
         NOTE:  take the contents tab then git then the github url
         https://github.com/jjfaircm/midb-precision-mri-atlas-downloader 
         https://github.com/jjfaircm/midb-precision-mri-atlas-downloader.git
         old repo in gitlab:
         https://gitlab.com/Fair_lab/midb-precision-functional-atas

   */
	
	private static final long serialVersionUID = 1L;
	private static final long VERSION_NUMBER = 0;
	public static final String BUILD_DATE = "Version beta_25.0  1107_2021:00:00__war=NPDownloader_1107.war"; 
	public static final String CONTENT_TEXT_PLAIN = "text/plain";
	public static final String CHARACTER_ENCODING_UTF8 = "UTF-8";
	public static final String DEFAULT_ROOT_PATH = "/midb/studies/abcd_template_matching/surface/";
	public static final String STUDY_NAME_PLACEHOLDER = "${STUDY_NAME}";
	public static final String DATA_TYPE_PLACEHOLDER = "${DATA_TYPE}";
	public static final String ROOT_PATH = "/midb/studies/${STUDY_NAME}/${DATA_TYPE}/"; 
	//public static final String ROOT_PATH = "/Users/jjfair/midb_old/"; 
	public static final String DEFAULT_NEURAL_NETWORK = "combined_clusters";
	public static final String DIAGNOSTICS_FILE = "/midb/diagnostics/diagnostics.txt";
	public static final String DIAGNOSTICS_DEMARCATION = "*********************************************************************";
	public static final String ADMIN_ACCESS_FILE = "/midb/tracking/admin_access.csv";
	public static Logger LOGGER = null;
	public static final String DOWNLOAD_ENTRY_TEMPLATE = "ID,IP_ADDRESS,TIMESTAMP,DOWNLOAD_REQUESTED_FILE";
	public static final String HIT_ENTRY_TEMPLATE = "ID,IP_ADDRESS,TIMESTAMP,USER_AGENT";
	public static final String ADMIN_ENTRY_TEMPLATE = "IP_ADDRESS,ACTION,TIMESTAMP,USER_AGENT";

    //private static final SimpleDateFormat SDF1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_FORMATTER_FOR_ID = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	public static final String ECLIPSE_RESOURCES_PATH = "/Users/jjfair/git/network_probability_downloader/NetworkProbabilityDownloader/build/classes/edu/umn/midb/population/atlas/config/files/";
	private String localHostName = "UNKNOWN";
	
	/**
	 * Checks for intermittent problem where NGINX generates a duplicate request which originates
	 * from the local loopback interface of 127.0.0.1
	 * 
	 * @param request
	 * @return
	 */
	protected boolean checkDuplicateRequest(HttpServletRequest request) {
		
		if(localHostName.contains("JAMESs-MacBook-Pro")) {
			return false;
		}
		boolean isDuplicate = false;
		String action = null;
		String originalIP = request.getHeader("X-Forwarded-For");
		String requestorIPAddress = request.getRemoteAddr();
		
		if(originalIP != null) {
			if(originalIP.contains("127.0.0.1")) {
				isDuplicate = true;
				action = request.getParameter("action");
				LOGGER.warn("Duplicate request received: action=" + action);
			}
		}
		
		/*
		if(requestorIPAddress.contains("127.0.0.1")) {
			action = request.getParameter("action");
			LOGGER.warn("Duplicate request received: action=" + action);
			isDuplicate = true;
		}
		*/
		
		return isDuplicate;
	}
	
	static protected void createDiagnosticEntry(String entry) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "createDiagnosticEntry()...invoked.");
		
		try {
			FileWriter fw = new FileWriter(DIAGNOSTICS_FILE, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    bw.write(DIAGNOSTICS_DEMARCATION);
		    bw.newLine();
		    bw.write(entry);
		    bw.newLine();
		    bw.write(DIAGNOSTICS_DEMARCATION);
		    bw.newLine();
		    bw.newLine();
		    bw.close();
		}
		catch(Exception e) {
			LOGGER.error(loggerId + "createDiagnosticEntry()...error encountered.");
			LOGGER.error(loggerId + e.getMessage(), e);
		}
		
		LOGGER.trace(loggerId + "createDiagnosticEntry()...invoked.");

	}
	
	/**
	 * Entry point into the servlet. The 'action' parameter is read from the query
	 * string which then directs the request to the appropriate action-handler method.
	 * The action will be one of the following:
	 * <ul>
	 * <li>getNeuralNetworkNames
	 * <li>getThresholdImages
	 * <li>downloadFile
	 * </ul>
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = request.getParameter("action");
				
		if(checkDuplicateRequest(request)) {
			return;
		}
		
		String ipAddress = null;
		
		ipAddress = request.getRemoteAddr();
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
				
		HttpSession session = request.getSession();
		ApplicationContext appContext = (ApplicationContext)session.getAttribute("applicationContext");
		if(appContext==null) {
			appContext = new ApplicationContext();
			appContext.setLoggerId(session.getId());
			//ThreadLocalLogTracker.set(appContext.getLoggerId());
			request.getSession().setAttribute("applicationContext", appContext);
		}
		String loggerId = appContext.getLoggerId();
		try {
			ThreadLocalLogTracker.set(appContext.getLoggerId());
			//action = request.getParameter("action");
			if(action==null) {
				action = request.getQueryString().substring(7,25);
			}
			else {
				action = action.trim();
			}
			LOGGER.trace(loggerId + "doGet() invoked, action param =" + action);
			response.setContentType(CONTENT_TEXT_PLAIN);
			response.setCharacterEncoding(CHARACTER_ENCODING_UTF8);
			
			String queryString = request.getQueryString();
			appContext.addQueryStringToHistoryChain(queryString);
			
			switch (action) {
			
			case "downloadFile":
				handleAjaxDownloadFile(appContext, request, response);
				break;
			case "getMenuData":
				//pause(5000);
				submitHitEntry(request);
				appContext.setTokenManager(new TokenManager());
				handleAjaxGetMenuDataRequest(appContext, request, response);
				break;
			case "getNetworkFolderNamesConfig":
				handleAjaxGetNetworkFolderNamesConfig(appContext, request, response);
				break;
			case "getThresholdImages":
				//pause(30000);
				long gtiActionBeginTime = System.currentTimeMillis();
				String selectedNeuralNetworkName = request.getParameter("neuralNetworkName");

				handleAjaxGetThresholdImages(appContext, request, response, false);
				long gtiActionEndTime = System.currentTimeMillis();
				long gtiProcessingTime = gtiActionEndTime-gtiActionBeginTime;
				//LOGGER.info("NetworkProbabilityDownloader.doGet()...selectedNeuralNetworkName=" + selectedNeuralNetworkName);
				//LOGGER.info("Processing time in ms for getThresholdImages=" + gtiProcessingTime);
				break;
			case "removeStudy":
				updateAdminAccessFile(request, ipAddress, action);
				handleAjaxRemoveStudy(appContext, request, response);
				break;
			case "validateAdminAccess":
				updateAdminAccessFile(request, ipAddress, action);
				handleAjaxValidateAdminAccess(appContext, request, response);
				break;
			case "validateAdminAccessStatus":
				updateAdminAccessFile(request, ipAddress, action);
				handleAjaxValidateAdminAccessStatus(appContext, request, response);
				break;
			}
		
		}
		catch(Exception e) {
			LOGGER.error("doGet()...error encountered", e);
			handleFatalError(appContext, request, response, e);
		} 
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		LOGGER.trace("doPost()...request received.");
		String action = request.getParameter("action");
		HttpSession session = request.getSession();
		ApplicationContext appContext = (ApplicationContext)session.getAttribute("applicationContext");
		String loggerId = appContext.getLoggerId();
		ThreadLocalLogTracker.set(appContext.getLoggerId());
		
		String ipAddress = null;
		
		ipAddress = request.getRemoteAddr();
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
		
		String queryString = request.getQueryString();
		appContext.addQueryStringToHistoryChain(queryString);


		try {
			switch (action) {
			case "uploadStudyFiles":
				LOGGER.trace(loggerId + "doPost()...action=" + action);
				updateAdminAccessFile(request, ipAddress, action);
				response.setContentType(CONTENT_TEXT_PLAIN);
				response.setCharacterEncoding(CHARACTER_ENCODING_UTF8);
				handleAjaxAddStudyRequest(appContext, request, response);
				break;
			case "uploadZipChunks":
				LOGGER.trace(loggerId + "doPost()...action=" + action);
				response.setContentType(CONTENT_TEXT_PLAIN);
				response.setCharacterEncoding(CHARACTER_ENCODING_UTF8);
				handleAjaxUploadZipChunk(appContext, request, response);
				break;
			}
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
			handleFatalError(appContext, request, response, e);
		}

	}

	
	/**
	 * 
	 * Handles any encountered exception by supplying an error description and
	 * a formatted stack trace that is delivered to the client.
	 * 
	 * @param request HttpServletRequest Reference to the current HttpServletRequest
	 * @param response HttpServletResponse Reference to the current HttpServletResponse
	 * @param e Exception Reference to the encountered exception
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void handleFatalError(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {

		String loggerId = appContext.getLoggerId();
		LOGGER.fatal(loggerId + "handleFatalError()...invoked");
		String NEW_LINE = "\r\n";
		String ipAddress = request.getRemoteAddr();
		
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
		
		appContext.setRemoteAddress(ipAddress);
		
		/*
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalTime localTime = LocalTime.now();
		String timeStamp = dtf.format(localTime);
		*/
		//use local system clock
	    String timeString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

		
		String id = request.getSession().getId();
		id += "__";
		id += timeString;
		appContext.setId(id);
		String queryStringHistory = appContext.getQueryStringHistory();
		
		StackTraceElement[] stackTraceEntries = e.getStackTrace();
	    int stackEntriesCount = stackTraceEntries.length;
	    String stackTraceData = BUILD_DATE + NEW_LINE;
	    HttpSession session = request.getSession(false);
	    String attributeName = null;
	    Object sessionObject = null;
	    Object backupSessionObject = null;
	    
	    String specificMessage = e.getMessage();
	    if(specificMessage==null) {
	    	specificMessage = "";
	    }
	    
	    if(session!=null) {
	    	ArrayList<String> allAttributeNames = new ArrayList<String>();
	    	allAttributeNames.add("session attributes follow:" + NEW_LINE );
	    	Enumeration<String> attributeNames = session.getAttributeNames();
	    	while(attributeNames.hasMoreElements()) {
	    		attributeName = attributeNames.nextElement();
	    		allAttributeNames.add(attributeName + NEW_LINE);
	    		sessionObject = session.getAttribute(attributeName);
	    		if(sessionObject != null) {
		    		allAttributeNames.add(sessionObject.toString() + NEW_LINE);
	    		}
	    		else {
	    			allAttributeNames.add("object is null" + NEW_LINE);
	    		}
	    		
	    	}
	    	stackTraceData += allAttributeNames.toString();
	    }	

	    stackTraceData += "" + e + NEW_LINE;
	    LOGGER.fatal(loggerId + e);
	   
	    for(int i=0;i<stackEntriesCount;i++) {
		  LOGGER.fatal(stackTraceEntries[i]);
		  stackTraceData += stackTraceEntries[i] + NEW_LINE;
	    }
	   
	    String fatalErrorPrefix = "$$$_FATAL_BEGIN_$$$";
	    String fatalErrorSuffix = "$$$_FATAL_END_$$$";
		String responseError1 = "An Unexpected Error Occurred: ";
		String responseError2 = "&We are unable to process your request.<br>"
				              + "Show details for more information.&";
		//responseError += stackTraceEntries[0] + "\n";
		//responseError += stackTraceEntries[1] + "\n";
		String completeResponseMessage = fatalErrorPrefix + responseError1 + responseError2 + stackTraceData + 
				                         queryStringHistory + fatalErrorSuffix;
		

		response.getWriter().println(completeResponseMessage);
		response.getWriter().flush();
		createDiagnosticEntry(completeResponseMessage);

		EmailNotifier.sendEmailNotification("INCIDENT_ID=" + id);
		LOGGER.fatal(loggerId + "handleFatalError()...exit");
		return;
		
	}
	
	/**
	 * Handles a request to download an NII file that maps to the selected probabilistic threshold percentage.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request A reference to the current HttpServletRequest
	 * @param response A reference to the current HttpServletResponse
	 */
	protected void handleAjaxDownloadFile(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAjaxDownloadFile()...invoked.");
		String filePathAndName = request.getParameter("filePathAndName");
		LOGGER.trace(loggerId + "handleAjaxDownloadFile()...filePathAndName=" + filePathAndName);
		String ipAddress = request.getRemoteAddr();
		//since we use NGINX as a front-end load distributor on AWS-LINUX then we need the following
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
		byte[] fileBinaryBuffer = DirectoryAccessor.getFileBytes(filePathAndName);
		int slashIndex = filePathAndName.lastIndexOf("/");
		String fileNameOnly = filePathAndName.substring(slashIndex+1);
		LocalDateTime localTime = LocalDateTime.now();
		String formattedTS = DT_FORMATTER_1.format(localTime);
		formattedTS = formattedTS.replace(" ", ",");
		String id = DT_FORMATTER_FOR_ID.format(localTime);
		String downloadEntry = DOWNLOAD_ENTRY_TEMPLATE.replace("ID", id);
		downloadEntry = downloadEntry.replace("IP_ADDRESS", ipAddress);
		downloadEntry = downloadEntry.replace("DOWNLOAD_REQUESTED_FILE", filePathAndName);
		downloadEntry = downloadEntry.replace("TIMESTAMP", formattedTS);
		DownloadTracker.getInstance().addDownloadEntry(downloadEntry);
		WebResponder.sendFileDownloadResponse(response, fileBinaryBuffer, fileNameOnly);
		LOGGER.trace(loggerId + "handleAjaxDownloadFile()...exit.");
		
		return;
	}
	
	protected void handleAjaxGetAdminValidationStatus(ApplicationContext appContext, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAjaxGetAdminValidationStatus()...invoked.");
		
		
		LOGGER.trace(loggerId + "handleAjaxGetAdminValidationStatus()...exit.");
	}
	
	protected void handleAjaxGetMenuDataRequest(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAjaxGetMenuData()...invoked.");

		WebResponder.sendMenuDataResponse(response, appContext);
		
		LOGGER.trace(loggerId + "handleAjaxGetMenuData()...exit.");
	}


	
	protected void handleAjaxGetNetworkFolderNamesConfig(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAjaxGetNetworkFolderNamesConfig()...invoked.");
		
		//ArrayList<String> folderNamesConfig = AtlasDataCacheManager.getInstance().getNeuralNetworkFolderNamesConfig();
		//handleAjaxGetThresholdImages(appContext, request, response, true, folderNamesConfig);
		WebResponder.sendNetworkFolderNamesConfigResponse(response);
		
		LOGGER.trace(loggerId + "handleAjaxGetNetworkFolderNamesConfig()...invoked.");
	}
	
	
	/**
	 * When the client selects a specific neural network, a request to get all the threshold images is sent
	 * to the servlet. This method will retrieve the collection of images from {@link AtlasDataCacheManager}.
	 * It will then forward the request to the {@link WebResponder} for further processing.
	 * 
	 * @param appContext A reference to the {@link ApplicationContext}
	 * @param request A reference to the current HttpServletRequest
	 * @param response A reference to the current HttpServletResponse
	 * @param isFirstRequest boolean
	 * @param neuralNetworkNames A list of available neural network names
	 */
	protected void handleAjaxGetThresholdImages(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response, boolean isFirstRequest) {

		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...invoked.");
		
		boolean isSingleNetworkResponse = false;
		NetworkMapData networkMapData = null;
			
		
		//String rootDirectory = "/myPath";
		
		//if isFirstRequest=true then this is the default request
		//a.k.a. the first request coming in and the arrayList of neuralNetworkNames
		//will be populated, otherwise it will be null.
		//Also if this is the default request then the 'selected' neuralNetworkName
		//will be DEFAULT_NEURAL_NETWORK
		
		String selectedNeuralNetworkName = null;
		//String selectedNeuralNetworkPathName = AtlasDataCacheManager.getInstance().getNetworkPathName(selectedNeuralNetworkName);
		//String selectedNeuralNetworkPathName = null;
		
		if(!isFirstRequest) {
			selectedNeuralNetworkName = request.getParameter("neuralNetworkName");
			LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...selectedNeuralNetworkName=" + selectedNeuralNetworkName);
			//selectedNeuralNetworkPathName = AtlasDataCacheManager.getInstance().getNetworkPathName(selectedNeuralNetworkName);
			LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...selectedNeuralNetworkPathName=" + selectedNeuralNetworkName);
		}
		
		if(!selectedNeuralNetworkName.equals("combined_clusters")) {
			isSingleNetworkResponse = true;
		}
		
		String selectedStudy = request.getParameter("selectedStudy");
		
		//for diagnostic logging test
		if(selectedStudy.contains("bogus")) {
				throw new NullPointerException("test");
		}
		String targetDirectory = ROOT_PATH.replace(STUDY_NAME_PLACEHOLDER, selectedStudy);
		String selectedDataType = request.getParameter("selectedDataType");
		targetDirectory = targetDirectory.replace(DATA_TYPE_PLACEHOLDER, selectedDataType);
		targetDirectory = targetDirectory + selectedNeuralNetworkName;
		LOGGER.trace(targetDirectory);
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...selected network name=" + targetDirectory);

		ArrayList<String> imagePaths = AtlasDataCacheManager.getInstance().getImagePathNames(targetDirectory);
		ArrayList<String> imageBase64Strings = AtlasDataCacheManager.getInstance().getBase64ImagePathStrings(targetDirectory);
		
		if(isSingleNetworkResponse) {
			networkMapData = AtlasDataCacheManager.getInstance().getNetworkMapData(targetDirectory);
		}
		
		WebResponder.sendThresholdImagesResponse(response, imagePaths, imageBase64Strings, networkMapData);
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...exit.");
		
		return;
	}
	
	protected void handleAjaxAddStudyRequest(ApplicationContext appContext, HttpServletRequest request,
			                                   HttpServletResponse response) throws IOException, BIDS_FatalException {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAjaxAddStudyRequest()...invoked.");
		
		//Part part = null;	
		String fileName = null;
		String currentFileNumberString = request.getParameter("currentFileNumber");
		int currentFileNumber = Integer.parseInt(currentFileNumberString);
		String totalFileNumberString = request.getParameter("totalFileNumber");
		int totalFileNumber = Integer.parseInt(totalFileNumberString);
		String fileSizeString = request.getParameter("fileSize");
		long fileSize = Long.parseLong(fileSizeString);
		boolean finished = false;
		
		if(currentFileNumber == totalFileNumber) {
			finished = true;
		}

		CreateStudyHandler createStudyHandler = null;
		if(currentFileNumber == 1) {
			createStudyHandler = new CreateStudyHandler(appContext, request, response);
			appContext.setCreateStudyHandler(createStudyHandler);
			appContext.clearErrors();
			fileName = createStudyHandler.uploadFile(request, fileSize);
		}
		else {
			createStudyHandler = appContext.getCreateStudyHandler();
			fileName = createStudyHandler.uploadFile(request, fileSize);
		}
		
		if(finished) {
			createStudyHandler.completeStudyDeploy();
			WebResponder.sendAddStudyResponse(appContext, response);
		}
		if(!finished) {
			WebResponder.sendUploadFileResponse(response, fileName);
		}
		
		LOGGER.trace(loggerId + "handleAjaxAddStudyRequest()...exit.");


	}
	
	protected void handleAjaxUploadZipChunk(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
		
		    //ZipChunkHandler.uploadChunk(request, response);
	}
	
	protected void handleAjaxRemoveStudy(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response) throws IOException, BIDS_FatalException {
				
		if(!appContext.isAdminActionValidated()) {
			response.getWriter().write("Session timeout<br>study not removed.<br>Please refresh browser page");
			return;
			/*
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String message = "Invalid Admin State";
			BIDS_FatalException bidsFatalError = new BIDS_FatalException(message, ste);
			throw bidsFatalError;
			*/
		}
		
		String studyFolder = request.getParameter("studyFolder");
		RemoveStudyHandler rsh = new RemoveStudyHandler(studyFolder);
		rsh.removeStudy();
		WebResponder.sendRemoveStudyResponse(response, studyFolder);
		
	}

	protected void handleAjaxValidateAdminAccess(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
		
			String loggerId = appContext.getLoggerId();
			LOGGER.trace(loggerId + "handleAjaxValidateAdminAccess()...invoked.");
			
			String token = request.getParameter("token");
			String password = request.getParameter("mriVersion");
			String ipAddress = request.getRemoteAddr();
			
			if(ipAddress.contains("127.0.0.1")) {
				String originalIP = request.getHeader("X-Forwarded-For");
				if(originalIP != null) {
					ipAddress = originalIP;
				}
			}
			
			WebResponder.sendAdminValidationResponse(response, appContext, token, password, ipAddress);

			LOGGER.trace(loggerId + "handleAjaxValidateAdminAccess()...exit.");
	}
	
	protected void handleAjaxValidateAdminAccessStatus(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
		
			String loggerId = appContext.getLoggerId();
			LOGGER.trace(loggerId + "handleAjaxValidateAdminAccessStatus()...invoked.");

			WebResponder.sendAdminValidationStatus(appContext, response);
			LOGGER.trace(loggerId + "handleAjaxValidateAdminAccessStatus()...exit.");			
	}


		
	/**
	 * Override of the init() method which handles instantiation and initialization of the
	 * {@link PropertyManager} and the {@link AtlasDataCacheManager} as well as the dynamic
	 * configuration of the log4j logging mechanism.
	 */
	public void init() {
		//DO NOT USE LOGGER YET BECAUSE LogConfigurator has not run yet
		System.out.println("NetworkProbabilityDownloader.init()...invoked...version=" + BUILD_DATE);
		//we preload PropertyManager because it will invoke the LogConfigurator
		PropertyManager.getInstance();
		LOGGER = LogManager.getLogger(NetworkProbabilityDownloader.class);
		
		try {
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    localHostName = addr.getHostName();
		    LOGGER.info("local machine name=" + localHostName);
		}
		catch (UnknownHostException ex) {
		    LOGGER.error("Hostname can not be resolved");
		    LOGGER.error(ex.getLocalizedMessage(), ex);
		}
		
		AtlasDataCacheManager.getInstance().setLocalHostName(localHostName);
		
		if(localHostName.contains("JAMESs-MacBook-Pro")) {
			AtlasDataCacheManager.getInstance().loadKeyFromFile();
			AtlasDataCacheManager.getInstance().loadSettingsConfig();
		}

		
		Map<String,String> envMap = System.getenv();
		
		LOGGER.trace("Environment Variables follow!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		for (Map.Entry<String, String> entry : envMap.entrySet()) {
			if(entry.getKey().contains("MIDB_ROOT")) {
				LOGGER.trace("EMAIL_ARGS found...continue");
				EmailNotifier.setKey(entry.getValue());
				TokenManager.setKey(entry.getValue());

				continue;
			}
			else if(entry.getKey().contains("MIDB_SERIALIZATION")) {
				LOGGER.trace("MIDB_SERIALIZATION found...continue");
				String[] encryptedArray = entry.getValue().split("::");
				EmailNotifier.setSender(encryptedArray[0]);
				EmailNotifier.setPassword(encryptedArray[1]);
				continue;
			}
			else if(entry.getKey().contains("MIDB_VERSION")) {
				LOGGER.trace("MIDB_VERSION found...continue");
				String encryptedRecipient = entry.getValue();
				EmailNotifier.setRecipient(encryptedRecipient);
				continue;
			}
			else if(entry.getKey().contains("MIDB_MRI")) {
				LOGGER.trace("MIDB_MRI found...continue");
				String encryptedPassword = entry.getValue();
				TokenManager.setPassword(encryptedPassword);
				continue;
			}
			
            LOGGER.trace(entry.getKey() + " : " + entry.getValue());
        }
		
		LOGGER.info("exiting init().");

		// AtlasDataCacheManager.getInstance() will cause the AtlasDataCacheManager
		// to preload the default image data
		DownloadTracker.getInstance();
		HitTracker.getInstance();
		try {
			Class.forName("edu.umn.midb.population.atlas.security.TokenManager");
			Class.forName("edu.umn.midb.population.atlas.utils.EmailNotifier");
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		//get local machine name
		
		LOGGER.info("exiting init().");
	}
	
	protected void submitHitEntry(HttpServletRequest request) {
		
		String ipAddress = request.getRemoteAddr();
		String userAgent = request.getHeader("USER-AGENT");
		
		LOGGER.trace("remoteAddr=" + ipAddress);
		
		/*
		Enumeration<String> headersEnum = request.getHeaderNames();
		LOGGER.trace("HEADERS follow");
		LOGGER.trace(headersEnum);
		String aHeaderName = null;
		String aHeaderValue = null;
		
		while(headersEnum.hasMoreElements()) {
			aHeaderName = headersEnum.nextElement();
			aHeaderValue = request.getHeader(aHeaderName);
			LOGGER.trace(aHeaderName + "=" + aHeaderValue);
		}
	    */
		
		//since we use NGINX as a front-end load distributor on AWS-LINUX then we need the following
		//we make this a conditional check since this might be an environment without a load distributor
		//in front of the servlet container (tomcat, for example)
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
		
		LocalDateTime localTime = LocalDateTime.now();
		String formattedTS = DT_FORMATTER_1.format(localTime);
		formattedTS = formattedTS.replace(" ", ",");
		String id = DT_FORMATTER_FOR_ID.format(localTime);
		String hitEntry = HIT_ENTRY_TEMPLATE.replace("ID", id);
		hitEntry = hitEntry.replace("IP_ADDRESS", ipAddress);
		hitEntry = hitEntry.replace("TIMESTAMP", formattedTS);
		hitEntry = hitEntry.replace("USER_AGENT", userAgent);
		HitTracker.getInstance().addHitEntry(hitEntry);
	}
	
	protected static synchronized void updateAdminAccessFile(HttpServletRequest request, String ipAddress, String action) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateAdminAccessFile()...invoked.");
		
		String userAgent = request.getHeader("USER-AGENT");
		
		LocalDateTime localTime = LocalDateTime.now();
		String formattedTS = DT_FORMATTER_1.format(localTime);
		formattedTS = formattedTS.replace(" ", ",");
		//String id = DT_FORMATTER_FOR_ID.format(localTime);
		String adminEntry = ADMIN_ENTRY_TEMPLATE.replace("IP_ADDRESS", ipAddress);
		adminEntry = adminEntry.replace("ACTION", action);
		adminEntry = adminEntry.replace("TIMESTAMP", formattedTS);
		adminEntry = adminEntry.replace("USER_AGENT", userAgent);

		FileWriter fw = null;
		PrintWriter pw = null;
		
		try {
			fw = new FileWriter(ADMIN_ACCESS_FILE, true);
			pw = new PrintWriter(fw);
			pw.println(adminEntry);
			pw.close();
			LOGGER.info(loggerId + "adminAccess, ipAddress=" + ipAddress);
		}
		catch(IOException ioE) {
			LOGGER.error(loggerId + "Failed to create PrintWriter for file=" + ADMIN_ACCESS_FILE);
			LOGGER.error(ioE.getMessage(), ioE);
		}
		LOGGER.trace(loggerId + "updateAdminAccessFile()...exit.");
	}

	public static void pause(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		}
		catch(Exception e) {
			
		}
	}

}
