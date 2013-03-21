package de.cil2012.cilsearch.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>UpdateIndexService</code>.
 */
public interface UpdateIndexServiceAsync {
	void updateIndex(AsyncCallback<Void> callback);
}
