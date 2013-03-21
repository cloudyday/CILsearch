package de.cil2012.cilsearch.shared.model;

import java.io.Serializable;
import java.util.Date;

/**
 * This object is used on the client side of the application.
 * It contains no <code>javax.mail</code> components. Hence,
 * it is compilable into java script code.
 */
@SuppressWarnings("serial")
public class MailRepresentation implements Serializable {
	private String messageId;
	private String folder;
	private String from;
	private String[] recipientsTO;
	private String[] recipientsCC;
	private String[] recipientsBCC;
	private Date date;
	private String subject;
	private String content;
	private String[] attachments;
	
	public MailRepresentation() {
		messageId = null;
	}
	

	/**
	 * Construct a complete <code>MailRepresentation</code>. Makes generating
	 * from a <code>MailObject</code> much more easy. 
	 * 
	 * @param messageId the id.
	 * @param from the sender.
	 * @param recipientsTO the recipients (to).
	 * @param recipientsCC the recipients (cc).
	 * @param recipientsBCC the recipients (bcc).
	 * @param subject the subject.
	 * @param date the date.
	 * @param contentTeaser the teaser text.
	 */
	public MailRepresentation(String messageId, String folder, String from, 
			String[] recipientsTO, String[] recipientsCC, String[] recipientsBCC, 
			String subject, Date date, String contentTeaser, String[] attachments) {
		this.messageId = messageId;
		this.folder = folder;
		this.from = from;
		this.recipientsTO = recipientsTO;
		this.recipientsCC = recipientsCC;
		this.recipientsBCC = recipientsBCC;
		this.subject = subject;
		this.date = date;
		this.content = contentTeaser;
		this.attachments = attachments;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getFolder() {
		return folder;
	}
	
	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String[] getRecipientsTO() {
		return recipientsTO;
	}

	public void setRecipientsTO(String[] recipientsTO) {
		this.recipientsTO = recipientsTO;
	}

	public String[] getRecipientsCC() {
		return recipientsCC;
	}

	public void setRecipientsCC(String[] recipientsCC) {
		this.recipientsCC = recipientsCC;
	}

	public String[] getRecipientsBCC() {
		return recipientsBCC;
	}

	public void setRecipientsBCC(String[] recipientsBCC) {
		this.recipientsBCC = recipientsBCC;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String[] getAttachments() {
		return attachments;
	}
	
	public void setAttachments(String[] attachments) {
		this.attachments = attachments;
	}
}
