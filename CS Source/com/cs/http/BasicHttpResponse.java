package com.cs.http;

import com.cs.interfaces.HttpResponse;

public class BasicHttpResponse extends BasicHttpMessage implements HttpResponse
{
	HttpStatusCode statusCode;
	boolean resourceAsBody = false;
	String resourceLocation;

	@Override
	public HttpStatusCode getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode( HttpStatusCode statusCode )
	{
		this.statusCode = statusCode;
	}
	
	public void setResourceAsBody( boolean value )
	{
		resourceAsBody = value;
	}
	
	public boolean getResourceAsBody()
	{
		return resourceAsBody;
	}
	
	public String getResourceLocation()
	{
		return resourceLocation;
	}
	
	public void setResourceLocation( String location )
	{
		resourceLocation = location;
	}
}
