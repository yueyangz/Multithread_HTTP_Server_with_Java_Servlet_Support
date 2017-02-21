package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * HTTP blocking queue that queues sockets
 * @author cis455
 *
 */
public class HttpBlockingQueue {
	private Queue<Socket> queue;
	private int limit;
	
	public HttpBlockingQueue(int limit) {
		queue = new LinkedList<Socket>();
		this.limit = limit;
	}
	
	/**
	 * Get the remaining capacity
	 * @return
	 */
	public int remainingCapacity() {
		return limit - queue.size(); 
	}
	
	/**
	 * Enqueue 
	 * @param s
	 */
	public synchronized void put(Socket s) {
		while (limit == queue.size()) {
			System.out.println("Task queue is full now!");
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (queue.size() == 0) notifyAll();
		queue.offer(s);
	}
	
	/**
	 * Dequeue
	 * @return
	 */
	public synchronized Socket take() {
		while (queue.size() == 0) {
//			System.out.println("Task queue is empty now!");
			try {
				wait();
			} catch (InterruptedException e) {
				return null;
			}
		}
		if (queue.size() == limit) notifyAll();
		return queue.poll();		
	}
	
	/**
	 * Return the max size
	 * @return
	 */
	public int getLimit() {
		return limit;
	}
	
	/**
	 * Return the current size
	 * @return
	 */
	public int size() {
		return queue.size();
	}
}
