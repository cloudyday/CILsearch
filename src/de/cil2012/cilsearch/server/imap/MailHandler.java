package de.cil2012.cilsearch.server.imap;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import de.cil2012.cilsearch.server.model.MailContent;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.shared.MailHelper;

/**
 * This class handles the interaction with an IMAP account
 * 
 */
public class MailHandler {

	private Store store;
	private String user;
	private String password;
	private String host;
	private boolean useSSL;
	private Folder[] f = null;

	public MailHandler() {
		this.establishConnection();
	}

	/**
	 * Establish connection to IMAP Server
	 */
	public void establishConnection() {
		// Load IMAP-Account credentials from the property file
		Properties prop = new Properties();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("server_resources/IMAPAccountCredentials.properties");
		try {
			prop.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		host = prop.getProperty("imapHost");
		user = prop.getProperty("imapUsername");
		password = prop.getProperty("imapPassword");
		useSSL = Boolean.parseBoolean(prop.getProperty("useSSL"));

		// Create session for account
		Properties props = new Properties();
		props.put("mail.imap.host", host);
		if (useSSL) {
			props.put("mail.imap.socketFactory.port", "993");
			props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.imap.auth", "true");
			props.put("mail.imap.port", "993");
		}
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});

		final URLName unusedUrlName = null;
		if (useSSL) {
			store = new IMAPSSLStore(session, unusedUrlName);
		} else {
			store = new IMAPStore(session, unusedUrlName);
		}

