package de.cil2012.cilsearch.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.mail.imap.IMAPFolder;

import de.cil2012.cilsearch.client.services.UpdateIndexService;
import de.cil2012.cilsearch.server.amazon.AWSIndexer;
import de.cil2012.cilsearch.server.amazon.AWSSearch;
import de.cil2012.cilsearch.server.elasticsearch.ESIndexer;
import de.cil2012.cilsearch.server.elasticsearch.ESSearcher;
import de.cil2012.cilsearch.server.imap.BulkFetcher;
import de.cil2012.cilsearch.server.imap.MailHandler;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.exceptions.ChangeIndexException;
import de.cil2012.cilsearch.server.model.exceptions.SearchException;
import de.cil2012.cilsearch.shared.MailHelper;

@SuppressWarnings("serial")
public class UpdateIndexServiceImpl extends RemoteServiceServlet implements
		UpdateIndexService {

	@Override
	public void updateIndex() {
		
		DefaultSearcher search = null;
		Indexer indexer = null;

		Properties prop = GlobalPropertyStore.getActiveServiceProperties();

		switch (GlobalPropertyStore.getActiveService()) {
		case CLOUDSEARCH:
			search = new AWSSearch(
					prop.getProperty("CloudSearch_domainName"),
					prop.getProperty("CloudSearch_domainId"),
					prop.getProperty("CloudSearch_region"));
			indexer = new AWSIndexer(
					prop.getProperty("CloudSearch_domainName"),
					prop.getProperty("CloudSearch_domainId"),
					prop.getProperty("CloudSearch_region"));
			break;
		case ELASTICSEARCH:
			search = new ESSearcher(prop.getProperty("domainName"));
			indexer = new ESIndexer(prop.getProperty("domainName"));
			break;
		}		

		long startTime = System.currentTimeMillis();

		// 1. Get all the Message-IDs from the IMAP account
		MailHandler mailHandler = new MailHandler();
		LinkedList<String> idsIMAP = new LinkedList<String>();
		HashMap<String, Message> idMessageMap = new HashMap<String, Message>();

		FetchProfile profile = new FetchProfile();
		profile.add("Message-ID");
		List<Folder> allFolders = mailHandler.getAllFolders();
		for (Folder folder : allFolders) {
			String messageID = "";
			Message[] messages = null;
			try {
				if (!folder.isOpen()) {
					try {
						folder.open(Folder.READ_ONLY);

						messages = folder.getMessages();
						folder.fetch(messages, profile);
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
				for (Message message : messages) {
					messageID = MailHelper.escapeMessageId(message.getHeader("Message-ID")[0]);

					// Check if the mail already has be listed (Enron test data fix)
					if (!idsIMAP.contains(messageID)) {
						// Add the Message-IDs to a list
						idsIMAP.add(messageID);
						// Save Message-ID together with the message object
						idMessageMap.put(messageID, message);
					}
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		long stepOneTime = System.currentTimeMillis();
		
		// 2. Get a list of all the Message-IDs in the Index
		LinkedList<String> idsDB = new LinkedList<String>();

		try {
			for (MailObject mail : search.getAllMailsIdAndDate().getResults()) {
				idsDB.add(mail.getMessageId());
			}
		} catch (SearchException e1) {
			e1.printStackTrace();
		}
		long stepTwoTime = System.currentTimeMillis();

		// 3. The elements of idsIMAP minus all elements of idsDB are new
		// messages
		ArrayList<String> idsNewMessages = new ArrayList<String>();
		for (String item : idsIMAP) {
			idsNewMessages.add(new String(item));
		}
		idsNewMessages.removeAll(idsDB);
		long stepThreeTime = System.currentTimeMillis();

		// 4. The elements of idsDB minus all elements of idsIMAP are
		// deleted messages
		List<String> idsDeletedMessages = new ArrayList<String>();
		for (String item : idsDB) {
			idsDeletedMessages.add(new String(item));
		}
		idsDeletedMessages.removeAll(idsIMAP);
		long stepFourTime = System.currentTimeMillis();

		// 5. Convert the new JavaMail messages to MailObjects and add them to
		// the CloudSearch index

		if (idsNewMessages.size() > 0) {
			// Set this variable to idsNewMessages.size() to add all messages or to another
			// number if to add only that much. Please make sure, that you can add as many
			// messages as specified here
			final int MESSAGE_MAX = idsNewMessages.size();
			
			LinkedList<Message> newMessages = new LinkedList<Message>();
			for (String id : idsNewMessages.subList(0, MESSAGE_MAX)) {
				newMessages.add(idMessageMap.get(id));
			}

			// Now we bulk fetch the messages we need
			List<MailObject> fetchedMessages = new LinkedList<MailObject>();

			// Find all folders that contain messages we want to upload and ...
			Set<IMAPFolder> folders = new HashSet<IMAPFolder>();
			for (Message msg : newMessages) {
				folders.add((IMAPFolder) msg.getFolder());
			}
			// ... iterate over all these folders
			for (final IMAPFolder folder : folders) {
				// Get all message in folder
				Collection<Message> msgsInFolder = Collections2.filter(
						newMessages, new Predicate<Message>() {
							@Override
							public boolean apply(Message msg) {
								return msg.getFolder().getName()
										.equals(folder.getName());
							}
						});

				// Now fetch the messages
				try {
					fetchedMessages.addAll(BulkFetcher.efficientGetContents(
							folder, msgsInFolder.toArray(new Message[0])));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}

			try {
				indexer.addToIndex(fetchedMessages);
			} catch (ChangeIndexException e) {
				e.printStackTrace();
			}
		}
		long stepFiveTime = System.currentTimeMillis();

		// 6. Remove deleted messages from the CouldSearch index
		if (idsDeletedMessages.size() > 0) {
			try {
				indexer.removeFromIndex(new HashSet<String>(idsDeletedMessages));
			} catch (ChangeIndexException e) {
				e.printStackTrace();
			}
		}
		mailHandler.closeConnection();
		long stepSixTime = System.currentTimeMillis();
		
		//
		// Print the elapsed time for every step
		//
		System.out.println("--- --- ---");
		System.out.println("--- Update-Benchmark ---");
		System.out.println("--- --- ---");
		System.out.println("Nachrichten im IMAP: " + idsIMAP.size());
		System.out.println("Nachrichten im Index (vor dem Update): "
				+ idsDB.size());
		System.out.println("Anzahl hinzugefuegter Nachrichten: "
				+ idsNewMessages.size());
		System.out.println("Anzahl geloeschter Nachrichten: "
				+ idsDeletedMessages.size());
		System.out.println("------");
		System.out.println("Gesamt benoetigte Zeit: "
				+ (stepSixTime - startTime) + "ms");
		System.out.println("Message-IDs vom IMAP holen: "
				+ (stepOneTime - startTime) + "ms");
		System.out.println("Message-IDs vom Index holen: "
				+ (stepTwoTime - stepOneTime) + "ms");
		System.out.println("Bestimmen der neuen Nachrichten: "
				+ (stepThreeTime - stepTwoTime) + "ms");
		System.out.println("Bestimmen der geloschten Nachrichten: "
				+ (stepFourTime - stepThreeTime) + "ms");
		System.out.println("Nachrichten hinzufuegen: "
				+ (stepFiveTime - stepFourTime) + "ms");
		System.out.println("Nachrichten loeschen: "
				+ (stepSixTime - stepFiveTime) + "ms");
	}
}