package com.cs.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.cs.http.BasicHttpRequest;
import com.cs.http.BasicHttpResponse;
import com.cs.http.Http;
import com.cs.http.HttpMethod;
import com.cs.http.HttpStatusCode;
import com.cs.http.HttpVersion;
import com.cs.interfaces.HttpRequest;
import com.cs.interfaces.HttpResponse;
import com.cs.logs.Logger;
import com.cs.routes.HandleGetRequest;
import com.cs.routes.HandlePostRequest;

/**
 * Apache Commons IO library is needed in this class to get file extensions.
 * 
 * @see https://commons.apache.org/
 */
public class HttpWorker implements Callable<Void>
{
	private final Socket SOCKET;
	private final HttpServer SERVER;
	
	/**
	 * Creates a new worker that handles the incoming request.
	 * 
	 * @param socket The socket this request is sent over.
	 * @param server A reference to the core server instance.
	 */
	public HttpWorker(Socket socket, HttpServer server)
	{
		this.SOCKET = socket;
		this.SERVER = server;
	}
	
	@Override
	public Void call() throws Exception
	{
		// Parse request from InputStream
		HttpRequest request = parseRequest(SOCKET.getInputStream());
		HttpResponse response = null;
		
		switch ( request.getHttpMethod() )
		{
			case GET:
				response = processGetRequest( request );
				break;
			case POST:
				response = processPostRequest( request );
				break;
			case OPTIONS:
				response = processOptionsRequest( request );
				break;
			default:
				break;
		}
		
		// The following headers is to allow cross-domain connections
		response.getHeaders().put( "Access-Control-Allow-Origin", "*" );
		response.getHeaders().put( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
		
		// Send response and close connection, if necessary
		if (keepAlive(request, response))
		{
			sendResponse(response, SOCKET.getOutputStream());
			SERVER.dispatchRequest(SOCKET);
		}
		else
		{
			response.getHeaders().put("Connection", "close");
			sendResponse(response, SOCKET.getOutputStream());
			SOCKET.close();
		}
		
		// We do not return anything here.
		return null;
	}
	
	/**
	 * A helper method that reads an InputStream until it reads a CRLF (\r\n\).
	 * Everything in front of the linefeed occured is returned as String.
	 * 
	 * @param inputStream The stream to read from.
	 * @return The character sequence in front of the linefeed.
	 * @throws IOException
	 */
	protected String readLine(InputStream inputStream) throws IOException
	{
		StringBuffer result = new StringBuffer();
		boolean crRead = false;
		int n;
		while ((n = inputStream.read()) != -1)
		{
			if (n == '\r')
			{
				crRead = true;
				continue;
			}
			else if (n == '\n' && crRead)
			{
				return result.toString();
			}
			else
			{
				result.append((char) n);
			}
		}
		return result.toString();
	}
	
	/**
	 * Creates an appropriate {@link HttpResponse} to the given {@link HttpRequest}. Note however, that this method
	 * is not yet sending the response.
	 * 
	 * @param request The {@link HttpRequest} that must be handled.
	 * @return
	 */
	protected HttpRequest parseRequest(InputStream inputStream) throws IOException
	{
		String firstLine = readLine(inputStream);

		BasicHttpRequest request = new BasicHttpRequest();

		request.setVersion(HttpVersion.extractVersion(firstLine));
		request.setRequestUri(firstLine.split(" ", 3)[1]);
		request.setMethod(HttpMethod.extractMethod(firstLine));

		Map<String, String> headers = new HashMap<String, String>();

		String nextLine = "";
		while (!(nextLine = readLine(inputStream)).equals(""))
		{
			String values[] = nextLine.split(":", 2);
			headers.put(values[0], values[1].trim());
		}
		request.setHeaders(headers);

		if (headers.containsKey(Http.CONTENT_LENGTH))
		{
			int size = Integer.parseInt(headers.get(Http.CONTENT_LENGTH));
			byte[] data = new byte[size];
			int n;
			for (int i = 0; i < size && (n = inputStream.read()) != -1; i++)
			{
				data[i] = (byte) n;
			}
			request.setEntity(data);
		}
		else
		{
			request.setEntity(null);
		}

		return request;
	}
	
	/**
	 * Sends a given {@link HttpResponse} over the given {@link OutputStream}.
	 * 
	 * @param response
	 * @param outputStream
	 * @throws IOException
	 */
	protected void sendResponse(HttpResponse response, OutputStream outputStream) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

		writer.write(response.getHttpVersion().toString());
		writer.write(' ');
		writer.write("" + response.getStatusCode().getCode());
		writer.write(' ');
		writer.write(response.getStatusCode().getReasonPhrase());
		writer.write(Http.CRLF);

		if (response.getEntity() != null && response.getEntity().length > 0)
		{
			response.getHeaders().put(Http.CONTENT_LENGTH, "" + response.getEntity().length);
		}
		else
		{
			response.getHeaders().put(Http.CONTENT_LENGTH, "" + 0);
		}

		if (response.getHeaders() != null)
		{
			for (String key : response.getHeaders().keySet())
			{
				writer.write(key + ": " + response.getHeaders().get(key) + Http.CRLF);
			}
		}
		writer.write(Http.CRLF);
		writer.flush();

		if (response.getEntity() != null && response.getEntity().length > 0)
		{
			outputStream.write(response.getEntity());
		}
		outputStream.flush();

	}
	
