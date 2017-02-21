
package test.edu.upenn.cis455.hw1;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.Cookie;
import edu.upenn.cis455.webserver.DataContainer;
import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyHttpServletResponse;
import edu.upenn.cis455.webserver.MyLog;
import junit.framework.TestCase;

public class MyHttpRServletResponseTest extends TestCase {

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
    	MyLog.create();
		socket = new Socket();
    	headers = new HashMap<String, String>();
    	queryString = "num1=abc&num2=3&num3=5&num2=def";
    	filePath = null;
    	sessionId = "2";
    	initLines = new HashMap<String, String>();
    	initLines.put("method", "GET");
    	initLines.put("url", "/init");
    	initLines.put("version", "HTTP/1.1");
    	headers.put("content-length", "12");
    	headers.put("content-type", "text/html");
    	headers.put("accept", "");
    	headers.put("date", "Date: Tue, 15 Nov 1994 08:12:31 GMT");
    	headers.put("host", "sdasdasdsa");
    	dc = new DataContainer(socket, headers, queryString, filePath, sessionId, initLines);
    	request = new MyHttpServletRequest(dc);
    	response = new MyHttpServletResponse(dc, request);
    	response.setCharacterEncoding("ASCII");
    }
    
    
    public void testAddCookie() {
    	response.addCookie(new Cookie("abc", "def"));
    	String cookie = response.getResponseMap().get("Set-Cookie");
    	assertEquals(true, cookie.contains("abc=def"));
    }
    
    
    public void testgetCharacterEncoding() {
    	assertEquals("ASCII", response.getCharacterEncoding());
    }
    
    
    public void testsetContentType() {
    	response.setContentType("music");
    	assertEquals("music", response.getContentType());
    }
    
    
    public void testresetBuffer() {
    	response.resetBuffer();
    	assertEquals(0, response.getBufferSize());
    }
    
    
    public void testsetDateHeader() {
    	response.setDateHeader("test", new Date(0).getTime());
    	assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", response.getResponseMap().get("test"));
    }


    
    public void testisCommited() {
    	assertEquals(false, response.isCommitted());
    }
    
    
    public void testaddHeader() {
    	response.addHeader("Content-Type", "mp3");
    	assertEquals("mp3", response.getResponseMap().get("Content-Type"));
    }
    
    
    public void testaddIntHeader() {
    	response.addIntHeader("abc", 1);
    	assertEquals("1", response.getResponseMap().get("abc"));
    }
    
    
    public void testAddHeader2() {
    	response = new MyHttpServletResponse(dc, null);
    	response.setHeader("abc", "def");
    	response.addHeader("abc", "ghi");
    	assertEquals("def, ghi", response.getResponseMap().get("abc"));
    }
    
    
    public void testContainsHeaderTest() {
    	assertEquals(false, response.containsHeader("non-exist"));
    }
    
    
    public void testgetContentType() {
    	assertEquals("text/html", response.getContentType());
    }
    
    
    public void testNotNullMap() {
    	assertNotNull(response.getResponseMap());
    }
    
    
    

    

}