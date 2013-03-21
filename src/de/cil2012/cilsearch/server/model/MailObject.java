package de.cil2012.cilsearch.server.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.cil2012.cilsearch.shared.model.MailRepresentation;


/**
 * This class represents an email that can be used within our webapp.
 */
public class MailObject implements Serializable {
	

	private static final long serialVersionUID = 1L;
	private String messageId;
	private String folder;
	private String subject;
	private Address from;
	private List<Address> recipientsTO;
	private List<Address> recipientsCC;
	private List<Address> recipientsBCC;
	private Date date;
	private List<MailContent> contents;
	
	
	public MailObject() {
		messageId = null;
	}

	public MailObject(String messageId) {
		this.messageId = messageId;
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


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}


	public Address getFrom() {
		return from;
	}


	public void setFrom(Address from) {
		this.from = from;
	}


	public List<Address> getRecipientsTO() {
		return recipientsTO;
	}


	public void setRecipientsTO(List<Address> recepients) {
		this.recipientsTO = recepients;
	}
	
	public List<Address> getRecipientsCC() {
		return recipientsCC;
	}
	
	public void setRecipientsCC(List<Address> recipientsCC) {
		this.recipientsCC = recipientsCC;
	}
	
	public List<Address> getRecipientsBCC() {
		return recipientsBCC;
	}
	
	public void setRecipientsBCC(List<Address> recipientsBCC) {
		this.recipientsBCC = recipientsBCC;
	}

	public Date getDate() {
		return date;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public List<MailContent> getContents() {
		return contents;
	}


	public void setContents(List<MailContent> contents) {
		this.contents = contents;
	}
	
	/**
	 * Returns the representation object used on the client side of the
	 * application.
	 * 
	 * @return the representation.
	 */
	public MailRepresentation getRepresentation() {
		Function<Address, String> convertFunction = new Function<Address, String>() {		
			@Override
			public String apply(Address arg0) {
				if(arg0 != null) {
					return arg0.toString();
				}
				return "";
			}
		};
		
		// Make the from look correct
		String from = null;
		if (this.from instanceof InternetAddress && ((InternetAddress) this.from).getPersonal() != null) {
			from = ((InternetAddress) this.from).getPersonal();
			
			// Check if there are quotation marks around the name and remove them
			if (from.startsWith("\"") && from.endsWith("\"")) {
				from = from.substring(1, from.length()-1);
			}
		} else {
			from = this.from.toString();
		}
		
		// Get names of attachments
		List<String> attachments = new LinkedList<String>();
		if (this.contents.size() > 1) {
			for (int index = 1; index < this.contents.size(); index++) {
				attachments.add(this.contents.get(index).getName());
			}
		}
		
		return new MailRepresentation(messageId, folder, from, 
				recipientsTO != null ? 
						Lists.transform(recipientsTO, convertFunction).toArray(new String[0]) : null, 
				recipientsCC != null ? 
						Lists.transform(recipientsCC, convertFunction).toArray(new String[0]) : null, 
				recipientsBCC != null ?
						Lists.transform(recipientsBCC, convertFunction).toArray(new String[0]) : null, 
				subject, date, getContents().get(0).getContent(),
				attachments.toArray(new String[attachments.size()]));
	}

}
