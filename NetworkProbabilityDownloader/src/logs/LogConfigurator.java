package logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import edu.umn.midb.population.atlas.config.files.FileLocator;

/**
 * Handles the dynamic runtime configuration of the log4j logging utility.
 * 
 * @author jjfair
 *
 */
public class LogConfigurator { 
	
	static {
		configureLOG4J2();
	}
	
	/**
	 * Dynamically configures the log4j2 environment for logging.
	 *
	 */
	public static void configureLOG4J2() {
				  
		  ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		  AppenderComponentBuilder console = builder.newAppender("stdout", "Console"); 
		 
		  builder.add(console);
		 		  
		  String classLocation = LogConfigurator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		  String packageName = LogConfigurator.class.getPackageName();
		  String packageNamePath = packageName.replace(".", "/");
		  packageNamePath = packageNamePath + "/";
		  String targetPath = classLocation + packageNamePath;
		  
		  if(classLocation.contains("eclipse-workspace")) {
			  targetPath = "/Users/jjfair/git/network_probability_downloader/NetworkProbabilityDownloader/build/classes/logs/";
		  }
		  String archiveTargetPath = targetPath + "archive/";
		  String fileName = targetPath + "network_probability_downloader.log";	
		  //System.out.println("LogConfigurator.configureLOG4J2()...creating logger in:" + fileName);
		  AppenderComponentBuilder rollingFile = builder.newAppender("rolling", "RollingFile"); 
		  rollingFile.addAttribute("fileName", fileName);
		  rollingFile.addAttribute("BufferedIO", "false");
		  rollingFile.addAttribute("filePattern", archiveTargetPath + "$${date:yyyy-MM}/app-%d{MM- dd-yyyy}-%i.log");
		  
		  LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout");
		  layoutBuilder.addAttribute("pattern", "%d{ISO8601} [%t] %-5p (%F\\:%L) - %m%n");
		  
		  ComponentBuilder<AppenderComponentBuilder> triggeringPolicy = builder.newComponent("Policies");
		  triggeringPolicy.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "1M"));
		  rollingFile.addComponent(triggeringPolicy);
		  rollingFile.add(layoutBuilder);
		  console.add(layoutBuilder);
		  
		  ComponentBuilder<AppenderComponentBuilder> deletePolicy = builder.newComponent("DefaultRolloverStrategy");
		  deletePolicy.addAttribute("max", "10");
		  ComponentBuilder<AppenderComponentBuilder> deleteAction = builder.newComponent("delete");
		  deleteAction.addAttribute("basePath", targetPath);
		  deleteAction.addAttribute("maxDepth", "3");
		  
		  ComponentBuilder<AppenderComponentBuilder> fileNameFilter = builder.newComponent("IfFileName");
		  fileNameFilter.addAttribute("glob", "*/*/app-*.log");
		  deleteAction.addComponent(fileNameFilter);
		  
		  ComponentBuilder<AppenderComponentBuilder> retentionDays = builder.newComponent("IfLastModified");
		  retentionDays.addAttribute("age", "7d");
		  deleteAction.addComponent(retentionDays);
		  
		  deletePolicy.addComponent(deleteAction);
		  

		  rollingFile.addComponent(deletePolicy);
		  
		  //rollingFile.addAttribute("policies", triggeringPolicy);
		 
		  builder.add(console);
		  builder.add(rollingFile);
		  		 
		  console.add(layoutBuilder);
		  rollingFile.add(layoutBuilder);
		  
		  RootLoggerComponentBuilder rootLogger = null;
		  
		  String loggingLevel = null;
		  InputStream is = FileLocator.class.getResourceAsStream("log4j_level.txt");
		  BufferedReader br = new BufferedReader(new InputStreamReader(is));
		  String fileLine = null;
		  
		  try {
			  while((fileLine=br.readLine())!= null) {
				  if(fileLine.contains("LEVEL")) {
					  int index = fileLine.indexOf("=");
					  loggingLevel = fileLine.substring(index+1).trim();
				  }
			  }
		  }
		  catch(IOException ioE) {
			  System.out.println("LogConfigurator caught exception trying to read log4j_level.txt");
			  System.out.println(ioE.getMessage());
			  loggingLevel = "INFO";
		  }
		  
		  switch(loggingLevel) {
		  case "ALL":
			  rootLogger = builder.newRootLogger(Level.ALL);
			  break;
		  case "TRACE":
			  //System.out.println("Building TRACE logger");
			  rootLogger = builder.newRootLogger(Level.TRACE);
			  break;
		  case "DEBUG":
			  rootLogger = builder.newRootLogger(Level.DEBUG);
			  break; 
		  case "INFO":
			  rootLogger = builder.newRootLogger(Level.INFO);
			  break;
		  case "WARN":
			  rootLogger = builder.newRootLogger(Level.WARN);
			  break;
		  case "ERROR":
			  rootLogger = builder.newRootLogger(Level.ERROR);
			  break;
		  case "FATAL":
			  rootLogger = builder.newRootLogger(Level.FATAL);
			  break; 
		  }
		  //System.out.println("rootLogger=" + rootLogger);
		  //rootLogger = builder.newRootLogger(Level.TRACE);
		 // RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.TRACE);
		  rootLogger.add(builder.newAppenderRef("stdout"));
		  rootLogger.add(builder.newAppenderRef("rolling"));
		 
		  builder.add(rootLogger);
		  
		  Configurator.initialize(builder.build());
		  //StdOutErrRedirector.tieSystemOutAndErrToLog();
	  
	}
	
}
