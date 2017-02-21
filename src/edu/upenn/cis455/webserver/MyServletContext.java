package edu.upenn.cis455.webserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


public class MyServletContext implements ServletContext {
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	
	public MyServletContext() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public ServletContext getContext(String name) {
		return ServletContainer.getMyServletContext();
	}

	public String getInitParameter(String name) {
		return initParams.get(name.toLowerCase());
	}

	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public int getMajorVersion() {
		return 2;
	}

	//Not required
	public String getMimeType(String arg0) {
		return null;
	}

	public int getMinorVersion() {
		return 4;
	}

	//Not required
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	public String getRealPath(String arg0) {
		File f = new File(Constants.rootDirectory + arg0);
		if (!f.exists()) return null;
		else {
			try {
				String path = f.getCanonicalPath();
				return path;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		
		return null;
	}

	//Not required
	public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}

	//Not required
	public InputStream getResourceAsStream(String arg0) {
		return null;
	}

	//Not required
	public Set<?> getResourcePaths(String arg0) {
		return null;
	}

	public String getServerInfo() {
		return Constants.SERVER_INFO;
	}

	//Deprecated
	public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

	public String getServletContextName() {
		return ServletContainer.getContextName();
	}

	//Deprecated
	public Enumeration<?> getServletNames() {	
		return null;
	}

	//Deprecated
	public Enumeration<?> getServlets() {
		return null;
	}

	//Not required
	public void log(String msg) {
		
	}

	//Deprecated
	public void log(Exception e, String msg) {

	}

	//Not required
	public void log(String msg, Throwable throwable) {
	
	}

	public void removeAttribute(String name) {
		attributes.remove(name);
		
	}

	public void setAttribute(String name, Object obj) {
		attributes.put(name, obj);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}


}