		try {
			store.connect(host, user, password);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a LinkedList of all the mails in the IMAP account
	 * 
	 * @return LinkedList of mails
	 */
	public LinkedList<Message> getAllEmails() {
		// Iterate over all folders in the IMAP account
		try {
			f = store.getDefaultFolder().list();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		LinkedList<Message> messages = new LinkedList<Message>();
		for (Folder fd : f) {
			try {
				// Only open a folder if there are messages in it and if the
				// folder can be selected
				if (fd.getType() != 2) {
					if (fd.getMessageCount() != 0) {
						fd.open(Folder.READ_ONLY);
						messages.addAll(Arrays.asList(receiveEmails(fd)));
					}
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		return messages;
	}

	/**
	 * Gets e-mails from a specific folder
	 * 
	 * @param currentFolder
	 *            The specific folder
	 * @return allMessages A message-object array
	 * @throws MessagingException
	 */
	public Message[] receiveEmails(Folder currentFolder) throws MessagingException {
		Message[] allMessages = currentFolder.getMessages();
		currentFolder.close(false);
		return allMessages;
	}

	/**
	 * Creates MailObjects from JavaMail messages
	 * 
	 * @param messages
	 *            List of JavaMail messages
	 * @return List of MailObjects
	 */
	public LinkedList<MailObject> buildMailObjectsFromMessages(LinkedList<Message> messages) {
		LinkedList<MailObject> mailObjects = new LinkedList<MailObject>();
		try {
			for (Message message : messages) {
				mailObjects.add(buildMailObjectFromMessage(message));
			}
			// Close the message store
			store.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mailObjects;
	}
	
	/**
	 * Build a <code>MailObject</code> from a given
	 * <code>javax.mail.Message</code> object.
	 * 
	 * @param message the message to build the <code>MailObject</code> from.
	 * @return the <code>MailObject</code>.
	 */
	public MailObject buildMailObjectFromMessage(Message message) {
		MailObject currentMailObject = new MailObject();

		try {
			// From
			if (((InternetAddress) message.getFrom()[0]).getPersonal() == null) {
				currentMailObject.setFrom(((InternetAddress) message.getFrom()[0]));
			} else {
				currentMailObject.setFrom(((InternetAddress) message.getFrom()[0]));
			}
			// TO
			if (message.getRecipients(RecipientType.TO) != null) {
				currentMailObject.setRecipientsTO(new LinkedList<Address>(Arrays.asList(message.getRecipients(RecipientType.TO))));
			}
			// CC
			if (message.getRecipients(RecipientType.CC) != null) {
				currentMailObject.setRecipientsCC(new LinkedList<Address>(Arrays.asList(message.getRecipients(RecipientType.CC))));
			}
			// BCC
			if (message.getRecipients(RecipientType.BCC) != null) {
				currentMailObject.setRecipientsBCC(new LinkedList<Address>(Arrays.asList(message.getRecipients(RecipientType.BCC))));
			}
			// Subject
			currentMailObject.setSubject(message.getSubject());
			// Folder (we check if it is set because when we bulk fetch it is
			// not)
			if (message.getFolder() != null) {
				currentMailObject.setFolder(message.getFolder().getName());
			}
			// Date
			currentMailObject.setDate(message.getSentDate());
			// Message-ID
			if (message.getHeader("Message-ID")[0] != null && message.getHeader("Message-ID")[0] != "") {
				currentMailObject.setMessageId(message.getHeader("Message-ID")[0]);
			} else {
				// Generate Message-ID
				/*
				 * Append "<". Get the current (wall-clock) time in the highest
				 * resolution to which you have access (most systems can give it
				 * to you in milliseconds, but seconds will do); Generate 64
				 * bits of randomness from a good, well-seeded random number
				 * generator; Convert these two numbers to base 36 (0-9 and A-Z)
				 * and append the first number, a ".", the second number, and an
				 * "@". This makes the left hand side of the message ID be only
				 * about 21 characters long. Append the FQDN of the local host,
				 * or the host name in the user's return address. Append ">".
				 * 
				 * Inspired by: http://www.jwz.org/doc/mid.html
				 */
				Folder f = message.getFolder();
				f.close(false);
				f.open(Folder.READ_WRITE);
				String temp_messageid = "<" + new BigInteger(String.valueOf(Calendar.getInstance().getTimeInMillis()), 36).toString()
						+ "." + new BigInteger(new BigInteger(64, new SecureRandom()).toString(32), 36) + "@" + 
						((InternetAddress)currentMailObject.getFrom()).getAddress().split("@")[1] + ">";
				System.out.println(temp_messageid);
				message.setHeader("Message-ID", temp_messageid);
				currentMailObject.setMessageId(temp_messageid);
				f.close(false);
				f.open(Folder.READ_ONLY);
			}

			//
			// Content
			//
			// Determine whether the message contains attachments
			List<MailContent> mailContentList = new LinkedList<MailContent>();
			if (message.getContent() instanceof Multipart) {
				Multipart multipart = (Multipart) message.getContent();
				// Loop over the parts of the message
				for (int i = 0; i < multipart.getCount(); i++) {
					boolean add = false;
					Part part = multipart.getBodyPart(i);
					
					MailContent currentMailContent = new MailContent();
					currentMailContent.setName(part.getFileName());
					currentMailContent.setType(part.getContentType());
					// Text
					if (i == 0) {
						add = true;
						currentMailContent.setMainContent(true);
						currentMailContent.setContent(part.getContent().toString());
					}
					// Attachments
					else {
						String text = "";
						if (part.getFileName() != null && part.getFileName().indexOf(".") != -1) {
							add = true;
							// PDF
							if (part.getFileName().toLowerCase().endsWith(".pdf")) {
								PDDocument doc = null;
								try {
									doc = PDDocument.load(((InputStream) part.getContent()));
									PDFTextStripper stripper = new PDFTextStripper();
									text = stripper.getText(doc);
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									if (doc != null) {
										try {
											doc.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}
							// Microsoft Office
							if (part.getFileName().toLowerCase().endsWith(".doc") || part.getFileName().toLowerCase().endsWith(".docx") || part.getFileName().toLowerCase().endsWith(".ppt") || part.getFileName().toLowerCase().endsWith(".pptx") || part.getFileName().toLowerCase().endsWith(".xls") || part.getFileName().toLowerCase().endsWith(".xlsx")) {
								POITextExtractor extractor = null;
								try {
									extractor = ExtractorFactory.createExtractor((InputStream) part.getContent());
								} catch (InvalidFormatException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (OpenXML4JException e) {
									e.printStackTrace();
								} catch (XmlException e) {
									e.printStackTrace();
								}
								text = extractor.getText();
							}
						}
						currentMailContent.setContent(text);
					}
					if (add) mailContentList.add(currentMailContent);
				}
			} else {
				MailContent currentMailContent = new MailContent();
				currentMailContent.setContent(message.getContent().toString());
				currentMailContent.setMainContent(true);
				mailContentList.add(currentMailContent);
			}
			currentMailObject.setContents(mailContentList);
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
		return currentMailObject;
	}
	
	/**
	 * Returns all folders in this mailbox that can contain messages.
	 * 
	 * @return the folders in this mailbox.
	 */
	public List<Folder> getAllFolders() {
		try {
			// Init our list of folders and start off with the default folder
			List<Folder> folders = new LinkedList<Folder>();
			folders.addAll(Arrays.asList(store.getDefaultFolder().list()));
			
			// The following loop is repeated as long as there are subfolders in
			// our current folder. This is done by checking if the folders list
			// size has been increased since the last loop run, i.e. if new folders
			// have been added. Each run adds a new sub-layer of folders.
			int currentListSize = 0;
			int currentNewIndex = 0;
			do {
				currentNewIndex = currentListSize;
				currentListSize = folders.size();
				
				// First save the new found folders and then add them to our folders
				// list. This can't be done at the same time as it leads to a 
				// ConcurrentModificationException.
				List<Folder> foldersToAdd = new LinkedList<Folder>();
				for (int index = currentNewIndex; index < folders.size(); index++) {
					// If folder cannot hold folders don't look for
					if ((folders.get(index).getType() & Folder.HOLDS_FOLDERS) != 0) {
						continue;
					}
					
					foldersToAdd.addAll(Arrays.asList(folders.get(index).list()));
				}
				folders.addAll(foldersToAdd);
			} while (currentListSize < folders.size());
			
			// Take only folders that can contain mails
			folders = Lists.newLinkedList(Iterables.filter(folders, new Predicate<Folder>() {
				@Override
				public boolean apply(Folder folder) {
					boolean containsMessages = false;
					
					try {
						containsMessages = (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
					} catch (MessagingException e) {
						Log.error(e.getMessage());
					}
					
					return containsMessages;
				}
			}));
			
			return folders;
		} catch (MessagingException e) {
			// Imposible to reach
			Log.error(e.getMessage());
		}
		return null; // Imposible
	}
	
	/**
	 * Finds the message with the given messageId in our mailbox.
	 * 
	 * @param messageId the messageId to finde.
	 * @return the message with the given messageId or null if none exists.
	 */
	public MailObject findMailByMessageId(String messageId) {
		// Our object to find
		MailObject messageToFind = null;
		
		// Create connection to imap server
		establishConnection();
		
		// Set up our fetch profile for fetching all mails message ids
		FetchProfile profile = new FetchProfile();
		profile.add("Message-ID");
		
		// Iterate over all folders and then over all messages in this folder.
		// If a messageId matches the given one, return this message.
		for (Folder folder : getAllFolders()) {
			try {				
				folder.open(Folder.READ_ONLY);
				
				// Indicate the server that we need the Message-ID en masse
				Message[] messages = folder.getMessages();
				folder.fetch(messages, profile);
				
				for (Message message : messages) {
					String foundId = message.getHeader("Message-ID")[0];
					
					// Escape our found id to look like the cloud search id.
					foundId = MailHelper.escapeMessageId(foundId);
					
					if (foundId != null && foundId.equals(messageId)) {
						messageToFind = buildMailObjectFromMessage(message);
						break;
					}
				}
				folder.close(true);
				
				// Exit the outer loop if message was found
				if (messageToFind != null) break;
			} catch (MessagingException e) {
				Log.error(e.getMessage());
			}
		}
		
		// Close connection to imap server
		closeConnection();
		
		// If no message was found return null.
		return messageToFind;
	}
	
	@SuppressWarnings("unchecked")
	public Message findMessageByIdAndFolder(String folderName, String messageId) {
		// Message and folder to find
		Message[] messageToFind = new Message[1];
		IMAPFolder folderToFind = null;
		
		// Create connection to imap server
		establishConnection();
		
		// Set up our fetch profile for fetching all mails message ids
		FetchProfile profile = new FetchProfile();
		profile.add("Message-ID");
		
		// Iterate over all folders and then over all messages in this folder.
		// If a messageId matches the given one, return this message.
		for (Folder folder : getAllFolders()) {
			try {				
				folder.open(Folder.READ_ONLY);
				
				// If its not our folder continue
				if (!folder.getName().equals(folderName)) {
					continue;
				}
				
				// Indicate the server that we need the Message-ID en masse
				Message[] messages = folder.getMessages();
				folder.fetch(messages, profile);
				
				for (Message message : messages) {
					String foundId = message.getHeader("Message-ID")[0];
	
					// Escape our found id to look like the cloud search id.
					foundId = MailHelper.escapeMessageId(foundId);
					
					if (foundId != null && foundId.equals(messageId)) {
						messageToFind[0] = message;
						folderToFind = (IMAPFolder)folder;
						break;
					}
				}
				// Exit the outer loop if message was found
				if (messageToFind != null) break;
				
				folder.close(true);
			} catch (MessagingException e) {
				Log.error(e.getMessage());
			}
		}
		
		// If no message has been found return null
		if (messageToFind[0] == null) return null;
		
		// Fetch all message data
		try {
			profile.add(FetchProfileItem.FLAGS);
			profile.add(FetchProfileItem.ENVELOPE);
			folderToFind.fetch(messageToFind, profile);
			int id = messageToFind[0].getMessageNumber();
			messageToFind = new Message[] {
					((List<Message>) folderToFind.doCommand(new CustomProtocolCommand(id, id))).get(0)
					};
			folderToFind.close(true);
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
		
		// Close connection to imap server
		closeConnection();
		
		return messageToFind[0];
	}
	
	/**
	 * Closes the imap connection
	 */
	public void closeConnection() {
		try {
			this.store.close();
		} catch (MessagingException e) {
			Log.error(e.getStackTrace().toString());
		}
	}
}