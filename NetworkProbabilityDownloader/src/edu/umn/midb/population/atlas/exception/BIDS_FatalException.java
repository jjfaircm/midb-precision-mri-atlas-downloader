package edu.umn.midb.population.atlas.exception;

public class BIDS_FatalException extends RuntimeException  {
	
	String className = null;
	String methodName = null;
	String lineNumber = null;
	StackTraceElement[] stackTraceElements = null;

	/**
	 * Encapsulates any unhandled exception encountered at runtime.
	 * 
	 * @param message The encapsulated Java message that was originally encountered
	 * @param ste Array of StackTraceElement
	 */
	public BIDS_FatalException(String message, StackTraceElement[] ste) {
		super(message);
		this.stackTraceElements = ste;
	}
	
	@Override
	/**
	 * Returns the array of StackTraceElement objects
	 */
	public StackTraceElement[] getStackTrace() {
		return this.stackTraceElements;
	}
}
