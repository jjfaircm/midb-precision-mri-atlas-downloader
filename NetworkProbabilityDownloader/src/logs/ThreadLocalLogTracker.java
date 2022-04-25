package logs;

/**
 * Encapsulates the logger id of the remote client.
 * 
 * @author jjfair
 *
 */
public class ThreadLocalLogTracker {

	private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();
	
	static {
		threadLocal.set("::DEFAULT:: ");
	}

	/**
	 * Sets the logger id
	 * 
	 * @param logIdString - String
	 */
	public static void set(String logIdString) {
		
		threadLocal.set(logIdString);
	}
	
	/**
	 * Returns the logger id encapsulated in the object
	 * 
	 * @return loggerId - String
	 */
	public static String get() {
		return threadLocal.get();
	}

}
