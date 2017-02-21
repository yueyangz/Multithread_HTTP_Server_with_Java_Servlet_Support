package test.edu.upenn.cis455.hw1;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.Cookie;


import edu.upenn.cis455.webserver.DataContainer;
import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyHttpServletResponse;
import junit.framework.TestCase;

public class MyHttpServletRequestTest extends TestCase {
    MyHttpServletRequest request;
    MyHttpServletResponse response;
    DataContainer dc;
    Socket socket;
    HashMap<String, String> headers;
    String queryString;
    String filePath;
    String sessionId;
    HashMap<String, String> initLines;
    

    public void setUp() throws UnsupportedEncodingException {
		socket = new Socket();
    	headers = new HashMap<String, String>();
    	queryString = "num1=abc&num2=3&num3=5&num2=def";
    	filePath = null;
    	sessionId = "2";
    	initLines = new HashMap<String, String>();
    	initLines.put("method", "GET");
    	initLines.put("url", "/init");
    	initLines.put("version", "HTTP/1.1");
    	initLines.put("query", "num1=abc&num2=3&num3=5&num2=def");
    	headers.put("content-length", "12");
    	headers.put("content-type", "text/html");
    	headers.put("content-type", "text/html, music/mp3, video/avi");
    	headers.put("accept", "");
    	headers.put("number", "1");
    	headers.put("host", "sdasdasdsa");
    	dc = new DataContainer(socket, headers, queryString, filePath, sessionId, initLines);
    	request = new MyHttpServletRequest(dc);
    	request.setAttribute("test", "lalala");
    	request.setMethod("POST");
    	request.setCharacterEncoding("ASCII");
    	headers.put("cookie", "abc=def, ghi=jkl; mno=pqr; stu=vwx, yz=yz");

    }
    
    
    public void testgetMethod() {
    	assertEquals("POST", request.getMethod());
    }
    
    
    public void testgetCharacterEncoding() {
    	assertEquals("ASCII", request.getCharacterEncoding());
    }

    
    public void testgetRequestedURI() {
    	assertEquals("/init", request.getRequestURI());
    }
    
    
    public void testgetHeader() {
    	assertEquals("text/html", request.getHeader("content-type"));
    }
    
    
    public void testgetHeaders() {
    	ArrayList<String> expect = new ArrayList<String>();
    	expect.add("text/html");
    	expect.add("music/mp3");
    	expect.add("video/avi");
    	
    	Enumeration<String> en = request.getHeaders("content-type");
    	ArrayList<String> actual = new ArrayList<String>();
    	
    	while (en.hasMoreElements()) {
    		String s = en.nextElement();
    		actual.add(s);
    	}
    	for (int i = 0; i < actual.size(); i++) {
    		assertEquals(expect.get(i), actual.get(i));
    	}
    }
    
    
    public void testgetIntHeader() {
    	assertEquals(1, request.getIntHeader("number"));
    }
    
    
    public void testgetQueryString() {
    	assertEquals("num1=abc&num2=3&num3=5&num2=def", request.getQueryString());
    }
    
    
    public void testgetParameter() {
    	assertEquals("abc", request.getParameter("num1"));
    }
    
    
    public void testSetAttribute() {
    	request.setAttribute("test", "attribute");
    	assertEquals("attribute", request.getAttribute("test"));
    }
    
    
    public void testGetCookies() {
    	Cookie[] c = request.getCookies();
    	Cookie cookie1 = c[0];
    	Cookie cookie2 = c[1];
    	Cookie cookie3=  c[2];
    	Cookie cookie4 = c[3];
    	Cookie cookie5 = c[4];
    	assertEquals(5, c.length);
    	assertEquals("abc", cookie1.getName());
    	assertEquals("def", cookie1.getValue());
    	assertEquals("ghi", cookie2.getName());
    	assertEquals("jkl", cookie2.getValue());
    	assertEquals("mno", cookie3.getName());
    	assertEquals("pqr", cookie3.getValue());
    	assertEquals("stu", cookie4.getName());
    	assertEquals("vwx", cookie4.getValue());
    	assertEquals("yz", cookie5.getName());
    	assertEquals("yz", cookie5.getValue());
    }
    
    

    
}
