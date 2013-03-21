package de.cil2012.cilsearch.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchResultRepresentation;

/**
 * The async counterpart of <code>MailSearchService</code>.
 */
public interface MailSearchServiceAsync {
	void findMessages(MailSearchRequest input, AsyncCallback<MailSearchResultRepresentation> callback) 
			throws IllegalArgumentException;
}
