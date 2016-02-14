package com.cs.interfaces;

import com.cs.http.HttpStatusCode;

/**
 * An interface for HTTP responses.
 */
public interface HttpResponse extends HttpMessage
{
	/**
	 * Returns the HTTP Status Code of this response.
	 */
	HttpStatusCode getStatusCode();
}
