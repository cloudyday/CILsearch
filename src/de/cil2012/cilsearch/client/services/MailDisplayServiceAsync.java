package de.cil2012.cilsearch.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.cil2012.cilsearch.shared.model.MailDisplayServiceResult;
import de.cil2012.cilsearch.shared.model.MailRepresentation;

public interface MailDisplayServiceAsync {
	void loadCompleteMessageData(MailRepresentation msg,
			AsyncCallback<MailDisplayServiceResult> callback);
}
