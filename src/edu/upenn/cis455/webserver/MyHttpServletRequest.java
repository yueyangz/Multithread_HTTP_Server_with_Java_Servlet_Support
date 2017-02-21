package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyHttpServletRequest implements HttpServletRequest{
	
	private String setCharacterEncoding = null;
	private Locale locale = null;
	private MyHttpSession session;
	private HashMap<String, String> params;
	private HashMap<String, String> attrs;
	private DataContainer dc;

	
	public MyHttpServletRequest(DataContainer dc) throws UnsupportedEncodingException {
		this.dc = dc;
		params = new HashMap<String, String>();
		attrs = new HashMap<String, String>();
		parseQueryString();
		String cookie = dc.getHeaders().get("cookie");
		if (cookie != null) {
			String[] cookies = cookie.split(";");
			String id = "";
			for (String s: cookies) {
				if (s.startsWith("JSESSIONID")) {
					String[] pair = s.split("=");
					id = pair[1];
				}
			}
//			System.out.println("id is: " + id);
			if (!id.isEmpty()) {
				MyHttpSession s = SessionsManager.getASession(id);
				session = s;
			}
//			System.out.println("cookie: " + cookie);
			
		}

	}
	
	/**
	 * Parse an incoming query string
	 * @throws UnsupportedEncodingException
	 */
	public void parseQueryString() throws UnsupportedEncodingException {
		String q = dc.getInitLines().get("query");
		
		if (q == null) q = dc.getHeaders().get("messageBody");
		if (q == null) {
//			System.out.println("Query string is null!");
			return;
		}
		q = URLDecoder.decode(q, "UTF-8");
		String[] params = q.trim().split("\\&"); 
		for (String s: params) {
			String[] splitByEqualSign = s.split("\\=");
			if (splitByEqualSign.length == 2) {
				putParamsInParamsMap(splitByEqualSign);
			}
			else {
				System.out.println("Invalid parameter!");
			}
		}
	}
	
	/**
	 * Put a param into the map
	 * @param param
	 */
	private void putParamsInParamsMap(String[] param) {
		String key = param[0];
		String value = param[1];
		if (params.containsKey(key)) {
			String val = params.get(key).concat(", ").concat(value);
			params.put(key, val);
		} else {
			params.put(key, value);
		}
	}
	
	/**
	 * Change path info
	 * @param s
	 */
	public void setPathInfo(String s) {
		dc.setPathInfo(s);
	}

	/**
	 * Get an attr
	 */
	public Object getAttribute(String arg0) {
		return attrs.get(arg0);
	}

	/**
	 * Get attr names
	 */
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attrs.keySet());
	}

	/**
	 * Get encoding
	 */
	public String getCharacterEncoding() {
		return setCharacterEncoding != null ? setCharacterEncoding : "ISO-8859-1";
	}

	/**
	 * Return content length
	 */
	public int getContentLength() throws NumberFormatException {
		int ret = -1;
		ret = Integer.parseInt(dc.getHeaders().get("content-length"));
		return ret;
	}

	/**
	 * Return content type
	 */
	public String getContentType() {
		String ret = null;
		String rawValue = dc.getHeaders().get("content-type");
		if (rawValue != null && rawValue.length() > 0) {
			String[] types = Helper.splitIntoList(rawValue);
			ret = types[0];
		}
		return ret;
		
	}

	//Not required
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	/**
	 * Return local address
	 */
	public String getLocalAddr() {
		return dc.getSocket().getLocalAddress().getHostAddress();
	}

	/**
	 * Return server name;
	 */
	public String getLocalName() {
		return dc.getSocket().getLocalAddress().getCanonicalHostName();
	}

	/**
	 * Get port number
	 */
	public int getLocalPort() {
		return dc.getSocket().getLocalPort();
	}

	/**
	 * Get locale
	 */
	public Locale getLocale() {
		return locale;
	}
	/**
	 * Change locale
	 * @param locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	//Not required
	public Enumeration<?> getLocales() {	
		return null;
	}

	/**
	 * Get a param
	 */
	public String getParameter(String arg0) {
		if (params.containsKey(arg0)) {
			String value = params.get(arg0);
//			System.out.println("value: " + value);
			String[] list = Helper.splitIntoList(value);
			return list[0];
		}
		return null;
	}

	/**
	 * Get param map
	 */
	public Map<String, String> getParameterMap() {
		return params;
	}

	/**
	 * Get param names
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(params.keySet());
	}

	/**
	 * Get param values
	 */
	public String[] getParameterValues(String arg0) {
		if (params.containsKey(arg0)) {
			return Helper.splitIntoList(params.get(arg0));
		}
		return null;
	}

	/**
	 * Get version
	 */
	public String getProtocol() {
		return dc.getInitLines().get("version");
	}

	/**
	 * Get a reader instance
	 */
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(dc.getSocket().getInputStream()));
	}

	//Deprecated
	public String getRealPath(String arg0) {
		return null;
	}

	/**
	 * Get client address
	 */
	public String getRemoteAddr() {
		return dc.getSocket().getInetAddress().getHostAddress();
	}

	/**
	 * Get client host name
	 */
	public String getRemoteHost() {
		return dc.getSocket().getInetAddress().getHostName();
	}

	/**
	 * Get client port number
	 */
	public int getRemotePort() {
		return dc.getSocket().getPort();
	}

	//Not required
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	/**
	 * Get scheme
	 */
	public String getScheme() {
		return "http";
	}

	/**
	 * Get server name
	 */
	public String getServerName() {
		return dc.getSocket().getLocalAddress().getCanonicalHostName();
	}

	/**
	 * Get server port
	 */
	public int getServerPort() {
		return dc.getSocket().getLocalPort();
	}

	/**
	 * Is secure
	 */
	public boolean isSecure() {
		return false;
	}

	/**
	 * Remove an attr
	 */
	public void removeAttribute(String arg0) {
		attrs.remove(arg0);
	}

	/**
	 * Change an attr
	 */
	public void setAttribute(String arg0, Object arg1) {
		attrs.put(arg0, (String) arg1);
	}

	/**
	 * Change encoding
	 */
	public void setCharacterEncoding(String encoding)
			throws UnsupportedEncodingException {
		this.setCharacterEncoding = encoding;
	}

	/**
	 * Return auth type
	 */
	public String getAuthType() {
		return BASIC_AUTH;
	}

	/**
	 * Get context / app path
	 */
	public String getContextPath() {
		return "";
	}

	/**
	 * Return cookies
	 */
	public Cookie[] getCookies() {
//		Helper.printMap(dc.getHeaders());
		Enumeration<String> cookies = getHeaders("cookie");
		ArrayList<Cookie> ret = new ArrayList<Cookie>();
		if (cookies == null) return new Cookie[0];
		while (cookies.hasMoreElements()) {
			String s = cookies.nextElement();
			String[] items = s.split("\\;\\s|\\,\\s"); 
			for (String cookie: items) {
				String[] pair = cookie.split("=");
				String key = pair[0];
				String value = pair[1];
				if(key.equalsIgnoreCase("JSessionID")) {
					MyHttpSession session = SessionsManager.getASession(value);
					if (session != null) {
						this.session = session;
						session.setLastActiveTime(new Date());
					}
				}
//				System.out.println(key + "  " + value);
				Cookie c = new Cookie(key, value);
				ret.add(c);
			}
		}
		int len = ret.size();
		Cookie[] arr = new Cookie[len];
		for (int i = 0; i < len; i++) {
			arr[i] = ret.get(i); 
		}
		return arr;
		
		
	}

	/**
	 * Get a date header
	 */
	public long getDateHeader(String arg0) {
		String date = dc.getHeaders().get("date");
		if (date == null) return -1;
		Date d = Helper.convertStringToDate(date);
		return d != null ? d.getTime() : -1;
	}

	/**
	 * Get a header
	 */
	public String getHeader(String arg0) {
		String header = dc.getHeaders().get(arg0.toLowerCase()).toString();
		String [] list = Helper.splitIntoList(header);
		return list[0];
	}

	/**
	 * Get header names
	 */
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(dc.getHeaders().keySet());
	}

	/**
	 * Get headers
	 */
	public Enumeration<String> getHeaders(String arg0) {
		String header = dc.getHeaders().get(arg0);
		if (header == null) return null;
		String[] list = Helper.splitIntoList(header);
		ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(list));
		return Collections.enumeration(arrayList);
	}

	/**
	 * Get int header
	 */
	public int getIntHeader(String arg0) throws NumberFormatException {
		String header = getHeader(arg0);
		if (header == null) return -1;
		int ret = -1;
		ret = Integer.parseInt(header);
		return ret;
		
	}

	/**
	 * Get method
	 */
	public String getMethod() {
//		System.out.println("Mtehod: " + dc.getInitLines().get("method"));
		return dc.getInitLines().get("method");
	}

	/**
	 * Get path info
	 */
	public String getPathInfo() {
		return dc.getPathInfo();
	}

	//Not required
	public String getPathTranslated() {
		return null;
	}

	/**
	 * Get query string
	 */
	public String getQueryString() {
		return dc.getInitLines().get("query");
	}

	//?
	public String getRemoteUser() {	
		return null;
	}

	/**
	 * Get URI
	 */
	public String getRequestURI() {
		if (dc.getInitLines().containsKey("url")) {
			return dc.getInitLines().get("url");
		}
		return null;
		
	}

	/**
	 * Get URL
	 */
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer();
		url.append(Constants.SCHEMA).append("://").append(getServerName()).append(":").append(getLocalPort()).append(getRequestURI());
