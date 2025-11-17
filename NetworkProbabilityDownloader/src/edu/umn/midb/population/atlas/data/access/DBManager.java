package edu.umn.midb.population.atlas.data.access;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.umn.midb.population.atlas.base.ApplicationContext;
import edu.umn.midb.population.atlas.exception.DiagnosticsReporter;
import edu.umn.midb.population.atlas.tasks.AdminAccessEntry;
import edu.umn.midb.population.atlas.utils.IPInfoRequestor;
import edu.umn.midb.population.atlas.utils.IPLocator;
import edu.umn.midb.population.atlas.utils.Utils;
import logs.ThreadLocalLogTracker;

/**
 * 
 * JDBC database access class for interacting with MYSQL tables. Connection pooling is
 * not used since there are only 3 threads that require a connection. On rare occasions
 * this class uses an internal connection, but this is rare and only occurs when someone
 * is using the admin console.
 * 
 * @author jjfair
 *
 */
public class DBManager {
	
	private static DBManager instance = null;
	private static String jdbcDriverName = "com.mysql.cj.jdbc.Driver";
	private static Logger LOGGER = null;
	private static String LOGGER_ID = " ::LOGGERID=DBManager:: ";
	protected static final String EMAIL_ADDRESSES_CSV_FILE = "/midb/email_addresses.csv";
	protected static final String EMAIL_ADDRESS_CSV_TEMPLATE = "EMAIL_ADDRESS,FIRST_NAME,LAST_NAME";
	protected static final String EMAIL_ADDRESS_CSV_HEADER = "Email Address,First Name,Last Name";
	protected static final String EMAIL_UNSUBSCRIBE_FILE = "/midb/unsubscribe_list.csv";
	protected static final String WEB_HITS_GEOLOC_CSV_FILE = "/midb/web_hits_geoloc.csv";
	protected static final String DOWNLOAD_HITS_GEOLOC_CSV_FILE = "/midb/file_downloads_geoloc.csv";
	protected static final String GEOLOC_CSV_HEADER = "Latitude,Longitude,Location_Name";
	protected static final String GEOLOC_CSV_TEMPLATE = "latitude,longitude,locationName";
	protected static final String ADMIN_ENTRY_TEMPLATE = "IP_ADDRESS,ACTION,TIMESTAMP,VALID_IPADDRESS,VALID_PASSWORD";
	protected static final String ADMIN_ACCESS_FILE = "/midb/tracking/admin_access.csv";
	public static final long MAX_IDLE_TIME = 30*60*1000;
	
	static {
		LOGGER = LogManager.getLogger(DBManager.class);
		
		try {
			Class.forName(jdbcDriverName);
		}
		catch(ClassNotFoundException cnfE) {
			LOGGER.fatal(cnfE.getMessage(), cnfE);
		}
	}
	
	private String jdbcUrl = "jdbc:mysql://localhost/midbatlas_db";
	private String password = null;
	private String queryTest = "SELECT COUNT(*) FROM email_addresses";
	private String queryEmailAddresses = "SELECT email_address, first_name, last_name FROM email_addresses";
	private String queryWebHitsMapUrl = "SELECT url from map_urls WHERE map_name LIKE 'WEB_HITS_MAP'";
	private String queryDownloadHitsMapUrl = "SELECT url from map_urls WHERE map_name LIKE 'FILE_DOWNLOADS_MAP'";
    private String queryWebHits = "SELECT create_date, ip_address, city, state, country, latitude, longitude from web_hits order by create_date desc limit 200";
	private String queryFileDownloads = "SELECT create_date, file_name, study, ip_address, email_address, city, state, country, latitude, longitude from file_downloads"; 
	private String queryAdminAccessRecords = "SELECT create_date, ip_address, action, valid_password, city, state, country FROM admin_access ORDER BY create_date";
	private String insertAdminAccessRecord = "INSERT INTO admin_access (ip_address, action, valid_password, city, state, country) VALUES(?,?,?,?,?,?)";
	private String updateMapURL = "UPDATE map_urls SET url= ? WHERE map_name LIKE ?";
	private String deleteEmailAddress = "DELETE FROM email_addresses WHERE email_address LIKE ?";
	private String deleteExtraneousWebHits = "DELETE FROM web_hits WHERE ip_address LIKE ?";

