package edu.umn.midb.population.atlas.study.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.base.ApplicationContext;

public class StudyMaintenanceLock {
	
	/** the singleton instance. */ 
	private static StudyMaintenanceLock instance;
	
	private String lockId = "000000";
	private String lockingIPAddress = null;
	private long startTime = -1;
	private long expirationTime = -1;
	boolean locked = false;
	private LockType lockType = null;
	private LockResetStatus lockResetStatus = null;
	private static final Logger LOGGER = LogManager.getLogger(StudyMaintenanceLock.class);

	public static synchronized StudyMaintenanceLock getInstance() {
		 
		if(instance == null) {
			instance = new StudyMaintenanceLock();
		}
		return instance;
	}
	
	public String getLockId(ApplicationContext appContext) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "getLockId()...invoked, lockId=" + this.lockId);
		LOGGER.trace(loggerId + "getLockId()...exit");
		return this.lockId;
	}
	
	public void lock(ApplicationContext appContext, LockType lockType) {
		
		String loggerId = appContext.getLoggerId();
		String lockId = appContext.getTokenManager().getToken();
		this.lockId = lockId;
		this.lockingIPAddress = appContext.getRemoteAddress();
		LOGGER.trace(loggerId + "lock()...invoked, lockId=" + lockId);
		LOGGER.trace(loggerId + "lock()...action=" + lockType.getAction());
		this.locked = true;
		long currentTime = System.currentTimeMillis();
		this.expirationTime = currentTime + lockType.getExpirationDurationSeconds()*1000 ;
		LOGGER.trace(loggerId + "lock()...exit.");
	}
	
	public void unlock(ApplicationContext appContext) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "unlock()...invoked.");
		this.locked = false;
		this.lockType = null;
		this.expirationTime = -1;
		LOGGER.trace(loggerId + "unlock()...exit.");
	}
	
	public boolean isLocked(ApplicationContext appContext) {
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "isLocked()...invoked. isLocked=" + locked);
		LOGGER.trace(loggerId + "isLocked()...exit");
		return this.locked;
	}
	
	public LockResetStatus reset(ApplicationContext appContext) {
		
		String loggerId = appContext.getLoggerId();
		LOGGER.trace(loggerId + "reset()...invoked.");
		this.lockResetStatus = new LockResetStatus();
		long currentTime = System.currentTimeMillis();
		String message = null;
		if(!this.locked || currentTime >= this.expirationTime) {
			this.lockResetStatus.setResetStatus(appContext, true);
			message = "Lock successfully reset";
			this.lockResetStatus.setMessage(appContext, message);
			this.locked = false;
			this.expirationTime = -1;
		}
		else {
			long secondsRemaining = (this.expirationTime - currentTime)/1000;
			long minutesRemaining = secondsRemaining/60 + 1; //round up
			this.lockResetStatus.setResetStatus(appContext, false);
			message = "The current lock has not expired yet. Minutes remaining=" + minutesRemaining + "<br>Please wait until expiration and try the operation again.";
			this.lockResetStatus.setMessage(appContext, message);
		}
		LOGGER.trace(loggerId + "reset()...exit.");
		return this.lockResetStatus;
	}

}
