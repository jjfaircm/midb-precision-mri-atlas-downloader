package edu.umn.midb.population.atlas.config.files;

import edu.umn.midb.population.atlas.utils.CountryNamesResolver;

/**
 * This is a placeholder class to easily locate files when using
 * Class.getResourceAsStream(...). Also used to locate a config file when it is stored
 * in this package folder. An example is {@link CountryNamesResolver}  
 * 
 * @author jjfair
 *
 */
public class FileLocator { 

	private static String rootPath = null;
	private static String packagePath = null;
	private static String absolutePath = null;
	
	static {
		rootPath = FileLocator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		packagePath = FileLocator.class.getPackageName().replace(".", "/");
		absolutePath = rootPath + packagePath + "/";
	}
	
	/**
	 * Get the absolute file path of a file located in this package directory
	 * @param fileName - String
	 * @return absolutePath - String
	 */
	public static String getPath(String fileName) {
		
		return absolutePath + fileName;
	}
	
}
