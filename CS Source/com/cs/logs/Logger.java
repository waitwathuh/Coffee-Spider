package com.cs.logs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cs.main.Config;

public class Logger
{
	public static void writeLog( String logType, String message, String callingTrace, String currentTrace )
	{
		String formatMessage = "";
		
		if ( logType.toUpperCase().equals( "LOG" ) && Config.getLogLevel().contains( "1" ) )
		{
			formatMessage = formatLogString( logType, message, callingTrace, currentTrace );
		}
		else if ( logType.toUpperCase().equals( "WARNING" ) && Config.getLogLevel().contains( "2" ) )
		{
			formatMessage = formatLogString( logType, message, callingTrace, currentTrace );
		}
		else if ( logType.toUpperCase().equals( "ERROR" ) && Config.getLogLevel().contains( "3" ) )
		{
			formatMessage = formatLogString( logType, message, callingTrace, currentTrace );
		}
		else
		{
			System.out.println( "Unable to write the log. \n\t Config log level: " + Config.getLogLevel() + ", Log type: " + logType.toUpperCase() );
			System.exit( 3 );
		}
		
		WriteToLog( formatMessage );
	}

	// Log everything
	private static String formatLogString( String logType, String message, String callingTrace, String currentTrace )
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat( Config.getLogDateFormat() );
		Date now = new Date();
		String newLineSpace = "\n\t\t";
		
		String logLine = sdfDate.format( now ) + "\t[" + logType.toUpperCase() + "]";
		
		logLine += newLineSpace + callingTrace;
		logLine += newLineSpace + currentTrace;
		logLine += newLineSpace + message;
		
		return logLine;
	}
	
	private static void WriteToLog( String data )
	{
		PrintWriter out = null;

		try
		{
			String filePath = Config.getLogPath() + System.getProperty( "file.separator" ) + Config.getLogFileName();
			out = new PrintWriter( new BufferedWriter( new FileWriter( filePath, true ) ) );
			out.println( data );
		}
		catch ( IOException e )
		{
			System.out.println( "ERROR: IO Exception." );
			System.out.println( "\t" + e.getMessage() );
			System.exit( 2 );
		}
		finally
		{
			if ( out != null )
			{
				out.close();
			}
		}
	}
}