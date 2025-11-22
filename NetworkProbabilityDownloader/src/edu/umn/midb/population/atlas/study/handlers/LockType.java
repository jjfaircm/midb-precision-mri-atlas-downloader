package edu.umn.midb.population.atlas.study.handlers;

public enum LockType {
	
	ADD("addStudy", 15*60),
	REMOVE("removeStudy", 5*60),
	UPDATE("updateStudy", 15*60);
	
	private final String action;
	private final long expirationDurationSeconds;
	
	LockType(String action, long expirationDurationSeconds) {
		this.action = action;
		this.expirationDurationSeconds = expirationDurationSeconds;
	}
	
	public long getExpirationDurationSeconds() {
		return this.expirationDurationSeconds;
	}
	
	public String getAction() {
		return this.action;
	}

}
