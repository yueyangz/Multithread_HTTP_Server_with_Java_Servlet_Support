package edu.upenn.cis455.webserver;

import java.util.HashMap;

public class SessionsManager {
	
	private static HashMap<String, MyHttpSession> sessions;
	private static SessionsManager instance = null;
	private static int sessionId;
	

	private SessionsManager(){
		sessions = new HashMap<String, MyHttpSession>();
		sessionId = 0;
	}
	
	/**
	 * Create a singleton of SessionsManager
	 * @return
	 */
	public synchronized static SessionsManager create(){
		if (instance == null) {
			instance = new SessionsManager();
			MyLog.info("Session Manager created!");
		}
		return instance;
	}
	
	/**
	 * Get a session from an id
	 * @param key
	 * @return
	 */
	public static MyHttpSession getASession(String key){
//		System.out.println("key: " + key);
//		System.out.println("in manager session id: " + sessionId);
		return sessions.get(key);
	}
	
	/**
	 * Get all sessions
	 * @return
	 */
	public static HashMap<String, MyHttpSession> getSessions(){
		return sessions;
	}
	
	/**
	 * Create a new session
	 * @return
	 */
	public synchronized static MyHttpSession createSession() {
		sessionId++;
		MyHttpSession session = new MyHttpSession(String.valueOf(sessionId));
		sessions.put(String.valueOf(sessionId), session);
		return session;
	}
	
	public static void addASession(MyHttpSession s) {
		sessions.put(s.getId(), s);
	}
	
	

}
