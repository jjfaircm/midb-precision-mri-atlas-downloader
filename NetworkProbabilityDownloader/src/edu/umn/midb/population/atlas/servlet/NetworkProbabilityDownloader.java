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
import edu.umn.midb.population.atlas.data.access.DirectoryAccessor;
import edu.umn.midb.population.response.handlers.WebResponder;
import logs.ThreadLocalLogTracker;


public class NetworkProbabilityDownloader extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final long VERSION_NUMBER = 0;
	public static final String BUILD_DATE = "Version 0.0.0.2  10_30_2020:14:05__war=NPDownloader_0916.war"; 
	public static final String CONTENT_TEXT_PLAIN = "text/plain";
	public static final String CHARACTER_ENCODING_UTF8 = "UTF-8";
	public static final String ROOT_PATH = "/Users/jjfair/midb/";
	public static Logger LOGGER = null;
	
	public static final String ECLIPSE_RESOURCES_PATH = "/Users/jjfair/git/network_probability_downloader/NetworkProbabilityDownloader/build/classes/edu/umn/midb/population/atlas/config/files/";


	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = null;
		HttpSession session = request.getSession();
		ApplicationContext appContext = (ApplicationContext)session.getAttribute("applicationContext");
		if(appContext==null) {
			appContext = new ApplicationContext();
			appContext.setLoggerId(session.getId());
			ThreadLocalLogTracker.set(appContext.getLoggerId());
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
				handleAjaxGetNeuralNetworkNames(appContext, request, response);
				break;
			case "getThresholdImages":
				handleAjaxGetThresholdImages(appContext, request, response);
				break;
		
				
			}
		
		}
		catch(Exception e) {
			LOGGER.error("doGet()...error encountered", e);
			handleFatalError(request, response, e);
		} 
	}
	
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

	
	protected void handleAjaxGetNeuralNetworkNames(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleAjaxGetNeuralNetworkNames()...invoked.");
		
		String rootDirectory = "/myPath";
		ArrayList<String> networkTypesList = DirectoryAccessor.getNeuralNetworkNames(rootDirectory);
		WebResponder.sendNeuralNetworkNamesResponse(response, networkTypesList);
		LOGGER.trace(loggerId + "handleAjaxGetNeuralNetworkNames()...exit.");

		return;
	}
	
	protected void handleAjaxGetThresholdImages(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...invoked.");

		
		//String rootDirectory = "/myPath";
		
		String neuralNetworkName = request.getParameter("neuralNetworkName");
		String targetDirectory = ROOT_PATH + neuralNetworkName;
		
		
		ArrayList<String> imagePaths = DirectoryAccessor.getThresholdImagePaths(targetDirectory);
		ArrayList<byte[]> imageByteBuffers = new ArrayList<byte[]>();
		
		Iterator<String> imagePathsIt = imagePaths.iterator();
		String anImagePath = null;
		byte[] imageBuffer = null;
		
		while(imagePathsIt.hasNext()) {

			anImagePath = imagePathsIt.next();
			imageBuffer = DirectoryAccessor.getFileBytes(anImagePath);
			imageByteBuffers.add(imageBuffer);
		}
		
		WebResponder.sendThresholdImagesResponse(response, imagePaths, imageByteBuffers);
		LOGGER.trace(loggerId + "handleAjaxGetThresholdImages()...exit.");
		
		return;
	}
	
	public void init() {
		System.out.println("NetworkProbabilityDownloader.init()...invoked.");
		//reference classes that we would like to preload
		//we preload PropertyManager because it will invoke the LogConfigurator
		try {
			Class.forName("edu.umn.midb.population.atlas.config.PropertyManager");
			LOGGER = LogManager.getLogger(NetworkProbabilityDownloader.class);
		}
		catch(ClassNotFoundException cnfE) {
			System.out.println("NetworkProbabilityDownloader.init()...unable to load PropertyManager");
			cnfE.printStackTrace();
		}
	}


}
