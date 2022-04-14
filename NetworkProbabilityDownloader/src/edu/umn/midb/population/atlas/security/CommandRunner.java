package edu.umn.midb.population.atlas.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umn.midb.population.atlas.utils.Utils;
import logs.ThreadLocalLogTracker;

/**
 * 
 * Convenience class for changing ownership by executing a system bash command.
 * 
 * @author jjfair
 *
 */
public class CommandRunner {
	
	private static Logger LOGGER = LogManager.getLogger(CommandRunner.class);
	private static String LOGGER_ID = " ::COMMAND_RUNNER:: ";
	private static String COMMAND_CHANGE_OWNER_TEMPLATE = "sudo chown newOwner objectPath";
	private static long PROGRAM_WAIT_TIMER = 500;
	private static int MAX_PROGRAM_WAIT_ATTEMPTS = 5;
	
	/**
	 * 
	 * Executes a system bash command for changing the ownership of an object.
	 * 
	 * @param newOwner - String
	 * @param objectPath - String
	 */
	public static void changeOwnership(String newOwner, String objectPath) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "changeOwnershipCSV()...invoked");
		
		String command = COMMAND_CHANGE_OWNER_TEMPLATE;
		command = command.replace("newOwner", newOwner);
		command = command.replace("objectPath", objectPath);
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", command);
		
		int processExitValue = -999;
		String processExitValueString = null;
		boolean errorDetected = false;
		int totalWaits = 0;
		Process commandProcess = null;
		BufferedReader stdoutReader = null;
		BufferedReader stderrReader = null;
		
		try {
			commandProcess = pb.start();
			InputStream stdinStream = commandProcess.getInputStream();
			InputStreamReader stdinStreamReader = new InputStreamReader(stdinStream);
			stdoutReader = new BufferedReader(stdinStreamReader);
			InputStream stderrStream = commandProcess.getErrorStream();
			InputStreamReader stderrStreamReader = new InputStreamReader(stderrStream);
			stderrReader = new BufferedReader(stderrStreamReader);
			
			boolean timeout = false;
			boolean success = true;
			int readCount = 0;
	
			String feedbackLine = null;
			
			while(!stdoutReader.ready()) {
				if(stderrReader.ready()) {
					errorDetected = true;
					break;
				}
				
	        	Utils.pause(PROGRAM_WAIT_TIMER);
	        	totalWaits++;
	        	
	        	if(totalWaits > MAX_PROGRAM_WAIT_ATTEMPTS) {
	        		timeout = true;
	        		break;
	        	}
			}
			
			if(stdoutReader.ready()) {
				while((feedbackLine=stdoutReader.readLine()) != null) {
					LOGGER.trace(feedbackLine);
					readCount++;
				}
				success = true;
			}
			
			else if(errorDetected) {
				while((feedbackLine = stderrReader.readLine()) != null) {
					LOGGER.trace(feedbackLine);
				}
			}
		}
		catch(IOException ioE) {
			LOGGER.trace(LOGGER_ID + "Error running command:" + command);
			LOGGER.trace(ioE.getMessage(), ioE);
		}
		LOGGER.trace(loggerId + "changeOwnershipCSV()...exit");

	}


}
