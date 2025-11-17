package edu.umn.midb.population.atlas.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import docs.DocumentLocator;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.config.PropertyManager;
import edu.umn.midb.population.atlas.data.access.AdminAccessRecord;
import edu.umn.midb.population.atlas.data.access.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.data.access.DBManager;
import edu.umn.midb.population.atlas.data.access.DirectoryAccessor;
import edu.umn.midb.population.atlas.data.access.EmailAddressRecord;
import edu.umn.midb.population.atlas.data.access.FileDownloadRecord;
import edu.umn.midb.population.atlas.data.access.WebHitRecord;
import edu.umn.midb.population.atlas.exception.BIDS_FatalException;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.menu.NetworkMapData;
import edu.umn.midb.population.atlas.security.TokenManager;
import edu.umn.midb.population.atlas.study.handlers.CreateStudyHandler;
import edu.umn.midb.population.atlas.study.handlers.LockResetStatus;
import edu.umn.midb.population.atlas.study.handlers.LockType;
import edu.umn.midb.population.atlas.study.handlers.RemoveStudyHandler;
import edu.umn.midb.population.atlas.study.handlers.StudyMaintenanceLock;
import edu.umn.midb.population.atlas.study.handlers.UpdateStudyHandler;
import edu.umn.midb.population.atlas.tasks.AdminAccessEntry;
import edu.umn.midb.population.atlas.tasks.DownloadTracker;
import edu.umn.midb.population.atlas.tasks.EmailAddressEntry;
import edu.umn.midb.population.atlas.tasks.EmailTracker;
import edu.umn.midb.population.atlas.tasks.FileDownloadEntry;
import edu.umn.midb.population.atlas.tasks.WebHitEntry;
import edu.umn.midb.population.atlas.tasks.WebHitsTracker;
import edu.umn.midb.population.atlas.utils.CommandRunner;
import edu.umn.midb.population.atlas.utils.CountryNamesResolver;
import edu.umn.midb.population.atlas.utils.IPInfoRequestor;
import edu.umn.midb.population.atlas.utils.IPLocator;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import edu.umn.midb.population.atlas.utils.ServerStorageStats;
import edu.umn.midb.population.atlas.utils.Utils;
import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;

/**
 * 
 * The servlet that is the entry point for client requests coming from a web browser.
 * The initial client request from the browser will retrieve menu data and information
 * from other menu configuration files that allows the client to build a dynamic menu
 * based on entries in the configuration files. After retrieving the menu data
 * the client will then select a neural network type (such as Combined or Integration Zone)
 * which results in a request for a collection of png images that map to each probabilistic
 * threshold in 1% steps from 1% to 100%.  The client can then send a request to
 * download the NII file related to the selected probabilistic threshold.
 * 
 * @author jjfair   
 *  
 */

@MultipartConfig(location = "/midb/temp")

public class NetworkProbabilityDownloader extends HttpServlet {
	
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
	

//  @MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1024 * 2,
//  maxFileSize = 2147483647, 
//  maxRequestSize = 1024 * 1024 * 1024 * 5)

	
	private static final long serialVersionUID = 1L;
	public static final String BUILD_DATE = "Version beta_132.0  1116_02:00_2025__war=NPDownloader_1116_02:00_2025.war"; 
	public static final String CONTENT_TEXT_PLAIN = "text/plain";
	public static final String CHARACTER_ENCODING_UTF8 = "UTF-8";
	public static final String DEFAULT_ROOT_PATH = "/midb/studies/abcd_template_matching/surface/";
	public static final String STUDY_NAME_PLACEHOLDER = "${STUDY_NAME}";
	public static final String DATA_TYPE_PLACEHOLDER = "${DATA_TYPE}";
	public static final String ROOT_PATH = "/midb/studies/${STUDY_NAME}/${DATA_TYPE}/"; 
	public static final String DEFAULT_NEURAL_NETWORK = "combined_clusters";
	public static final String DIAGNOSTICS_FILE = "/midb/diagnostics/diagnostics.txt";
	public static final String DIAGNOSTICS_DEMARCATION = "*********************************************************************";
	public static Logger LOGGER = null;
	//public static final String DOWNLOAD_ENTRY_TEMPLATE = "ID,IP_ADDRESS,TIMESTAMP,DOWNLOAD_REQUESTED_FILE";
    private static final DateTimeFormatter DT_FORMATTER_FOR_ID = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	public static final String ECLIPSE_RESOURCES_PATH = "/Users/jjfair/git/network_probability_downloader/NetworkProbabilityDownloader/build/classes/edu/umn/midb/population/atlas/config/files/";
	public static final String DEFAULT_LOGGER_ID = " ::LOGGERID=SERVLET_INIT:: ";
	private static String DOMAIN_NAME = "midbatlas.io";
	private static int HIT_COUNT = 0;
	private static Object HIT_COUNT_LOCK = new Object();
	private static String ENCRYPTION_KEY = null;
	//private static boolean STUDY_MAINTENANCE_IN_PROGRESS = false;
	//private static Object STUDY_MAINTENANCE_LOCK = new Object();
	//private static String STUDY_MAINTENANCE_LOCK_ID = "000000";
	private static StudyMaintenanceLock STUDY_MAINTENANCE_LOCK = null;
	
	private String localHostName = "UNKNOWN";
	//local dev link: http://localhost:8080/NetworkProbabilityDownloader/

	
	
	/**
	 * External classes use this when sending SMS notifications. The message will
	 * typically include the domain name of the sending server.
	 * @see SMSNotifier
	 *
	 * @return String containing domain name such as midbatlas.io
	 *
	 */
	public static String getDomainName() {
		return DOMAIN_NAME;
	}
	
	
	/**
	 * Checks for intermittent problem where NGINX generates a duplicate request which originates
	 * from the local loopback interface of 127.0.0.1
	 * 
	 * @param request - HttpServletRequest
	 * @return isDuplicate - boolean
	 */
	protected boolean checkDuplicateRequest(HttpServletRequest request) {
		
		if(localHostName.contains("JAMESs-MacBook-Pro")) {
			return false;
		}
		boolean isDuplicate = false;
		String action = null;
		String originalIP = request.getHeader("X-Forwarded-For");
		
		if(originalIP != null) {
			if(originalIP.contains("127.0.0.1")) {
				isDuplicate = true;
				action = request.getParameter("action");
				LOGGER.warn("Duplicate request received: action=" + action);
			}
		}
		
		return isDuplicate;
	}
	
