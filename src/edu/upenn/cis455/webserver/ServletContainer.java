package edu.upenn.cis455.webserver;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;



public class ServletContainer {
	
	private static MyLog log;
	private static SessionsManager manager;
	private static HashMap<String, HttpServlet> servlets;
	private static HashMap<String, String> urlToServletName;
	private static HashMap<String, String> wildcardMap;
	private static MyServletContext c;
	private static String contextName;
	
	private ServletContainer() {

	}
	
	/**
	 * Initialize the servlet container
	 * @throws Exception
	 */
	public static void initialize() throws Exception {
		Handler handler = null;
		handler = parseWebdotxml(Constants.WEB_XML_PATH);
		c = ServletContainer.createContext(handler);
		urlToServletName = new HashMap<String, String>();
		wildcardMap = new HashMap<String, String>();
		servlets = createServlets(handler, c);
		urlToServletName = handler.exactUrl;
		wildcardMap = handler.wildcardMap;
		contextName = handler.contextName;
//		Iterator<String> itr = urlToServletName.keySet().iterator();
//		while (itr.hasNext()) {
//			String key = itr.next();
//			String val = urlToServletName.get(key);
//			System.out.println("url:" + key + "		servletName:" + val);
//		}		
//		itr = servlets.keySet().iterator();
//		while (itr.hasNext()) {
//			String key = itr.next();
//			String val = servlets.get(key).getClass().toString();
////			System.out.println("servletName:" + key + "		class:" + val);
//		}
		log = MyLog.create();
		manager = SessionsManager.create();
		MyLog.info("Servlet container created!");
	}
	
	/**
	 * Return context
	 * @return
	 */
	public static MyServletContext getMyServletContext() {
		return c;
	}
	
	/**
	 * Parsing a wildcard url to match with an exact servlet URL
	 * @param s
	 * @param dc
	 * @return
	 */
	public static String pathMap(String s, DataContainer dc) {
		String ret = null;
//		System.out.println("HERE");
//		Helper.printMap(wildcardMap);
//		System.out.println("here!!!!");
//		System.out.println("path map size: " + wildcardMap.size());
//		if (s.startsWith("/")) {
//			s = s.substring(1, s.length());
//			System.out.println("s " + s);
//		}
//		System.out.println("s is: " + s);
		for (int i = 0; i < s.length(); i++) {
			String sub = s.substring(0, s.length() - i);
//			System.out.println("sub: "  +sub);
			String pathInfo = s.substring(i, s.length());
			if (wildcardMap.containsKey(sub)) {
				ret = sub;
				dc.setServletPath(ret);
//				System.out.println("servlet path: " + ret);
//				System.out.println("path info: " + pathInfo);
				dc.setPathInfo(pathInfo);
				break;
			}
		}
//		System.out.println("ret: " +ret);
		return ret;
	}
	
	/**
	 * Return a url-servlet name hashmap based on whether there is exact matching / path mapping
	 * @param exact
	 * @return
	 */
	public static HashMap<String, String> getServletName(boolean exact) {
		if (exact) return urlToServletName;
		else return wildcardMap;
	}
	
	/**
	 * Return a name-servlet instance hashmap
	 * @return
	 */
	public static HashMap<String, HttpServlet> getServlets() {
		return servlets;
	}
	
	/**
	 * Kill all servlets
	 */
	public static void killServlets() {
		Iterator<String> itr = servlets.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			HttpServlet sv = servlets.get(key);
			sv.destroy();
		}
		MyLog.info("All servlets are killed");
	}
	
	public static String getContextName() {
		return contextName;
	}
	
	/**
	 * Parser than processes web.xml and put things into the relevant hashmaps
	 * Based on TestHarness
	 * @author cis455
	 *
	 */
	protected static class Handler extends DefaultHandler {
			private int state = 0;
	        public String servletName;
	        private String paramName;
	        public String servletNameState40;
	        public String contextName;

	        HashMap<String, String> servlets = new HashMap<String, String>();
	        HashMap<String, String> exactUrl = new HashMap<String, String>();
	        HashMap<String, String> contextParams = new HashMap<String, String>();
	        HashMap<String, String> wildcardMap = new HashMap<String, String>();
	        HashMap<String, HashMap<String, String>> servletParams = new HashMap<String, HashMap<String, String>>();
	        
	        public void startElement(String uri, String localName, String qName,
	                Attributes attributes) {

	            if (qName.compareTo("servlet") == 0) {
	                state = 1;
	            } else if (qName.compareTo("servlet-mapping") == 0) {
	                state = 2;
	            } else if (qName.compareTo("context-param") == 0) {
	                state = 3;
	            } else if (qName.compareTo("init-param") == 0) {
	                state = 4;
	            } else if (qName.compareTo("display-name") == 0) {
	            	state = 5;
	            } else if (qName.compareTo("servlet-name") == 0) {
	                state = (state == 1) ? 30 : 40;
	            } else if (qName.compareTo("servlet-class") == 0) {
	                state = 31;
	            } else if (qName.compareTo("url-pattern") == 0) {
	                state = 41;
	            } else if (qName.compareTo("param-name") == 0) {
	                state = (state == 3) ? 10 : 20;
	            } else if (qName.compareTo("param-value") == 0) {
	                state = (state == 10) ? 11 : 21;
	            }
	        }

	        public void characters(char[] ch, int start, int length) {
	            String value = new String(ch, start, length);
	            if (state == 30) {
	                servletName = value;
	                state = 0;
	            } else if (state == 5) {
	            	contextName = value;
	            	state = 0;
	            } else if (state == 31) {
	                servlets.put(servletName, value);
	                state = 0;
	            } else if (state == 40) {
	                servletNameState40 = value;
	                state = 0;
	            } else if(state == 41) {
//	            	System.out.println("value: " + value + "  servletNameState40: " + servletNameState40);
	            	if (value.endsWith("/*")) {
	            		value = value.substring(0, value.length() - 2);
	            		wildcardMap.put(value, servletNameState40);
	            	} else exactUrl.put(value, servletNameState40);
//	            	System.out.println("value is: " + value + "  servletname_40: " + servletNameState40);
	                state = 0;
	            } else if (state == 1) {
	                servletName = value;
	                state = 0;
	            } else if (state == 2) {
	                servlets.put(servletName, value);
	                state = 0;
	            } else if (state == 10 || state == 20) {
	                paramName = value;
	            } else if (state == 11) {
	                if (paramName == null) {
	                    System.exit(-1);
	                }
	                contextParams.put(paramName, value);
	                paramName = null;
	                state = 0;
	            } else if (state == 21) {
	                if (paramName == null) {
	                    System.exit(-1);
	                }
	                HashMap<String, String> p = servletParams.get(servletName);
	                if (p == null) {
	                    p = new HashMap<String, String>();
	                    servletParams.put(servletName, p);
	                }
	                p.put(paramName, value);
	                paramName = null;
	                state = 0;
	            }
	        } 
	    } 

	
	
	public static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		return h;
	}
	
	public static MyServletContext createContext(Handler h) {
		MyServletContext sc = new MyServletContext();
		for (String param : h.contextParams.keySet()) {
			sc.setInitParam(param, h.contextParams.get(param));
		}
		return sc;
	}
	
	public static HashMap<String,HttpServlet> createServlets(Handler h, MyServletContext fc) throws Exception {
		servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.servlets.keySet()) {
			MyServletConfig config = new MyServletConfig(servletName, fc);
			String className = h.servlets.get(servletName);
			Class<?> servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

	

}
