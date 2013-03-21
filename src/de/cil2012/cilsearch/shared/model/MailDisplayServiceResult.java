package de.cil2012.cilsearch.shared.model;

import java.io.Serializable;

/** 
 * Represents the result of a mail display request performed by
 * MailDisplayService. Contains the complete mail that has been loaded
 * as well as the amount of time consumed for performing the request.
 */
@SuppressWarnings("serial")
public class MailDisplayServiceResult implements Serializable {
	private MailRepresentation message;
	private long loadTime;
	
	public MailDisplayServiceResult() {
	}
	
	public MailDisplayServiceResult(MailRepresentation message, long loadTime) {
		this.message = message;
		this.loadTime = loadTime;
	}
	
	public MailRepresentation getMessage() {
		return message;
	}
	
	public void setMessage(MailRepresentation message) {
		this.message = message;
	}
	
	public long getLoadTime() {
		return loadTime;
	}
	
	public void setLoadTime(long loadTime) {
		this.loadTime = loadTime;
	}
}