	/**
	 * Determines whether a connection should be kept alive or not on
	 * server-side. This decision is made based upon the given (
	 * {@link HttpRequest}, {@link HttpResponse}) couple, respectively their
	 * header values.
	 * 
	 * @param request
	 * @param response
	 * @return true, if the server should keep open the connection, otherwise
	 *         false.
	 */
	protected boolean keepAlive(HttpRequest request, HttpResponse response)
	{
		if (response.getHeaders().containsKey(Http.CONNECTION) && response.getHeaders().get(Http.CONNECTION).equalsIgnoreCase("close"))
		{
			return false;
		}
		if (request.getHttpVersion().equals(HttpVersion.VERSION_1_1))
		{
			if (request.getHeaders().containsKey(Http.CONNECTION) && request.getHeaders().get(Http.CONNECTION).equalsIgnoreCase("close"))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	protected HttpResponse processGetRequest( HttpRequest request )
	{
		BasicHttpResponse response = new BasicHttpResponse();
		response.setHeaders( new HashMap< String, String >() );
		response.getHeaders().put( Http.SERVER, HttpServer.getServerSignature() );
		response.setVersion( request.getHttpVersion() );
		
		String bodyAsString = "";
		byte[] bodyAsBytes = request.getEntity();
		
		try
		{
			if ( bodyAsBytes != null )
			{
				bodyAsString = new String( bodyAsBytes, "UTF-8" );
			}
		}
		catch ( UnsupportedEncodingException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Unable to set encoding: " + e.getMessage(), callingMethod, currentMethod );
		}
		
		response = HandleGetRequest.processRequest( response, request.getRequestUri(), bodyAsString );
		
		if ( response.getResourceAsBody() && ( response.getResourceLocation().isEmpty() == false ) && ( response.getResourceLocation() != null ) )
		{
			File f = new File( response.getResourceLocation() );
			
			if ( f.exists() )
			{
				response.setStatusCode( HttpStatusCode.OK );
				InputStream inputStream;
				try
				{
					inputStream = new FileInputStream( f );
					byte fileContent[] = new byte[ ( int ) f.length() ];
					inputStream.read( fileContent );
					inputStream.close();
					response.setEntity( fileContent );

					// guess and set the content type
					String extention = getExtension( f.getPath() );
					String httpContentType = Http.getContentType( extention );
					response.getHeaders().put( Http.CONTENT_TYPE, httpContentType );
				}
				catch ( FileNotFoundException e )
				{
					response.setStatusCode( HttpStatusCode.NOT_FOUND );
					
					StackTraceElement[] ste = Thread.currentThread().getStackTrace();
					String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
					String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
					
					Logger.writeLog( "WARNING", "Unable to find resource: " + e.getMessage(), callingMethod, currentMethod );
				}
				catch ( IOException e )
				{
					response.setStatusCode( HttpStatusCode.INTERNAL_SERVER_ERROR );
					
					StackTraceElement[] ste = Thread.currentThread().getStackTrace();
					String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
					String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
					
					Logger.writeLog( "ERROR", "Unable to read resource: " + e.getMessage(), callingMethod, currentMethod );
				}
			}
			else
			{
				response.setStatusCode( HttpStatusCode.NOT_FOUND );
			}
		}
		
		return response;
	}
	
	protected HttpResponse processPostRequest( HttpRequest request )
	{
		BasicHttpResponse response = new BasicHttpResponse();
		response.setHeaders( new HashMap< String, String >() );
		response.getHeaders().put( Http.SERVER, HttpServer.getServerSignature() );
		response.setVersion( request.getHttpVersion() );
		
		String bodyAsString = "";
		byte[] bodyAsBytes = request.getEntity();
		
		try
		{
			if ( bodyAsBytes != null )
			{
				bodyAsString = new String( bodyAsBytes, "UTF-8" );
			}
		}
		catch ( UnsupportedEncodingException e )
		{
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
			String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
			
			Logger.writeLog( "ERROR", "Unable to set encoding: " + e.getMessage(), callingMethod, currentMethod );
		}
		
		response = HandlePostRequest.processRequest( response, request.getRequestUri(), bodyAsString );
		
		if ( response.getResourceAsBody() && ( response.getResourceLocation().isEmpty() == false ) && ( response.getResourceLocation() != null ) )
		{
			File f = new File( response.getResourceLocation() );
			
			if ( f.exists() )
			{
				response.setStatusCode( HttpStatusCode.OK );
				InputStream inputStream;
				try
				{
					inputStream = new FileInputStream( f );
					byte fileContent[] = new byte[ ( int ) f.length() ];
					inputStream.read( fileContent );
					inputStream.close();
					response.setEntity( fileContent );

					// guess and set the content type
					String extention = getExtension( f.getPath() );
					String httpContentType = Http.getContentType( extention );
					response.getHeaders().put( Http.CONTENT_TYPE, httpContentType );
				}
				catch ( FileNotFoundException e )
				{
					response.setStatusCode( HttpStatusCode.NOT_FOUND );
					
					StackTraceElement[] ste = Thread.currentThread().getStackTrace();
					String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
					String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
					
					Logger.writeLog( "WARNING", "Unable to find resource: " + e.getMessage(), callingMethod, currentMethod );
				}
				catch ( IOException e )
				{
					response.setStatusCode( HttpStatusCode.INTERNAL_SERVER_ERROR );
					
					StackTraceElement[] ste = Thread.currentThread().getStackTrace();
					String callingMethod = ste[2].getClassName() + "_" + ste[2].getMethodName() + "_" + ste[2].getLineNumber();
					String currentMethod = ste[1].getClassName() + "_" + ste[1].getMethodName() + "_" + ste[1].getLineNumber();
					
					Logger.writeLog( "ERROR", "Unable to read resource: " + e.getMessage(), callingMethod, currentMethod );
				}
			}
			else
			{
				response.setStatusCode( HttpStatusCode.NOT_FOUND );
			}
		}
		
		return response;
	}
	
	/**
	 * Options request only needs to be taken into account when making use of external API's
	 * 
	 * @param request HttpRequest from the client
	 * @see Cross-Origin Resource Sharing (CORS)
	 */
	protected HttpResponse processOptionsRequest( HttpRequest request )
	{
		BasicHttpResponse responses = new BasicHttpResponse();
		responses.setHeaders( new HashMap< String, String >() );
		responses.getHeaders().put( Http.SERVER, HttpServer.getServerSignature() );
		responses.setVersion( request.getHttpVersion() );
		responses.setStatusCode( HttpStatusCode.OK );
		
		return responses;
	}
	
	protected String getExtension( String fName )
	{
		int extStart = fName.lastIndexOf( "." ) + 1;
		String fileExtention = fName.substring( extStart );
		
		return fileExtention;
	}
}