package com.cs.interfaces;

import com.cs.http.HttpMethod;

/**
 * An interface for HTTP requests.
 */
public interface HttpRequest extends HttpMessage
{
	/**
	 * Returns the HTTP method of this request.
	 */
	HttpMethod getHttpMethod();

	/**
	 * Returns the request URI of this request.
	 */
	String getRequestUri();
}
