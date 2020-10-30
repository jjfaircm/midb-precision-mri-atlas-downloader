package logs;

public class ThreadLocalLogTracker {

	private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();
	
	static {
		threadLocal.set("::DEFAULT:: ");
	}

	public static void set(String logIdString) {
		
		threadLocal.set(logIdString);
	}
	
	public static String get() {
		return threadLocal.get();
	}

}
