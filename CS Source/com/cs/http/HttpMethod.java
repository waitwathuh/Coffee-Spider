package com.cs.http;

/**
 * An enum of available HTTP methods.
 */
public enum HttpMethod
{
	HEAD,
	GET,
	POST,
	PUT,
	DELETE,
	OPTIONS;

	@Override
	public String toString()
	{
		return this.name();
	}

	public static HttpMethod extractMethod( String headerLine ) throws IllegalArgumentException
	{
		String method = headerLine.split( " " )[ 0 ];
		if ( method != null )
		{
			return HttpMethod.valueOf( method );
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
}
