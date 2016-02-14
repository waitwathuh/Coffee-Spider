package com.cs.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cs.interfaces.Server;
import com.cs.logs.Logger;

public class HttpServer implements Server
{
	private final static String SERVER_NAME = "Coffee Spider";
	private final static String SERVER_VERSION = "v.1.0";
	private final static String SERVER_SIGNATURE = SERVER_NAME + " " + SERVER_VERSION;

	private volatile boolean RUNNING = false;

	private final ServerSocket SERVERSOCKET;
	private final ExecutorService WORKERPOOL;
	private final ExecutorService DISPATCHERSERVICE;

	public HttpServer()
	{
		this( 80 );
	}

	public HttpServer( int port )
	{
		try
		{
			SERVERSOCKET = new ServerSocket( port );
			WORKERPOOL = Executors.newFixedThreadPool( 16 );
			DISPATCHERSERVICE = Executors.newSingleThreadExecutor();
		}
		catch ( IOException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error while starting server: " + e.getMessage(), callingMethod, currentMethod );
			throw new RuntimeException( "Error while starting server", e );
		}
	}

	@Override
	public void start()
	{
		RUNNING = true;
		// Initiate the main server loop accepting incoming connections.
		DISPATCHERSERVICE.submit( new Runnable()
		{
			@Override
			public void run()
			{
				while ( RUNNING )
				{
					try
					{
						Socket socket = SERVERSOCKET.accept();
						dispatchRequest( socket );
					}
					catch ( IOException e )
					{
						StackTraceElement[] ste = Thread.currentThread().getStackTrace();
						String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
						String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
						
						Logger.writeLog( "ERROR", "Error while accepting a connection: " + e.getMessage(), callingMethod, currentMethod );
					}
				}
			}
		} );

		try
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "LOG", "Webserver started: https://" + InetAddress.getLocalHost().getHostName() + ":" + SERVERSOCKET.getLocalPort(), callingMethod, currentMethod );
		}
		catch ( UnknownHostException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error while getting server details: " + e.getMessage(), callingMethod, currentMethod );
		}
	}

	@Override
	public void stop()
	{
		try
		{
			RUNNING = false;
			DISPATCHERSERVICE.shutdown();
			WORKERPOOL.shutdown();
			SERVERSOCKET.close();
		}
		catch ( IOException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error while shutting down the server: " + e.getMessage(), callingMethod, currentMethod );
		}
		finally
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "LOG", "The server has been shut down successfully.", callingMethod, currentMethod );
		}
	}

	@Override
	public void dispatchRequest( Socket socket )
	{
		WORKERPOOL.submit( new HttpWorker( socket, this ) );
	}

	/**
	 * Returns the signature of the web server.
	 * 
	 * @return String representing the web server name and version
	 */
	public static String getServerSignature()
	{
		return SERVER_SIGNATURE;
	}
}
