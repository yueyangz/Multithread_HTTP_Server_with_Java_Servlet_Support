package edu.upenn.cis455.webserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class BusyServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		long start = System.currentTimeMillis();
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Busy Servlet</TITLE></HEAD><BODY>");
		out.println("<P>Starting work...</P>");
		for (int j = 1; j < 3; ++j) {
			for (int i = 0; i < Integer.MAX_VALUE; ++i) {
			}
		}
		out.println("<P>Done!</P>");
		out.println("</BODY></HTML>");	
		System.out.println("Takes: " + String.valueOf(System.currentTimeMillis() - start));
	}
}