//		System.out.println("Req URL is: " + url);
		return url;
	}

	/**
	 * Get session ID
	 */
	public String getRequestedSessionId() {
		if (session == null) return null;
		else return session.getId();
	}

	/**
	 * Get servlet path
	 */
	public String getServletPath() {
		return dc.getServletPath();
	}

	/**
	 * Get session and always create one
	 */
	public HttpSession getSession() {
		return getSession(true);
	}


	/**
	 * Get session
	 */
	public HttpSession getSession(boolean create) {
		if (validSession()) {
			session.setLastActiveTime(new Date());
			return session;
		} else if (create) {
			session = SessionsManager.createSession();
			return session;
		} else return null;		
	}
	
	/**
	 * Is the session valid
	 * @return
	 */
	public boolean validSession() {
		return session != null && session.isValidSession();
	}

	//Not required
	public Principal getUserPrincipal() {
		return null;
	}

	/**
	 * Get a session id
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return session != null;
	}
	/**
	 * Get session id
	 */
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	//deprecated
	public boolean isRequestedSessionIdFromUrl() {	
		return false;
	}
	/**
	 * Check if a session id valid
	 */
	public boolean isRequestedSessionIdValid() {	
		if (session != null && session.isValidSession()) return true;
		else return false;
	}

	//Not required
	public boolean isUserInRole(String arg0) {	
		return false;
	}

	/**
	 * Change a method
	 * @param arg0
	 */
	public void setMethod(String arg0) {
		dc.getInitLines().put("method", arg0);
	}
}
