package com.cs.interfaces;

import java.net.Socket;

public interface Server
{
	/**
	 * Starts the configured web server.
	 */
	public void start();

	/**
	 * Shuts down the web server.
	 */
	public void stop();
	
	/**
	 * Dispatches an incoming socket connection to an appropriate handler.
	 * 
	 * @param socket
	 */
	public void dispatchRequest(Socket socket);
}