	/**
	 * Hides the default constructor since this uses a Singleton pattern.
	 */
	private DBManager() {
		
	}
	
	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return instance {@link DBManager}
	 */
	public static synchronized DBManager getInstance() {
	
		if(instance==null) {
			instance = new DBManager();
		}
		return instance;
	}
	
	/**
	 * 
	 * Adds an {@link AdminAccessEntry} to the /midb/tracking/admin_access.csv file.
	 * 
	 * @param aaEntry - {@link AdminAccessEntry}
	 * @param appContext -  {@link ApplicationContext}
	 */
	protected void addAdminAccessEntryToCSVFile(AdminAccessEntry aaEntry, ApplicationContext appContext) {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateAdminAccessFile()...invoked.");
		
		
		String formattedTS = appContext.getCurrentActionFormattedTimestamp();
		formattedTS = formattedTS.replace(" ", ",");
		String adminEntry = ADMIN_ENTRY_TEMPLATE.replace("IP_ADDRESS", aaEntry.getRequestorIPAddress());
		adminEntry = adminEntry.replace("ACTION", appContext.getCurrentAction());
		adminEntry = adminEntry.replace("TIMESTAMP", formattedTS);
		adminEntry = adminEntry.replace("VALID_IP", "n/a");
		adminEntry = adminEntry.replace("VALID_PASSWORD", aaEntry.getValidPasswordString());

		FileWriter fw = null;
		PrintWriter pw = null;
		
		try {
			fw = new FileWriter(ADMIN_ACCESS_FILE, true);
			pw = new PrintWriter(fw);
			pw.println(adminEntry);
			pw.close();
			LOGGER.info(loggerId + "adminAccess, ipAddress=" + aaEntry.getRequestorIPAddress());
		}
		catch(IOException ioE) {
			LOGGER.error(loggerId + "Failed to create PrintWriter for file=" + ADMIN_ACCESS_FILE);
			LOGGER.error(ioE.getMessage(), ioE);
		}
		LOGGER.trace(loggerId + "updateAdminAccessFile()...exit.");
	}
	
