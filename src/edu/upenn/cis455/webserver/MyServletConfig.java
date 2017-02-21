package edu.upenn.cis455.webserver;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class MyServletConfig implements ServletConfig {
	
	private String name;
	private MyServletContext context;
	private HashMap<String, String> initParams;
	
	public MyServletConfig(String name, MyServletContext context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String, String>();
	}

	public String getInitParameter(String key) {
		return initParams.get(key.toLowerCase());
	}

	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public ServletContext getServletContext() {
		return context;
	}

	public String getServletName() {
		return name;
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name.toLowerCase(), value);
	}

}
