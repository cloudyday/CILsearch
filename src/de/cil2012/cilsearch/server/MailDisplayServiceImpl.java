package de.cil2012.cilsearch.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.cil2012.cilsearch.client.services.MailDisplayService;
import de.cil2012.cilsearch.server.imap.MailHandler;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.shared.model.MailDisplayServiceResult;
import de.cil2012.cilsearch.shared.model.MailRepresentation;

@SuppressWarnings("serial")
public class MailDisplayServiceImpl extends RemoteServiceServlet
		implements MailDisplayService {

	@Override
	public MailDisplayServiceResult loadCompleteMessageData(MailRepresentation msg) throws Exception {
		// Keep start time in mind
		long startTime = System.currentTimeMillis();
		
		// Get the id from the message to load completely
		String messageId = msg.getMessageId();
		
		MailHandler mailHandler = new MailHandler();
		MailObject messageObject = mailHandler.findMailByMessageId(messageId);

		if(messageObject == null) {
			throw new Exception("Message could not be found on the server");
		}
		msg = messageObject.getRepresentation();
		
		
		// Calculate runtime
		long runTime = System.currentTimeMillis() - startTime;
		
		return new MailDisplayServiceResult(msg, runTime); // RETURN THE SAME MESSAGE, IF NOTHING IS FOUND (TESTING)
	}

}
