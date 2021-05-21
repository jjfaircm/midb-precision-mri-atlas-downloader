package edu.umn.midb.population.atlas.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.config.PropertyManager;
import edu.umn.midb.population.atlas.data.access.DirectoryAccessor;
import edu.umn.midb.population.atlas.utils.AtlasDataCacheManager;
import edu.umn.midb.population.atlas.utils.NetworkMapData;
import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;

/**
 * https://github.com/jjfaircm/midb-precision-mri-atlas-downloader
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
 * Move button (side by side)
 * 
 * Integration Zone Atlas instead of Number of Networks
 * 
 * Probabilistic Atlas: Combined Networks
 * 
 * Probabilistic Atlas: Single Networks
 * 
 * 2 submenus:  Study (choice)-->Atlas (choice)
 * 
 * 
 * Instead of Instructions, Probabilistic Atlas Summary
 * 
 * Data Source:  ABCD Study 
 * Methodology:  Template Matching
 * Version:      1.0
 * Number of Subjects:  5000
 * Age of Subjects:     9-10y
 * Funding:  R01MH096906, R01MH096906
 * Citation:     Hermosillo et al
 * Acknowledgements: 
 * 
 * Created and maintained by
 * 
 * 
 * Stuff for Robert:  
 * 
 * Google groups
 * GitHub
 * combined: .8 vs .81
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

 */
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
	
	private static final long serialVersionUID = 1L;
	private static final long VERSION_NUMBER = 0;
	public static final String BUILD_DATE = "Version 0.0.0.36  0518__2021:23:15__war=NPDownloader_0518.war"; 
	public static final String CONTENT_TEXT_PLAIN = "text/plain";
	public static final String CHARACTER_ENCODING_UTF8 = "UTF-8";
	public static final String ROOT_PATH = "/midb/networks_small_package-compressed/"; 
	//public static final String ROOT_PATH = "/Users/jjfair/midb_old/"; 
	public static final String DEFAULT_NEURAL_NETWORK = "combined_clusters";
	//public static final String DEFAULT_NEURAL_NETWORK = "Aud";
	public static Logger LOGGER = null;
	
	public static final String ECLIPSE_RESOURCES_PATH = "/Users/jjfair/git/network_probability_downloader/NetworkProbabilityDownloader/build/classes/edu/umn/midb/population/atlas/config/files/";

	
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
		
		String action = null;
		HttpSession session = request.getSession();
		ApplicationContext appContext = (ApplicationContext)session.getAttribute("applicationContext");
		if(appContext==null) {
			appContext = new ApplicationContext();
			appContext.setLoggerId(session.getId());
			ThreadLocalLogTracker.set(appContext.getLoggerId());
			request.getSession().setAttribute("applicationContext", appContext);
		}
		String loggerId = appContext.getLoggerId();
		try {
			action = request.getParameter("action");
			if(action==null) {
				action = request.getQueryString().substring(7,25);
			}
			else {
				action = action.trim();
			}
			LOGGER.trace(loggerId + "doGet() invoked, action param =" + action);
			response.setContentType(CONTENT_TEXT_PLAIN);
			response.setCharacterEncoding(CHARACTER_ENCODING_UTF8);
			
			switch (action) {
			
			case "downloadFile":
				handleAjaxDownloadFile(appContext, request, response);
				break;
			case "getNeuralNetworkNames":
				long gnnActionBeginTime = System.currentTimeMillis();
				handleAjaxGetNeuralNetworkNames(appContext, request, response);
				long gnnActionEndTime = System.currentTimeMillis();
				long gnnProcessingTime = gnnActionEndTime-gnnActionBeginTime;
				LOGGER.info("Processing time in ms for getNeuralNetworkNames=" + gnnProcessingTime);
				break;
			case "getThresholdImages":
				long gtiActionBeginTime = System.currentTimeMillis();
				String selectedNeuralNetworkName = request.getParameter("neuralNetworkName");

				handleAjaxGetThresholdImages(appContext, request, response, false, null);
				long gtiActionEndTime = System.currentTimeMillis();
				long gtiProcessingTime = gtiActionEndTime-gtiActionBeginTime;
				LOGGER.info("NetworkProbabilityDownloader.doGet()...selectedNeuralNetworkName=" + selectedNeuralNetworkName);
				LOGGER.info("Processing time in ms for getThresholdImages=" + gtiProcessingTime);
				break;
			}
		
		}
		catch(Exception e) {
			LOGGER.error("doGet()...error encountered", e);
			handleFatalError(request, response, e);
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
	protected void handleFatalError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.fatal(loggerId + "handleFatalError()...invoked");
		String NEW_LINE = "\r\n";
	
		String queryString = request.getQueryString();
		String privateQueryString = queryString;
		
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
				                         privateQueryString + fatalErrorSuffix;
		

		response.getWriter().println(completeResponseMessage);
		response.getWriter().flush();

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
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleAjaxDownloadFile()...invoked.");
		String filePathAndName = request.getParameter("filePathAndName");
		byte[] fileBinaryBuffer = DirectoryAccessor.getFileBytes(filePathAndName);
		int slashIndex = filePathAndName.lastIndexOf("/");
		String fileNameOnly = filePathAndName.substring(slashIndex+1);
		WebResponder.sendFileDownloadResponse(response, fileBinaryBuffer, fileNameOnly);
		LOGGER.trace(loggerId + "handleAjaxDownloadFile()...exit.");
		
		return;
	}

	
	/**
	 * Handles the request to retrieve the list of available neural network types.
	 * 
	 * @param appContext  A reference to the session's {@linkplain ApplicationContext}
	 * @param request HttpServletRequest A reference to the current HttpServletRequest
	 * @param response HttpServletResponse A reference to the current HttpServletResponse
	 */
	protected void handleAjaxGetNeuralNetworkNames(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleAjaxGetNeuralNetworkNames()...invoked.");
		
		boolean includeNetworkNames = true;
		ArrayList<String> networkTypesList = AtlasDataCacheManager.getInstance().getNeuralNetworkNames();
		handleAjaxGetThresholdImages(appContext, request, response, true, networkTypesList);
		//WebResponder.sendNeuralNetworkNamesResponse(response, networkTypesList);
		LOGGER.trace(loggerId + "handleAjaxGetNeuralNetworkNames()...exit.");

		return;
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
	protected void handleAjaxGetThresholdImages(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response, boolean isFirstRequest, ArrayList<String> neuralNetworkNames) {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...invoked.");
		
		boolean isSingleNetworkResponse = false;
		NetworkMapData networkMapData = null;
		
		//String rootDirectory = "/myPath";
		
		//if isFirstRequest=true then this is the default request
		//a.k.a. the first request coming in and the arrayList of neuralNetworkNames
		//will be populated, otherwise it will be null.
		//Also if this is the default request then the 'selected' neuralNetworkName
		//will be DEFAULT_NEURAL_NETWORK
		
		String selectedNeuralNetworkName = DEFAULT_NEURAL_NETWORK;
		String selectedNeuralNetworkPathName = AtlasDataCacheManager.getInstance().getNetworkPathName(selectedNeuralNetworkName);
		
		if(!isFirstRequest) {
			selectedNeuralNetworkName = request.getParameter("neuralNetworkName");
			selectedNeuralNetworkPathName = AtlasDataCacheManager.getInstance().getNetworkPathName(selectedNeuralNetworkName);
			neuralNetworkNames = AtlasDataCacheManager.getInstance().getNeuralNetworkNames();
		}
		
		if(!selectedNeuralNetworkName.equals("combined_clusters")) {
			isSingleNetworkResponse = true;
		}
		
		String targetDirectory = ROOT_PATH + selectedNeuralNetworkPathName;
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...selected network name=" + targetDirectory);

		ArrayList<String> imagePaths = AtlasDataCacheManager.getInstance().getImagePathNames(targetDirectory);
		ArrayList<String> imageBase64Strings = AtlasDataCacheManager.getInstance().getBase64ImagePathStrings(targetDirectory);
		
		if(isSingleNetworkResponse) {
			String shortNetworkName = AtlasDataCacheManager.getInstance().getNetworkPathName(selectedNeuralNetworkName);
			networkMapData = AtlasDataCacheManager.getInstance().getNetworkMapData(shortNetworkName);
		}
		
		WebResponder.sendThresholdImagesResponse(response, imagePaths, imageBase64Strings, neuralNetworkNames, networkMapData);
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...exit.");
		
		return;
	}
		
	/**
	 * Override of the init() method which handles instantiation and initialization of the
	 * {@link PropertyManager} and the {@link AtlasDataCacheManager} as well as the dynamic
	 * configuration of the log4j logging mechanism.
	 */
	public void init() {
		System.out.println("NetworkProbabilityDownloader.init()...invoked.");
		//we preload PropertyManager because it will invoke the LogConfigurator
		PropertyManager.getInstance();
		LOGGER = LogManager.getLogger(NetworkProbabilityDownloader.class);

		// AtlasDataCacheManager.getInstance() will cause the AtlasDataCacheManager
		// to preload the default image data
		AtlasDataCacheManager.getInstance();
		
	}


}
