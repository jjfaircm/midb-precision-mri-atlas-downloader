package edu.umn.midb.population.atlas.study.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.base.ApplicationContext;

public class LockResetStatus {
	private boolean resetStatus = true;
	private String message = null;
	private static final Logger LOGGER = LogManager.getLogger(StudyMaintenanceLock.class);

	
	public boolean isSuccessful(ApplicationContext appContext) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "isSuccessful()...invoked: resetStatus=" + this.resetStatus);
		LOGGER.trace(loggerId + "isSuccessful()...exit.");
		return resetStatus;
	}
	
	public String getMessage(ApplicationContext appContext) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "getMessage()...invoked.Message=" + this.message);
		LOGGER.trace(loggerId + "getMessage()...exit.");
		return this.message;
	}
	
	public void setMessage(ApplicationContext appContext, String message) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "setMessage()...invoked.Message=" + this.message);
		this.message = message;
		LOGGER.trace(loggerId + "setMessage()...exit");	
	}
	
	public void setResetStatus(ApplicationContext appContext, boolean status) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "setResetStatus()...invoked: status=" + status);
		this.resetStatus = status;
		LOGGER.trace(loggerId + "setResetStatus()...exit");
	}

}
