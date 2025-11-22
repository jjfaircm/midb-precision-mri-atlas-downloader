package docs;

import edu.umn.midb.population.atlas.config.files.FileLocator;

/**
 * 
 * Placeholder class for easily locating and retrieving the absolute path of a document
 * stored in this package.
 * 
 * @author jjfair
 *
 */
public class DocumentLocator {

	private static String rootPath = null;
	private static String packagePath = null;
	private static String absolutePath = null;
	
	static {
		rootPath = DocumentLocator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		packagePath = DocumentLocator.class.getPackageName().replace(".", "/");
		absolutePath = rootPath + packagePath + "/";
	}
	
	/**
	 * Get the absolute file path of a document located in this package directory
	 * @param documentName - String
	 * @return absolutePath - String
	 */
	public static String getPath(String documentName) {
		
		return absolutePath + documentName;
	}
}
