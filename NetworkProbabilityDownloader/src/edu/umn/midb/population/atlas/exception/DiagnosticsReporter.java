package edu.umn.midb.population.atlas.exception;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.data.access.DBManager;
import edu.umn.midb.population.atlas.servlet.NetworkProbabilityDownloader;
import edu.umn.midb.population.atlas.tasks.DownloadTracker;
import edu.umn.midb.population.atlas.tasks.EmailTracker;
import edu.umn.midb.population.atlas.tasks.WebHitsTracker;
import edu.umn.midb.population.atlas.utils.EmailNotifier;
import edu.umn.midb.population.atlas.utils.SMSNotifier;
import logs.ThreadLocalLogTracker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Creates an entry in the /midb/diagnostics/diagnostics.txt file. Each entry will have
 * an incidentId. The id will include a timestamp. When an incident is created, an SMS
 * notification is also sent via the {@link SMSNotifier}. The incident entries make debugging
 * easier since the log file does not need to be examined to extract the relevant data.
 * 
 * Each entry will include the exception stack trace. When the exception occurs during
 * client interaction, other information such as the history of requested actions, and
 * queryStrings will also be included.
 * 
 * @author jjfair
 *
 */
public class DiagnosticsReporter {
	
	private static Logger LOGGER = null;
	public static final String DIAGNOSTICS_FILE = "/midb/diagnostics/diagnostics.txt";
	public static final String DIAGNOSTICS_DEMARCATION = "*********************************************************************";
	private static final String LOGGER_ID = " ::LOGGERID=DiagnosticsReporter:: ";
	private static final String NEW_LINE = "\r\n";
	private static final String CLASS_NAME = "DiagnosticsReports";
	
	static {
		LOGGER = LogManager.getLogger(DiagnosticsReporter.class);

	}
	
	/**
	 * 
	 * Creates a diagnostic entry for an exception that will be inserted in the
	 * /midb/diagnostics/diagnostics.txt file. This method is typically invoked by 
	 * asynchronous threads when the client interaction by asynchronous threads when the client
	 * interaction may have already completed. Examples of asynchronous threads are:
	 * <ul>
	 * <li>{@link WebHitsTracker}
	 * <li>{@link DownloadTracker}
	 * <li>{@link EmailTracker}
	 * </ul>
	 * <p>
	 * In situations where the thread encountering the problem is a synchronous servlet
	 * thread, it is preferable to invoke {@link #createDiagnosticsEntry(ApplicationContext, HttpServletRequest, HttpServletResponse, Exception)}.
	 * 
	 * @param e - Exception
	 */
	public static void createDiagnosticsEntry(Exception e) {
		LOGGER.fatal(LOGGER_ID + "createDiagnosticsEntry(e)...invoked");
		
		StackTraceElement[] stackTraceEntries = e.getStackTrace();
	    int stackEntriesCount = stackTraceEntries.length;
	    String timeStringID = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	    timeStringID = "INCIDENT_ID=" + timeStringID;
	    
	    String stackTraceData = timeStringID + NEW_LINE;
	    stackTraceData += NetworkProbabilityDownloader.BUILD_DATE + NEW_LINE;
	    stackTraceData += NEW_LINE;
	    stackTraceData += "" + e + NEW_LINE;
	    stackTraceData += NEW_LINE;


	    for(int i=0;i<stackEntriesCount;i++) {
		  LOGGER.fatal(stackTraceEntries[i]);
		  stackTraceData += stackTraceEntries[i] + NEW_LINE;
	    }
	    
	    addDiagnosticEntryToFile(stackTraceData);
		EmailNotifier.sendEmailNotification("APP_ERROR::INCIDENT_ID=" + timeStringID);
		String domainName = NetworkProbabilityDownloader.getDomainName();
		SMSNotifier.sendNotification("APP_ERROR::DOMAIN_NAME=" + domainName + "::INCIDENT_ID=" + timeStringID, CLASS_NAME);	    		
	}

	/**
	 * Creates a diagnostic entry for an exception that will be inserted into
	 * the /midb/diagnostics/diagnostics.txt. If the invoker is an asynchronous thread
	 * then null should be passed as the HttpServletResponse parameter since the asynchronous
	 * thread has no connection to the original servlet thread that received the request.
	 * 
	 * @param appContext - {@link ApplicationContext}
	 * @param request - HttpServletRequest
	 * @param response - HttpServletResponse
	 * @param e - Exception
	 */
	public static void createDiagnosticsEntry(ApplicationContext appContext, HttpServletRequest request, HttpServletResponse response, Exception e) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.fatal(loggerId + "createDiagnosticsEntry(...)...invoked");

	    String timeString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		String id = appContext.getSessionId();
		id += "__";
		id += timeString;
		appContext.setIncidenceId(id);
		String queryStringHistory = appContext.getQueryStringHistory();
		
		StackTraceElement[] stackTraceEntries = e.getStackTrace();
	    int stackEntriesCount = stackTraceEntries.length;
	    String stackTraceData = NetworkProbabilityDownloader.BUILD_DATE + NEW_LINE;
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
				              + "Show details for more information.&" + NEW_LINE;
		//responseError += stackTraceEntries[0] + "\n";
		//responseError += stackTraceEntries[1] + "\n";
		String completeResponseMessage = fatalErrorPrefix + responseError1 + responseError2 + stackTraceData + 
				                         queryStringHistory + fatalErrorSuffix;
		
		addDiagnosticEntryToFile(completeResponseMessage);
		
		if(response != null) {
			try {
				response.getWriter().println(completeResponseMessage);
				response.getWriter().flush();
			}
			catch(IOException ioE) {
				LOGGER.fatal(LOGGER_ID + "Can not write response");
				LOGGER.fatal(ioE.getMessage(), ioE);
			}
		}

		EmailNotifier.sendEmailNotification("INCIDENT_ID=" + id);
		String message = "MIDB_APP_ERROR::INCIDENT_ID=" + id + "::::DOMAIN_NAME=" + NetworkProbabilityDownloader.getDomainName();
		SMSNotifier.sendNotification(message, CLASS_NAME);
		LOGGER.fatal(loggerId + "createDiagnosticsEntry(...)...exit");
	}
	
	/**
	 * 
	 * Inserts the diagnostic entry into the /midb/diagnostics/diagnostics.txt file.
	 * 
	 * @param entry - String
	 */
	static private void addDiagnosticEntryToFile(String entry) {
		
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

}
