package de.cil2012.cilsearch.server.elasticsearch;

import java.util.Arrays;
import java.util.Properties;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import de.cil2012.cilsearch.server.GlobalPropertyStore;

/**
 * This class provides static methods to connect to an elasticsearch cluster
 */
public abstract class ESSearch {

	private static String currentClusterName = null;
	private static String[] currentAddress = null;
	private static TransportClient currentClient = null;
	
	public static TransportClient createClient() {
		Properties prop = GlobalPropertyStore.getActiveServiceProperties();
	
		String clusterName = prop.getProperty("clusterName");
		String[] addresses = prop.getProperty("addresses").split(",");

		return createClient(clusterName, addresses);
	}

	public static TransportClient createClient(String clusterName,
			String[] address) {

		if (clusterName.equals(currentClusterName)) {
			if (Arrays.deepEquals(currentAddress, address)) {
				return currentClient;
			}
		}
		
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", clusterName).build();

		TransportClient client = new TransportClient(settings);

		for (String addre : address) {
			String[] addr = addre.split(":");

			client.addTransportAddress(new InetSocketTransportAddress(addr[0],
					Integer.parseInt(addr[1])));
		}

		currentClusterName = clusterName;
		currentAddress = address;
		currentClient = client;
		
		return client;
	}

}
