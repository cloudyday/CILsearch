package de.cil2012.cilsearch.server.elasticsearch;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gson.JsonObject;

/**
 * This class is used to setup a new mail search domain on an elasticsearch cluster
 */
public class ESSetupDomain {

	private String domainName;
	private TransportClient client;

	public ESSetupDomain(String domainName) {
		this.client = ESSearch.createClient();
		this.domainName = domainName;
	}

	public void setUpDomain() throws Throwable {
		AdminClient admin = client.admin();
		IndicesAdminClient indices = admin.indices();

		// Settings object of mail analyzer
		JsonObject mailAnalyzer = new JsonObject();
		mailAnalyzer.addProperty("type", "pattern");
		mailAnalyzer.addProperty("pattern", "[^a-zA-Z0-9]+");
		JsonObject analyzer = new JsonObject();
		analyzer.add("mail", mailAnalyzer);
		JsonObject analysis = new JsonObject();
		analysis.add("analyzer", analyzer);
		JsonObject settings = new JsonObject();
		settings.add("analysis", analysis);

		CreateIndexRequest req = new CreateIndexRequest(domainName);
		req.settings(settings.toString());
		ActionFuture<CreateIndexResponse> future = indices.create(req);
		try {
			future.actionGet();
		} catch (IndexAlreadyExistsException e1) {
			Log.info(e1.getMessage());
		} catch (Throwable t) {
			Log.error("Creating Index failed");
			throw (t);
		}

		JsonObject mapping = new JsonObject();

		JsonObject mail = new JsonObject();
		
		JsonObject id = new JsonObject();
		id.addProperty("store", "yes");
		mail.add("_id", id);

		JsonObject source = new JsonObject();
		source.addProperty("enabled", false);
		
		mail.add("_source", source);

		JsonObject properties = new JsonObject();

		JsonObject field;
		
		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		properties.add("subject", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		properties.add("folder", field);

		field = new JsonObject();
		field.addProperty("type", "long");
		field.addProperty("store", "yes");
		properties.add("date", field);

		field = new JsonObject();
		field.addProperty("type", "long");
		field.addProperty("store", "yes");
		properties.add("date", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		field.addProperty("index_analyzer", "mail");
		properties.add("from", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		field.addProperty("index_analyzer", "mail");
		properties.add("recipients_to", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		field.addProperty("index_analyzer", "mail");
		properties.add("recipients_cc", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		field.addProperty("index_analyzer", "mail");
		properties.add("recipients_bcc", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "no");
		properties.add("content", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		properties.add("content_other_names", field);

		field = new JsonObject();
		field.addProperty("type", "string");
		field.addProperty("store", "yes");
		properties.add("content_main_preview", field);

		mail.add("properties", properties);
		mapping.add("mail", mail);
		
		PutMappingRequest putMap = new PutMappingRequest(domainName);
		putMap.type("mail");
		putMap.source(mapping.toString());
		

		ActionFuture<PutMappingResponse> future1 = indices.putMapping(putMap);
		try {
			future1.actionGet();
		} catch (Throwable t) {
			Log.error("putting the mapping failed");
			throw (t);
		}
		
		Log.debug("Created domain "+domainName);
	}

}