	protected void createAdminAccessRecord(HttpServletRequest request, ApplicationContext appContext) {
		
		String formattedTS = appContext.getCurrentActionFormattedTimestamp();
		formattedTS = formattedTS.replace(" ", ",");

		AdminAccessEntry aaEntry = new AdminAccessEntry();
		String ipAddress = appContext.getRemoteAddress();
		String action = appContext.getCurrentAction();
		aaEntry.setAction(action);
		aaEntry.setAppContext(appContext);
		aaEntry.setRequestorIPAddress(ipAddress);
		aaEntry.setFormattedTimeStamp(formattedTS);
		aaEntry.setRequest(request);

		DBManager.getInstance().insertAdminAccessRecord(aaEntry, appContext);
	}
	
	/**
	 * Entry point into the servlet. The 'action' parameter is read from the query
	 * string which then directs the request to the appropriate action-handler method.
	 * The action may be one of the following, but not limited to the example list.
	 * <ul>
	 * <li>getMenuData
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
		
		String ipAddress = getOriginalIPAddress(request);

		
		HttpSession session = request.getSession();
		ApplicationContext appContext = (ApplicationContext)session.getAttribute("applicationContext");
		if(appContext==null) {
			appContext = new ApplicationContext();
			appContext.setTokenManager(new TokenManager(appContext, ipAddress));
			appContext.setLoggerId(session.getId(), ipAddress);
			appContext.setRemoteAddress(ipAddress);
			appContext.setSessionId(session.getId());
			//ThreadLocalLogTracker.set(appContext.getLoggerId());
			request.getSession().setAttribute("applicationContext", appContext);
			appContext.setBrowserType(request.getHeader("User-Agent"));
		}
		appContext.setCurrentAction(action);
		appContext.setCurrentReguest(request);
		
		try {
			ThreadLocalLogTracker.set(appContext.getLoggerId());			
			action = action.trim();
	
			//LOGGER.trace(loggerId + "doGet() invoked, action param =" + action);
			response.setContentType(CONTENT_TEXT_PLAIN);
			response.setCharacterEncoding(CHARACTER_ENCODING_UTF8);
			
			String queryString = request.getQueryString();
			appContext.addQueryStringToHistoryChain(queryString);
			appContext.incrementActionCount();
			
			switch (action) {
			
			case "downloadFile":
				long downloadActionBeginTime = System.currentTimeMillis();
				handleDownloadFile(appContext, request, response);
				long downloadActionEndTime = System.currentTimeMillis();
				LOGGER.info("Processing time in ms for downloadFile=" + (downloadActionEndTime - downloadActionBeginTime));
				break;
			case "getMenuData":
				appContext.setTokenManager(new TokenManager(appContext, ipAddress));
				ipAddress = getOriginalIPAddress(request);
				submitHitEntry(appContext, request);
				handleGetMenuDataRequest(appContext, request, response);
				break;
			case "getNetworkFolderNamesConfig":
				handleGetNetworkFolderNamesConfig(appContext, request, response);
				break;
			case "getThresholdImages":
				String selectedNeuralNetworkName = request.getParameter("neuralNetworkName");
				long gtiActionBeginTime = System.currentTimeMillis();
				handleGetThresholdImages(appContext, request, response);
				long gtiActionEndTime = System.currentTimeMillis();
				long gtiProcessingTime = gtiActionEndTime-gtiActionBeginTime;
				LOGGER.info("NetworkProbabilityDownloader.doGet()...selectedNeuralNetworkName=" + selectedNeuralNetworkName);
				LOGGER.info("Processing time in ms for getThresholdImages=" + gtiProcessingTime);
				break;
			case "removeStudy":
				handleRemoveStudy(appContext, request, response);
				break;
			case "validateAdminAccess":
				handleValidateAdminAccess(appContext, request, response);
				break;
			case "validateAdminAccessStatus":
				handleValidateAdminAccessStatus(appContext, request, response);
				break;
			case "unsubscribe":
				handleUnsubscribe(request, response, appContext);
				break;
			case "getWebHits":
				handleGetWebHits(appContext, request, response);
				break;
			case "getEmailAddresses": //this is not the csv download request
				handleGetEmailAddresses(appContext, request, response);
				break;
			case "getFileDownloadRecords": //this is not download a file, it's download history
				handleGetFileDownloadRecords(appContext, request, response);
				break;
			case "getAdminAccessRecords":
				handleGetAdminAccessRecords(appContext, request, response);
				break;
			case "getWebHitsMapURL":
				handleGetWebHitsMapURL(appContext, request, response);
				break;
			case "updateMapURL":
				handleUpdateMapURL(appContext, request, response);
				break;
			case "resynchWebHits":
				handleResynchWebHits(appContext, request, response);
				break;
			case "getStorageStats":
				handleGetStorageStats(appContext, response);
				break;
			case "updateStudyFile":
				handleUpdateStudy(appContext, request, response);
				break;
			case "encryptData":
				handleEncryptData(appContext, request, response);
				break;	
			case "updateConfig":
				handleUpdateConfigProperty(appContext, request, response);
				break;	
			case "sms":
				handleSMSReceived(request, response, appContext);
				break;
			case "resetStudyMaintenanceLock":
				handleResetStudyMaintenanceLock(appContext, request, response);
				break;
			}
		
		}
		catch(Exception e) {
			LOGGER.error("doGet()...error encountered", e);
			handleFatalError(appContext, request, response, e);
		} 
	}
	
	/**
	 * Receives requests coming in via a post.  Currently, the only functionality requiring
	 * this is the 'uploadStudyFiles' action. This action is restricted to individuals who
	 * have access to the Admin Console in the client.
	 * 
	 */
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
		if(appContext != null) {
			appContext.addQueryStringToHistoryChain(queryString);
			appContext.setCurrentAction(action);
		}
		
		response.setContentType(CONTENT_TEXT_PLAIN);
		response.setCharacterEncoding(CHARACTER_ENCODING_UTF8);

