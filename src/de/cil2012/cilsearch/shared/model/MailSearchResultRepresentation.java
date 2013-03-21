package de.cil2012.cilsearch.shared.model;

import java.io.Serializable;

/** 
 * Represents the result of a mail search request performed by
 * MailSearchService. Contains the mails that have been found
 * as well as the amount of time consumed for performing the request.
 */
@SuppressWarnings("serial")
public class MailSearchResultRepresentation implements Serializable {
	private MailRepresentation[] messages;
	private long searchTimeService;
	private long searchTimeTotal;
	private long totalHits;
	
	public MailSearchResultRepresentation() {
	}
	
	public MailSearchResultRepresentation(MailRepresentation[] messages, long searchTime) {
		this.messages = messages;
		this.searchTimeService = searchTime;
	}

	public MailRepresentation[] getMessages() {
		return messages;
	}
	
	public void setMessages(MailRepresentation[] messages) {
		this.messages = messages;
	}
	
	public long getSearchTimeService() {
		return searchTimeService;
	}
	
	public void setSearchTimeService(long searchTimeService) {
		this.searchTimeService = searchTimeService;
	}
	
	public long getSearchTimeTotal() {
		return searchTimeTotal;
	}
	
	public void setSearchTimeTotal(long searchTimeTotal) {
		this.searchTimeTotal = searchTimeTotal;
	}
	
	public long getTotalHits() {
		return totalHits;
	}
	
	public void setTotalHits(long totalHits) {
		this.totalHits = totalHits;
	}
}
