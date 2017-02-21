package test.edu.upenn.cis455.hw1;




import junit.framework.TestCase;
import edu.upenn.cis455.webserver.Constants;
import edu.upenn.cis455.webserver.MyServletContext;
import edu.upenn.cis455.webserver.ServletContainer;

public class MyServletContextTest extends TestCase {
	
	MyServletContext c; 


	public void setUp() {
		Constants.WEB_XML_PATH = "./conf/web.xml";
		try {
			ServletContainer.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c = ServletContainer.getMyServletContext();
	}
	
	
	
	public void test() {
		String name = c.getServletContextName();
		assertEquals("Test servlets", name);
	}

}
