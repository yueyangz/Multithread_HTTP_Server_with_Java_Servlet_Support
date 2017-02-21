package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;

import edu.upenn.cis455.webserver.Helper.FileValidity;

/**
 * Thread worker class that handles all the tasks
 * @author cis455
 *
 */
public class ThreadWorker extends Thread {
	

	private String id;								//the id of this thread
	private HttpBlockingQueue bq;
	private boolean shut;							//flag
	private BufferedReader in;
	private HashMap<String, String> initialLine;	//HashMap that stores HTTP request initial line
	private HashMap<String, String> requestMap;		//HashMap that stores all headers in requests
	private String urlWIithoutQueryString;
	private String queryString;
	private String rootDirecory;					//root dir
	private DataOutputStream out;					//out stream
	private Socket s;
	private DataContainer dc;
	private boolean hasMessageBody;

	
	public ThreadWorker(String id, HttpBlockingQueue bq, String rootDirectory) {
		this.id = id;
		this.bq = bq;
		this.rootDirecory = rootDirectory;
		shut = false;
		requestMap = new HashMap<String, String>();
		initialLine = new HashMap<String, String>();
		urlWIithoutQueryString = null;
		queryString = null;
		hasMessageBody = false;
		out = null;
		s = null;
	}
	
	public Socket getSocket(){
		return s;
	}
	/**
	 * Always call this method
	 */
	@Override
	public void run() {
		while (!shut) {
			try {
				initialLine.clear();
				requestMap.clear();
				s = bq.take();		//TAKE A TASK
				if (s == null) throw new SocketException();
				s.setSoTimeout(Constants.TIME_OUT);
				parsingRequest(s);		//READ
				handleRequests(s);		//WRITE
			} catch (SocketException e) {
				MyLog.info("Thread " + id + " is shutting down!");
//				System.out.println("THREAD " + id + " IS SHUTTING DOWN");
				shut = true;
			} catch (IOException e) {
				MyLog.info("Reading client request time out!");
			}
		}

	}
	
	/**
	 * Parsing the HTTP request and put the values into hashmaps
	 * @param s
	 * @throws IOException
	 */
	private void parsingRequest(Socket s) throws IOException {
		InputStreamReader reader = new InputStreamReader(s.getInputStream());
		in = new BufferedReader(reader);
		String line = in.readLine();						
		if (line == null || line.isEmpty()) {										
			initialLine.put("method", null);
			initialLine.put("url", null);
			initialLine.put("version", null);
			return;
		}
		String[] initialLineElements = line.split("\\s+");
		initialLine.put("method", initialLineElements[0]);
		initialLine.put("url", initialLineElements[1]);
		initialLine.put("version", initialLineElements[2]);
		processURL();

		StringBuilder query = new StringBuilder();
		while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
//			line = in.readLine();
//			if (line == null) {
//				System.out.println("Line is null");
//				break;
//			}
//			if (line.trim().length() == 0) {
//				break;
//			}
//			line = line.toLowerCase();
			ArrayList<String> header = Helper.parseColon(line);
			String key = header.get(0).toLowerCase();
			String value = header.get(1);
			if (requestMap.containsKey(key)) {
				value = requestMap.get(key) + ", " + value;
				requestMap.put(key, value);
			} else requestMap.put(key, value);
		}
//		Helper.printMap(requestMap);
		if (initialLine.get("method").equals("POST")) hasMessageBody = true;

		if (hasMessageBody) {
//			while (in.ready() && (line = in.readLine()) != null) {
//				query.append(line);
//			}	
			line = in.readLine();
			query.append(line);
		}
		
