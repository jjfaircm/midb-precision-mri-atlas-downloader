package edu.umn.midb.population.atlas.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import logs.ThreadLocalLogTracker;

/**
 * 
 * Convenience class for changing ownership of a system object or getting available disk storage.
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
	
	public static ServerStorageStats getFreeStorageStats() {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "executeCommand()...invoked");
		
		String command = "df -h";
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", command);
		
		int processExitValue = -999;
		String processExitValueString = null;
		boolean errorDetected = false;
		int totalWaits = 0;
		Process commandProcess = null;
		BufferedReader stdoutReader = null;
		BufferedReader stderrReader = null;
		String response = null;
		String targetAmountMarker = "G";
		int beginIndex = 0;
		int endIndex = 0;
		String availAmountString = null;
		boolean isGB = true;
		boolean isTB = false;
		ServerStorageStats stats = new ServerStorageStats();
		
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
					readCount++;

					if(readCount == 2) {
						availAmountString = parseFreeStorageAmount(feedbackLine);
						if(availAmountString.indexOf("G") == -1) {
							targetAmountMarker = "Ti";
							stats.setUnitOfMeasure("TB");
							isTB = true;
							isGB = false;
						}
						if(isGB) {
							availAmountString = availAmountString.replace("G", " Gigabytes");
						    response = "Available free storage: " + availAmountString;
							endIndex = availAmountString.indexOf("Gigabytes");
						}
						else if(isTB) {
							availAmountString = availAmountString.replace("Ti", " Terrabytes");
						    response = "Available free storage: " + availAmountString;
							endIndex = availAmountString.indexOf("Terrabytes");
						}
						stats.setMessage(response);
						String amountString = availAmountString.trim();
						amountString = amountString.substring(0, endIndex);
						amountString = amountString.trim();
						float amountF = Float.parseFloat(amountString);
						stats.setAmount(amountF);
					}
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
		LOGGER.trace(loggerId + "executeCommand()...exit");
		return stats;
	}
	
	private static String parseFreeStorageAmount(String statsLine) {
		int beginIndex = -1;
		int endIndex = -1;
		
		for(int i=0; i<3; i++) {
			statsLine = statsLine.trim();
			beginIndex = statsLine.indexOf(" ");
			statsLine = statsLine.substring(beginIndex).trim();
		}
		statsLine = statsLine.trim();
		endIndex = statsLine.indexOf(" ");
		statsLine = statsLine.substring(0, endIndex);
		
		return statsLine;
		
	}
	
}
