package de.cil2012.cilsearch.server.amazon;

import de.cil2012.cilsearch.server.amazon.cloudsearch.Fields;

/**
 * This class stores the mail fields of a SDF-document
 * 
 * folder = the IMAP folder
 * subject = mail subject
 * from = mail senders
 * recipients_to{1,2} = mail TO-recipients: AWS limits each field to 100 values, so {1} and {2} are 
 * 						only filled if there are more than 100 / 200 recipients. 
 * 						In the cloudsearch index, these fields are joined (i.e. they are sources for the single field "recipients_to")
 * recipients_cc = mail CC-recipients
 * recipients_bcc = mail BCC-recipients
 * date = timestamp. milliseconds MUST be cut off because it must match AWS' uint field
 * content_other_names = the names of the attachments (files)
 * content = the content, i.e. message body and attachment content
 * content_main_preview = a truncated version of the main content (which is usually the message body)
 * id_search = the message id, that is saved as the AWS document id, is copied here with "search_" in front of it
 * 				This field is then used to find a message by id or retrieve all messages from the cloud search index
 *
 */
public class MailFields extends Fields {

	private String folder;
	private String subject;
	private String[] from;
	private String[] recipients_to;
	private String[] recipients_to1;
	private String[] recipients_to2;
	private String[] recipients_cc;
	private String[] recipients_bcc;
	private long date;
	private String[] content_other_names;
	private String[] content;
	private String content_main_preview;
	private String id_search;

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String[] getContent() {
		return content;
	}

	public void setContent(String[] content) {
		this.content = content;
	}

	public String[] getFrom() {
		return from;
	}

	public void setFrom(String[] from) {
		this.from = from;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String[] getRecipients_to() {
		return recipients_to;
	}

	public void setRecipients_to(String[] recipients_to) {
		this.recipients_to = recipients_to;
	}
	

	public String[] getRecipients_to1() {
		return recipients_to1;
	}

	public void setRecipients_to1(String[] recipients_to1) {
		this.recipients_to1 = recipients_to1;
	}

	public String[] getRecipients_to2() {
		return recipients_to2;
	}

	public void setRecipients_to2(String[] recipients_to2) {
		this.recipients_to2 = recipients_to2;
	}


	public String[] getRecipients_cc() {
		return recipients_cc;
	}

	public void setRecipients_cc(String[] recipients_cc) {
		this.recipients_cc = recipients_cc;
	}

	public String[] getRecipients_bcc() {
		return recipients_bcc;
	}

	public void setRecipients_bcc(String[] recipients_bcc) {
		this.recipients_bcc = recipients_bcc;
	}

	public String[] getContent_other_names() {
		return content_other_names;
	}

	public void setContent_other_names(String[] content_other_names) {
		this.content_other_names = content_other_names;
	}

	public String getContent_main_preview() {
		return content_main_preview;
	}

	public void setContent_main_preview(String content_main_preview) {
		this.content_main_preview = content_main_preview;
	}

	public String getId_search() {
		return id_search;
	}

	public void setId_search(String id_search) {
		this.id_search = id_search;
	}
	
	

}
