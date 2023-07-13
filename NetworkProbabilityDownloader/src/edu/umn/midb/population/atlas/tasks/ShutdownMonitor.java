package edu.umn.midb.population.atlas.tasks;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Listens for the shutdown event from the servlet container so that independent 
 * asynchronous threads can be notified and terminate gracefully.
 * 
 * 
 * @author jjfair
 *
 */
@WebListener
public class ShutdownMonitor implements ServletContextListener {

	//NOTE:  DO NOT USE ANY LOG4J FUNCTIONALITY HERE BECAUSE THIS CLASS WILL
	//       LOAD DURING THE WEBAPP STARTUP, WHICH IS BEFORE THE LOG4J
	//       DYNAMIC CONFIGURATION AND WILL CAUSE ALL LOGGING TO FAIL
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//add any code you want/need to do at startup
	}
	
	/**
	 * Sends shutdown triggers to the three {@link Tracker} threads that run as independent
	 * asynchronous threads. This is the list of threads:
	 * <ul>
	 * <li>{@link DownloadTracker}</li>
	 * <li>{@link EmailTracker}</li>
	 * <li>{@link WebHitsTracker}</li>
	 * </ul>
	 * 
	 * <p>
	 * It also invokes the JDBC DriverManager.deregisterDriver to unload any JDBC
	 * Drivers.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent destroyedEvent) {
		System.out.println("SHUTDOWN in ShutdownMonitor");
		FileDownloadEntry fdEntry = new FileDownloadEntry();
		fdEntry.setShutdownTrigger(true);
		DownloadTracker.getInstance().addDownloadEntry(fdEntry);
		WebHitEntry whEntry = new WebHitEntry();
		whEntry.setShutdownTrigger(true);
		WebHitsTracker.getInstance().addWebHitEntry(whEntry);
		EmailAddressEntry emailEntry = new EmailAddressEntry();
		emailEntry.setShutdownTrigger(true);
		EmailTracker.getInstance().addEmailAddressEntry(emailEntry);
		System.out.println("SHUTDOWN in ShutdownMonitor, after sending SHUTDOWN entry.");
		//LOGGER.info(" ::LOGGER_ID=ShutdownMonitor:: context destroyed handler invoked.");
		
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		   while (drivers.hasMoreElements()) {
		     Driver driver = drivers.nextElement();
		     try {
		       DriverManager.deregisterDriver(driver);
		     }
		     catch (SQLException e) {
		    	 System.out.println(e);
		    	 e.printStackTrace();
		     }
		   }
	}

}
