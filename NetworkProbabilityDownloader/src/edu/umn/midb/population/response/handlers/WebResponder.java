package edu.umn.midb.population.response.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import logs.ThreadLocalLogTracker;

public class WebResponder {
	
	private static final String DELIMITER = ":@:";
	private static final Logger LOGGER = LogManager.getLogger(WebResponder.class);
	
	
	public static void sendFileDownloadResponse(HttpServletResponse response, byte[] fileBinaryBuffer, String fileName) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "sendFileDownloadResponse()...invoked.");
				
		try {
			response.setContentType("image/nii");
			response.setHeader("X-Content-Type-Options", "nosniff");
			String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", fileName);
            response.setHeader(headerKey, headerValue);
			//response.setHeader("Content-Disposition","attachment;filename=" + fileName);
			//response.getWriter().println(encodedString);
			response.getOutputStream().write(fileBinaryBuffer);
			response.getOutputStream().flush();
		}
		catch(IOException ioE) {
			LOGGER.error(ioE.getLocalizedMessage(), ioE);
		}
		
		LOGGER.trace(loggerId + "sendFileDownloadResponse()...exit.");

	}
	
	public static void sendNeuralNetworkNamesResponse(HttpServletResponse response, ArrayList<String> networkTypes) {
		
		  String loggerId = ThreadLocalLogTracker.get();
		  LOGGER.trace(loggerId + "sendNeuralNetworkNamesResponse()...invoked.");

		
	      JsonArray jsonArray1 = new Gson().toJsonTree(networkTypes).getAsJsonArray();
	      
	      try {
	    	  response.getWriter().println(jsonArray1);
	      }
	      catch(IOException ioE) {
	    	  LOGGER.error(ioE.getLocalizedMessage(), ioE);
	      }
		  LOGGER.trace(loggerId + "sendNeuralNetworkNamesResponse()...exit.");
	}
	
	public static void sendThresholdImagesResponse(HttpServletResponse response, ArrayList<String> filePaths, ArrayList<byte[]> fileByteBuffers) {
		
		 String loggerId = ThreadLocalLogTracker.get();
		 LOGGER.trace(loggerId + "sendThresholdImagesResponse()...invoked.");

		//parse out the threshold numbers
		String aFileName = null;
		String beginIndexMarker = "thresh";
		String endIndexMarker = ".png";
		int beginIndexMarkerLength = beginIndexMarker.length();
		int beginIndex = 0;
		int endIndex = 0;
		int numOfFiles = filePaths.size();
		int counter = 0;
		
		ArrayList<String> theshValues = new ArrayList<String>();
		String aThreshValue = null;
		String threshMin = null;
		float threshMinFloat = 0;
		String threshMax = null;
		float threshMaxFloat = 0;
		float threshStepFloat = 0;
		float floatValue = 0;
		
		Iterator<String> filePathsIt = filePaths.iterator();
		
		while(filePathsIt.hasNext()) {
			aFileName = filePathsIt.next();
			beginIndex = aFileName.indexOf(beginIndexMarker) + beginIndexMarkerLength+1;
			endIndex = aFileName.indexOf(endIndexMarker);
			aThreshValue = aFileName.substring(beginIndex, endIndex);
			theshValues.add(aThreshValue);
			
			if(counter==0) {
				threshMin = aThreshValue;
				threshMinFloat = Float.parseFloat(threshMin);
			}
			else if(counter==numOfFiles-1) {
				threshMax = aThreshValue;
				threshMaxFloat = Float.parseFloat(threshMax);
			}
			else if(counter==1) {
				floatValue = Float.parseFloat(aThreshValue);
				threshStepFloat = floatValue - threshMinFloat;
			}
			counter++;
		}
		
		String threshStepString = Float.toString(threshStepFloat);
		int threshStepIndex = threshStepString.indexOf(".")+3;
		threshStepString = threshStepString.substring(0, threshStepIndex);
		
		Iterator<byte[]> bufferIt = fileByteBuffers.iterator();
		byte[] imageBuffer = null;
		ArrayList<String> base64ImageStrings = new ArrayList<String>();
		String encodedString = null;
		
		while(bufferIt.hasNext()) {
			imageBuffer = bufferIt.next();
			encodedString = Base64.getEncoder().encodeToString(imageBuffer);
			base64ImageStrings.add(encodedString);
		}
		
		String base64ImageStringsCleaned = base64ImageStrings.toString();
		base64ImageStringsCleaned = base64ImageStringsCleaned.replace("[", "");
		base64ImageStringsCleaned = base64ImageStringsCleaned.replace("]", "");
		
		String filePathsCleaned = filePaths.toString();
		filePathsCleaned = filePathsCleaned.replace("[", "");
		filePathsCleaned = filePathsCleaned.replace("]", "");
				
		try {
	      response.getWriter().println(base64ImageStringsCleaned + DELIMITER + filePathsCleaned);	      
		}
	    catch(IOException ioE) {
	    	  System.out.println(ioE.getMessage());
	     }
		 LOGGER.trace(loggerId + "sendThresholdImagesResponse()...exit.");
	}
	
	public static void sendTestFile(HttpServletResponse resp, HttpServletRequest req) {
		
		 resp.setContentType("image/png");
	        resp.setHeader("Content-disposition", "attachment; filename=DCAN.png");
	 
	        try(InputStream in = req.getServletContext().getResourceAsStream("/WEB-INF/DCAN.png");
	          OutputStream out = resp.getOutputStream()) {
	 
	            byte[] buffer = new byte[1048];
	        
	            int numBytesRead;
	            while ((numBytesRead = in.read(buffer)) > 0) {
	                out.write(buffer, 0, numBytesRead);
	            }
	        }
		catch(Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}
	}
}
