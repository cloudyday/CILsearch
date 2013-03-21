package de.cil2012.cilsearch.server.imap;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder.ProtocolCommand;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * Method for fetching all messages in range start-end including the
 * message content. This should give us significant performance gains
 * when loading all messages from server.
 * 
 * Quelle: http://stackoverflow.com/questions/8322836/javamail-imap-over-ssl-quite-slow-bulk-fetching-multiple-messages
 */
public class CustomProtocolCommand implements ProtocolCommand {
	  
	/** Index on server of first mail to fetch **/
	int start;

	/** Index on server of last mail to fetch **/
	int end;
	
	public CustomProtocolCommand(int start, int end) {
		this.start = start;
		this.end = end; 
	}

	@Override
	public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
 	    Argument args = new Argument();
		args.writeString(Integer.toString(start) + ":" +
				Integer.toString(end));
		args.writeString("BODY[]");
		Response[] r = protocol.command("FETCH", args);
		Response response = r[r.length - 1];      
		List<MimeMessage> msgs = new LinkedList<MimeMessage>();
		
		if (response.isOK()) {
			Properties props = new Properties();
			props.setProperty("mail.store.protocol", "imap");
			props.setProperty("mail.mime.base64.ignoreerrors", "true");
			props.setProperty("mail.imap.partialfetch", "false");
			props.setProperty("mail.imaps.partialfetch", "false");
			Session session = Session.getInstance(props, null);

			FetchResponse fetch;
			BODY body;
			MimeMessage mm;
			ByteArrayInputStream is = null;
			
			// last response is only result summary: not contents
			for (int i = 0 ; i < r.length - 1 ; i++) {
				// Between our messages there are sometimes IMAP response that only
				// contain the actual search state of our server, giving us the elapsed
				// time and the percentage of searched mails
				// We ignore those responses of type IMAPResponse and only look for the
				// FetchResponses which actually contain messages.
				if (r[i] instanceof FetchResponse) {
		   	 		fetch = (FetchResponse)r[i];
					body = (BODY)fetch.getItem(0);
					is = body.getByteArrayInputStream();
					
					try {
					    mm = new MimeMessage(session, is);
					    msgs.add(mm);
					} catch (MessagingException e) {
					    e.printStackTrace();
					}     
		 		}  
			}   
		}
		// dispatch remaining untagged responses
		protocol.notifyResponseHandlers(r);
		protocol.handleResult(response);

		return msgs;   
	}
                
}
