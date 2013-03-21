package de.cil2012.cilsearch.benchmark;

import java.util.Properties;

import de.cil2012.cilsearch.server.GlobalPropertyStore.SearchService;


public class SearchServiceConfiguration {
	private SearchService service = null;
	private Properties properties = null;
	
	private SearchServiceConfiguration(SearchService service, String domainName, 
			String addresses, String clusterName, String domainId, String region) {
		this.service = service;
		properties = new Properties();
		if (clusterName != null) properties.put("clusterName", clusterName);
		if (addresses != null) properties.put("addresses", addresses);
		if (domainName != null) properties.put("domainName", domainName);
		if (domainName != null) properties.put("CloudSearch_domainName", domainName);
		if (domainId != null) properties.put("CloudSearch_domainId", domainId);
		if (region != null) properties.put("CloudSearch_region", region);
	}
	
	
	public static SearchServiceConfiguration createCloudsearchConfiguration(
			String domainName, String domainId, String region) {
		SearchServiceConfiguration configuration = new SearchServiceConfiguration(
				SearchService.CLOUDSEARCH, domainName, null, null, domainId, region);
		return configuration;
	}
	
	public static SearchServiceConfiguration createElasticsearchConfiguration(
			String domainName, String addresses, String clusterName) {
		SearchServiceConfiguration configuration = new SearchServiceConfiguration(
				SearchService.ELASTICSEARCH, domainName, addresses, clusterName, null, null);
		return configuration;
	}
	
	public SearchService getService() {
		return service;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	@Override
	public String toString() {
		switch (service) {
		case CLOUDSEARCH:
			return "cs_" + properties.getProperty("CloudSearch_domainName");
		case ELASTICSEARCH:
			return "es_" + properties.getProperty("domainName");
		}
		return null; //never reached
	}
}