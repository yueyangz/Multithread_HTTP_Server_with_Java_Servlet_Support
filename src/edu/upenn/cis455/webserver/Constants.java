package edu.upenn.cis455.webserver;

public class Constants {
	
	/**
	 * Constants used in the program
	 */
	public static String rootDirectory;
	public static final int TIME_OUT = 10000;
	public static int NUMBER_OF_THREADS = 9;
	public static int MAX_SIZE_OF_QUEUE = 10000;
	public static int MAX_INACTIVE_INTERVAL = 1800;
	public static int PORT_NUMBER;
	public static String CONTENT_TYPE = "Content-Type: ";
	public static String CONTENT_LENGTH = "Content-Length: ";
	public static String LAST_MODIFIED = "Last-Modified: ";
	public static String DATE = "Date: ";
	public static String RESPONSE = "";
	public static String WEB_XML_PATH = "";
	public static String SERVER_INFO = "Yueyang's HTTP Server";
	public static String SCHEMA = "http";

	
	public Constants() {}
	
	public static void setRoot(String s){
		rootDirectory = s;
	}
	
	public static void setWebXmlPath(String s){
		WEB_XML_PATH = s;
	}
	
	
	public static String getRootDirectory() {
		return rootDirectory;
	}

	public static void setRootDirectory(String rootDirectory) {
		Constants.rootDirectory = rootDirectory;
	}

	public static int getPORT_NUMBER() {
		return PORT_NUMBER;
	}

	public static void setPORT_NUMBER(int pORT_NUMBER) {
		PORT_NUMBER = pORT_NUMBER;
	}

	public static String getCONTENT_TYPE() {
		return CONTENT_TYPE;
	}

	public static void setCONTENT_TYPE(String cONTENT_TYPE) {
		CONTENT_TYPE = cONTENT_TYPE;
	}

	public static String getCONTENT_LENGTH() {
		return CONTENT_LENGTH;
	}

	public static void setCONTENT_LENGTH(String cONTENT_LENGTH) {
		CONTENT_LENGTH = cONTENT_LENGTH;
	}

	public static String getLAST_MODIFIED() {
		return LAST_MODIFIED;
	}

	public static void setLAST_MODIFIED(String lAST_MODIFIED) {
		LAST_MODIFIED = lAST_MODIFIED;
	}

	public static String getDATE() {
		return DATE;
	}

	public static void setDATE(String dATE) {
		DATE = dATE;
	}

	public static String getRESPONSE() {
		return RESPONSE;
	}

	public static void setRESPONSE(String rESPONSE) {
		RESPONSE = rESPONSE;
	}

	public static String getWEB_XML_PATH() {
		return WEB_XML_PATH;
	}

	public static void setWEB_XML_PATH(String wEB_XML_PATH) {
		WEB_XML_PATH = wEB_XML_PATH;
	}

	public static String getSERVER_INFO() {
		return SERVER_INFO;
	}

	public static void setSERVER_INFO(String sERVER_INFO) {
		SERVER_INFO = sERVER_INFO;
	}

	public static int getTimeOut() {
		return TIME_OUT;
	}

	public static int getNumberOfThreads() {
		return NUMBER_OF_THREADS;
	}

	public static int getMaxSizeOfQueue() {
		return MAX_SIZE_OF_QUEUE;
	}

	public static void setPortNumber(int port) {
		PORT_NUMBER = port;
	}
	

}
