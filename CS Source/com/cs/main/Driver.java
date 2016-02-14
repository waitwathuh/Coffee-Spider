package com.cs.main;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.cs.server.HttpServer;
import com.cs.server.HttpsServer;

public class Driver
{
	public static void main( String[] args ) throws IOException, URISyntaxException
	{
		Config.Initialize( System.getProperty( "user.dir" ) + "/config.xml" );
		
		// Exit the program if the config file is not valid
		if ( Config.validateConfig() == false )
		{
			System.exit( 1 );
		}
		
		if ( Config.getServerType().toLowerCase().equals( "http" ) )
		{
			HttpServer httpServer = new HttpServer( Config.getServerPort() );
			httpServer.start();
			
			// Opens browser on the correct address and port number to view the index page
			openBrowser();
		}
		else if ( Config.getServerType().toLowerCase().equals( "https" ) )
		{
			HttpsServer httpsServer = new HttpsServer( Config.getServerPort() );
			httpsServer.start();
			
			// Opens browser on the correct address and port number to view the index page
			openBrowser();
		}
	}
	
	private static void openBrowser() throws IOException, URISyntaxException
	{
		Desktop.getDesktop().browse( new URI( Config.getServerType() +  "://localhost:" + Config.getServerPort() ) );
	}
}