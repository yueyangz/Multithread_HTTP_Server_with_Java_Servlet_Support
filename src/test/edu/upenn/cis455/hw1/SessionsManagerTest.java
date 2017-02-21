package test.edu.upenn.cis455.hw1;


import edu.upenn.cis455.webserver.MyHttpSession;
import edu.upenn.cis455.webserver.MyLog;
import edu.upenn.cis455.webserver.SessionsManager;
import junit.framework.TestCase;

public class SessionsManagerTest extends TestCase {

	SessionsManager sm;

	public void setUp() {
		MyLog.create();
		sm = SessionsManager.create();
		SessionsManager.addASession(new MyHttpSession("123"));
		SessionsManager.addASession(new MyHttpSession("124"));
	}
	
	
	public void testSingleton() {
		SessionsManager sm2 = SessionsManager.create();
		assertTrue(sm2.equals(sm));
	}
	
	
	public void testAddASession() {
		SessionsManager.addASession(new MyHttpSession("125"));
		MyHttpSession s = SessionsManager.getASession("125");
		assertNotNull(s);
		assertEquals("125", s.getId());
	}
	
	
	public void testgetASession() {
		MyHttpSession s = SessionsManager.getASession("123");
		assertNotNull(s);
		assertEquals("123", s.getId());
	}
	
	

}
