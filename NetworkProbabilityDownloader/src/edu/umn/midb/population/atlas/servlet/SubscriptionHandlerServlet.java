package edu.umn.midb.population.atlas.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.data.access.DBManager;
import edu.umn.midb.population.atlas.utils.SMSNotifier;


/**
 * 
 * Servlet for handling unsubscribing. Used when adding an unsubscribe link to emails.
 * An example of the url in the link is:
 * 
 * http://midbatlas.io/NetworkProbabilityDownloader/SubscriptionHandlerServlet?action=unsubscribe&id=someone@gmail.com
 * 
 * @author jjfair
 *
 */
public class SubscriptionHandlerServlet extends HttpServlet  {
	
	private static Logger LOGGER = null;
	private static final String LOGGER_ID = " ::LOGGERID=Unsubscribe_Handler:: ";
	
	/**
	 * Handles a get request.
	 *  
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		
		String action = request.getParameter("action");
		
		Enumeration<String> params = request.getParameterNames();
		
		
		switch (action) {
		case "unsubscribe":
			handleUnsubscribe(request, response);
			break;
		}
	}


	/**
	 * 
	 * Handles the unsubscribe action by invoking {@link DBManager#deleteEmailAddress(String)}
	 * 
	 * @param request - HttpServletRequest
	 * @param response - HttpServletResponse
	 */
	public void handleUnsubscribe(HttpServletRequest request, HttpServletResponse response) {
		
		//String loggerId = appContext.getLoggerId();
		LOGGER.trace(LOGGER_ID + "handleUnsubscribe()...invoked.");
		
		String emailAddress = request.getParameter("id");
		int deletedRowCount = 0;
		
		try {
			deletedRowCount = DBManager.getInstance().deleteEmailAddress(emailAddress);
		}
		catch(Exception e) {
			LOGGER.fatal(LOGGER_ID + "Unable to delete email:" + emailAddress);
		}
		
		if(deletedRowCount==0) {
			String domainName = NetworkProbabilityDownloader.getDomainName();
			String message = "MIDB_APP_ERROR::::UNABLE TO DELETE EMAIL_ADDRESS::::" + domainName;
			message += "::::" + emailAddress + "::::check /midb/unsubscribe_list.cvs";
			SMSNotifier.sendNotification(message, "NetworkProbabilityDownloader");
		}		
		ServletContext sc = getServletContext();
		try {
			sc.getRequestDispatcher("/HTML/unsubscribe.html").forward(request, response);
		}
		catch(Exception e) {
			LOGGER.fatal(e.getMessage(), e);
		}
	}
	
	public void init() {
		LOGGER = LogManager.getLogger(SubscriptionHandlerServlet.class);
		LOGGER.info("SubscriptionHandlerServlet...init() invoked");
	}
	
}
