package com.cs.routes;

import com.cs.http.BasicHttpResponse;
import com.cs.http.HttpStatusCode;
import com.cs.logs.Logger;

public class HandlePostRequest
{
	public static BasicHttpResponse processRequest( BasicHttpResponse request, String requestUri, String body )
	{
		switch ( requestUri )
		{
			default:
				StackTraceElement[] ste = Thread.currentThread().getStackTrace();
				String callingMethod = ste[ 2 ].getClassName() + "_" + ste[ 2 ].getMethodName() + "_" + ste[ 2 ].getLineNumber();
				String currentMethod = ste[ 1 ].getClassName() + "_" + ste[ 1 ].getMethodName() + "_" + ste[ 1 ].getLineNumber();
				
				Logger.writeLog( "WARNING", "There is no POST request for: " + requestUri, callingMethod, currentMethod );
				request.setStatusCode( HttpStatusCode.BAD_REQUEST );
				break;
		}
		
		return request;
	}
}