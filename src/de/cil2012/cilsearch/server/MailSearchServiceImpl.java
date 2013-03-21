package de.cil2012.cilsearch.server;

import java.util.Properties;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.cil2012.cilsearch.client.services.MailSearchService;
import de.cil2012.cilsearch.server.amazon.AWSSearch;
import de.cil2012.cilsearch.server.elasticsearch.ESSearcher;
import de.cil2012.cilsearch.server.model.MailSearchResult;
import de.cil2012.cilsearch.server.model.exceptions.SearchException;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchResultRepresentation;

/**
 * Implements the <code>MailSearchService</code> on client side.
 */
@SuppressWarnings("serial")
public class MailSearchServiceImpl extends RemoteServiceServlet 
		implements MailSearchService  {

	@Override
	public MailSearchResultRepresentation findMessages(MailSearchRequest mailSearchRequest)
			throws IllegalArgumentException {
		
		Properties prop = GlobalPropertyStore.getActiveServiceProperties();		
		DefaultSearcher searcher = null;
		
		switch (GlobalPropertyStore.getActiveService()) {
		case CLOUDSEARCH:
			searcher = new AWSSearch(
					prop.getProperty("CloudSearch_domainName"), 
					prop.getProperty("CloudSearch_domainId"), 
					prop.getProperty("CloudSearch_region"));
			break;
		case ELASTICSEARCH:
			searcher = new ESSearcher(prop.getProperty("domainName"));
			break;
		} 
		
		MailSearchResultRepresentation resultRepresentation = null;
		try {
			MailSearchResult result = searcher.search(mailSearchRequest, 0, 100);
			resultRepresentation = result.getRepresentation();
		} catch (SearchException e1) {
			e1.printStackTrace();
		}
		
		return resultRepresentation;
	}

}
