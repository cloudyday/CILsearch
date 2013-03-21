package de.cil2012.cilsearch.server.model;

import java.io.Serializable;

/**
 * This class stores one mail content, such as body or an attachment
 * 
 * Use the mainContent boolean to identify that it is the body of the mail
 * 
 * type should feature the type of the content, e.g. text/plain
 * name should feature the name of the content, e.g. the name of an attachment file
 * content should feature the content itself
 */
public class MailContent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private boolean mainContent;
	private String type;
	private String name;
	private String content;
	
	public MailContent() {
		mainContent = false;
	}

	public boolean isMainContent() {
		return mainContent;
	}

	public void setMainContent(boolean mainContent) {
		this.mainContent = mainContent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
	
	

}
