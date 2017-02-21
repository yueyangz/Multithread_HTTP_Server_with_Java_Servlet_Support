package edu.upenn.cis455.webserver;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MyHttpServletResponse implements HttpServletResponse {
	
	private String setContentType = null;
	private Locale locale;
	private DataContainer dc;
	private OutputStream out;
	private StringBuffer sb;
	private boolean committed;
	private String setCharacterEncoding;
	private String errorMessage;
	private HashMap<String, String> responseHeaders;
	private int statusCode;
	private boolean shouldFlush;
	private MyHttpServletRequest req;
	
	public MyHttpServletResponse(DataContainer dc, MyHttpServletRequest req) {
		this.dc = dc;
		this.req = req;
		statusCode = 200;
		setCharacterEncoding = null;
		errorMessage = null;
		responseHeaders = new HashMap<String, String>();
		sb = new StringBuffer();
		committed = false;
		shouldFlush = false;
		try {
			out = dc.getSocket().getOutputStream();
		} catch (IOException e) {
			MyLog.warn("Server throws IO Exception in MyHttpServletResponse!");
		}
	}


	/**
	 * Flush the buffer and write the response
	 */
	public void flushBuffer() throws IOException {
		committed = true;
		out.flush();
		out.write(sb.toString().getBytes());
		out.flush();
		clean();
		MyLog.info("A response was sent to the client!");
	}
	
	/**
	 * Generating a response
	 */
	private void appendToBuffer() {
		sb.append(Helper.generateInitial(statusCode, dc));
		for (String s: responseHeaders.keySet()) {
			String field = s.concat(": ").concat(responseHeaders.get(s)).concat("\r\n");
//			System.out.println("field: " + field);
			sb.append(field);
		}
		if (req.validSession()) {
			sb.append("Set-Cookie: ");
			sb.append("JSESSIONID=");
			sb.append(req.getSession().getId());
			sb.append("\r\n");
		}
		sb.append("\r\n");
//		System.out.println("sb: " + sb);
		if (shouldFlush) {
//			System.out.println("Should flush!");
//			System.out.println("Res: " + sb.toString());
			try {
				flushBuffer();
			} catch (IOException e) {
				MyLog.warn("Server throws NulPointerException in stopServer!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Clean everything
	 */
	private void clean() {
		sb.setLength(0);
		responseHeaders.clear();
		setContentType = null;
		locale = null;
		setContentLength(0);
		statusCode = 0;
	}

	/**
	 * Return buffer size
	 */
	public int getBufferSize() {
		return sb.length();
	}

	/**
	 * Return encoding
	 */
	public String getCharacterEncoding() {
		return setCharacterEncoding != null ? setCharacterEncoding : "ISO-8859-1";
	}

	/**
	 * Return content type
	 */
	public String getContentType() {
//		System.out.println("content type: " + setContentType);
		return setContentType != null ? setContentType : "text/html";
	}

	/**
	 * Return locale
	 */
	public Locale getLocale() {
		return locale;
	}

	//Not required
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	/**
	 * Return an output stream
	 */
	public PrintWriter getWriter() throws IOException {
//		return new PrintWriter(dc.getSocket().getOutputStream(), true);
		shouldFlush = true;
		appendToBuffer();
		return new PrintWriter(out, true);
	}

	/**
	 * Return whether the response is commited
	 */
	public boolean isCommitted() {
		return committed;
	}

	/**
	 * Reset the buffer / other data
	 */
	public void reset() {
		if (committed) {
			MyLog.warn("Server throws IllegalStateException in reset!");
			throw new IllegalStateException("Already commited!");
		} else {
			clean();
		}
		
	}

	/**
	 * Reset the buffer only
	 */
	public void resetBuffer() {
		if (isCommitted()) {
			MyLog.warn("Server throws IllegalStateException in resetBuffer!");
			throw new IllegalStateException("Already commited!");
		} else {
			sb.setLength(0);
		}
	}

	/**
	 * Change the buffer size
	 */
	public void setBufferSize(int arg0) {
		if (committed) {
			MyLog.warn("Server throws IllegalStateException in setBufferSize!");
			throw new IllegalStateException("Already commited!");
		} else {
			sb.setLength(arg0);
		}
	}

	/**
	 * Change the encoding
	 */
	public void setCharacterEncoding(String arg0) {
		this.setCharacterEncoding = arg0;
	}

	/**
	 * Change the content length
	 */
	public void setContentLength(int arg0) {
		String cl = Constants.CONTENT_LENGTH;
		responseHeaders.put(cl, String.valueOf(arg0));
	}

	/**
	 * Change the content type
	 */
	public void setContentType(String type) {
//		String encoding = getCharacterEncoding();
		this.setContentType = type;
		responseHeaders.put("Content-type", type);
	}

	/**
	 * Change locale
	 */
	public void setLocale(Locale arg0) {
		this.locale = arg0;
	}

	/**
	 * Add cookie to response 
	 */
	public void addCookie(Cookie arg0) {
		if (containsHeader("Set-Cookie")) {
			String header = responseHeaders.get("Set-Cookie");
			String headerWithoutMaxAge = changeMaxAge(header);
			header = headerWithoutMaxAge + "; " + arg0.getName() + "=" + arg0.getValue();
			header = header + "; " + "Max-Age=" + arg0.getMaxAge();
			responseHeaders.put("Set-Cookie", header);
		} else {
			String cookie = arg0.getName() + "=" + arg0.getValue() + "; " + "Max-Age=" + arg0.getMaxAge();
			responseHeaders.put("Set-Cookie", cookie);
		}
	}
	
	/**
	 * Return all headers
	 * @return
	 */
	public HashMap<String, String> getResponseMap() {
		return responseHeaders;
	}
	
	/**
	 * Change max-age = ?
	 * @param s
	 * @return
	 */
	private String changeMaxAge(String s) {
		String[] list = s.split(";\\s");
		String emp = "";
		for (int i = 0; i < list.length - 1; i++) {
			emp += list[i];
		}
		System.out.println("emp: " + emp);
		return emp;
	}

	/**
	 * Add a date header
	 */
	public void addDateHeader(String arg0, long arg1) {
		Date d = new Date(arg1);
		String date = Helper.getDate(d);
		if (responseHeaders.containsKey(arg0)) {
			String value = responseHeaders.get(arg0);
			value = value + ", " + date;
			responseHeaders.put(arg0, value);
		} else {
			responseHeaders.put(arg0, date);
		}
	}

	/**
	 * Add a header
	 */
	public void addHeader(String arg0, String arg1) {
		if (responseHeaders.containsKey(arg0)) {
			if (arg0.equals("Set-Cookie")) {
				String value = responseHeaders.get(arg0);
				value = value + "; " + arg1;
				responseHeaders.put(arg0, value);
			} else {
				String value = responseHeaders.get(arg0);
				value = value + ", " + arg1;
				responseHeaders.put(arg0, value);
			}

		} else {
			responseHeaders.put(arg0, arg1);
		}
		
	}

	/**
	 * Add a int header
	 */
	public void addIntHeader(String arg0, int arg1) {
		if (responseHeaders.containsKey(arg0)) {
			String value = responseHeaders.get(arg0);
			value = value + ", " + String.valueOf(arg1);
			responseHeaders.put(arg0, value);
		} else {
			responseHeaders.put(arg0, String.valueOf(arg1));
		}
		
	}
	/**
	 * Check contains header?
	 */
	public boolean containsHeader(String arg0) {
		return responseHeaders.containsKey(arg0);
	}

	/**
	 * encode a string
	 */
	public String encodeRedirectURL(String arg0) {
		return encodeURL(arg0);
	}

	//Deprecated
	public String encodeRedirectUrl(String arg0) {	
		return null;
	}
	
	/**
	 * encode a URL
	 */
	public String encodeURL(String arg0) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(arg0, getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encoded;
	}

	//Deprecated
	public String encodeUrl(String arg0) {
		return null;
	}

	/**
	 * Send an error
	 */
	public void sendError(int arg0) throws IOException {
		if (committed) {
			MyLog.warn("Server throws IllegalStateException in sendError!");
			throw new IllegalStateException();
		} else {
			setStatus(arg0);
			setContentType("text/html");
			responseHeaders.put(Constants.CONTENT_TYPE, "text/html");
			responseHeaders.put(Constants.CONTENT_LENGTH, String.valueOf(errorMessage.getBytes().length));
			shouldFlush = true;
			appendToBuffer();
		}
		
	}
	/**
	 * Send an error
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		errorMessage = arg1;
		MyLog.warn("An error was sent in HttpServletResponse!");
		sendError(arg0);
	}

	/**
	 * Redirect
	 */
	public void sendRedirect(String arg0) throws IOException {
		
		if (committed) {
			MyLog.warn("Server throws IllegalStateException in sendRedirect!");
			throw new IllegalStateException();
		} else {
			System.out.println("[DEBUG] redirect to " + arg0 + " requested");
			System.out.println("[DEBUG] stack trace: ");
			setStatus(302);
			responseHeaders.put("Location", arg0);
		}
		

	}

	/**
	 * Change date header
	 */
	public void setDateHeader(String arg0, long arg1) {
		Date d = new Date(arg1);
		String date = Helper.getDate(d);
		responseHeaders.put(arg0, date);
	}

	/**
	 * Change header
	 */
	public void setHeader(String arg0, String arg1) {
		responseHeaders.put(arg0, arg1);
	}
	
	/**
	 * Change int header
	 */
	public void setIntHeader(String arg0, int arg1) {
		responseHeaders.put(arg0, String.valueOf(arg1));
	}

	/**
	 * Change statuscode
	 */
	public void setStatus(int arg0) {
		statusCode = arg0;
	}
	
	//Deprecated
	public void setStatus(int arg0, String arg1) {
		
	}

}
