package de.cil2012.cilsearch.server.imap;

import java.util.LinkedList;
import java.util.List;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.sun.mail.imap.IMAPFolder;

import de.cil2012.cilsearch.server.model.MailObject;

/**
 * Class for bulk fetching messages including contents from
 * the IMAP server.
 * 
 * Quelle: http://stackoverflow.com/questions/8322836/javamail-imap-over-ssl-quite-slow-bulk-fetching-multiple-messages
 */
public class BulkFetcher {

	/**
	 * Efficiently load all given message object from the given folder
	 * and fetch contents of all messages immediately. Requests are seperated
	 * every 100 MB or if there are messages that shouldn't be fetched we
	 * have to seperate request because we can only fetch ranges.
	 * @param folder the folder our messages are in.
	 * @param messages the messages that should be fetched completely.
	 * @return the completely fetched messages.
	 * @throws MessagingException
	 */
	@SuppressWarnings("unchecked")
	public static List<MailObject> efficientGetContents(final IMAPFolder folder, Message[] messages)
			throws MessagingException {
		
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.FLAGS);
		fp.add(FetchProfile.Item.ENVELOPE);
		folder.fetch(messages, fp);
		
		int index = 0;
		int nbMessages = messages.length;
		final int maxDoc = 5000;
		final long maxSize = 100000000; // 100Mo

		// Message numbers limit to fetch
		int start;
		int end;

		List<MailObject> msgs = new LinkedList<MailObject>();
		final MailHandler mailHandler = new MailHandler();
		
		while (index < nbMessages) {
			start = messages[index].getMessageNumber();
			int docs = 0;
			int totalSize = 0;  
			
			// There are no jumps in the message numbers list          
			boolean noskip = true; 
			boolean notend = true;                
			
			// Until we reach one of the limits
			while (docs < maxDoc && totalSize < maxSize && noskip && notend) {
				docs++;
				totalSize += messages[index].getSize();
				index++;
				if (notend = (index < nbMessages)) {
					noskip = (messages[index - 1].getMessageNumber() + 1 == 
							messages[index].getMessageNumber()); 
				}     
			}

			end = messages[index - 1].getMessageNumber();
			
			// Transform messages to mailObjects and set the correct folder
			Function<Message, MailObject> messageTransformator = new Function<Message, MailObject>() {
				@Override
				public MailObject apply(Message message) {
					MailObject obj = mailHandler.buildMailObjectFromMessage(message);
					obj.setFolder(folder.getName());
					return obj;
				}
			};
			
			msgs.addAll(Lists.transform((List<Message>)folder.doCommand(new CustomProtocolCommand(start, end)), messageTransformator));
		}
		

		return msgs;
	}                        
}
