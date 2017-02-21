package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


public class ThreadPool {

	protected static HttpBlockingQueue bq;
	private static  ArrayList<ThreadWorker> threads;
	
	/**
	 * Create the thread pool by adding threads into the arraylist
	 * @param limit
	 * @param maxThreads
	 * @param rootDirectory
	 */
	public ThreadPool(int limit, int maxThreads, String rootDirectory) {
		System.out.println("Creating thread pool!");
		bq = new HttpBlockingQueue(limit);
		threads = new ArrayList<ThreadWorker>();
		for (int i = 0; i < maxThreads; i++) {
			String id = Integer.toString(i + 1); 
			threads.add(new ThreadWorker(id, bq, rootDirectory));
			threads.get(i).start();
		}
		System.out.println("Thread pool building completed!");

	}
	
	public static ArrayList<ThreadWorker> getThreadPool() {
		return threads;
	}
	
	public static HttpBlockingQueue getQueue() {
		return bq;
	}
	
	/**
	 * Enqueue method
	 * @param s
	 */
	public synchronized void receiveTask(Socket s) {
		bq.put(s);
//			System.out.println("Currently there is " + bq.size() + " tasks in the queue and the limit is " + bq.getLimit());
	}
	
	
	/**
	 * Stop the pool
	 */
	public synchronized void stop() {
		System.out.println("Killing all threads!");
		for (ThreadWorker t: threads) {
			t.kill();
			if (t.getSocket() != null)
				try {
					t.getSocket().close();
				} catch (IOException e) {
					MyLog.warn("Server throws IO Exception in stop!");
				}
		}
		System.out.println("All threads are killed!");
	}
}
 