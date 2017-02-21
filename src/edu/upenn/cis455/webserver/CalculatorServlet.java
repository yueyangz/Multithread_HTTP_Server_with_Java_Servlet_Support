package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CalculatorServlet extends HttpServlet {

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;

	@Override 
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
//		System.out.println("I am in Calc");
//		if (true) throw new IOException();
		int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
		int v2 = Integer.valueOf(request.getParameter("num2")).intValue();
		int sum = v1 + v2;
		
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Hello</title></head>");
		out.println("<body>" + v1 + "+" + v2 + "=" + sum +"</body></html>");
	}
	
	@Override 
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
		int v2 = Integer.valueOf(request.getParameter("num2")).intValue();
		int sum = v1 + v2;
		
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Hello</title></head>");
		out.println("<body>" + v1 + "+" + v2 + "=" + sum +"</body></html>");
	}
}
