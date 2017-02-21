package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class HttpServer {
	

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	public static ServerSocket serverSocket;
	protected static ThreadPool pool;
	protected static ServletContainer sc;
	protected static boolean running = true;

	
  
  public static void main(String args[]) 
  {

	System.out.println("This server takes neccessary and optional arguments in the following order (N for neccessary, O for optional): ");
	System.out.println("PORT NUMBER(N), ROOT DIRECTORY(N), PATH TO WEB.XML(N), NUMBER OF WORKER THREADS (O), SIZE OF BLOCKING QUEUE(O), MAX SESSION INACTIVE TIME(O)");
    if(args.length < 3) {
    	System.out.println(args.length);
        	System.out.println("Name: Yueyang Zheng \nSEAS Login: yueyangz \nWrong number of arguments");
    } else {
    	argumentHandling(args);
    	Socket socket = null;
//    	System.out.println(Constants.WEB_XML_PATH);
    	prepare(socket);
    	while (running) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				MyLog.info("Server throws IO Exception in main!");
				running = false;
			} catch (NullPointerException e) {
				MyLog.info("Server throws NulPointerException in main!");
				System.out.println("Socket is null!");			//handle exceptions
				break;
			}
    		pool.receiveTask(socket);
    	}
    	
    }
    
    System.out.println("Shut down already!");
  }
  
  /**
   * Set the required constants
   * @param port
   * @param rootDir
   * @param xmlPath
   */
  private static void setConstants(int port, String rootDir, String xmlPath){
  	Constants.setPortNumber(port);
  	rootDir = Helper.fixRootDirectory(rootDir);
  	Constants.setRoot(rootDir);
  	Constants.setWebXmlPath(xmlPath);
  }
  
  /**
   * Handling command line arguments
   * @param args
   */
  private static void argumentHandling(String[] args) {
  	int argSize = args.length;
  	int port = 8080;
  	try {
  		port = Integer.parseInt(args[0]);
  	} catch (NumberFormatException e) {
  		System.out.println("Invalid argument");
  		return;
  	}
  	String rootDir = args[1];
  	String xmlPath = args[2]; 
  	setConstants(port, rootDir, xmlPath);
  	if (argSize > 3) {
  		try {
  			Constants.NUMBER_OF_THREADS = Integer.parseInt(args[3]);
  		} catch (NumberFormatException e) {
  			System.out.println("Invalid argument");
  			return;
  		}	
  		if (argSize > 4) {
      		try {
      			Constants.MAX_SIZE_OF_QUEUE = Integer.parseInt(args[4]);
      		} catch (NumberFormatException e) {
      			System.out.println("Invalid argument");
      			return;
      		}
      		if (argSize > 5) {
          		try {
          			Constants.MAX_INACTIVE_INTERVAL = Integer.parseInt(args[5]);
          		} catch (NumberFormatException e) {
          			System.out.println("Invalid argument");
          			return;
          		}
      		}
  		}
  	}
 }
  
  /**
   * Creating servlet container session manager and log instance
   * @param socket
   */
  public static void prepare(Socket socket) {
	  try {
		ServletContainer.initialize();
	} catch (Exception e1) {
		Helper.send500InternalError(socket);
//		e1.printStackTrace();
	}
	  pool = new ThreadPool(Constants.MAX_SIZE_OF_QUEUE, Constants.NUMBER_OF_THREADS, Constants.rootDirectory);	//create thread pool
	  try {
			serverSocket = new ServerSocket(Constants.PORT_NUMBER);	//create the socket
		} catch (IOException e) {
			MyLog.warn("Server throws IO Exceptions in prepare!");
			e.printStackTrace();
			stopServer();
		}
  }
  

  
  
  /**
   * Stop the server
   */
  public static void stopServer() {	  
	  pool.stop();
	  try {
		serverSocket.close();
	} catch (NullPointerException e) {
		MyLog.info("Server throws NulPointerException in stopServer!");
		System.out.println("Socket is null!");
	} catch (IOException e) {
		MyLog.warn("Server throws IO Exception in stopServer!");
		System.out.println("Cannot close socket!");
		e.printStackTrace();
	}
	  MyLog.close();
  }
  

// GET /sb.html HTTP/1.1
// host: dsadasas
// If-Modified-since: Mon, 20 Feb 2014 20:54:06 GMT

  
}
