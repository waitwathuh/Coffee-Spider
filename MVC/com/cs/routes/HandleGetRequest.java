package com.cs.routes;

import java.io.File;

import com.cs.http.BasicHttpResponse;
import com.cs.http.HttpStatusCode;
import com.cs.logs.Logger;
import com.cs.main.Config;

public class HandleGetRequest
{
	public static BasicHttpResponse processRequest( BasicHttpResponse request, String requestUri, String body )
	{
		switch ( requestUri )
		{
			case "/":
				request.setResourceAsBody( true );
				request.setResourceLocation( Config.getResourcePath() + "/index.html" );
				break;
			default:
				if ( requestUri.matches( "\\/assets\\/.{0,}" ) )
				{
					request.setResourceAsBody( true );
					request.setResourceLocation( Config.getResourcePath() + ( requestUri.replace( "/assets", "" ) ) );
				}
				else
				{
					if ( new File( Config.getResourcePath() + requestUri ).exists() )
					{
						request.setResourceAsBody( true );
						request.setResourceLocation( Config.getResourcePath() + requestUri );
					}
					else
					{
						StackTraceElement[] ste = Thread.currentThread().getStackTrace();
						String callingMethod = ste[ 2 ].getClassName() + "_" + ste[ 2 ].getMethodName() + "_" + ste[ 2 ].getLineNumber();
						String currentMethod = ste[ 1 ].getClassName() + "_" + ste[ 1 ].getMethodName() + "_" + ste[ 1 ].getLineNumber();
						
						Logger.writeLog( "WARNING", "There is no GET request for: " + requestUri, callingMethod, currentMethod );
						
						request.setResourceAsBody( true );
						request.setStatusCode( HttpStatusCode.NOT_FOUND );
						request.setResourceLocation( Config.getResourcePath() + "/404.html" );
					}
				}
				break;
		}
		
		return request;
	}
}