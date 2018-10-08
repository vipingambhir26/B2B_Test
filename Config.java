//vipin

package com.uhc.uht.services.configuration;

/**
 *  *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
//vipin
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.uhc.uht.services.util.PpKey;
import com.uhc.uht.services.util.PropertiesPlus;
import com.uhg.prv.ServiceContext;
import com.uhg.prv.UHGApplicationException;
import com.uhg.prv.logging.B2BLogger;
import com.uhg.prv.logging.B2BLoggerUtil;
import com.uhg.prv.tools.B2BProps;
import com.uhg.tools.PropFileLoader;

public class Config {

	public static final String MASTER_CONFIG = "masterConfig";
	public static final String APP_DOMAIN_KEY = "appDomain";
	public static final String BASE_DIRECTORY_KEY = "baseConfigDirectory";
	public static final String APPDOMAIN_KEY_PREFIX = "appDomain.";
	public static final String GLOBAL_INITIALIZER = "globalInitializer";
	public static final String APP_INITIALIZER = "appInitializer";
	public static final String PROPERTIES_EXT = ".properties";
	public static final String XML_EXT = ".xml";
	private static final String DEFAULT_APP_DOMAIN_KEY = "";
	protected static HashMap s_domainConfMap = new HashMap();
	private static boolean s_successfulInit;
    private static B2BLogger logger = new B2BLogger(Config.class);
    private static boolean debug = logger.isDebugEnabled();
    private static PropertiesPlus messageExchangeProperties = null;
    private static final String MESSAGEEXCHANGE_PROPFILENAME = "MessageExchange";
    private static final String MAP_EXTENSION = "MAP_EXTENSION";
    private static String className = Config.class.getName();

	static {
		init();
		className = className.substring(className.lastIndexOf('.')+1);
	}

	protected Config() {
	}

	private static void init() throws ConfigurationException {
		
		try {
		s_successfulInit = false;
		//  String s = System.getProperties().getProperty("masterConfig");
		String s = B2BProps.getPropPath() + "master.properties";
		PropertiesPlus propertiesplus = loadFile("", s);
		s_domainConfMap.put("", propertiesplus);

		s_successfulInit = true;
		PropertiesPlus propertiesplus1 =
			(PropertiesPlus) s_domainConfMap.get("");
		String s1 = propertiesplus1.getProperty("globalInitializer");
		try {
			processInitializer(s1);
		} catch (ConfigurationException configurationexception) {
			s_successfulInit = false;
			throw configurationexception;
		}
		processMasterConfigObj(propertiesplus);
		messageExchangeProperties = getConfig(MESSAGEEXCHANGE_PROPFILENAME);
		} catch (Throwable th) {
    		System.out.println("");
    		System.out.println("============== FAILED to RUN INIT in Config.java ==============================");
    		System.out.println("");
			th.printStackTrace();
		}
		
	}

	private static void processMasterConfigObj(PropertiesPlus propertiesplus)
		throws ConfigurationException {
		PropertiesPlus propertiesplus1 =
			(PropertiesPlus) s_domainConfMap.get("");
		String s = B2BProps.getPropPath();
		for (Enumeration enumeration = propertiesplus.keys();
			enumeration.hasMoreElements();
			) {
			String s1 = ((PpKey) enumeration.nextElement()).getLocalName();
			int i = "appDomain.".length() - 1;
			String s2 = "appDomain.".substring(0, i);
			if (s1.equalsIgnoreCase(s2)) {
				PropertiesPlus propertiesplus2 = propertiesplus.getSection(s2);
				for (Enumeration enumeration1 = propertiesplus2.keys();
					enumeration1.hasMoreElements();
					) {
					String s4 =
						((PpKey) enumeration1.nextElement()).getLocalName();
					String s6 = propertiesplus2.getProperty(s4);
					PropertiesPlus propertiesplus4 = loadFile(s, s6);
					s_domainConfMap.put(s4, propertiesplus4);
					s_successfulInit = true;
					String s8 = propertiesplus4.getProperty("appInitializer");
					try {
						processInitializer(s8);
					} catch (ConfigurationException configurationexception1) {
						s_successfulInit = false;
						throw configurationexception1;
					}
				}

			} else if (
				s1.toLowerCase().startsWith("appDomain.".toLowerCase())) {
				String s3 = propertiesplus.getProperty(s1);
				PropertiesPlus propertiesplus3 = loadFile(s, s3);
				String s5 = s1.substring("appDomain.".length());
				s_domainConfMap.put(s5, propertiesplus3);
				s_successfulInit = true;
				String s7 = propertiesplus3.getProperty("appInitializer");
				try {
					processInitializer(s7);
				} catch (ConfigurationException configurationexception) {
					s_successfulInit = false;
					throw configurationexception;
				}
			}
		}

	}

	private static PropertiesPlus loadFile(String s, String s1)
		throws ConfigurationException {
		PropertiesPlus propertiesplus = new PropertiesPlus();
		BufferedInputStream bufferedinputstream = null;
		File file = null;
		try {
			file = new File(s, s1);
			if (!s_domainConfMap.isEmpty()) {
				PropertiesPlus propertiesplus1 =
					(PropertiesPlus) s_domainConfMap.get("");
				propertiesplus.putAll(propertiesplus1);
			}
			if (isPropsConfigFile(file)) {
				Properties props = PropFileLoader.loadProperties(s + s1);
				if (props != null && props.size()>0) {
					propertiesplus.putAll(new PropertiesPlus(props));
				}
				
			} else if (isXMLConfigFile(file)) {
				bufferedinputstream =
					new BufferedInputStream(new FileInputStream(file));
				propertiesplus.loadXML(bufferedinputstream);
			} else {
				String s2 =
					"Invalid configuration file type -- " + file.getName();
				throw new ConfigurationException(s2);
			}
		} catch (FileNotFoundException filenotfoundexception) {
			String s3 = file.getName() + " not found";
			throw new ConfigurationException(s3, filenotfoundexception);
		} catch (NullPointerException nullpointerexception) {
			String s4 = " Configuration file not found";
			throw new ConfigurationException(s4, nullpointerexception);
		} catch (IOException ioexception) {
			String s5 = "Error accessing configuration file!";
			throw new ConfigurationException(s5, ioexception);
		} catch (SAXException saxexception) {
			String s6 = "Syntax error in XML file -- invalid XML file";
			throw new ConfigurationException(s6, saxexception);
		} catch (ParserConfigurationException parserconfigurationexception) {
			String s7 = "JAXP configuration error.";
			throw new ConfigurationException(s7, parserconfigurationexception);
		} finally {
			if (bufferedinputstream != null)
				try {
					bufferedinputstream.close();
				} catch (IOException ioexception1) {
					String s8 = "Error closing configuration file!";
					throw new ConfigurationException(s8, ioexception1);
				}
		}
		return propertiesplus;
	}

	private static boolean isXMLConfigFile(File file) {
		String s = file.getName();
		return s.toLowerCase().endsWith(".xml");
	}

	private static boolean isPropsConfigFile(File file) {
		String s = file.getName();
		return s.toLowerCase().endsWith(".properties");
	}

	private static void processInitializer(String s)
		throws ConfigurationException {
		if (s != null)
			try {
				logger.debug("Reading initializer " + s);
				Class class1 = Class.forName(s);
			} catch (ClassNotFoundException classnotfoundexception) {
				String s1 = s + " not found!";
				throw new ConfigurationException(s1, classnotfoundexception);
			}
	}

	private static void checkInitialized() {
		if (!s_successfulInit) {
			String s = "Configuration has not been initialized!";
			throw new ConfigurationException(s);
		} else {
			return;
		}
	}

	public static PropertiesPlus getConfig(String s)
		throws ConfigurationException {
		checkInitialized();
		
		PropertiesPlus propertiesplus = (PropertiesPlus) s_domainConfMap.get(s);
		
		if (propertiesplus == null) {
			String s1 = s + " not found!";
			
			throw new ConfigurationException(s1);
		} else {
		
			return propertiesplus;
		}
	}

	public static PropertiesPlus getConfig() throws ConfigurationException {
		checkInitialized();
		return getConfig(getDomain());
	}

	public static String getDomain() throws ConfigurationException {
		String s = null;
		checkInitialized();
		try {
			InitialContext initialcontext = new InitialContext();
			s = (String) initialcontext.lookup("java:comp/env/appDomain");
		} catch (NamingException namingexception) {
			s = "";
		}
		return s;
	}
	
	public static String getMapFileName(String mapAlias) throws UHGApplicationException {
		String mapName = messageExchangeProperties.getProperty(mapAlias);
		if (mapName == null || mapName.trim().length() <= 0) {
			throw new UHGApplicationException(
					"1",
					"Missing map alias '" + mapAlias + "' in MessageExchange properties");
		}
		
		String mapExt = messageExchangeProperties.getProperty(MAP_EXTENSION);
		String filePrefix = B2BProps.getMercMapPath();
		if (debug) logger.debug("getMapFileName() - mapAlias '"+mapAlias+"' translated to map file name: " + filePrefix + mapName + mapExt);
		return filePrefix + mapName + mapExt;
	}
	
	public static String getMapFileName(String mapAlias, ServiceContext srvCntxt) throws UHGApplicationException {
		
		B2BLoggerUtil.debug(srvCntxt.getTypeOfRequest(), className + " getMapFileName(String mapAlias, ServiceContext srvCntxt) ", srvCntxt.getLoggingCorrelationID(), "start");
		String mapName = messageExchangeProperties.getProperty(mapAlias);
		if (mapName == null || mapName.trim().length() <= 0) {
			throw new UHGApplicationException(
					"1",
					"Missing map alias '" + mapAlias + "' in MessageExchange properties");
		}
		String mapExt = messageExchangeProperties.getProperty(MAP_EXTENSION);
		String filePrefix = B2BProps.getMercMapPath();
		B2BLoggerUtil.debug(srvCntxt.getTypeOfRequest(), className + " getMapFileName(String mapAlias, ServiceContext srvCntxt) ", srvCntxt.getLoggingCorrelationID(), "getMapFileName() - mapAlias '"+mapAlias+"' translated to map file name: " + filePrefix + mapName + mapExt);
		return filePrefix + mapName + mapExt;
	}

	
}
