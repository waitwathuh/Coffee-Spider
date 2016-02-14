package com.cs.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.cs.interfaces.Server;
import com.cs.logs.Logger;
import com.cs.main.Config;

/**
 * A Java KeyStore (JKS) file is needed to host a HTTPS server. For testing purposes a keyfile can be generated
 * using the following command in a terminal: 
 * "keytool -genkey -alias mykey -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 365 -keypass password2 -keystore identity.jks -storepass password1"
 * 
 * @see https://blogs.oracle.com/blogbypuneeth/entry/steps_to_create_a_self
 */
public class HttpsServer implements Server
{
	private final static String SERVER_NAME = "Coffee Spider";
	private final static String SERVER_VERSION = "v.1.0";
	private final static String SERVER_SIGNATURE = SERVER_NAME + " " + SERVER_VERSION;

	private volatile boolean RUNNING = false;

	private ServerSocket SERVERSOCKET;
	private final ExecutorService WORKERPOOL;
	private final ExecutorService DISPATCHERSERVICE;
	
	// Full path to the keyfile
	private final String KSNAME = Config.keyFilePath();
	// Password for the keystore
	private final char KSPASS[] = Config.keyStorePassword1().toCharArray();
	// Secondary password for the keystore 
	private final char CTPASS[] = Config.keyStorePassword2().toCharArray();
	
	public HttpsServer()
	{
		this( 80 );
	}

	public HttpsServer( int port )
	{
		WORKERPOOL = Executors.newFixedThreadPool( 16 );
		DISPATCHERSERVICE = Executors.newSingleThreadExecutor();
		
		try
		{
			KeyStore ks = KeyStore.getInstance( "JKS" );
			ks.load( new FileInputStream( KSNAME ), KSPASS );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, CTPASS );
			
			SSLContext sc = SSLContext.getInstance( "TLS" );
			sc.init( kmf.getKeyManagers(), null, null );
			
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			
			SERVERSOCKET = ( SSLServerSocket ) ssf.createServerSocket( port );
		}
		catch ( IOException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error while starting server: " + e.getMessage(), callingMethod, currentMethod );
			throw new RuntimeException( "Error while starting server", e );
		}
		catch ( KeyStoreException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error with SSL key: " + e.getMessage(), callingMethod, currentMethod );
			throw new RuntimeException( "Error while starting server", e );
		}
		catch ( NoSuchAlgorithmException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error with SSL key: " + e.getMessage(), callingMethod, currentMethod );
			throw new RuntimeException( "Error while starting server", e );
		}
		catch ( CertificateException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error with SSL key: " + e.getMessage(), callingMethod, currentMethod );
			throw new RuntimeException( "Error while starting server", e );
		}
		catch ( UnrecoverableKeyException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error with SSL key: " + e.getMessage(), callingMethod, currentMethod );
			throw new RuntimeException( "Error while starting server", e );
		}
		catch ( KeyManagementException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Error with SSL key: " + e.getMessage(), callingMethod, currentMethod );
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
						SSLSocket socket = ( SSLSocket ) SERVERSOCKET.accept();
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
		WORKERPOOL.submit( new HttpsWorker( socket, this ) );
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
