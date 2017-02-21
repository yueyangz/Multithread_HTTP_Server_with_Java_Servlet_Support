package test.edu.upenn.cis455.hw1;



import edu.upenn.cis455.webserver.MyHttpServletRequest;
import edu.upenn.cis455.webserver.MyHttpSession;
import junit.framework.TestCase;

public class MyHttpSessionTest extends TestCase {
    // need to change the HttpServletRequest here
    MyHttpServletRequest request;
    String id = "3";
    MyHttpSession testSession1;
    
    public void setUp() {
    	testSession1 = new MyHttpSession(id);
    	testSession1.setAttribute("test", "abc");
    }
    
    
    public void testgetAttribute() {
    	assertEquals("abc", testSession1.getAttribute("test"));
    }
    
    
    public void testIsNew() {
    	assertEquals(true, testSession1.isNew());
    }

    
    public void testInterval() {
    	assertEquals(1800, testSession1.getMaxInactiveInterval());
    }
    
    
    public void testgetId() {
    	assertEquals("3", testSession1.getId());
    }
    
    
    public void testcreationTime() {
    	assertEquals(true, testSession1.isValidSession());
    }
    
    
    public void testInvalidate() {
    	testSession1.invalidate();
    	assertSame(false, testSession1.getValid());
    }

}