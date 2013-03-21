package de.cil2012.cilsearch.server.amazon;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;

import com.allen_sauer.gwt.log.client.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudsearch.AmazonCloudSearchClient;
import com.amazonaws.services.cloudsearch.model.CreateDomainRequest;
import com.amazonaws.services.cloudsearch.model.DefineIndexFieldRequest;
import com.amazonaws.services.cloudsearch.model.DescribeIndexFieldsRequest;
import com.amazonaws.services.cloudsearch.model.DescribeIndexFieldsResult;
import com.amazonaws.services.cloudsearch.model.IndexDocumentsRequest;
import com.amazonaws.services.cloudsearch.model.IndexField;
import com.amazonaws.services.cloudsearch.model.IndexFieldStatus;
import com.amazonaws.services.cloudsearch.model.IndexFieldType;
import com.amazonaws.services.cloudsearch.model.LiteralOptions;
import com.amazonaws.services.cloudsearch.model.SourceAttribute;
import com.amazonaws.services.cloudsearch.model.SourceData;
import com.amazonaws.services.cloudsearch.model.SourceDataFunction;
import com.amazonaws.services.cloudsearch.model.TextOptions;
import com.amazonaws.services.cloudsearch.model.UpdateServiceAccessPoliciesRequest;



/**
 * This class is used to setup an Amazon CloudSearch search domain
 */
public class AutoCloudSearchInstance {
	
	// fill in the account id of your aws account 
	private final String awsAccountId = ""; 
	private AmazonCloudSearchClient client;
	private String domainName;
	private String region;
	
	/**
	 * Creates an instance of this class with default AWS settings for region and endpoint
	 * 
	 * @param domainName
	 * @throws IOException
	 */
	public AutoCloudSearchInstance(String domainName) throws IOException {
		if(domainName == null) {
			throw new NullArgumentException("domainName");
		}
		
		client = AWSCloudSearch.getConfigurationServiceClientInstance();
		this.domainName = domainName;
		this.region = "us-east-1";
	}
	
	/**
	 * Creates an instance of this class with specific AWS settings for region and endpoint
	 * 
	 * @param region
	 * @param domainName
	 * @param endPoint
	 * @throws IOException
	 */
	public AutoCloudSearchInstance(String region, String domainName, 
			String endPoint) throws IOException{
		/**
		 * endPoint, serviceName, region by default:
		 * "cloudsearch.us-east-1.amazonaws.com", "cloudsearch", "us-east-1"
		 */
		if(domainName == null) {
			throw new NullArgumentException("domainName");
		}
		if(! domainName.matches("[a-z][a-z0-9-]+") && domainName.length() >= 28 && domainName.length() < 3){
			throw new IllegalArgumentException("domainName: must match RegEx [a-z][a-z0-9-]+ & length between 3-27 characters");
		}
		if(! region.equals("us-east-1")) {
			throw new IllegalArgumentException("region: only 'us-east-1' is allowed");
		}
		
		client = AWSCloudSearch.getConfigurationServiceClientInstance();
		client.setEndpoint(endPoint, "cloudsearch", region);
		this.domainName = domainName;
	}
	
	
	/**
	 * Creates AWS CloudSearch Instance with predefined Index Fields
	 * 
	 * @param domainName should be created from Mail Account Name
	 * @throws IOException AWS Credentials
	 */
	public void setUpSearchInstance() {
		
		//Parameter for createDomain Method: Container for necessary Parametes to exec CreateDomain service
		CreateDomainRequest request = new CreateDomainRequest(); 
		
		//see method below - param should be account name
		//String name = this.createDomainName();
		request.setDomainName(domainName);
		
		/**AmazonCloudSearchClient createDomain(CDR request):CreateDomainResult
		 * A response message that contains the status of a newly created domain.
		 */
		
		client.createDomain(request);

		// retrieve all fields
		DescribeIndexFieldsResult describe = client.describeIndexFields(new DescribeIndexFieldsRequest().withDomainName(domainName));
		List<IndexFieldStatus> fields = describe.getIndexFields();
		List<String> fieldNames = new LinkedList<String>();
		for(IndexFieldStatus fs : fields) {
			fieldNames.add(fs.getOptions().getIndexFieldName());
		}
		
		// check if all fields are present, if not.. reconfigure
		if(!fieldNames.contains("content") || !fieldNames.contains("content_main_preview") || !fieldNames.contains("content_other_names") || !fieldNames.contains("date") || !fieldNames.contains("folder") || !fieldNames.contains("from") || !fieldNames.contains("id_search") || !fieldNames.contains("recipients_bcc") || !fieldNames.contains("recipients_cc") || !fieldNames.contains("recipients_to") || !fieldNames.contains("subject")) {
			Log.debug("configuring index fields");

			try {
				this.configureIndexingOptions();
			
			} catch (AmazonServiceException e1) {
				Log.warn("Defining Index Fields did not work properly. Please retry");
				e1.printStackTrace();
				return;
			} catch (AmazonClientException e2) {
				Log.warn("Defining Index Fields did not work properly. Please retry");
				e2.printStackTrace();
				return;
			}
		}
		

		// start the indexing process as indexing is needed to make all fields work
		Log.debug("starting indexing");
		try {
			client.indexDocuments(new IndexDocumentsRequest().withDomainName(domainName));
		} catch (AmazonServiceException e1) {
			Log.warn("Indexing request failed. Please retry");
			return;
		} catch (AmazonClientException e2) {
			Log.warn("Indexing request failed. Please retry");
			return;
		}
		
		// update the access policy to be able to use the document and search service
		try {
			this.updateAccessPolicy();
		} catch (AmazonServiceException e1) {
			Log.error("Updating access policy failed");
			return;
		} catch (AmazonClientException e2) {
			Log.error("Updating access policy failed");
			return;
		}
			
		
	}
	
