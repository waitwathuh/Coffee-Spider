package com.cs.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config
{
	// Web Server
	private static String serverType;
	private static String serverPort;
	private static String keyFile;
	private static String password1;
	private static String password2;
	private static boolean	folderCreation = true;
	private static boolean	defaultWebSettings = false;
	
	// Log files
	private static String logPath;
	private static String logFile;
	private static String logDateFormat;
	private static String logLevel;
	private static boolean	defaultLogSettings = false;
	
	// Web resources
	private static String resourcePath;
	
	// Database settings
	private static String dbIP;
	private static String dbPort;
	private static String dbName;
	
	/**
	 * Initialize the config controller.
	 * 
	 * @param confLoc Full path to the config file
	 */
	public static void Initialize( String confLoc )
	{
		readConfig( confLoc );
		processDefaults();
		setLogFileName();
		createFolders();
	}
	
	/**
	 * Validates the config file.
	 * <br><br>
	 * The function will return false if there is an error in the config file
	 * and true if the config file is valid.
	 * 
	 * @return Boolean value whether the config file is valid or not.
	 */
	public static boolean validateConfig()
	{
		if ( serverType.toLowerCase().equals( "http" ) == false && serverType.toLowerCase().equals( "https" ) == false )
		{
			System.out.println( "ERROR: Invalid server type. \n\t Only http and https is accepted values." );
			return false;
		}
		
		try
		{
			if ( Integer.parseInt( serverPort ) < 1 )
			{
				System.out.println( "ERROR: Invalid server port. \n\t Port numbers can not be less than 1." );
				return false;
			}
			else if ( Integer.parseInt( serverPort ) > 65535 )
			{
				System.out.println( "ERROR: Invalid server port. \n\t Port numbers can not be more than 65,535." );
				return false;
			}
		}
		catch ( NumberFormatException e )
		{
			System.out.println( "ERROR: Number Format Exception." );
			System.out.println( "\t" + e.getMessage() );
			System.exit( 3 );
		}
		
		if ( serverType.toLowerCase().equals( "https" ) && keyFile.equals( "" ) || keyFile == null )
		{
			System.out.println( "ERROR: Invalid HTTPS keyfile location. \n\t A full system path is needed for the HTTPS Certificate location." );
			return false;
		}
		
		if ( serverType.toLowerCase().equals( "https" ) && password1.equals( "" ) || password1 == null )
		{
			System.out.println( "ERROR: Invalid keystore password. \n\t A first password needs to be specified for the keystore file." );
			return false;
		}
		
		if ( serverType.toLowerCase().equals( "https" ) && password2.equals( "" ) || password2 == null )
		{
			System.out.println( "ERROR: Invalid keystore password. \n\t A second password needs to be specified for the keystore file." );
			return false;
		}
		
		if ( logPath == null || logPath.equals( "" ) )
		{
			System.out.println( "ERROR: Invalid system log path. \n\t A full system path is needed for system logs to be created." );
			return false;
		}
		else if ( new File(logPath).exists() == false )
		{
			System.out.println( "ERROR: Invalid system log path. \n\t The folder logs will be saved in does not exist." );
			return false;
		}
		
		if ( logDateFormat == null || logDateFormat.equals( "" ) )
		{
			System.out.println( "ERROR: Invalid log date format. \n\t A valid date format is needed to dateTime stamp the system logs." );
			return false;
		}
		
		if ( logLevel.contains( "1" ) == false && logLevel.contains( "2" ) == false && logLevel.contains( "3" ) == false )
		{
			System.out.println( "ERROR: Invalid log level. \n\t Valid log level values are only '1', '2' and '3'. Or any combanation of these numbers." );
			return false;
		}
		
		if ( resourcePath == null || resourcePath.equals( "" ) )
		{
			System.out.println( "ERROR: Invalid resource path. \n\t A valid path is needed for the web resources." );
			return false;
		}
		
		return true;
	}
	
	/**
	 * Read the config file. 
	 * 
	 * @param confLoc Full path to config file
	 */
	private static void readConfig( String confLoc )
	{
		try
		{
			File fXmlFile = new File( confLoc );
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse( fXmlFile );
			doc.getDocumentElement().normalize();

			processWebSettings( doc );
			processLogSettings( doc );
			processResourceSettings( doc );
			processDatabaseSettings( doc );
		}
		catch ( ParserConfigurationException e )
		{
			System.out.println( "ERROR: Parser Configuration Exception." );
			System.out.println( e.getMessage() );
			System.exit( 2 );
		}
		catch ( SAXException e )
		{
			System.out.println( "ERROR: SAX Exception." );
			System.out.println( e.getMessage() );
			System.exit( 2 );
		}
		catch ( IOException e )
		{
			System.out.println( "ERROR: IO Exception." );
			System.out.println( e.getMessage() );
			System.exit( 2 );
		}
	}
	
	/**
	 * Use default settings for components specified in config file.
	 */
	private static void processDefaults()
	{
		if ( defaultWebSettings == true )
		{
			serverType = "http";
			serverPort = "80";
			folderCreation = true;
		}
		
		if ( defaultLogSettings == true )
		{
			logPath = System.getProperty( "user.dir" ) + System.getProperty( "file.separator" ) + "logs";
			logDateFormat = "yyyy-MM-dd HH.mm.ss";
		}
		
		if ( logLevel.length() < 1 )
		{
			logLevel = "123";
		}
	}
	
	/**
	 * This method creates a log file name using the 'dateFormat' in the config.xml
	 * 
	 * @see https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
	 */
	private static void setLogFileName()
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat( logDateFormat );
		Date now = new Date();
	    String strDate = sdfDate.format(now);
		
		logFile = strDate + ".log";
	}
	
	/**
	 * Creates folders if they don't exist that is specified as application folders
	 */
	private static void createFolders()
	{
		if ( folderCreation == true )
		{
			new File( logPath ).mkdir();
			new File( resourcePath ).mkdir();
		}
	}
	
	
	
	/**
	 * Refactors paths to allow keywords to be used in the config file.
	 * <br>
	 * <br>
	 * This method only allows the keyword "curdir" to be translated to the
	 * working directory of this application. The keyword is NOT case sensitive. 
	 * 
	 * @param line Path string that needs refactoring.
	 * @return Refactored path
	 */
	private static String refactorConfigPathString( String line )
	{
		if ( line.toLowerCase().startsWith( "curdir" ) )
		{
			String curDir = line.substring( 0, 6 );
			return line.replace( curDir, System.getProperty( "user.dir" ) );
		}
		else
		{
			return line;
		}
	}
	
	/**
	 * Reads and returns a XML element string value.
	 * 
	 * @param eElement XML element.
	 * @param tagName Tag name as is specified in the config file.
	 * @param itemNumber Element number based on tag name.
	 * @return String found in the XML based on the tag name and item number.
	 */
	private static String getXmlValue( Element eElement, String tagName, int itemNumber )
	{
		try
		{
			return eElement.getElementsByTagName( tagName ).item( itemNumber ).getTextContent();
		}
		catch ( Exception ex )
		{
			// There is no value in the config file. Return an empty string
			return "";
		}
	}
	
	/**
	 * Process the web settings section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private static void processWebSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "web" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;
			
			serverType = getXmlValue( eElement, "type", 0 );
			serverPort = getXmlValue( eElement, "port", 0 );
			keyFile = refactorConfigPathString( getXmlValue( eElement, "keyfile", 0 ) );
			password1 = getXmlValue( eElement, "password1", 0 );
			password2 = getXmlValue( eElement, "password2", 0 );
			folderCreation = Boolean.parseBoolean( getXmlValue( eElement, "createFolders", 0 ) );
			defaultWebSettings = Boolean.parseBoolean( getXmlValue( eElement, "defaultSettings", 0 ) );
		}
	}
	
	/**
	 * Process the log settings section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private static void processLogSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "log" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;

			logPath = refactorConfigPathString( getXmlValue( eElement, "path", 0 ) );
			logDateFormat = getXmlValue( eElement, "dateFormat", 0 );
			logLevel = getXmlValue( eElement, "logLevel", 0 );
			defaultLogSettings = Boolean.parseBoolean( getXmlValue( eElement, "defaultSettings", 0 ) );
		}
	}
	
	/**
	 * Process the web recourse section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private static void processResourceSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "recourse" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;

			resourcePath = refactorConfigPathString( getXmlValue( eElement, "path", 0 ) );
		}
	}
	
	/**
	 * Process the database section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private static void processDatabaseSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "database" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;
			
			dbIP = getXmlValue( eElement, "ip", 0 );
			dbPort = getXmlValue( eElement, "port", 0 );
			dbName = getXmlValue( eElement, "dbName", 0 );
		}
	}
	
	
	
	// Public get methods
	public static String getServerType()
	{
		return serverType;
	}
	
	public static int getServerPort()
	{
		return Integer.parseInt( serverPort );
	}
	
	public static String keyFilePath()
	{
		return keyFile;
	}
	
	public static String keyStorePassword1()
	{
		return password1;
	}
	
	public static String keyStorePassword2()
	{
		return password2;
	}
	
	public static String getLogPath()
	{
		return logPath;
	}
	
	public static void setLogFileName( String fileName )
	{
		logFile = fileName;
	}
	
	public static String getLogFileName()
	{
		return logFile;
	}
	
	public static String getLogDateFormat()
	{
		return logDateFormat;
	}
	
	public static String getLogLevel()
	{
		return logLevel;
	}
	
	public static String getResourcePath()
	{
		return resourcePath;
	}
	
	public static String getDbIP()
	{
		return dbIP;
	}
	
	public static String getDbPort()
	{
		return dbPort;
	}
	
	public static String getDbName()
	{
		return dbName;
	}
}