	/**
	 * 
	 * Adds an emailAddress entry to the /midb/unsubscribe_list.csv file.
	 * 
	 * @param emailAddress - String
	 */
	private void addUnsubscribedEmailToCSVFile(String emailAddress) {
		try {
			FileWriter fw = new FileWriter(EMAIL_UNSUBSCRIBE_FILE, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(emailAddress);
			pw.close();
		}
		catch(IOException ioE) {
			LOGGER.fatal(LOGGER_ID + "Unable to write emailAddress to " + EMAIL_UNSUBSCRIBE_FILE);
			DiagnosticsReporter.createDiagnosticsEntry(ioE);
		}
	}
	
	/**
	 * 
	 * Checks a jdbc connection by executing a simple query.
	 * 
	 * @param connection - {@link Connection}
	 * @param failedMessageRef - {@link AtomicReference}
	 * @param invokerClassName - String
	 * @return boolean - indicating if connection is valid
	 */
	public boolean checkDatabaseConnection(Connection connection, AtomicReference<String> failedMessageRef, String invokerClassName) {
		
		LOGGER.trace(LOGGER_ID + "checkDatabaseConnection()...invoked");		
		boolean connectionOK = false;
		Statement testStatement = null;
		
		try {
			testStatement = connection.createStatement();
			testStatement.execute(this.queryTest);
			connectionOK = true;
			testStatement.close();
		}
		catch(Exception e) {
			LOGGER.error(LOGGER_ID + "checkDatabaseConnection()...failed.");
			LOGGER.error(e.getMessage(), e);
			if(failedMessageRef != null) {
				failedMessageRef.set("MIDB_ATLAS APP: Database Connectivity in failed state. Error=" + e.getMessage());
			}
		}
		LOGGER.trace(LOGGER_ID + "checkDatabaseConnection() for " + invokerClassName + "...exit, connectionOK=" + connectionOK);		
		return connectionOK;
		
	}
	
	/**
	 * 
	 * Creates a new csv file containing all email address currently stored in the database
	 * 
	 * @param emailAddresses - ArrayList of emailAddresses
	 */
	private void createNewEmailAddressesCSVFile(ArrayList<EmailAddressRecord> emailAddresses)  {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "createNewEmailAddressesCSVFile()...invoked");
	
		int recordCount = 0;
		Iterator<EmailAddressRecord> emailAddressesIt = emailAddresses.iterator();
		EmailAddressRecord emailAddressRecord = null;
		String emailEntryLine = null;
		String emailAddress = null;
		String firstName = null;
		String lastName = null;
		
		
		try {
			FileWriter fileWriter = new FileWriter(EMAIL_ADDRESSES_CSV_FILE);
			BufferedWriter bufWriter = new BufferedWriter(fileWriter);
			bufWriter.write(EMAIL_ADDRESS_CSV_HEADER);
			bufWriter.newLine();
			
			
			while(emailAddressesIt.hasNext()) {
				recordCount++;
				emailAddressRecord = emailAddressesIt.next();
				emailAddress = emailAddressRecord.getEmailAddress();
				firstName = emailAddressRecord.getFirstName();
				lastName = emailAddressRecord.getLastName();
				emailEntryLine = EMAIL_ADDRESS_CSV_TEMPLATE;
				emailEntryLine = emailEntryLine.replace("EMAIL_ADDRESS", emailAddress);
				emailEntryLine = emailEntryLine.replace("FIRST_NAME", firstName);
				emailEntryLine = emailEntryLine.replace("LAST_NAME", lastName);
				bufWriter.write(emailEntryLine);
				bufWriter.newLine();
			}
		
			bufWriter.flush();
			fileWriter.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		LOGGER.trace(loggerId + "createNewEmailAddressesCSVFile()...exit, recordCount=" + recordCount);
	}
	
	/**
	 * Create a new version of the file_downloads_geoloc.csv file in the /midb folder. This file is
	 * used by google maps to create a new map that depicts locations where all file download requests
	 * originated from.
	 * 
	 * @param downloadHits - ArrayList of {@link FileDownloadRecord}
	 */
	private void createNewFileDownloadHitsGeoCSVFile(ArrayList<FileDownloadRecord> downloadHits) {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "createNewFileDownloadHitsGeoCSVFile()...invoked");
	
		int recordCount = 0;
		Iterator<FileDownloadRecord> webHitsIt = downloadHits.iterator();
		FileDownloadRecord downloadHitRecord = null;
		String downloadGeoEntryLine = null;
		String latitude = null;
		String longitude = null;
		String locationName = null;
		
		try {
			FileWriter fileWriter = new FileWriter(DOWNLOAD_HITS_GEOLOC_CSV_FILE);
			BufferedWriter bufWriter = new BufferedWriter(fileWriter);
			bufWriter.write(GEOLOC_CSV_HEADER);
			bufWriter.newLine();
			
			while(webHitsIt.hasNext()) {
				recordCount++;
				downloadHitRecord = webHitsIt.next();
				latitude = downloadHitRecord.getLatitude();
				longitude = downloadHitRecord.getLongitude();
				locationName = downloadHitRecord.getLocationName();
				downloadGeoEntryLine = GEOLOC_CSV_TEMPLATE;
				downloadGeoEntryLine = downloadGeoEntryLine.replace("latitude", latitude);
				downloadGeoEntryLine = downloadGeoEntryLine.replace("longitude", longitude);
				downloadGeoEntryLine = downloadGeoEntryLine.replace("locationName", locationName);
				bufWriter.write(downloadGeoEntryLine);
				bufWriter.newLine();
			}
		
			bufWriter.flush();
			fileWriter.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			DiagnosticsReporter.createDiagnosticsEntry(e);
		}
		LOGGER.trace(loggerId + "createNewFileDownloadHitsGeoCSVFile()...exit, recordsInserted=" + recordCount);
	}
	
