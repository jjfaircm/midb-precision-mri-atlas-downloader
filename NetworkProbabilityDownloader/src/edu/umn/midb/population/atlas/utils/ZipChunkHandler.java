package edu.umn.midb.population.atlas.utils;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import logs.ThreadLocalLogTracker;

public class ZipChunkHandler {
	
	private ArrayList<String> filePartNames = new ArrayList<String>();
	private static final Logger LOGGER = LogManager.getLogger(ZipChunkHandler.class);


	public void uploadChunk(HttpServletRequest request, HttpServletResponse response) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "CreateMenuHandler()...constructor invoked.");
		
			
		try {
		    Part filePart = request.getPart("file");
		    String fileName = filePart.getSubmittedFileName();
		    
		    for (Part part : request.getParts()) {
		      part.write("/midb/staging/" + fileName);
		    }
		    response.getWriter().print("The file uploaded sucessfully.");
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
	}
}