	private void configureIndexingOptions() {
		//Options for Index Fields
		LiteralOptions config_content_preview = new LiteralOptions();
		config_content_preview.setSearchEnabled(false);
		config_content_preview.setFacetEnabled(false);
		config_content_preview.setResultEnabled(true);
		
		LiteralOptions config_folder = new LiteralOptions().withSearchEnabled(true).withResultEnabled(true);	
		
		LiteralOptions config_id = new LiteralOptions();
		config_id.setSearchEnabled(true);
		config_id.setFacetEnabled(false);
		config_id.setResultEnabled(false);
		
		TextOptions config_content = new TextOptions();
		config_content.setFacetEnabled(false);
		config_content.setResultEnabled(false);
		
		TextOptions config_addresses = new TextOptions();
		config_addresses.setFacetEnabled(false);
		config_addresses.setResultEnabled(true);
		
		
		//Index Fields
		IndexField content = new IndexField();
		content.setIndexFieldName("content");
		content.setIndexFieldType(IndexFieldType.Text);
		content.setTextOptions(config_content);
		
		IndexField content_main_preview = new IndexField();
		content_main_preview.setIndexFieldName("content_main_preview");
		content_main_preview.setIndexFieldType(IndexFieldType.Literal);
		content_main_preview.setLiteralOptions(config_content_preview);
		
		IndexField content_other_names = new IndexField();
		content_other_names.setIndexFieldName("content_other_names");
		content_other_names.setIndexFieldType(IndexFieldType.Text);
		content_other_names.setTextOptions(config_addresses);
		
		IndexField date = new IndexField();
		date.setIndexFieldName("date");
		date.setIndexFieldType(IndexFieldType.Uint);
		
		IndexField folder = new IndexField();
		folder.setIndexFieldName("folder");
		folder.setIndexFieldType(IndexFieldType.Literal);
		folder.setLiteralOptions(config_folder);
		
		
		IndexField from = new IndexField();
		from.setIndexFieldName("from");
		from.setIndexFieldType(IndexFieldType.Text);
		from.setTextOptions(config_addresses);
		
		IndexField id_search = new IndexField();
		id_search.setIndexFieldName("id_search");
		id_search.setIndexFieldType(IndexFieldType.Literal);
		id_search.setLiteralOptions(config_id);
		
		IndexField recipients_bcc = new IndexField();
		recipients_bcc.setIndexFieldName("recipients_bcc");
		recipients_bcc.setIndexFieldType(IndexFieldType.Text);
		recipients_bcc.setTextOptions(config_addresses);
		
		IndexField recipients_cc = new IndexField();
		recipients_cc.setIndexFieldName("recipients_cc");
		recipients_cc.setIndexFieldType(IndexFieldType.Text);
		recipients_cc.setTextOptions(config_addresses);
		
		IndexField recipients_to = new IndexField();
		recipients_to.setIndexFieldName("recipients_to");
		recipients_to.setIndexFieldType(IndexFieldType.Text);
		recipients_to.setTextOptions(config_addresses);
		
		Set<SourceAttribute> recipients_to_Sources = new HashSet<SourceAttribute>();
		SourceData sourceDataCopy = new SourceData().withSourceName("recipients_to");
		SourceAttribute sourceAttribute = new SourceAttribute().withSourceDataCopy(sourceDataCopy).withSourceDataFunction(SourceDataFunction.Copy);
		recipients_to_Sources.add(sourceAttribute);
		
		sourceDataCopy = new SourceData().withSourceName("recipients_to1");
		sourceAttribute = new SourceAttribute().withSourceDataCopy(sourceDataCopy).withSourceDataFunction(SourceDataFunction.Copy);
		recipients_to_Sources.add(sourceAttribute);
		
		sourceDataCopy = new SourceData().withSourceName("recipients_to2");
		sourceAttribute = new SourceAttribute().withSourceDataCopy(sourceDataCopy).withSourceDataFunction(SourceDataFunction.Copy);
		recipients_to_Sources.add(sourceAttribute);
		
		recipients_to.setSourceAttributes(recipients_to_Sources);
	
		IndexField subject = new IndexField();
		subject.setIndexFieldName("subject");
		subject.setIndexFieldType(IndexFieldType.Text);
		subject.setTextOptions(config_addresses);
		
		
		DefineIndexFieldRequest r;
		
		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(subject);
		client.defineIndexField(r);

		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(recipients_to);
		client.defineIndexField(r);
		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(recipients_cc);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(recipients_bcc);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(id_search);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(from);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(folder);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(date);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(content_other_names);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(content_main_preview);
		client.defineIndexField(r);

		
		r = new DefineIndexFieldRequest();
		r.setDomainName(domainName);
		r.setIndexField(content);
		client.defineIndexField(r);
	}
	
