package de.cil2012.cilsearch.server.amazon.cloudsearch;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * This class implements the REST interface for the DOCUMENT service of Amazon Cloud Search.
 * There is only one endpoint to implement: documents/batch
 */

public class AWSCloudSearchClient {

	private String API_VERSION;
	private String domainName;
	private String domainId;
	private String region;

	private Client client;

	/**
	 * 
	 * @param API_VERSION the current API_VERSION of the cloud search service. needed for the REST service URL
	 * @param domainName the name of your search domain
	 * @param domainId the id of your search domain
	 * @param region the region where your search domain is hosted (currently only us-east-1)
	 */
	public AWSCloudSearchClient(String API_VERSION, String domainName, String domainId,
			String region) {

		this.API_VERSION = API_VERSION;
		this.domainName = domainName;
		this.domainId = domainId;
		this.region = region;

		this.client = Client.create();
	}

	
	/**
	 * Submits a request to add / remove items from the index
	 * 
	 * @param request ChangeIndexRequest object
	 * @throws AWSChangeIndexException if anything goes wrong with the HTTP call, this exception will be thrown
	 */
	public void changeIndex(ChangeIndexRequest request) throws AWSChangeIndexException {

		WebResource resource = client.resource("http://doc-" + domainName + "-"
				+ domainId + "." + region + ".cloudsearch.amazonaws.com/"
				+ API_VERSION + "/documents/batch");
		ClientResponse response = null;
		String sdf = request.toSDF();
		
		try {
			response = resource.accept(MediaType.APPLICATION_XML_TYPE)
					.type(MediaType.APPLICATION_JSON_TYPE+ ";charset=utf-8")
					.post(ClientResponse.class, sdf.getBytes("utf-8"));
		} catch (UniformInterfaceException e) {
			throw new AWSChangeIndexException(e);
		} catch (ClientHandlerException e) {
			throw new AWSChangeIndexException(e);
		} catch (UnsupportedEncodingException e) {
			throw new AWSChangeIndexException(e);
		}

		//System.out.println(response.getStatus());
		if(response.getStatus() != 200) {
			throw new AWSChangeIndexException(response.getEntity(String.class));
		}
		//System.out.println(response.getEntity(String.class));

	}
}
