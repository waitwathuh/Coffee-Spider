package com.cs.interfaces;

import java.util.Map;

import com.cs.http.HttpVersion;

/**
 * An interface for basic HTTP message operations.
 */
public interface HttpMessage
{
	/**
	 * Returns the used protocol version of this message.
	 */
	HttpVersion getHttpVersion();

	/**
	 * Returns a list of key-value header fields.
	 */
	Map<String, String> getHeaders();

	/**
	 * Returns the entity as a byte array or null, if no entity is available.
	 */
	byte[] getEntity();

}