	/**
	 * Updates the access policy for the document service and the search service
	 * 
	 * Currently set to access for everyone, i.e. every ip
	 * 
	 */
	private void updateAccessPolicy() {
		
		String policyDoc = "{" +
    "\"Statement\":[{"+
            "\"Effect\":\"Allow\"," +
            "\"Action\":\"*\"," +
            "\"Resource\":\"arn:aws:cs:"+region+":"+awsAccountId+":doc/"+domainName+"\"," +
            "\"Condition\":{" +
                "\"IpAddress\":{" +
                    "\"aws:SourceIp\":[\"0.0.0.0/0\"]"+
                "}"+
            "}"+
        "},"+
        "{"+
                "\"Effect\":\"Allow\"," +
                "\"Action\":\"*\"," +
                "\"Resource\":\"arn:aws:cs:"+region+":"+awsAccountId+":search/"+domainName+"\"," +
                "\"Condition\":{" +
                    "\"IpAddress\":{" +
                        "\"aws:SourceIp\":[\"0.0.0.0/0\"]"+
                    "}"+
                "}"+
            "}"+
    "]"+
"}";
		client.updateServiceAccessPolicies(new UpdateServiceAccessPoliciesRequest().withDomainName(domainName).withAccessPolicies(policyDoc));
		
//		DescribeServiceAccessPoliciesResult result = client.describeServiceAccessPolicies(new DescribeServiceAccessPoliciesRequest().withDomainName(domainName));
//		System.out.println(result.getAccessPolicies().getOptions());
	}
	
	
	
    /**
     * @return domainName created from current Account Name
     */
    //Parameter soll aktuelles benutzerkonto (String || Objekt) sein, aus dem domainname abgeleitet wird
    /**public String createDomainName(){ 
     
      
            //String accountName = currentUser.toString().split &removeAll;
            //String name = accountName.split &removeAll --> Sonderzeichen entfernen
            String domainName = null;
            //RegEx for Search Domain
            String name;
            //if(name.matches("[a-z][a-z0-9-]+") && name.length() <= 28 && name.length() >= 3)
                    //domainName = name;
            //else Log.debug("Name of Search Domain is not valid");
                    
     return domainName;
     }
     */
	

	
}
