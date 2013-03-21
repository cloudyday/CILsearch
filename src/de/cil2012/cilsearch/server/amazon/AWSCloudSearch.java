package de.cil2012.cilsearch.server.amazon;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudsearch.AmazonCloudSearchClient;
import com.searchtechnologies.cloudsearch.api.CloudSearchClient;

import de.cil2012.cilsearch.server.amazon.cloudsearch.AWSCloudSearchClient;

/**
 * This class is used to connect with the AWS cloud search services through various API's
 * The DOCUMENT service uses a self-made API
 * The CONFIGURATION service uses the AWS SDK
 * The SERACH service uses the SearchTechnologies.com Library
 * 
 *  Use the functions getDocumentServiceClientInstance(), getConfigurationServiceClientInstance(), getSearchServiceClientInstance()
 *  to obtain an instance of these services
 */
public abstract class AWSCloudSearch {
	
	private final static String API_VERSION = "2011-02-01";
	
	
	public static AWSCloudSearchClient getDocumentServiceClientInstance(String domainName, String domainId, String region) {
		return new AWSCloudSearchClient(API_VERSION, domainName, domainId, region);
	}
	
	public static AmazonCloudSearchClient getConfigurationServiceClientInstance() throws IOException {
		InputStream in = AWSCloudSearch.class.getClassLoader().getResourceAsStream("server_resources/AwsCredentials.properties");
		PropertiesCredentials credentials = new PropertiesCredentials(in);

		return new AmazonCloudSearchClient(credentials);
	}
	
	public static CloudSearchClient getSearchServiceClientInstance(String domainName, String domainId, String region) {
		return new CloudSearchClient("search-" + domainName + "-"
				+ domainId + "." + region + ".cloudsearch.amazonaws.com");
	}


	public String getAPI_VERSION() {
		return API_VERSION;
	}
	
	
	

}
