package de.cil2012.cilsearch.benchmark;

import java.io.IOException;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;

import de.cil2012.cilsearch.server.elasticsearch.ESSearch;

public class GetMessagesInIndex {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public int countMessages() throws IOException {
		
		//ClouSearch
		
		int messageCount = 0;
		
//		InputStream in = AWSCloudSearch.class.getClassLoader().getResourceAsStream("server_resources/AwsCredentials.properties");
//		PropertiesCredentials credentials = new PropertiesCredentials(in);
//		AmazonCloudSearchClient client = new AmazonCloudSearchClient(credentials);
//		
//		DescribeDomainsResult result = client.describeDomains(new DescribeDomainsRequest().withDomainNames("suche-7"));
//		List<DomainStatus> domain = result.getDomainStatusList();
//		
//		final DomainStatus currentStatus = domain.get(0);
//		messageCount = currentStatus.getNumSearchableDocs();
//		System.out.println("# Mails alle 10 Sekunden: "+messageCount);
		
			 		
		//Elasticseach
		TransportClient client = ESSearch.createClient();
		IndicesAdminClient index = client.admin().indices();
		
		ActionFuture<IndicesStats> response = index.stats(new IndicesStatsRequest().indices("cilsearch"));
		IndicesStats stats = response.actionGet();
		IndexStats cil = stats.getIndices().get("cilsearch");
		
		//UpdateIndexServiceImpl u = new UpdateIndexServiceImpl();
		//u.updateIndex();
		
		System.out.println(cil.getPrimaries().docs().count());
		System.out.println(cil.getPrimaries().getIndexing().total().getIndexCount());
		
		
		return messageCount;
	}

}
