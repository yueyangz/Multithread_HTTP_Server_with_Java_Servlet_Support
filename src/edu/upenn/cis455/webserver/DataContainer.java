package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.HashMap;

public class DataContainer {
	
	private  Socket socket;
	private  HashMap<String, String> headers;
	private  String queryString;
	private  String filePath;
	private  String sessionId;
	private  HashMap<String, String> initLines;
	private String pathInfo;
	private String servletPath;
	

	public DataContainer(Socket socket, HashMap<String, String> headers, String queryString, String filePath, String sessionId, HashMap<String, String> initLines) {
		this.socket = socket;
		this.headers = headers;
		this.queryString = queryString;
		this.filePath = filePath;
		this.sessionId = sessionId;
		this.initLines = initLines;
		servletPath = null;
		pathInfo = null;
	}

	public  Socket getSocket() {
		return socket;
	}

	public  void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public void setPathInfo(String s) {
		pathInfo = s;
	}
	

	public  void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public  void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public  void setServletPath(String s) {
		this.servletPath = s;
	}

	public  void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public  void setInitLines(HashMap<String, String> initLines) {
		this.initLines = initLines;
	}

	public  HashMap<String, String> getHeaders() {
		return headers;
	}

	public  String getQueryString() {
		return queryString;
	}

	public  String getServletPath() {
		return servletPath;
	}

	public  String getSessionId() {
		return sessionId;
	}
	
	public String getPathInfo() {
		return pathInfo;
	}

	public  HashMap<String, String> getInitLines() {
		return initLines;
	}

}