		if (query.length() > 0) requestMap.put("messageBody", query.toString());
		dc = new DataContainer(s, requestMap, queryString, urlWIithoutQueryString, null, initialLine);
		hasMessageBody = false;
	}
	


	
	/**
	 * Processing query strings
	 */


	/**
	 * Check 403
	 * @param url
	 * @return
	 */
	private boolean send403ErrorInsecurePath(String url) {
		if (!Helper.isSecurePath(url)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Generating initial line response
	 * @param code
	 * @return
	 */
	private String generateInitial (int code) {
		String initialLine = this.initialLine.get("version");
		switch(code) {
		case 100: {
			initialLine = initialLine.concat(" 100 CONTINUE \r\n\n"); 
			break;
		}
		case 200: {
			initialLine = initialLine.concat(" 200 OK \r\n"); 
			break;
		}
		case 304: {
			initialLine = initialLine.concat(" 304 NOT MODIFIED \r\n");
			break;
		}
		case 400: {
			initialLine = initialLine.concat(" 400 BAD REQUEST \r\n"); 
			break;
		}
		case 403: {
			initialLine = initialLine.concat(" 403 FORBIDDEN \r\n"); 
			break;
		}
		case 404: {
			initialLine = initialLine.concat(" 404 NOT FOUND \r\n"); 
			break;
		}
		
		case 412: {
			initialLine = initialLine.concat(" 412 PRECONDITION FAILED \r\n"); 
			break;
		}
		
		case 500: {
			initialLine = initialLine.concat(" 500 INTERNAL SERVER ERROR \r\n");
			break;
		}
		case 501: {
			initialLine = initialLine.concat(" 501 NOT IMPLEMENTED \r\n"); 
			break;
		}
		
		}
		return initialLine;
	}
	
	/**
	 * Check 304
	 * @param initialLine
	 * @return
	 */
	private boolean send304IfNotModified(String version, HashMap<String, String> requestMap) {
		if (version.equals("HTTP/1.0") || !initialLine.get("method").equals("GET")) {
			System.out.println("here");
			return false;
		}
		else {
			if (requestMap.get("if-modified-since") != null) {
				String clientDate = requestMap.get("if-modified-since");
				Date d = Helper.convertStringToDate(clientDate);
				String path = fullPath(initialLine.get("url"));
				if (Helper.isFileModified(d, getFile(path))) {
					return false;
				}
				else {
					return true;	
				}
			} else {
				return false;
			}
				
		}
	}
	
	/**
	 * Check 412
	 * @param initialLine
	 * @return
	 */
	private boolean send412PreConditionFailed(HashMap<String, String> requestMap) {
		if (initialLine.get("version").equals("HTTP/1.0")) return false;
		else {
			if (requestMap.get("if-unmodified-since") != null) {
				String clientDate = requestMap.get("if-unmodified-since");
				Date d = Helper.convertStringToDate(clientDate);
				String path = fullPath(initialLine.get("url"));
				if (Helper.isFileModified(d, getFile(path))) {
					return true;
				}
				else {
					return false;
				}
			} else {
				return false;
			}
				
		}
			
	}
	
	/**
	 * Check 400
	 * @param method
	 * @return
	 */
	private boolean send400ErrorMethodNotUppercase(String method) {
		if (!Helper.isMethodUppercase(method)) {
			System.out.println("Method not uppercase, 400!");
			return true;
		}
		return false;	
	}
	
	/**
	 * Check whether messagebody has the same length of content-length header
	 * @param messageBody
	 * @return
	 */
	private boolean send400ErrorContentLengthIssuePost(String messageBody) {
		if (!initialLine.get("method").equals("POST")) return false;
		int len = 0;
		if (messageBody != null) len = messageBody.length(); 
		String reportedLen = requestMap.get("content-length");
		if (reportedLen == null) return true;
		int reportedLenValue = Integer.valueOf(reportedLen);
		if (len != reportedLenValue) return true;
		else return false;		
	}
	
	/**
	 * Check 400
	 * @param version
	 * @param rawUrl
	 * @param requestMap
	 * @return
	 */
	private boolean send400ErrorNewHttpNeedsHostHeader(String version, String rawUrl, HashMap<String, String> requestMap) {
			if (version.equals("HTTP/1.0")) return false;
			else if (version.equals("HTTP/1.1") && requestMap.get("host") != null) {
				return false;
			}
			else if (!version.equals("HTTP/1.1") && requestMap.get("host") == null && Helper.isAbs(rawUrl)) return false;
			else {
				System.out.println("Require Host header for non 1.0 HTTP version, 400!");
				return true;
			}
				
	}
	
	/**
	 * Check 400
	 * @param version
	 * @param url
	 * @return
	 */
	private boolean send400ErrorOldHttpNotHaveAbsolutePath(String version, String url) {
		
		if (version.equals("HTTP/1.0") && url.contains("http://")){
			System.out.println("abs path!");
			return true;
		}
		else return false;
	}
	
	/**
	 * Check 400
	 * @param version
	 * @return
	 */
	private boolean send400ErrorWrongHttpVersionFormat(String version) {
		if (!version.equals("HTTP/1.0") && (!version.equals("HTTP/1.1")) && !version.matches("HTTP/1\\.[01]") && !version.matches("HTTP/\\d\\.\\d")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check 400
	 * @return
	 */
	private boolean send400ErrorNullInitialLine() {	
		if (!Helper.isValid(initialLine.get("method")) || !Helper.isValid(initialLine.get("url")) ||!Helper.isValid(initialLine.get("version"))) {
			System.out.println("Null initial line");
			initialLine.put("version", "HTTP/1.1");
			return true;
		}
		else return false;
	}
		
	/**
	 * Check 100
	 * @param version
	 * @param requestMap
	 * @return
	 */
	private boolean send100ContinueToNewHttpClient(String version, HashMap<String, String> requestMap) {
		if (version == null) return false;
		if (version.equalsIgnoreCase("HTTP/1.0")) return false;
		else {
			if (requestMap.get("Expect") != null && requestMap.get("Expect").equals("100-continue")) {
				return true;
			}
		}
		return false;
	}
		
	/**
	 * Parse URLs
	 * @return
	 */
	private String processURL() {
		String rawUrl = initialLine.get("url");
		if (rawUrl == null) return null;
		initialLine.put("rawUrl", rawUrl);
		initialLine.put("url", Helper.parseAbsolutePath(rawUrl));
		String url = initialLine.get("url");
//		System.out.println("url is: " + url);
		separateQueryString(url);
		return url;
	}
	
	/**
	 * Produce ERROR headers
	 * @param code
	 * @param date
	 * @param contentType
	 * @param contentLength
	 * @param lastModified
	 * @return
	 */
	private String sendErrorResponse(int code, String date, String fileDate, String contentType, String contentLength, String lastModified) {
		StringBuilder html = new StringBuilder();
		String status = generateInitial(code);
		String strippedStatus = status.substring(9, status.length());
		String content = Helper.wrapInHtmlTags(strippedStatus);
		date = date.concat(Helper.getDate()).concat("\r\n");
		String server = "Server: Yueyang Zheng \r\n";
		lastModified = lastModified.concat(fileDate).concat("\r\n");
		contentType = contentType.concat("text/html\r\n");
		contentLength = contentLength.concat(String.valueOf(content.length())).concat("\r\n\r\n");
		String connection = "Connection: closed\r\n";
		html.append(status).append(date).append(server).append(lastModified).append(connection).append(contentType).append(contentLength).append(content);
		return html.toString();
	}
	
	/**
	 * Produce headers for HEAD requests
	 * @param code
	 * @param date
	 * @param contentType
	 * @param contentLength
	 * @param lastModified
	 * @return
	 */
	private String sendHEADErrorResponse(int code, String date, String fileDate, String contentType, String contentLength, String lastModified) {
		StringBuilder html = new StringBuilder();
		String status = generateInitial(code);
		String strippedStatus = status.substring(9, status.length());
		String content = Helper.wrapInHtmlTags(strippedStatus);
		date = date.concat(Helper.getDate()).concat("\r\n");
		String server = "Server: Yueyang Zheng \r\n";
		lastModified = lastModified.concat(fileDate).concat("\r\n");
		contentType = contentType.concat("text/html\r\n");
		contentLength = contentLength.concat(String.valueOf(content.length())).concat("\r\n\r\n");
		String connection = "Connection: closed\r\n";
		html.append(status).append(date).append(server).append(lastModified).append(connection).append(contentType).append(contentLength);
		return html.toString();
	}
	
	/**
	 * Produce OK headers + resource
	 * @param code
	 * @param date
	 * @param fileDate
	 * @param content
	 * @param fileType
	 * @param contentType
	 * @param contentLength
	 * @param lastModified
	 * @return
	 */
	private String sendOKResponse(int code, boolean head, String date, String fileDate, String content, String fileType, String contentType, String contentLength, String lastModified) {
		StringBuilder html = new StringBuilder();
		String status = generateInitial(code);
		date = date.concat(Helper.getDate()).concat("\r\n");
		String server = "Server: Yueyang Zheng \r\n";
		String connection = "Connection: closed \r\n\r\n";
		contentType = contentType.concat(fileType.concat("\r\n"));
		contentLength = contentLength.concat(String.valueOf(content.length())).concat("\r\n");
		lastModified = lastModified.concat(fileDate).concat("\r\n");
		html.append(status).append(date).append(server).append(lastModified).append(contentType).append(contentLength).append(connection);
		if (!head) html.append(content);
		return html.toString();
	}
	
	/**
	 * Produce OK headers + binary resource
	 * @param code
	 * @param date
	 * @param fileDate
	 * @param content
	 * @param fileType
	 * @param contentType
	 * @param contentLength
	 * @param lastModified
	 * @return
	 */
	private String sendOKResponse(int code, String date, String fileDate, File content, String fileType, String contentType, String contentLength, String lastModified) {
		String html = "";
		String status = generateInitial(code);
		date = date.concat(Helper.getDate()).concat("\r\n");
		String server = "Server: Yueyang Zheng \r\n";
		String connection = "Connection: closed \r\n\r\n";
		contentType = contentType.concat(fileType.concat("\r\n"));
		contentLength = contentLength.concat(String.valueOf(content.length())).concat("\r\n");
		lastModified = lastModified.concat(fileDate).concat("\r\n");
		html = status + date + server + lastModified + contentType + contentLength + connection;
		return html;
	}
	
	/**
	 * Actually send 100 continue
	 * @param version
	 * @param out
	 * @throws IOException
	 */
	private void handle100(String version, DataOutputStream out) throws IOException{
		if (send100ContinueToNewHttpClient(version, requestMap)) {
			String response = generateInitial(100);
			out.write(response.getBytes());
//			out.flush();
		}
	}
	
	/**
	 * Check whether a request needs servlet invoking
	 * @param dc
	 * @return
	 */
	private HttpServlet needsServlet(DataContainer dc) {
		if (ServletContainer.getServlets() == null) System.out.println("servlet pool is null!");
//		for (String key: ServletContainer.getPool().keySet()) {
//			System.out.println("servlet key: " + key);
//		}
//		System.out.println("url: " + urlWIithoutQueryString);
		String servletName = ServletContainer.getServletName(true).get(urlWIithoutQueryString); 
//		System.out.println("1st serv name: " + servletName);
		if (servletName == null){
			String parsedPath = ServletContainer.pathMap(urlWIithoutQueryString, dc);
//			System.out.println("parsedPath: " + parsedPath);
			servletName = ServletContainer.getServletName(false).get(parsedPath);
//			System.out.println("2nd servletName: " + servletName);
		}
//		System.out.println("servletname: " + servletName);
		HttpServlet servlet = ServletContainer.getServlets().get(servletName);
//		System.out.println("get instance!");
		return servlet;
	}
	
	/**
	 * Writing a whole response
	 * @param s
	 * @throws IOException
	 */
	private void handleRequests(Socket s) throws IOException {
		out = new DataOutputStream(s.getOutputStream());
		String method = initialLine.get("method");						//get the request values
		String url = urlWIithoutQueryString;
		String version = initialLine.get("version");
		String response = "";
		String messageBody = requestMap.get("messageBody");


		
		//Checking 400 level errors

		if (send400ErrorNullInitialLine()) {
			MyLog.info("400 Bad Request!");
			response = sendErrorResponse(400, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}

		else if(send400ErrorOldHttpNotHaveAbsolutePath(version, initialLine.get("rawUrl"))) {
			MyLog.info("400 Bad Request!");
			response = sendErrorResponse(400, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}
			
		else if (send403ErrorInsecurePath(url)) {
			MyLog.info("403 Forbidden!");
			response = sendErrorResponse(403, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}
			
		else if (send400ErrorMethodNotUppercase(method)) {
			MyLog.info("400 Bad Request!");
			response = sendErrorResponse(400, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}
			
		else if (send400ErrorWrongHttpVersionFormat(version)) {
			MyLog.info("400 Bad Request!");
			response = sendErrorResponse(400, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}
			
		else if (send400ErrorNewHttpNeedsHostHeader(version, initialLine.get("rawUrl"), requestMap)) {
			MyLog.info("400 Bad Request!");
			response = sendErrorResponse(400, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}
			
		else if (send400ErrorContentLengthIssuePost(messageBody)) {
			MyLog.info("400 Bad Request!");
			response = sendErrorResponse(400, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
		}
			
		//Check if GET or HEAD
		else if (method.equals("GET") || method.equals("HEAD") || method.equals("POST")) {
			handle100(version, out);
			//System.out.println("Passed a bunch of error code! and method is GET/HEAD");
			//separate simple url and query string
//			System.out.println("GETHEADPOST!!!!!!!!!!!!!!!!!");
			
			String fullPath = fullPath(urlWIithoutQueryString);
			FileValidity fv = Helper.pathChecking(fullPath);
			HttpServlet servlet = needsServlet(dc);
			if (servlet != null) {
//				System.out.println("Needs servlet!");
//				System.out.println("servlet url " + urlWIithoutQueryString);
					try {
//						System.out.println("call service");
						MyHttpServletRequest req = new MyHttpServletRequest(dc);
						servlet.service(req, new MyHttpServletResponse(dc, req));

					} catch (Exception e) {
//						e.printStackTrace();
						MyLog.warn("500 Internal Server Error because servlet throws exceptions during service!");
						response = sendErrorResponse(500, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					} finally {
						out.write(response.getBytes());
						out.flush();
						out.close();
					}
					return;
			}
			
			//Check special URLs
			if (url.equalsIgnoreCase("/control")) {
				//handleControlPanel
//				handle100(version,  out);
				if (method.equals("HEAD")) response = sendOKResponse(200, true, Constants.DATE, "", handleControlPanelURL(), "text/html", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
				else response = sendOKResponse(200, false, Constants.DATE, "", handleControlPanelURL(), "text/html", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
			}
			

			else if (url.equalsIgnoreCase("/shutdown")) {
				//handleShutdown
//				handle100(version,  out);
				if (method.equals("HEAD")) response = sendOKResponse(200, true, Constants.DATE, "", generateShutDownHtml(), "text/html", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
				else response = sendOKResponse(200, false, Constants.DATE, "", generateShutDownHtml(), "text/html", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);

				handleShutDownURL(response);
			}
			
			//Check if file exists
			else if (!fv.exists) {
				if (method.equals("HEAD")) response = sendHEADErrorResponse(404, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
				else {
					MyLog.info("404 Not Found!");
					response = sendErrorResponse(404, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
				}
			}
			
			//Check if is directory
			else if (fv.isDirectory) {
				//generate directory list in html
//				handle100(version,  out);
				if (method.equals("HEAD")) response = sendOKResponse(200, true, Constants.DATE, "", handleDirectory(), "text/html", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);		
				else response = sendOKResponse(200, false, Constants.DATE, "", handleDirectory(), "text/html", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);		
			}
	
			else if (fv.isFile) {
				String type = handleFileType(fullPath);
				
				//Check if it can be read
				if (!fv.canRead) {
					if (method.equals("HEAD")) response = sendHEADErrorResponse(403, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					else {
						MyLog.info("403 Forbidden!");
						response = sendErrorResponse(403, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					}
				}
			
				//Check if type is supported, if not, send 501 error
				else if (type.isEmpty()) {
					if (method.equals("HEAD")) {
						MyLog.warn("501 Not Implemented!");
						response = sendHEADErrorResponse(501, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					}
					else {
						MyLog.warn("501 Not Implemented!");
						response = sendErrorResponse(501, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					}
				}
				
				//Check 304
				else if (send304IfNotModified(version, requestMap)){
//					if (method.equals("HEAD")) response = sendHEADErrorResponse(304, Constants.DATE, Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					MyLog.info("304 Not Modified!");
					response = sendErrorResponse(304, Constants.DATE, Helper.getFileDate(getFile(fullPath)), Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
				}

				
				//Check 412
				else if (send412PreConditionFailed(requestMap)){
					if (method.equals("HEAD")) {
						MyLog.info("412 Precondition Failed!");
						response = sendHEADErrorResponse(412, Constants.DATE, Helper.getFileDate(getFile(fullPath)), Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					}
					else {
						MyLog.info("412 Precondition Failed!");
						response = sendErrorResponse(412, Constants.DATE, Helper.getFileDate(getFile(fullPath)), Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					}
				}
				
				//good binary image now
				else if (type.startsWith("image") || type.startsWith("text")) {
					//it's a picture, send it in binary form
					File file = getFile(fullPath);
//					handle100(version,  out);
					String header = sendOKResponse(200, Constants.DATE, Helper.getFileDate(getFile(fullPath)), file, type, Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
					if (method.equals("HEAD")) {
						response = header;
					} else {
						handleFiles(file, header);
						return;	
					}
				}			
			}
		}
		
		else {
			//Not supported method
			if (method.equals("HEAD")) response = sendHEADErrorResponse(501, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
			else {
				MyLog.warn("501 Not Implemented!");
				response = sendErrorResponse(501, Constants.DATE, "", "text/html", Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED);
			}
		}
		
		//Output the response
		out.write(response.getBytes());
		out.flush();
		out.close();
			
	}
	
	/**
	 * Generate Shutdown HTML
	 * @return
	 */
	private String generateShutDownHtml() {
		String res = "";
		res += "<html>";
		res += " <body>";
		res += "	<h3>Shutting down the server!</h3>";
		res += " </body>";
		res += "</html>";
		return res;
	}
	
	/**
	 * Separate URL
	 * @param s
	 */
	private void separateQueryString(String s) {
		if (s.charAt(s.length() - 1) == '/') s = s.substring(0, s.length()-1);
		if (s.contains("?")) {
			String[] segments = s.split("\\?");
			urlWIithoutQueryString = segments[0];
//			System.out.println("pure url: " + urlWIithoutQueryString);
			queryString = segments[1];
			initialLine.put("query", queryString);
//			System.out.println("qstring: " + queryString);
//			checkQueryString(queryString);
			
		} else urlWIithoutQueryString = s;
		
	}
	
	/**
	 * Get a full path
	 * @param url
	 * @return
	 */
	private String fullPath(String url) {
		String ret = rootDirecory.concat(url);
		return ret;
	}
	
	/**
	 * Generate control panel HTML
	 * @return
	 */
	private String handleControlPanelURL() {
		String responseBody = "";
		responseBody += "<html>";
		responseBody += " <body>";
		responseBody += "<p>";
		responseBody +=	"	<h2>Control Panel</h2>";
		responseBody += "</p>";
		responseBody += "<p>";
		responseBody += "	<h4>Yueyang Zheng (yueyangz)</h4>";
		responseBody += "</p>";
		responseBody += "<p>";
		responseBody += "	<h4>Thread Monitor</h4>";
		responseBody += "</p>";
		for (ThreadWorker t: ThreadPool.getThreadPool()) {
			responseBody += "<p>";
			responseBody += t.id; 
			responseBody += "          ";
			if (t.getState() == Thread.State.WAITING) {
				responseBody += "WAITING";
				responseBody += "</p>";
			}
			else if (t.getState() == Thread.State.BLOCKED) {
				responseBody += "BLOCKED";
				responseBody += "</p>";
			}
			else {
				if (!t.shut) {
					String url = t.initialLine.get("url");
					if (url == null) {
						responseBody = responseBody +"WORKING ON SOMETHING BUT NO URL INFORMATION IS RETRIEVED";
						responseBody += "</p>";
					} else {
						responseBody = responseBody +"WORKING ON: " + t.initialLine.get("url");
						responseBody += "</p>";
					}

				}
				else System.out.println("Thread is in neither state! Something is wrong");
			}
		}
		responseBody += "<p>";
		responseBody += "	<a href=\"ServletLog.txt\"><button type=\"button\"> View Server Logs </button></a>";
		responseBody += "</p>";
		responseBody += "<p>";
		responseBody += "	<a href=\"shutdown\"><button type=\"button\"> Shut Down </button></a>";
		responseBody += "</p>";
		responseBody += " </body>";
		responseBody += "</html>";
		return responseBody.toString();
	}
	
	/**
	 * Handle shut down
	 * @throws IOException
	 */
	private void handleShutDownURL (String s) throws IOException {
		out.write(s.getBytes());
		out.flush();
		out.close();
		ServletContainer.killServlets();
		HttpServer.running = false;
		HttpServer.stopServer();
	}
	
	/**
	 * Generate directory HTML
	 * @return
	 */
	private String handleDirectory() {
		String responseBody = "";
		responseBody += "<html>";
		responseBody += " <body>";
		responseBody += "	<h2>Directory View</h2>";
		String path = fullPath(initialLine.get("url"));
		File file = getFile(path);
		for (File f: file.listFiles()) {
			String dir = initialLine.get("url").concat("/").concat(f.getName());
			dir = dir.trim().replaceAll("/+", "/");
			String tag = "<p>"; 
			tag = tag.concat("<a href=\"");
			tag = tag.concat(dir).concat("\"").concat(">").concat(f.getName()). concat("</a><br>");
			tag = tag.concat("</p>");
			responseBody += tag;
		}
		responseBody+="	</body>";
		responseBody+="<html>";
		return responseBody.toString();
	}
	
	/**
	 * Return a file descriptor 
	 * @param path
	 * @return
	 */
	private File getFile(String path) {
		File file = new File(path);
		return file;
	}
	
	/**
	 * Determine the file type
	 * @param fullPath
	 * @return
	 */
	private String handleFileType(String fullPath) {
		if (fullPath.endsWith(".jpg") || fullPath.endsWith(".jpeg")) 
			return "image/jpeg";
		if (fullPath.endsWith(".gif")) 
			return "image/gif";
		if (fullPath.endsWith(".png"))
			return "image/png";
		if (fullPath.endsWith(".txt"))
			return "text/plain";
		if (fullPath.endsWith(".html"))
			return "text/html";
		else return "";
	}
	
	/**
	 * Generate header for success (picture)
	 * @param file
	 * @param header
	 * @throws IOException
	 */
	private void handleFiles(File file, String header) throws IOException {
		FileInputStream in = new FileInputStream(file);
		byte[] buffer = new byte[2048];
		int length;
		try {
			out.write(header.getBytes());
			while ((length = in.read(buffer)) >= 0) {
				out.write(buffer, 0, length);
			}	
		} catch(IOException e) {
			MyLog.warn("Server throws IO Exceptions in handleFiles!");
			out.write(sendErrorResponse(500, Constants.DATE, "", Constants.CONTENT_TYPE, Constants.CONTENT_LENGTH, Constants.LAST_MODIFIED).getBytes());
			e.printStackTrace();
		} finally {
			in.close();
			out.close();
		}
		
	}
	
	/**
	 * Kill the thread
	 */
	public void kill() {
		shut = true;
		this.interrupt();
	}
	
	/**
	 * Return thread ID
	 * @return
	 */
	public String getThreadID() {
		return id;
	}
}