	/**
	 * 
	 * Creates a csv file where each entry contains: longitude, latitude, locationName
	 * 
	 * @param webHits - ArrayList of {@link WebHitRecord}
	 */
	private void createNewWebHitsGeoCSVFile(ArrayList<WebHitRecord> webHits) {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "createNewWebHitGeoCSVFile()...invoked");
	
		int recordCount = 0;
		Iterator<WebHitRecord> webHitsIt = webHits.iterator();
		WebHitRecord webHitRecord = null;
		String whGeoEntryLine = null;
		String latitude = null;
		String longitude = null;
		String locationName = null;
		
		
		try {
			FileWriter fileWriter = new FileWriter(WEB_HITS_GEOLOC_CSV_FILE);
			BufferedWriter bufWriter = new BufferedWriter(fileWriter);
			bufWriter.write(GEOLOC_CSV_HEADER);
			bufWriter.newLine();
			
			
			while(webHitsIt.hasNext()) {
				recordCount++;
				webHitRecord = webHitsIt.next();
				latitude = webHitRecord.getLatitude();
				longitude = webHitRecord.getLongitude();
				locationName = webHitRecord.getLocationName();
				whGeoEntryLine = GEOLOC_CSV_TEMPLATE;
				whGeoEntryLine = whGeoEntryLine.replace("latitude", latitude);
				whGeoEntryLine = whGeoEntryLine.replace("longitude", longitude);
				whGeoEntryLine = whGeoEntryLine.replace("locationName", locationName);
				bufWriter.write(whGeoEntryLine);
				bufWriter.newLine();
			}
		
			bufWriter.flush();
			fileWriter.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			DiagnosticsReporter.createDiagnosticsEntry(e);
		}
		
