package de.cil2012.cilsearch.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchResultRepresentation;

/**
 * The client side interface to our mail search service that
 * executes a search request on a web search service and
 * returns all found messages.
 */
@RemoteServiceRelativePath("search")
public interface MailSearchService extends RemoteService {
	/**
	 * Find all messages which contain the given search string on our
	 * mailbox by calling the specified cloud search service.
	 * 
	 * @param search the string to search
	 * @return the found messages
	 * @throws IllegalArgumentException if the search is not valid
	 */
	MailSearchResultRepresentation findMessages(MailSearchRequest search) throws IllegalArgumentException;
}
