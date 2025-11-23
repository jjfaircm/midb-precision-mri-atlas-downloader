package edu.umn.midb.population.atlas.utils;

import java.io.File;

/**
 * Convenience class for correcting file names.
 * 
 * @author jjfair
 *
 */
public class FileCorrector {
	
	/**
	 * Corrects file names so that the client can correctly infer the threshold percentage
	 * value. For example, a file named avg_number_of_networks_thresh1.6.png will be renamed
	 * to avg_number_of_networks_thresh0.16.png
	 * 
	 * 
	 * @param oldName - String
	 * @param rootPath - String
	 */
	public static void renameFile(String oldName, String rootPath) {
		
		   String oldAbsolutePath = rootPath + oldName;
		   
		   String newRootPath = "/midb/rename/overlapping_networks_new2/";
		   
		   File oldFile = new File(oldAbsolutePath);
		
		   if(oldName.equals("avg_number_of_networks_thresh0.2.png")) {
			   System.out.println("error");
		   }
	
	       String newName = oldName;
		   String savedChar = newName.substring(29, 30);
		   String png = ".png";
		   String dlabel = ".dlabel.nii";
		   boolean zeroProcessed = false;
		   
			if(newName.contains(png)) {
				int pngIndex = newName.indexOf(png);
				newName = newName.substring(0, pngIndex);
				if(newName.endsWith("0")) {
					int endIndex = newName.length()-2;
					newName = newName.substring(0, endIndex);
					newName = newName + png;
					zeroProcessed = true;
				}
				else {
					newName = newName + png;
				}
			}
			else {
				int dlabelIndex = newName.indexOf(dlabel);
				newName = newName.substring(0, dlabelIndex);
				if(newName.endsWith("0")) {
					int endIndex = newName.length()-2;
					newName = newName.substring(0, endIndex);
					newName = newName + dlabel;
					zeroProcessed = true;
				}
				else {
					newName = newName + dlabel;
				}
			}
		
	       int endIndex1 = newName.indexOf("sh") + 2;
	       int endIndex2 = newName.indexOf(".");
	       
	       if(!zeroProcessed) {
	    	   endIndex2 += 1;
	       }
	 
	       String part1 = newName.substring(0, endIndex1);
	       String part2 = newName.substring(endIndex2);
	       part1 = part1 + "0." + savedChar;
	       newName = part1 + part2;
	       
	       if(newName.contains("0.00")) {
	    	   System.out.println("!!!!!!!!!!!!!!!!!!!!!" + oldName);
	       }
	       System.out.println(oldName + "::::" + newName);
	       File newFile = new File(newRootPath + newName);
	       oldFile.renameTo(newFile);
	       
	       if(oldName.contains(".png") && !newName.contains(".png")) {
	    	   System.out.println("error");
	       }
	       if(oldName.contains(".dlabel") && !newName.contains(".dlabel")) {
	    	   System.out.println("error");
	       }
	}

}