		LOGGER.trace(loggerId + "createNewWebHitGeoCSVFile()...exit, recordsInserted=" + recordCount);
	}
	
	/**
	 * Deletes an email address from the email_addresses table in MYSQL.
	 * 
	 * @param emailAddress - String
	 * @return deletedRowCount - int
	 */
	public int deleteEmailAddress(String emailAddress)  {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "deleteEmailAddress()...invoked");
		
		int deletedRowCount = 0;
		
		try {
			Connection conn = getDBConnection();
			PreparedStatement ps = conn.prepareStatement(deleteEmailAddress);
			ps.setString(1, emailAddress);
			deletedRowCount = ps.executeUpdate();
		}
		catch(SQLException sqlE) {
			LOGGER.fatal("Unable to delete emailAddress=" + emailAddress);
			DiagnosticsReporter.createDiagnosticsEntry(sqlE);
		}
			
		if(deletedRowCount==0) {
			addUnsubscribedEmailToCSVFile(emailAddress);
		}
		LOGGER.trace(loggerId + "deleteEmailAddress()...exit");
		return deletedRowCount;
	}

	/**
	 * 
	 * Returns and ArrayList of all records in the admin_access table in MYSQL.
	 * Each record in the ArrayList will be an instance of {@link AdminAccessRecord}
	 * 
	 * @return aaRecords - ArrayList of {@link AdminAccessRecord}
	 * @throws SQLException - unhandled exception
	 */
	public ArrayList<AdminAccessRecord> getAdminAccessRecords() throws SQLException {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getAdminAccessRecords()...invoked");
		
		ArrayList<AdminAccessRecord> aaRecords = new ArrayList<AdminAccessRecord>();
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(queryAdminAccessRecords);
		ResultSet rs = stmt.getResultSet();
		Timestamp ts = null;
		String ipAddress = null;
		AdminAccessRecord aaRecord = null;
		String timestampStr = null;
		String currentAction = null;
		String validPassword = null;
		String city = null;
		String state = null;
		String country = null;
				
		int index = 0;
			
		while(rs.next()) {
			ts = rs.getTimestamp(1);
			timestampStr = ts.toString();
			index = timestampStr.lastIndexOf(".");
			timestampStr = timestampStr.substring(0, index); 
			ipAddress = rs.getString(2);
			currentAction = rs.getString(3);
			validPassword = rs.getString(4);
			city = rs.getString(5);
			state = rs.getNString(6);
			country = rs.getString(7);
			aaRecord = new AdminAccessRecord(ipAddress, currentAction);
			aaRecord.setCreateDate(timestampStr);
			aaRecord.setValidPassword(validPassword);
			aaRecord.setCity(city);
			aaRecord.setState(state);
			aaRecord.setCountry(country);
			aaRecords.add(aaRecord);
		}
		
		stmt.close();
		conn.close();
		LOGGER.trace(loggerId + "getAdminAccessRecords()...exit");
		return aaRecords;
	}
	
	/**
	 * 
	 * Returns a jdbc connection that has been tested with a simple default query.
	 * 
	 * @return jdbcConnection - {@link Connection}
	 * @throws SQLException - unhandled exception
	 */
	public Connection getDBConnection() throws SQLException {
		LOGGER.trace(LOGGER_ID + "getDBConnection()...invoked");		
		
		boolean connectionOK = false;
		Connection jdbcConnection = null;
	
		jdbcConnection = DriverManager.getConnection(jdbcUrl, "midbatlas", password);
		connectionOK = checkDatabaseConnection(jdbcConnection, null, "DBManager");

		LOGGER.trace(LOGGER_ID + "getDBConnection()...exit, connectionOK=" + connectionOK);		
		return jdbcConnection;
	}
	
	/**
	 * 
	 * Returns an ArrayList of {@link EmailAddressRecord}.
	 * 
	 * @return emailAddresses - ArrayList
	 * @throws SQLException - unhandled exception
	 */
	public ArrayList<EmailAddressRecord> getEmailAddresses() throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getEmailAddresses()...invoked");
				
		ArrayList<EmailAddressRecord> emailRecords = new ArrayList<EmailAddressRecord>();
		String emailAddress = null;
		String firstName = null;
		String lastName = null;
		EmailAddressRecord eaRecord = null;
		
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(this.queryEmailAddresses);
		
		while(rs.next()) {
			emailAddress = rs.getString(1);
			firstName = rs.getString(2);
			lastName = rs.getString(3);
			eaRecord = new EmailAddressRecord(emailAddress, firstName, lastName);
			emailRecords.add(eaRecord);
		}
			
		stmt.close();
		conn.close();
		LOGGER.trace(loggerId + "getEmailAddresses()...exit");
		return emailRecords;
	}
	
	/**
	 * 
	 * Returns an ArrayList of {@link FileDownloadRecord} containing all the records
	 * in the file_downloads table in MYSQL.
	 * 
	 * @return fileDownloadRecords - ArrayList
	 * @throws SQLException - unhandled exception
	 */
	public ArrayList<FileDownloadRecord> getFileDownloads() throws SQLException {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getFileDownloads()...invoked");
		
		ArrayList<FileDownloadRecord> fileDownloads = new ArrayList<FileDownloadRecord>();
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(queryFileDownloads);
		ResultSet rs = stmt.getResultSet();
		Timestamp ts = null;
		String fileName = null;
		String study = null;
		String ipAddress = null;
		String emailAddress = null;
		String city = null;
		String state = null;
		String country = null;
		String latitude = null;
		String longitude = null;
		FileDownloadRecord fdr = null;
		String timestampStr = null;
		int index = 0;
			
		while(rs.next()) {
			fdr = new FileDownloadRecord();
			ts = rs.getTimestamp(1);
			timestampStr = ts.toString();
			index = timestampStr.lastIndexOf(".");
			timestampStr = timestampStr.substring(0, index);
			fdr.setCreateDate(timestampStr);
			fileName = rs.getString(2);
			fdr.setFileName(fileName);
			study = rs.getString(3);
			fdr.setStudy(study);
			ipAddress = rs.getString(4);
			fdr.setIpAddress(ipAddress);
			emailAddress = rs.getString(5);
			fdr.setEmailAddress(emailAddress);
			city = rs.getString(6);
			fdr.setCity(city);
			state = rs.getString(7);
			fdr.setState(state);
			country = rs.getString(8);
			fdr.setCountry(country);
			latitude = rs.getString(9);
			fdr.setLatitude(latitude);
			longitude = rs.getString(10);
			fdr.setLongitude(longitude);
			fileDownloads.add(fdr);
		}
		
		stmt.close();
		conn.close();
		LOGGER.trace(loggerId + "getFileDownloads()...exit");
		return fileDownloads;
	}
	
	/**
	 * 
	 * Returns an ArrayList of {@link WebHitRecord} representing all records
	 * in the web_hits table in MYSQL.
	 * 
	 * @return webHitRecords - ArrayList
	 * @throws SQLException - unhandled exception
	 */
	public ArrayList<WebHitRecord> getWebHits() throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getWebHits()...invoked");
		
		ArrayList<WebHitRecord> webHits = new ArrayList<WebHitRecord>();
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(queryWebHits);
		ResultSet rs = stmt.getResultSet();
		Timestamp ts = null;
		String ipAddress = null;
		WebHitRecord whRecord = null;
		String timestampStr = null;
		String city = null;
		String state = null;
		String country = null;
		String latitude = null;
		String longitude = null;
		int recordCount = 0;
		int index = 0;
			
		while(rs.next()) {
			recordCount++;
			whRecord = new WebHitRecord();
			whRecord.setHitCount(recordCount + "");
			ts = rs.getTimestamp(1);
			timestampStr = ts.toString();
			index = timestampStr.lastIndexOf(".");
			timestampStr = timestampStr.substring(0, index);
			whRecord.setCreateDate(timestampStr);
			ipAddress = rs.getString(2);
			whRecord.setIpAddress(ipAddress);
			city = rs.getString(3);
			whRecord.setCity(city);
			state = rs.getString(4);
			whRecord.setState(state);
			country = rs.getString(5);
			whRecord.setCountry(country);
			latitude = rs.getString(6);
			whRecord.setLatitude(latitude);
			longitude = rs.getString(7);
			whRecord.setLongitude(longitude);
			webHits.add(whRecord);
		}
		
		stmt.close();
		conn.close();
		LOGGER.trace(loggerId + "getWebHits()...exit");
		return webHits;
	}
	
	/**
	 * Returns the url used to embed the FILE_DOWNLOADS_MAP in the web page. This url is
	 * a link to a google generated map. The url is stored in the map_urls table in the
	 * mysql database. Refer to the NetworkProbabilityDownloader_Application_Overview
	 * document for more information on how this map is generated.
	 * 
	 * @return mapURL - String
	 * @throws SQLException - SQLException
	 */
	public String getDownloadHitsMapURL() throws SQLException {

		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getDownloadHitsMapURL()...invoked");
		
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(queryDownloadHitsMapUrl);
		ResultSet rs = stmt.getResultSet();
		
		rs.next();
		String mapURL = rs.getString(1);
		
		stmt.close();
		conn.close();
		
		LOGGER.trace(loggerId + "getDownloadHitsMapURL()...exit");

		return mapURL;
	
	}
	
	/**
	 * 
	 * Returns the url used to embed the WEB_HITS_MAP in the web page. This url is
	 * a link to a google generated map. The url is stored in the map_urls table in the
	 * mysql database. Refer to the NetworkProbabilityDownloader_Application_Overview
	 * document for more information on how this map is generated.
	 * 
	 * 
	 * @return mapURL - String
	 * @throws SQLException - unhandled exception
	 */
	public String getWebHitsMapURL() throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "getWebHitsMapURL()...invoked");
		
		Connection conn = getDBConnection();
		Statement stmt = conn.createStatement();
		stmt.execute(queryWebHitsMapUrl);
		ResultSet rs = stmt.getResultSet();
		
		rs.next();
		String mapURL = rs.getString(1);
		
		stmt.close();
		conn.close();
		
		LOGGER.trace(loggerId + "getWebHitsMapURL()...exit");

		return mapURL;
	}
	
	/**
	 * Initializes the password used to connect to the database.
	 * 
	 * @param encryptedPassword - String
	 * @param key - String used for encryption
	 */
	public void initAuthentication(String encryptedPassword, String key) {
		String loggerId = " ::LOGGERID=DBManager:: ";
		LOGGER.trace(loggerId + "initAuthentication()...invoked");
		this.password = Utils.convertJcpyt(encryptedPassword, key);
		LOGGER.trace(loggerId + "initAuthentication()...exit");
	}
	
	/**
	 * 
	 * Inserts an {@link AdminAccessEntry} into the admin_access table in MYSQL
	 * 
	 * @param aaEntry - {@link AdminAccessEntry}
	 * @param appContext - {@link ApplicationContext}
	 */
	public void insertAdminAccessRecord(AdminAccessEntry aaEntry, ApplicationContext appContext) {
		
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "insertAdminAccessRecord()...invoked");
				
		IPLocator.locateIP(aaEntry);	
		
		String resolvedCountry = aaEntry.getCountry();
		String resolvedState = aaEntry.getState();
		String resolvedCity = aaEntry.getCity();
		int updateCount = 0;
		
		if(resolvedCountry.equalsIgnoreCase("unknown") || 
		   resolvedState.equalsIgnoreCase("unknown") ||
		   resolvedCity.equalsIgnoreCase("unknown")) {
			
				IPInfoRequestor.getIPInfo(aaEntry);	
		}
			
		try {
			Connection connection = getDBConnection();
			PreparedStatement ps = connection.prepareStatement(this.insertAdminAccessRecord);
			ps.setString(1, aaEntry.getRequestorIPAddress());
			ps.setString(2, aaEntry.getAction());
			ps.setString(3, aaEntry.getValidPasswordString());
			ps.setString(4, aaEntry.getCity());
			ps.setString(5, aaEntry.getState());
			ps.setString(6, aaEntry.getCountry());

			ps.execute();
			updateCount = ps.getUpdateCount();
			
			ps.close();
			connection.close();
			
		}
		catch(SQLException sqlE) {
			LOGGER.trace(loggerId + "Failed to insert AdminAccessRecord, ip=" + aaEntry.getRequestorIPAddress());
			DiagnosticsReporter.createDiagnosticsEntry(sqlE);
		}
		
		if(updateCount==0) {
			LOGGER.trace(LOGGER_ID + "insertAdminAccessRecord()...failed to insert admin access record into database");
			addAdminAccessEntryToCSVFile(aaEntry, appContext);
		}
		LOGGER.trace(loggerId + "insertAdminAccessRecord()...exit");
	}
	
		
	/**
	 * 
	 * Removes extraneous records from web_hits table (such as hits from
	 * the web developer team, etc.).
	 * 
	 * @return success - boolean
	 * @throws SQLException - unhandled exception
	 */
	public boolean resynchWebHits() throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "resynchWebHits()...invoked");		

		boolean success = true;
		
		Connection conn = getDBConnection();
		PreparedStatement ps = conn.prepareStatement(deleteExtraneousWebHits);
		ps.setString(1, "68.47.110.229");
		ps.executeUpdate();
		ps.close();
		conn.close();
		
		LOGGER.trace(loggerId + "resynchWebHits()...exit");		
		return success;
	}
	
	/**
	 * 
	 * This method deletes any existing version of the /midb/email_addresses.csv file.
	 * It then invokes the {@link #getEmailAddresses()} method before invoking the
	 * {@link #createNewEmailAddressesCSVFile(ArrayList)}.
	 * 
	 * @return success - boolean
	 * @throws Exception - unhandled exception
	 */
	public boolean updateEmailAddressesCSVFile() throws Exception {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateEmailAddressesCSVFile()...invoked");		

		boolean success = true;
		
		File csvFile = new File(EMAIL_ADDRESSES_CSV_FILE);
		if(csvFile.exists()) {
			csvFile.delete();
		}
		
		ArrayList<EmailAddressRecord> emailRecords = getEmailAddresses();		
		this.createNewEmailAddressesCSVFile(emailRecords);
		
		LOGGER.info(loggerId + "updateEmailAddressesCSVFile()...Successfully updated /midb/email_addresses.csv");
		
		return success;
	}
	
	
	/**
	 * 
	 * Updates the url associated with either the WEB_HITS_MAP or the FILE_DOWNLOADS_MAP. 
	 * These records are located in the map_urls in the mysql database.
	 * 
	 * @param url - String 
	 * @param targetMap - Map name
	 * @return updateCount - number of rows updated (should be 1)
	 * @throws SQLException - unhandled exception
	 */
	public int updateMapURL(String url, String targetMap) throws SQLException {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateMapURL()...invoked");

		int updatedRowCount = 0;
		
		Connection conn = getDBConnection();
		PreparedStatement ps = conn.prepareStatement(updateMapURL);
		ps.setString(1, url);
		ps.setString(2,targetMap);
		updatedRowCount = ps.executeUpdate();
		
		ps.close();
		conn.close();
		
		LOGGER.trace(loggerId + "updateMapURL()...exit");

		return updatedRowCount;
	}
	
	/**
	 * Creates a new version of the /midb/web_hits_geoloc.csv file. This file is used to generate
	 * a new web hits map in google maps. Refer to the  NetworkProbabilityDownloader_Application_Overview
	 * for more information.
	 * 
	 * @return success - boolean
	 * @throws Exception - Exception
	 */
	public boolean updateDownloadHitsGeoLocCSVFile() throws Exception {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateDownloadbHitsGeoLocCSVFile()...invoked");		

		boolean success = true;
		
		File csvFile = new File(DOWNLOAD_HITS_GEOLOC_CSV_FILE);
		if(csvFile.exists()) {
			csvFile.delete();
		}
		
		ArrayList<FileDownloadRecord> downloadHitsRecords = getFileDownloads();
		this.createNewFileDownloadHitsGeoCSVFile(downloadHitsRecords);
		
		LOGGER.info(loggerId + "updateDownloadbHitsGeoLocCSVFile()...Successfully updated " + DOWNLOAD_HITS_GEOLOC_CSV_FILE);
		return success;
	}
	
	
	/**
	 * 
	 * Creates a new version of the /midb/web_hits_geoloc.csv file which contains
	 * the location information for every web hit. This file is used to create a
	 * google map.
	 * 
	 * @return success - boolean
	 * @throws Exception - unhandled exception
	 */
	public boolean updateWebHitsGeoLocCSVFile() throws Exception {
		String loggerId = ThreadLocalLogTracker.get();
		LOGGER.trace(loggerId + "updateWebHitsGeoLocCSVFile()...invoked");		

		boolean success = true;
		
		File csvFile = new File(WEB_HITS_GEOLOC_CSV_FILE);
		if(csvFile.exists()) {
			csvFile.delete();
		}
		
		ArrayList<WebHitRecord> webHitsRecords = getWebHits();	
		this.createNewWebHitsGeoCSVFile(webHitsRecords);
		
		LOGGER.info(loggerId + "updateWebHitsGeoLocCSVFile()...Successfully updated " + WEB_HITS_GEOLOC_CSV_FILE);
		return success;
	}

} // end class defintion
