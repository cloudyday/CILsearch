package de.cil2012.cilsearch.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.cil2012.cilsearch.shared.model.MailDisplayServiceResult;
import de.cil2012.cilsearch.shared.model.MailRepresentation;

/**
 * This service is used to load the complete mail message from an
 * imap mailbox to display the found result.
 */
@RemoteServiceRelativePath("display")
public interface MailDisplayService extends RemoteService {
	MailDisplayServiceResult loadCompleteMessageData(MailRepresentation msg) throws Exception;
}
