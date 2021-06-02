package edu.umn.midb.population.atlas.tasks;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebListener
public class ShutdownMonitor implements ServletContextListener {

	//NOTE:  DO NOT USE ANY LOG4J FUNCTIONALITY HERE BECAUSE THIS CLASS WILL
	//       LOAD DURING THE WEBAPP STARTUP, WHICH IS BEFORE THE LOG4J
	//       DYNAMIC CONFIGURATION AND WILL CAUSE ALL LOGGING TO FAIL
	
	//private static Logger LOGGER = null;
	
	/*
	static {
		LOGGER = LogManager.getLogger(ShutdownMonitor.class);
	}
	*/
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//LogConfigurator.configureLOG4J2();
		//add any code you want/need to do at startup
		//System.out.println("DadsAppContextListener.contextInitialized()...dads-app starting.");
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent destroyedEvent) {
		System.out.println("SHUTDOWN in ShutdownMonitor");
		DownloadTracker.getInstance().addDownloadEntry("SHUTDOWN");
		HitTracker.getInstance().addHitEntry("SHUTDOWN");
		System.out.println("SHUTDOWN in ShutdownMonitor, after sending SHUTDOWN entry.");
		//LOGGER.info(" ::LOGGER_ID=ShutdownMonitor:: context destroyed handler invoked.");
	}

}
