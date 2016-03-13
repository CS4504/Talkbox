package talkbox.client;

import talkbox.lib.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the backend for the client. It is responsible for coordinating between the GUI and networking.
 * For ease of use, the main thread should create a new backend with the static method Backend.Backend().
 */
public class Backend implements Runnable {
	//TODO: Get server hostname
	public static final String hostname = "";
	public static final int port = 86754;
	/**
	 * Create a new Backend, start it, then return it.
	 * @return A new Backend.
	 */
	public static Backend Backend() {
		Backend b = new Backend();
		b.start();
		return b;
	}

	private final Thread thread;
	private final AtomicBoolean started;
	private final ConcurrentLinkedQueue<Message> sendQueue, receiveQueue;
	private boolean running = true;

	public Backend() {
		sendQueue = new ConcurrentLinkedQueue<>();
		receiveQueue = new ConcurrentLinkedQueue<>();
		started = new AtomicBoolean();
		thread = new Thread(this);
	}

	/**
	 * Start this Backend.
	 * If it has already been started, it will not be started again.
	 */
	public void start() {
		if(started.getAndSet(true))
			return;
		thread.start();
	}

	/**
	 * Stop this Backend.
	 * Once stopped, it can not be restarted.
	 */
	public void stop() {
		running = false;
		resume();
	}

	/**
	 * Queue a single message for sending.
	 * @param m The Message to send.
	 */
	public void sendMessage(Message m) {
		sendQueue.add(m);
		resume();
	}

	/**
	 * Queue multiple messages for sending.
	 * @param m An array of Messages to send.
	 */
	public void sendMessages(Message[] m) {
		receiveQueue.addAll(Arrays.asList(m));
		resume();
	}

	/**
	 * A singe received message should be passed to this method.
	 * It will be processed and displayed.
	 * @param m The message that was received.
	 */
	public void receiveMessage(Message m) {
		receiveQueue.add(m);
		//resume();
	}

	/**
	 * An array of received messages should be passed to this method.
	 * They will be processed and displayed.
	 * @param m The array of messages that were received.
	 */
	public void receiveMessages(Message[] m) {
		receiveQueue.addAll(Arrays.asList(m));
		//resume();
	}

	private synchronized void pause() {
		try {
			wait();
		} catch(InterruptedException e) {
		}
	}

	private synchronized void resume() {
		notifyAll();
	}

	@Override
	public void run() {
		NetworkMethods.openConnection(hostname, port);
		//TODO: Get unique id from networking.
		while(running) {
			//pause();
			if(!sendQueue.isEmpty()) {
				Message[] messages = (Message[])sendQueue.toArray();
				for(Message m : messages) {
					sendQueue.remove(m);
				}
				NetworkMethods.sendMessage(messages);
			} else {
				NetworkMethods.sendMessage(new Message[0]);
			}
			NetworkMethods.receiveMessage();
			if(!receiveQueue.isEmpty()) {
				Message m;
				while((m = receiveQueue.poll()) != null) {
					//TODO: Tell GUI to display message
				}
			}
			try {
				Thread.sleep(10);
			} catch(InterruptedException e) {
			}
		}
		try {
			NetworkMethods.closeConnection();
		} catch(IOException e) {
		}
	}
}
