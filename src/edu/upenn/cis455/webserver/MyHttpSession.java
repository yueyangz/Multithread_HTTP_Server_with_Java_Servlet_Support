package edu.upenn.cis455.webserver;

import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;


public class MyHttpSession implements HttpSession {
	
	private HashMap<String, Object> attrs;
	private boolean valid;
	private Date startingTime;
	private Date lastActiveTime;
	private static int maxInactiveInterval;
	private String sessionId;
	private boolean isNew;
	
	public MyHttpSession(String id) {
		attrs = new HashMap<String, Object>();
		valid = true;
		startingTime = new Date();
		lastActiveTime = new Date();
		isNew = true;
		maxInactiveInterval = Constants.MAX_INACTIVE_INTERVAL;
		sessionId = id;
		
	}
	
	/**
	 * Check if the session is valid
	 * @return
	 */
	public boolean isValidSession() {
		if (!valid) return valid;
		long interval = (long) maxInactiveInterval * 1000;
		long lastActive = lastActiveTime.getTime();
		if (new Date().getTime() - (long)lastActive > interval) {
			System.out.println(new Date().getTime() - (long)lastActive);
			System.out.println("invalidate!");
			return false;
		}
		return valid;
	}
	
	/**
	 * Return validity
	 * @return
	 */
	public boolean getValid() {
		return valid;
	}

	/**
	 * Get a attribute
	 */
	public Object getAttribute(String arg0) {
		if (!isValidSession()) {
			MyLog.info("Session expired in getAttribute!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			return attrs.get(arg0);
		}
	}

	/**
	 * Get all attr names
	 */
	public Enumeration<String> getAttributeNames() {
		if (!isValidSession()) {
			MyLog.info("Session expired in getAttributeNames!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			return Collections.enumeration(attrs.keySet());
		}
	}

	/**
	 * Return creation time
	 */
	public long getCreationTime() {
		if (!isValidSession()) {
			MyLog.info("Session expired in getCreationTime!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			return startingTime.getTime();
		}
	}

	/**
	 * Return Id
	 */
	public String getId() {
		if (!isValidSession()) {
			MyLog.info("Session expired in getId!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			return sessionId;
		}
	}
	
	/**
	 * Get last access time
	 */
	public long getLastAccessedTime() {
		if (!isValidSession()) {
			MyLog.info("Session expired in getLastAccessTime!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			return lastActiveTime.getTime();
		}
	}

	/**
	 * Get max inactive time
	 */
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	/**
	 * Get context
	 */
	public ServletContext getServletContext() {
		return ServletContainer.getMyServletContext();
	}

	//Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}
	
	//Deprecated
	public Object getValue(String arg0) {
		return null;
	}

	//Deprecated
	public String[] getValueNames() {
		return null;
	}

	/**
	 * Invalidate a session
	 */
	public void invalidate() {
		if (valid) valid = false;
		else {
			MyLog.info("Invalidation illegal!");
			throw new IllegalStateException("invalidate!");
		}
		
	}

	/**
	 * Get if new
	 */
	public boolean isNew() {
		if (!isValidSession()) {
			MyLog.info("Session expired in isNew!");
			throw new IllegalStateException("getAttribute illegal!");	
		} else return isNew;
	}

	//Deprecated
	public void putValue(String arg0, Object arg1) {
			
	}

	/**
	 * Remove an attr
	 */
	public void removeAttribute(String arg0) {
		if (!isValidSession()) {
			MyLog.info("Session expired in removeAttribute!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			attrs.remove(arg0);
		}
		
	}

	//Deprecated
	public void removeValue(String arg0) {
		
	}

	/**
	 * Change an attr
	 */
	public void setAttribute(String arg0, Object arg1) {
		if (!isValidSession()) {
			MyLog.info("Session expired in setAttribute!");
			throw new IllegalStateException("getAttribute illegal!");
			
		} else {
			attrs.put(arg0, arg1);
		}
		
	}
	
	/**
	 * Change isNew
	 * @param val
	 */
	public void setIsNew (boolean val) {
		isNew = val;
	}

	/**
	 * CHange max inactive interval
	 */
	public void setMaxInactiveInterval(int arg0) {
		MyLog.info("Max inactive internal was changed!");
		maxInactiveInterval = arg0;
	}
	
	/**
	 * Change last active time
	 * @param time
	 */
	public void setLastActiveTime(Date time) {
		MyLog.info("Accessed an existing session!");
		this.lastActiveTime = time;
	}

}
