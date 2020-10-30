package edu.umn.midb.population.atlas.exception;

public class BIDS_FatalException extends RuntimeException  {
	
	String className = null;
	String methodName = null;
	String lineNumber = null;
	StackTraceElement[] stackTraceElements = null;

	public BIDS_FatalException(String message, StackTraceElement[] ste) {
		super(message);
		this.stackTraceElements = ste;
	}
	
	@Override
	public StackTraceElement[] getStackTrace() {
		return this.stackTraceElements;
	}
}
