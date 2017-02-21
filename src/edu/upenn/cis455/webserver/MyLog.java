package edu.upenn.cis455.webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLog {
	
	private static MyLog log = null;
	private static FileHandler handler;
	protected static Logger logger;
	protected static File file;
	
	private MyLog() {
		try {
			handler = new FileHandler("./ServletLog.txt", true);
			logger = Logger.getLogger("Yueyang's HTTP Server Log");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		  catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.addHandler(handler);
		SimpleFormatter sf = new SimpleFormatter();
		handler.setFormatter(sf);
	}
	
	/**
	 * Create a singleton instance of log
	 * @return
	 */
	public synchronized static MyLog create() {
		if (log == null) {
			log = new MyLog();
		}
		return log;
	}
	
	/**
	 * Write out warnings
	 * @param msg
	 */
	public static void warn(String msg) {
		logger.warning(msg.concat("\n\n"));
	}
	
	/**
	 * Write out information
	 * @param msg
	 */
	public static void info(String msg) {
		logger.info(msg.concat("\n\n"));
	}
	
	/**
	 * Close the handler
	 */
	public static void close() {
		handler.close();
	}

}
