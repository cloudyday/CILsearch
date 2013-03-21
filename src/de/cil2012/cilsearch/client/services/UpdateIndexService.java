package de.cil2012.cilsearch.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side interface to our update index service that
 * is used to update the index of our search service with
 * new mail messages.
 */
@RemoteServiceRelativePath("updateIndex")
public interface UpdateIndexService extends RemoteService {
	public void updateIndex();
}
