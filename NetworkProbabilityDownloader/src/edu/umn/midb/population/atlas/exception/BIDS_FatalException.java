package edu.umn.midb.population.atlas.exception;


/**
 * 
 * Custom application exception.
 * 
 * @author jjfair
 *
 */
public class BIDS_FatalException extends RuntimeException  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