		try {
			switch (action) {
			case "uploadStudyFiles":
				LOGGER.trace(loggerId + "doPost()...action=" + action);
				handleAddStudy(appContext, request, response);
				break;

			case "uploadUpdateStudyFile":
				LOGGER.trace(loggerId + "doPost()...action=" + action);
				handleUpdateStudy(appContext, request, response);
				break;
		}
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
			handleFatalError(appContext, request, response, e);
		}

	}
	
	/**
	 * Resolves the ip address of the original requestor. This is necessary since each
	 * request is forwarded from the NGINX reverse proxy server.
	 * 
	 * @param request HttpServletRequest
	 * @return String The ip address of the original requestor
	 */
	protected String getOriginalIPAddress(HttpServletRequest request) {
		String ipAddress = null;
		
		ipAddress = request.getRemoteAddr();
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
		return ipAddress;
	}
	
	public static StudyMaintenanceLock getStudyMaintenanceLock(ApplicationContext appContext) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "getStudyMaintenanceLock()...invoked.");
		LOGGER.trace(loggerId + "getStudyMaintenanceLock()...exit.");
		return STUDY_MAINTENANCE_LOCK;
	}
	
	
	/**
	 * Handles a request to add a study to the studies repository. This allows for
	 * dynamic creation of studies in the client study menu. All data is stored under the
	 * midb root folder. This method will create an instance of {@link CreateStudyHandler}
	 * and delegate the required tasks to the handler.
	 * 
	 * This method tracks how many of the required files necessary to create a study have
	 * been uploaded. Once the final file is uploaded, the {@link CreateStudyHandler#completeStudyDeploy()}
	 * method will be invoked. Any error messages will be stored in the handler.
	 * The success or failure status will be sent to the client via the {@link WebResponder#sendAddStudyResponse(ApplicationContext, HttpServletResponse)} 
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException - uncaught exception
	 * @throws BIDS_FatalException - application-generated exception
	 */
	protected void handleAddStudy(ApplicationContext appContext, HttpServletRequest request,
			                                   HttpServletResponse response) throws IOException, BIDS_FatalException {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleAddStudy()...invoked.");
		
		CreateStudyHandler createStudyHandler = null;
		String adminToken = request.getParameter("adminToken");
		
		synchronized (STUDY_MAINTENANCE_LOCK) {
			if(STUDY_MAINTENANCE_LOCK.isLocked(appContext)) {
				String lockId = STUDY_MAINTENANCE_LOCK.getLockId(appContext);
				if(!adminToken.equals(lockId)) {
					LOGGER.trace(loggerId + "handleAddStudy()...studyMaintenance locked and id does not match loggerId. lockId=" + lockId);
					createStudyHandler = new CreateStudyHandler(appContext, request, response);
					createStudyHandler.setErrorEncountered(appContext, true);
					String errorMessage = "Study maintenance by another session is currently in progress.";
					errorMessage += " <br> Please try again in a few minutes.";
					createStudyHandler.setErrorMessage(appContext, errorMessage);
					WebResponder.sendAddStudyResponse(appContext, response);
					createAdminAccessRecord(request, appContext);
					LOGGER.trace(loggerId + "handleAddStudy()...exit. Study maintenance currently locked");
					return;
				}
			}
			else {
				STUDY_MAINTENANCE_LOCK.lock(appContext, LockType.ADD);
			}
		} // end synchronized block
		
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

		if(currentFileNumber == 1) {
			createStudyHandler = new CreateStudyHandler(appContext, request, response);
			appContext.setCreateStudyHandler(createStudyHandler);
			fileName = createStudyHandler.uploadFile(request, fileSize);
			createAdminAccessRecord(request, appContext);
		}
		else {
			createStudyHandler = appContext.getCreateStudyHandler();
			fileName = createStudyHandler.uploadFile(request, fileSize);
		}
		
		if(finished) {
			createStudyHandler.completeStudyDeploy();
			WebResponder.sendAddStudyResponse(appContext, response);
			STUDY_MAINTENANCE_LOCK.unlock(appContext);
		}
		if(!finished) {
			WebResponder.sendUploadFileResponse(response, fileName);
		}
		
		LOGGER.trace(loggerId + "handleAddStudy()...exit.");


	}
	
	/**
	 * Handles a request to download one of the admin files, such as email_addresses.csv. 
	 * The {@link DirectoryAccessor#getFileBytes(String, String)} method will be invoked to get
	 * the binary file bytes as a byte array. The byte array will then be passed to the
	 * {@link WebResponder#sendFileDownloadResponse(HttpServletResponse, byte[], String, String)}
	 * method.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws Exception uncaught exception
	 */
	protected void handleDownloadAdminFile(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleDownloadAdminFile()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleDownloadAdminFile()...exit.");
			return;
		}
		
		/* now allowing file downloads even if session expired, etc.
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, true);
			return;
		}
		*/
		
		String filePathAndName = request.getParameter("filePathAndName");
		
		if(filePathAndName.equals("/midb/email_addresses.csv")) {
			DBManager.getInstance().updateEmailAddressesCSVFile();
		}
		else if(filePathAndName.equals("/midb/web_hits_geoloc.csv")) {
			DBManager.getInstance().updateWebHitsGeoLocCSVFile();
		}
		else if(filePathAndName.equals("/midb/file_downloads_geoloc.csv")) {
			DBManager.getInstance().updateDownloadHitsGeoLocCSVFile();
		}
		
		
		//application documentation will not have the path specified in filePathAndName
		//since it resides in the docs package
		
		if(!filePathAndName.endsWith("csv") && !filePathAndName.contains("_surface.zip")) {
			filePathAndName = DocumentLocator.getPath(filePathAndName);
		}
		
		byte[] fileBinaryBuffer = DirectoryAccessor.getFileBytes(filePathAndName, null);
		int index = filePathAndName.lastIndexOf("/");
		String fileNameOnly = filePathAndName.substring(index+1);
		WebResponder.sendFileDownloadResponse(response, fileBinaryBuffer, fileNameOnly, null);
		LOGGER.trace(loggerId + "handleDownloadAdminFile()...exit.");
	}
	
	
	/**
	 * Handles a request to download an NII file that maps to the selected probabilistic threshold percentage.
	 * The {@link DirectoryAccessor#getFileBytes(String, String)} method will be invoked to get the binary file bytes
	 * that will be sent back to the client via the {@link WebResponder#sendFileDownloadResponse(HttpServletResponse, byte[], String, String)}
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request A reference to the current HttpServletRequest
	 * @param response A reference to the current HttpServletResponse
	 * 
	 * @throws Exception uncaught exception
	 */
	protected void handleDownloadFile(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String loggerId = appContext.getLoggerId();
		String filePathAndName = request.getParameter("filePathAndName");
		
		LOGGER.trace(loggerId + "handleDownloadFile()...invoked, filePathAndName=" + filePathAndName);
		
		//we don't track file downloads for admin files
		if(filePathAndName.contains("addStudy_sample.zip") || filePathAndName.contains(".csv") ||
		   filePathAndName.contains("javadoc") || filePathAndName.contains(".odt") ||
		   filePathAndName.contains(".rtf") || filePathAndName.contains("surface.zip")) {
				handleDownloadAdminFile(appContext, request, response);
				return;
		}
		
		
		boolean optedOut = true;
		
		String ipAddress = request.getRemoteAddr();
		//since we use NGINX as a front-end load distributor on AWS-LINUX then we need the following
		if(ipAddress.contains("127.0.0.1")) {
			String originalIP = request.getHeader("X-Forwarded-For");
			if(originalIP != null) {
				ipAddress = originalIP;
			}
		}
		
		if(SMSNotifier.shouldNotifyDownloads()) {
			String message = "MIDB_APP_FILE_DOWNLOAD::::" + DOMAIN_NAME + "::::IP=" + ipAddress;
			SMSNotifier.sendNotification(message, "NetworkProbabilityDownloader");
		}

		
		String optedOutParm = request.getParameter("optedOut");
		if(optedOutParm == null) {
			optedOut = false;
		}
		
		String emailAddress = request.getParameter("emailAddress");
		if(emailAddress == null) {
			emailAddress = "unknown";
		}
		String selectedStudy = request.getParameter("selectedStudy");
		String selectedNeuralNetworkName = request.getParameter("selectedNeuralNetworkName");

		
		if(!optedOut && !appContext.isEmailAddressAlreadyTracked()) {
			String fname = request.getParameter("fname");
			String lname = request.getParameter("lname");
			
			EmailAddressEntry emailEntry = new EmailAddressEntry(fname, lname, emailAddress);
			emailEntry.setAppContext(appContext);
			emailEntry.setRequest(request);
			emailEntry.setResponse(response);
			emailEntry.setRequestorIPAddress(ipAddress);
			EmailTracker.getInstance().addEmailAddressEntry(emailEntry);
			appContext.setEmailAlreadyTracked(true);
		}

		LOGGER.trace(loggerId + "handleAjaxDownloadFile()...filePathAndName=" + filePathAndName);

		LocalDateTime localTime = LocalDateTime.now();
		String formattedTS = appContext.getCurrentActionFormattedTimestamp();
		String id = DT_FORMATTER_FOR_ID.format(localTime);
		
		int index = filePathAndName.lastIndexOf("/");
		String fileNameOnly = filePathAndName.substring(index+1);
		FileDownloadEntry fdEntry = new FileDownloadEntry();
		fdEntry.setId(id);
		fdEntry.setFormattedTimeStamp(formattedTS);
		fdEntry.setAppContext(appContext);
		fdEntry.setRequest(request);
		fdEntry.setResponse(response);
		fdEntry.setFileName(fileNameOnly);
		fdEntry.setFilePath(filePathAndName);
		fdEntry.setStudy(selectedStudy);
		fdEntry.setNeuralNetworkName(selectedNeuralNetworkName);
		fdEntry.setRequestorIPAddress(ipAddress);
		if(!optedOut) {
			fdEntry.setEmailAddress(emailAddress);
		}
		DownloadTracker.getInstance().addDownloadEntry(fdEntry);
		
		byte[] fileBinaryBuffer = DirectoryAccessor.getFileBytes(filePathAndName, selectedStudy);

		WebResponder.sendFileDownloadResponse(response, fileBinaryBuffer, fileNameOnly, selectedStudy);
		LOGGER.trace(loggerId + "handleDownloadFile()...exit.");
		
		return;
	}


	
	/**
	 * 
	 * Handles any encountered exception by supplying an error description and
	 * a formatted stack trace that is delivered to the client via delegation to the
	 * {@link DiagnosticsReporter}
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param request HttpServletRequest Reference to the current HttpServletRequest
	 * @param response HttpServletResponse Reference to the current HttpServletResponse
	 * @param e Exception Reference to the encountered exception
	 * @throws ServletException - uncaught exception
	 * @throws IOException - uncaught exception
	 */
	protected void handleFatalError(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {

		String loggerId = appContext.getLoggerId();
		LOGGER.fatal(loggerId + "handleFatalError()...invoked");
		
		DiagnosticsReporter.createDiagnosticsEntry(appContext, request, response, e);

		LOGGER.fatal(loggerId + "handleFatalError()...exit");
		return;
		
	}
	
	/**
	 * Handles a request to encrypt text sent from an admin user. Certain environment variables
	 * are stored on the server in an encrypted format. For example, the account key for the
	 * twilio account which is used to send SMS notifications. The encryption tool in the admin
	 * console provides a way for an admin user to encrypt data that should not be stored in 
	 * plain text.
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param request - HttpServletRequest
	 * @param response - HttpServletResponse
	 * @throws UnsupportedEncodingException - exception
	 */
	protected void handleEncryptData(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleEncryptData()...invoked");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			return;
		}

		String textToEncrypt = request.getParameter("encryptionTarget");
		textToEncrypt = URLDecoder.decode(textToEncrypt, "UTF-8");
		String encryptedText = Utils.encryptJsypt(textToEncrypt, ENCRYPTION_KEY);
		WebResponder.sendEncrytpionRequestResponse(response, appContext, encryptedText);
		LOGGER.trace(loggerId + "handleEncryptData()...exit");
	}

	
	
	/**
	 * Handles a request to view the admin access records that exist in the admin_access
	 * table in mysql. Since this is a request that can only come from the admin console,
	 * the admin access validation will be checked. If the admin access has not been validated,
	 * then the {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * will be invoked. Otherwise, {@link DBManager#getAdminAccessRecords()} will be invoked to
	 * retrieve the records. Then the {@link WebResponder#sendAdminAccessRecordsResponse(HttpServletResponse, ApplicationContext, ArrayList)}
	 * method will be invoked. 
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException - unhandled exception
	 */
	protected void handleGetAdminAccessRecords(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleGetAdminAccessRecords()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleGetAdminAccessRecords()...exit.");
			return;
		}

		ArrayList<AdminAccessRecord> aaRecords = DBManager.getInstance().getAdminAccessRecords();
		WebResponder.sendAdminAccessRecordsResponse(response, appContext, aaRecords);
		
		LOGGER.trace(loggerId + "handleGetAdminAccessRecords()...exit.");
	}
	
	/**
	 * Handles a request to view email addresses stored in the mysql table named email_addresses.
	 * First, the admin access validation will be checked. If the validation is not confirmed,
	 * then the {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * method will be invoked. Otherwise, {@link DBManager#getEmailAddresses()} will be invoked
	 * and {@link WebResponder#sendEmailAddressesResponse(HttpServletResponse, ApplicationContext, ArrayList)}
	 * will be invoked.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException - unhandled exception
	 */
	protected void handleGetEmailAddresses(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleGetEmailAddresses()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleGetEmailAddresses()...exit.");
			return;
		}

		ArrayList<EmailAddressRecord> emailAddresses = DBManager.getInstance().getEmailAddresses();
		WebResponder.sendEmailAddressesResponse(response, appContext, emailAddresses);
		
		LOGGER.trace(loggerId + "handleGetEmailAddresses()...exit.");
	}
	
	/**
	 * Handles a request to view the file download records in the mysql file_downloads table.
	 * First, the admin access validation will be checked. If the validation is not confirmed,
	 * then {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * will be invoked. Otherwise {@link DBManager#getFileDownloads()} will be invoked and then
	 * {@link WebResponder#sendFileDownloadRecordsResponse(HttpServletResponse, ApplicationContext, ArrayList)}
	 * will be invoked.
	 * 
	 * @param appContext  {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException - unhandled exception
	 */
	protected void handleGetFileDownloadRecords(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleGetFileDownloadRecords()...invoked.");

		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			return;
		}
		
		ArrayList<FileDownloadRecord> fileDownloads = DBManager.getInstance().getFileDownloads();
		WebResponder.sendFileDownloadRecordsResponse(response, appContext, fileDownloads);
		
		LOGGER.trace(loggerId + "handleGetFileDownloadRecords()...exit.");
	}

	/**
	 * Handles the getMenuData action. This handling of this action is delegated to
	 * {@link WebResponder#sendMenuDataResponse(HttpServletResponse, ApplicationContext)}.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	protected void handleGetMenuDataRequest(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleGetMenuDataRequest()...invoked.");

		WebResponder.sendMenuDataResponse(response, appContext);
		
		LOGGER.trace(loggerId + "handleGetMenuDataRequest()...exit.");
	}
	
	protected void handleGetStudyMaintenanceStatus(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleGetStudyMaintenanceStatus()...invoked.");
		String status = "unlocked";
		
		synchronized(STUDY_MAINTENANCE_LOCK) {
			if(STUDY_MAINTENANCE_LOCK.isLocked(appContext)) {
				status = "locked";
			}
		}
		WebResponder.sendStudyMaintenanceStatusResponse(response, appContext, status);
		LOGGER.trace(loggerId + "handleGetStudyMaintenanceStatus()...exit.");
	}
	
	
	/**
	 * Handles the request to get the single network folders configuration. Each study
	 * has an entry in the network_folder_names.config file. This file is sent to the client
	 * so that it may cache the entries for each study in a map. The task for this request
	 * is delegated to {@link WebResponder#sendNetworkFolderNamesConfigResponse(HttpServletResponse)}.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	protected void handleGetNetworkFolderNamesConfig(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleGetNetworkFolderNamesConfig()...invoked.");
		
		//ArrayList<String> folderNamesConfig = AtlasDataCacheManager.getInstance().getNeuralNetworkFolderNamesConfig();
		//handleAjaxGetThresholdImages(appContext, request, response, true, folderNamesConfig);
		WebResponder.sendNetworkFolderNamesConfigResponse(response);
		
		LOGGER.trace(loggerId + "handleGetNetworkFolderNamesConfig()...invoked.");
	}
	
	/**
	 * Handles the request to get server storage stats. An admin client may issue this
	 * request to determine if there is enough storage to add a study.
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param response - HttpServletResponse
	 */
	protected void handleGetStorageStats(ApplicationContext appContext, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleGetStorageStats()...invoked.");
		
		ServerStorageStats freeStorageStats = CommandRunner.getFreeStorageStats();
		
		LOGGER.trace(loggerId + "amount=" + freeStorageStats.getAmount());
		WebResponder.sendStorageStatsResponse(response, freeStorageStats.getMessage());
		
		LOGGER.trace(loggerId + "handleGetStorageStats()...exit.");
	}
	

	/**
	 * When the client selects a specific neural network, a request to get all the threshold images for that
	 * network is sent to the servlet. This method will retrieve the collection of images from {@link AtlasDataCacheManager}.
	 * There are 3 arrays that comprise the response data:
	 * <ul>
	 * <li>An array of base64 encoded png images of each threshold image
	 * <li>An array of file paths designating .nii files that map to each threshold image
	 * <li>A reference to a {@link NetworkMapData} instance. This object contains a base64 encoded png image
	 *     and a file path designating a .nii file.
	 * </ul>
	 *  <p>   
	 * It will then forward the request to the {@link WebResponder} for further processing. 
	 * 
	 * @param appContext A reference to the {@link ApplicationContext}
	 * @param request A reference to the current HttpServletRequest
	 * @param response A reference to the current HttpServletResponse
	 */
	protected void handleGetThresholdImages(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleGetThresholdImages()...invoked.");
		
		boolean isSingleNetworkResponse = false;
		NetworkMapData networkMapData = null;
			
		
		String selectedNeuralNetworkName = request.getParameter("neuralNetworkName");
			
		if(!selectedNeuralNetworkName.equals("combined_clusters")) {
			isSingleNetworkResponse = true;
		}
		
		String selectedStudy = request.getParameter("selectedStudy");
		
		String targetDirectory = ROOT_PATH.replace(STUDY_NAME_PLACEHOLDER, selectedStudy);
		String selectedDataType = request.getParameter("selectedDataType");
		targetDirectory = targetDirectory.replace(DATA_TYPE_PLACEHOLDER, selectedDataType);
		targetDirectory = targetDirectory + selectedNeuralNetworkName;
		LOGGER.trace(loggerId + targetDirectory);
		LOGGER.trace(loggerId + "handleGetThresholdImages()...selected network name=" + targetDirectory);

		ArrayList<String> imagePaths = AtlasDataCacheManager.getInstance().getImagePathNames(targetDirectory);
		ArrayList<String> imageBase64Strings = AtlasDataCacheManager.getInstance().getBase64ImagePathStrings(targetDirectory);
		
		if(isSingleNetworkResponse) {
			networkMapData = AtlasDataCacheManager.getInstance().getNetworkMapData(targetDirectory);
		}
		
		WebResponder.sendThresholdImagesResponse(response, imagePaths, imageBase64Strings, networkMapData);
		LOGGER.trace(loggerId + "handleGetThresholdImages()...exit.");
		
		return;
	}
	
	/**
	 * Handles a request to view the web hits history. First, the admin access validation
	 * will be checked. If the admin access has not been validated yet, then
	 * {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * will be invoked. Otherwise, {@link DBManager#getWebHits()} will be invoked, and
	 * then {@link WebResponder#sendWebHitsResponse(HttpServletResponse, ApplicationContext, ArrayList)}
	 * will be invoked.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException - unhandled exception
	 */
	protected void handleGetWebHits(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleGetWebHits()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleGetWebHits()...exit.");
			return;
		}

		ArrayList<WebHitRecord> webHits = DBManager.getInstance().getWebHits();
		WebResponder.sendWebHitsResponse(response, appContext, webHits);
		
		LOGGER.trace(loggerId + "handleGetWebHits()...exit.");
	}


		
	/**
	 * Handles a request to get the current url to the google map that displays the 
	 * locations for all web hits. First, the session will be checked to see if
	 * admin access has been validated. If not, then {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * will be invoked. Otherwise, {@link DBManager#getWebHitsMapURL()} will be invoked,
	 * and then {@link WebResponder#sendWebHitsMapURLResponse(HttpServletResponse, ApplicationContext, String)} will be invoked.
	 * 
	 * @param appContext  {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException - unhandled exception
	 */
	protected void handleGetWebHitsMapURL(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleGetWebHitsMapURL()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			return;
		}
		
		String mapURL = DBManager.getInstance().getWebHitsMapURL();
		WebResponder.sendWebHitsMapURLResponse(response, appContext, mapURL);
		
		LOGGER.trace(loggerId + "handleGetWebHitsMapURL()...exit.");

	}
	
	/**
	 * Handles a request to remove a study. This action will remove the study entry from
	 * 3 different configuration files:
	 * <ul>
	 * <li>menu.conf
	 * <li>network_folder_names.conf
	 * <li>summary.conf
	 * </ul>
	 * 
	 * It will then remove the study folder which holds all the threshold image files,
	 * and the .nii files for every threshold. All of these tasks are delegated to an instance
	 * of {@link RemoveStudyHandler}.
	 * <p>
	 * Before this action is executed, the session will be examined to see if admin access
	 * has been validated. If not, then {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * will be invoked. Once a study has been removed, {@link WebResponder#sendRemoveStudyResponse(HttpServletResponse, String)} will
	 * be invoked.
	 * 
	 * @param appContext  {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException - unhandled exception
	 * @throws BIDS_FatalException - application-generated exception
	 */
	protected synchronized void handleRemoveStudy(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response) throws IOException, BIDS_FatalException {
		
		String loggerId = appContext.getLoggerId();
		String adminToken = request.getParameter("adminToken");
		LOGGER.trace(loggerId + "handleRemoveStudy()...invoked, adminToken=" + adminToken);		
		
		createAdminAccessRecord(request, appContext);
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleRemoveStudy()...exit");
			return;
		}
		
		if(STUDY_MAINTENANCE_LOCK.isLocked(appContext)) {
			String errorMessage = "Study maintenance locked by another process and is currently in progress.";
			errorMessage += " <br> Please try again in a few minutes.";
			WebResponder.sendRemoveStudyResponse(response, loggerId, false, errorMessage);
			LOGGER.trace(loggerId + "handleRemoveStudy()...exit. Study maintenance currently locked");
			return;
		}
		
		String studyFolder = request.getParameter("studyFolder");
		String removeStudyDisabled = PropertyManager.getInstance().getApplicationConfigProperty("removeStudyDisabled");
		
		if(removeStudyDisabled.equalsIgnoreCase("true")) {
			WebResponder.sendRemoveStudyResponse(response, studyFolder, true, null);
			LOGGER.trace(loggerId + "handleRemoveStudy()...exit");
			return;
		}
		
		STUDY_MAINTENANCE_LOCK.lock(appContext, LockType.REMOVE);
		
		RemoveStudyHandler rsh = new RemoveStudyHandler(studyFolder);
		rsh.removeStudy();
		WebResponder.sendRemoveStudyResponse(response, studyFolder, false, null);
		STUDY_MAINTENANCE_LOCK.unlock(appContext);

		LOGGER.trace(loggerId + "handleRemoveStudy()...exit");
	}
	
	protected void handleResetStudyMaintenanceLock(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleResetStudyMaintenanceLock()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleResetStudyMaintenanceLock()...exit");
			return;
		}
		
		LockResetStatus lockResetStatus = null;
		
		synchronized(STUDY_MAINTENANCE_LOCK) {
			lockResetStatus = STUDY_MAINTENANCE_LOCK.reset(appContext);
		}
		
		WebResponder.sendResetStudyMaintenanceLockResponse(response, appContext, lockResetStatus);
		LOGGER.trace(loggerId + "handleResetStudyMaintenanceLock()...exit.");				
	}
	
	/**
	 * Handles a request to re-synch the web_hits table in MYSQL. This will be
	 * accomplished by invoking {@link DBManager#resynchWebHits()}. The underlying
	 * database mechanism to re-synch the file is encapsulated in a stored procedure
	 * named rWebHits which exists in the midbatlas_db database. The table is re-synched
	 * by deleting any records with an ip_address that matches a developer ip_address.
	 * Then all the records are re-numbered for a correct number sequence with no gaps
	 * in the hit_count column.  After the DBManager finishes the task, 
	 * {@link WebResponder#sendResynchWebHitsResponse(HttpServletResponse, String)} is
	 * invoked.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException - exception that needs to be reported
	 */
	protected void handleResynchWebHits(ApplicationContext appContext, HttpServletRequest request,
				HttpServletResponse response) throws SQLException {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleResynchWebHits()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			return;
		}
		
		boolean success = DBManager.getInstance().resynchWebHits();
		String responseMessage = null;
		
		if(success) {
			responseMessage = "Successfully resynced table: web_hits";
		}
		else {
			responseMessage = "Unable to resync table: web_hits";

		}
		WebResponder.sendResynchWebHitsResponse(response, responseMessage);
		
		LOGGER.trace(loggerId + "handleResynchWebHits()...exit.");

	}
	
	protected void handleSMSReceived(HttpServletRequest request, HttpServletResponse response, ApplicationContext appContext) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleSMSReceived()...invoked.");
		SMSNotifier.handleSMSReceived(request, response);
		LOGGER.trace(loggerId + "handleSMSReceived()...exit.");
	}
	
	/**
	 * Handles a request to be removed from the mailing list, which means the 
	 * email address will be removed from the email_addresses table in MYSQL.
	 * If the operation fails {@link SMSNotifier#sendNotification(String, String)}
	 * will be invoked and an SMS notification will be sent since being able to
	 * unsubscribe is a legal requirement.
	 * 
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param appContext {@link ApplicationContext}
	 */
	public void handleUnsubscribe(HttpServletRequest request, HttpServletResponse response, ApplicationContext appContext) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleUnsubscribe()...invoked.");
		String emailAddress = request.getParameter("id");
		int deleteCount = 0;
		
		try {
			deleteCount = DBManager.getInstance().deleteEmailAddress(emailAddress);
		}
		catch(Exception e) {
			LOGGER.trace(loggerId + "Unable to delete email address, address=" + emailAddress);
		}
		
		if(deleteCount==0) {
			String domainName = getDomainName();
			String message = "MIDB_APP_ERROR::::UNABLE TO DELETE EMAIL_ADDRESS::::" + domainName;
			message += "::::" + emailAddress + "::::check /midb/unsubscribe_list.cvs";
			SMSNotifier.sendNotification(message, "NetworkProbabilityDownloader");
		}
		
		ServletContext sc = getServletContext();
		try {
			sc.getRequestDispatcher("/HTML/unsubscribe.html").forward(request, response);
		}
		catch(Exception e) {
			LOGGER.fatal(loggerId + e.getMessage(), e);
		}

	}
	
	/**
	 * Handles the request to update a configuration property
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param request - HttpServletRequest
	 * @param response - HttpServletResponse
	 */
	protected synchronized void handleUpdateConfigProperty(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleUpdateConfigProperty()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			LOGGER.trace(loggerId + "handleUpdateConfigProperty()...exit.");
			return;
		}
		
		String propertyKey = request.getParameter("configPropertyKey");
		String propertyValue = request.getParameter("configPropertyValue");
		
		appContext.setConfigErrorExists(false);
		appContext.setConfigError(null);
		PropertyManager.getInstance().updateConfigProperty(appContext, propertyKey, propertyValue);
		WebResponder.sendUpdateConfigResponse(appContext, response);
		
		LOGGER.trace(loggerId + "handleUpdateConfigProperty()...exit.");
	}
	
	/**
	 * Handles a request to update the url to either the WEB_HITS_MAP or the FILE_DOWNLOADS_MAP.
	 * These urls are stored in the map_urls table in MYSQL. Before proceeding with this task,
	 * the session will be checked to see if admin access has been validated. If not, then the
	 * {@link WebResponder#sendAdminAccessDeniedResponse(HttpServletResponse, ApplicationContext, boolean)}
	 * will be invoked. Otherwise, {@link DBManager#updateMapURL(String, String)} will be invoked
	 * and then {@link WebResponder#sendUpdateMapURLResponse(HttpServletResponse, int, String, String)}
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws SQLException unhandled exception to be reported by servlet
	 */
	protected void handleUpdateMapURL(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleUpdateMapURL()...invoked.");
		
		if(!appContext.isAdminActionValidated()) {
			WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
			return;
		}
		
		String url = request.getParameter("newURL");
		url = url.replace("!!!", "&");
		String targetMap = request.getParameter("targetMap");
		
		int beginIndex = 0;
		int endIndex = 0;
		
		beginIndex = url.indexOf("https");
		endIndex = url.indexOf("width");
		
		if(endIndex > -1) {
			url = url.substring(beginIndex, endIndex);
			beginIndex = url.indexOf("https");
			endIndex = url.indexOf("\"");
			url = url.substring(beginIndex, endIndex);
		}
		else {
			url = url.substring(beginIndex);
		}
	
		int updatedRowCount = DBManager.getInstance().updateMapURL(url, targetMap);

		WebResponder.sendUpdateMapURLResponse(response, updatedRowCount, targetMap, url);
		
		LOGGER.trace(loggerId + "handleUpdateMapURL()...exit.");

	}
	
	/**
	 * Handles the request to update a study, such as adding/updating volume data.
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param request - HttpServletRequest
	 * @param response - HttpServletResponse
	 */
	protected void handleUpdateStudy(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "handleUpdateStudy()...invoked.");
		
		synchronized(STUDY_MAINTENANCE_LOCK) {
			
			createAdminAccessRecord(request, appContext);
			
			STUDY_MAINTENANCE_LOCK.lock(appContext, LockType.UPDATE);
			if(!appContext.isAdminActionValidated()) {
				WebResponder.sendAdminAccessDeniedResponse(response, appContext, false);
				LOGGER.trace(loggerId + "handleUpdateStudy()...exit.");
				return;
			}
			
			String studyId = request.getParameter("studyFolderName");
			String updateAction = request.getParameter("updateAction");
			String fileSizeString = request.getParameter("fileSize");
			
			UpdateStudyHandler updateHandler = new UpdateStudyHandler(studyId, appContext);
			
			if(!updateAction.contains("addStudyPrefix")) {
				long fileSize = Long.parseLong(fileSizeString);
				updateHandler.uploadFile(request, fileSize);
			}
			else {
				String dataType = null;
				if(updateAction.contains("Surface")) {
					dataType = "surface";
				}
				else if(updateAction.contains("Volume")) {
					dataType = "volume";
				}
				updateHandler.addStudyPrefixToFileNames(dataType);
			}
	
			WebResponder.sendUpdateStudyResponse(appContext, response, updateHandler);
			STUDY_MAINTENANCE_LOCK.unlock(appContext);
		}
	
			
		LOGGER.trace(loggerId + "handleUpdateStudy()...exit.");
	}

	
	/**
	 * Handles validating admin access. This action is delegated to {@link WebResponder#sendAdminValidationResponse(HttpServletResponse, ApplicationContext, String, String, String)}.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	protected void handleValidateAdminAccess(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response)  {
		
			String loggerId = appContext.getLoggerId();
			LOGGER.trace(loggerId + "handleValidateAdminAccess()...invoked.");
			
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

			LOGGER.trace(loggerId + "handleValidateAdminAccess()...exit.");
	}
	
	/**
	 * Handles determining if the session has already been validated for admin access.
	 * This task is delegated to {@link WebResponder#sendAdminValidationStatus(ApplicationContext, HttpServletResponse)}.
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	protected void handleValidateAdminAccessStatus(ApplicationContext appContext, HttpServletRequest request,
            HttpServletResponse response)  {
		
			String loggerId = appContext.getLoggerId();
			LOGGER.trace(loggerId + "handleValidateAdminAccessStatus()...invoked.");

			WebResponder.sendAdminValidationStatus(appContext, response);
			LOGGER.trace(loggerId + "handleValidateAdminAccessStatus()...exit.");			
	}
	
	/**
	 * Override of the init() method which handles instantiation and initialization of the
	 * {@link PropertyManager} and the {@link AtlasDataCacheManager} as well as the dynamic
	 * configuration of the log4j logging mechanism. Other components are also initialized.
	 */
	public void init() {
		//DO NOT USE LOGGER YET BECAUSE LogConfigurator has not run yet
		System.out.println("NetworkProbabilityDownloader.init()...invoked...version=" + BUILD_DATE);
		
		//we preload PropertyManager because it will invoke the LogConfigurator and also
		//load properties required for startup
		PropertyManager.getInstance();
		LOGGER = LogManager.getLogger(NetworkProbabilityDownloader.class);
		
		// AtlasDataCacheManager.getInstance() will cause the AtlasDataCacheManager
		// to preload the default image data
		AtlasDataCacheManager.getInstance().setLocalHostName(localHostName);	
		
		initFromEnvVars();
		
		//initialize Singleton instances
		DownloadTracker.getInstance();
		WebHitsTracker.getInstance();
		EmailTracker.getInstance();
		CountryNamesResolver.getInstance();
		STUDY_MAINTENANCE_LOCK = StudyMaintenanceLock.getInstance();
				
		LOGGER.info("exiting init().");
	}
	
	
	/**
	 * Initializes certain configuration properties from environment variables. These are
	 * variables hold the encrypted values of properties that are very seldom (if ever)
	 * changes. Other configuration properties are stored in the /midb/midb_app.properties
	 * and are loaded by the {@link PropertyManager#loadSettingsConfig()} method.
	 */
	private void initFromEnvVars() {
		LOGGER.trace(DEFAULT_LOGGER_ID + "initFromEnvVars()...invoked");
		
		Map<String,String> envMap = System.getenv();
		Set<String> envKeys = envMap.keySet();
		
		//key must be processed before other env vars
		String key = envMap.get("MIDB_KEY");
		ENCRYPTION_KEY = key;
		
		if(key != null) {
			LOGGER.trace("Processing MIDB_KEY...");
			PropertyManager.getInstance().setEncryptionKey(key);
			TokenManager.setKey(key);
			initSMSNotifier(key);
			initIPLocator(key);
			initIPInfoRequestor(key);
		}
		else {
			LOGGER.fatal("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			LOGGER.fatal("MIDB_KEY not found! Key is null!");
		}

		Iterator<String> envIt = envKeys.iterator();
		String envKey = null;
		String envValue = null;
		LOGGER.trace("Processing environment variables::!!!!!!!!!!!!!!!!!!!!!!!");
		
		while(envIt.hasNext()) {
			envKey = envIt.next();
			envValue = envMap.get(envKey);
			//LOGGER.trace(envKey + "==" + envValue);
			
			switch(envKey) {
			case	"MIDB_ADMIN" :
				LOGGER.trace("Processing MIDB_ADMIN");
				String encryptedPassword = envValue;
				TokenManager.setPassword(encryptedPassword);
				break;
			case	"MIDB_DB" :
				LOGGER.trace("Processing MIDB_DB");
				if(key != null) {
					DBManager.getInstance().initAuthentication(envValue, key);
				}
			}
		}
		LOGGER.trace(DEFAULT_LOGGER_ID + "initFromEnvVars()...exit");		
	}
	
	
	/**
	 * Initializes the {@link IPInfoRequestor}.
	 * 
	 * @param key String
	 */
	private void initIPInfoRequestor(String key) {

		LOGGER.trace(DEFAULT_LOGGER_ID + "initIPInfoRequestor()...invoked");
		
		String ipInfoToken = PropertyManager.getInstance().getApplicationConfigProperty("MIDB_IPINF_TAU");
		IPInfoRequestor.initAuthentication(key, ipInfoToken);
		
		LOGGER.trace(DEFAULT_LOGGER_ID + "initIPInfoRequestor()...exit");
	}
	
	
	/**
	 * Initializes the {@link IPLocator}.
	 * 
	 * @param key String
	 */
	private void initIPLocator(String key) {
		LOGGER.trace(DEFAULT_LOGGER_ID + "initIPLocator()...invoked");
		
		String ipLocatorAccountId = PropertyManager.getInstance().getApplicationConfigProperty("MIDB_IPLOC_ACC");
		String ipLocatorLicenseKey = PropertyManager.getInstance().getApplicationConfigProperty("MIDB_IPLOC_AUT");
		
		IPLocator.initAuthentication(key, ipLocatorAccountId, ipLocatorLicenseKey);
		
		LOGGER.trace(DEFAULT_LOGGER_ID + "initIPLocator()...exit");
	}
	
	/**
	 * Initializes the {@link SMSNotifier}.
	 * 
	 * @param key String
	 */
	private void initSMSNotifier(String key) {
		LOGGER.trace(DEFAULT_LOGGER_ID + "initSMSNotifier()...invoked");
		
		boolean success = true;
		int successCount = 0;
		
		SMSNotifier.setEncryptionKey(key);
		PropertyManager propMgr = PropertyManager.getInstance();
		
		
		String accSidE = propMgr.getApplicationConfigProperty("MIDB_TAC");
		if(accSidE != null) {
			SMSNotifier.setAccountSIDE(accSidE);
			successCount++;
		}

		
		String accAuthE = propMgr.getApplicationConfigProperty("MIDB_TAU");
		if(accAuthE != null) {
			SMSNotifier.setAuthTokenE(accAuthE);
			successCount++;
		}

		String toPhone = propMgr.getApplicationConfigProperty("MIDB_TTP");
		if(toPhone != null) {
			SMSNotifier.setToNumberE(toPhone);
			successCount++;
		}

		String fromPhone = propMgr.getApplicationConfigProperty("MIDB_FTP");
		if(fromPhone != null) {
			SMSNotifier.setFromNumberE(fromPhone);
			successCount++;
		}
		
		String textBeltKey = propMgr.getApplicationConfigProperty("MIDB_TBELT");
		if(textBeltKey != null) {
			SMSNotifier.setTextBeltKey(textBeltKey);
		}
		
		String smsMode = propMgr.getApplicationConfigProperty("MIDB_SMS_MODE");
		if(smsMode != null) {
			SMSNotifier.setSendMode(smsMode);
		}
		
		String notifyDownloads = propMgr.getApplicationConfigProperty("MIDB_DOWNLOAD_NOTIFICATIONS");
		if(notifyDownloads.equalsIgnoreCase("ON")) { //the default is OFF
			SMSNotifier.setDownloadNotificationMode(true);
		}
		
		String disableSMSNotificationsProp = propMgr.getApplicationConfigProperty("MIDB_DISABLE_SMS_NOTIFICATIONS");
		if(disableSMSNotificationsProp != null && disableSMSNotificationsProp.equalsIgnoreCase("true")) {
			SMSNotifier.disableSMSNotifications(true);
		}
				
		LOGGER.trace(DEFAULT_LOGGER_ID + "initSMSNotifier()...exit.");
		
	}
	
	/*
	protected void setStudyMaintenanceStatus(boolean inProgressIndicator, String adminToken) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "setStudyMaintenanceStatus()...invoked. inProgressIndicator=" + inProgressIndicator);
		
		synchronized(STUDY_MAINTENANCE_LOCK) {
			STUDY_MAINTENANCE_IN_PROGRESS = inProgressIndicator;
			if(STUDY_MAINTENANCE_IN_PROGRESS) {
				STUDY_MAINTENANCE_LOCK_ID = adminToken;
			}
			else {
				STUDY_MAINTENANCE_LOCK_ID = "";
			}
		}
		LOGGER.trace(loggerId + "setStudyMaintenanceStatus()...exit.");

	}
	*/
	
	
	
	/**
	 * Entry point for adding a record to the web_hits history table in MYSQL.
	 * An instance of {@link WebHitEntry} will be created and queued via the
	 * {@link WebHitsTracker#addWebHitEntry(WebHitEntry)}. The WebHitEntry will
	 * then be added to the database. 
	 * 
	 * @param appContext {@link ApplicationContext}
	 * @param request HttpServletRequest
	 */
	protected void submitHitEntry(ApplicationContext appContext, HttpServletRequest request) {
		
		String ipAddress = request.getRemoteAddr();
		String userAgent = request.getHeader("USER-AGENT");	
		
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
		String formattedTS = appContext.getCurrentActionFormattedTimestamp();
		formattedTS = formattedTS.replace(" ", ",");
		String id = DT_FORMATTER_FOR_ID.format(localTime);

		WebHitEntry whEntry = new WebHitEntry();
		whEntry.setAppContext(appContext);
		whEntry.setRequestorIPAddress(ipAddress);
		whEntry.setFormattedTimeStamp(formattedTS);
		whEntry.setUserAgent(userAgent);
		whEntry.setRequest(request);
		whEntry.setId(id);
		WebHitsTracker.getInstance().addWebHitEntry(whEntry);
	}

}
