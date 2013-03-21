package de.cil2012.cilsearch.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class GlobalPropertyStore {
	/**
	 * Enumeration of all possible search services.
	 */
	public enum SearchService {
		ELASTICSEARCH,
		CLOUDSEARCH
	}
	
	// change here if you want to use AWS cloudsearch or an elasticSearch cluster
	private static SearchService activeService = SearchService.CLOUDSEARCH; 
	private static Properties activeServiceProperties = null;
	
	/**
	 * Get the search service to use.
	 * @return the search service.
	 */
	public static SearchService getActiveService() {
		return activeService;
	}
	
	/**
	 * Set the search service to use.
	 * @param activeService the search service.
	 */
	public static void setActiveService(SearchService activeService) {
		GlobalPropertyStore.activeService = activeService;
	}
	
	/**
	 * Get the configuration properties of the active service.
	 * @return the configuration properties.
	 */
	public static Properties getActiveServiceProperties() {
		// if properties are null, they are loaded from the configuration
		if (activeServiceProperties == null) {
			activeServiceProperties = getServiceProperties(GlobalPropertyStore.activeService);
		}
		
		return activeServiceProperties;
	}
	
	/**
	 * Set the configuration properties of the active service.
	 * Thereby the configuration file is ignored. This could
	 * for example be used to benchmark different services without
	 * restarting the application everytime.
	 * 
	 * Set the activeService to null to use the configuration 
	 * specified in the configuration file.
	 * 
	 * @param activeServiceProperties the properties to used.
	 */
	public static void setActiveServiceProperties(
			Properties activeServiceProperties) {
		GlobalPropertyStore.activeServiceProperties = activeServiceProperties;
	}
	
	/**
	 * Get the configuration for the specified service from the
	 * corresponding configuration file. The results of this method
	 * are not influenced by setActiveServiceProperties.
	 * @param activeService the specified service.
	 * @return the properties of this service.
	 */
	public static Properties getServiceProperties(SearchService activeService) {
		Properties serviceProperties = new Properties();
		InputStream in = null;
		
		switch (activeService) {
		case ELASTICSEARCH:
			in = GlobalPropertyStore.class.getClassLoader().getResourceAsStream("server_resources/ESdata.properties");
			break;
		case CLOUDSEARCH:
			in = GlobalPropertyStore.class.getClassLoader().getResourceAsStream("server_resources/AwsData.properties");
			break;
		}

		try {
			serviceProperties.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return serviceProperties;
	}
}
