package de.cil2012.cilsearch.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

import de.cil2012.cilsearch.client.handler.LoadHandler;
import de.cil2012.cilsearch.shared.model.MailRepresentation;

/**
 * Widget for showing our mail results in a nicely looking
 * and well formatted table.
 */
public class MailPreviewWidget extends Composite {
	private MailRepresentation[] mails;
	
	// Contains the HTML-Content representation of our widget
	private FlexTable table;
	
	// The maximum length for a content teaser
	final int MAX_TEASER_LENGTH = 140;
	
	public MailPreviewWidget() {
		initControl();
	}
	
	public MailPreviewWidget(MailRepresentation[] mails) {
		setMails(mails);
		initControl();
	}
	
	private void initControl() {
		table = new FlexTable();
		initWidget(table);
	}
	
	public MailRepresentation[] getMails() {
		return mails;
	}
	
	public void setMails(MailRepresentation[] mails) {
		this.mails = mails;
		updateContents();
	}
	
	private void updateContents() {
		table.removeAllRows();
		for (int i = 0; i < mails.length; i++) {
			HTML html = buildHTMLFromMessage(mails[i]);
			html.setStyleName("cil-mail-preview");
			html.addClickHandler(new LoadHandler(mails[i]));
			if (i == mails.length - 1) {
				html.addStyleName("cil-mail-preview-last");
			}
			table.setWidget(i, 0, html);
		}
	}
	
	/**
	 * Generates a table cells content from a mail message
	 * by building our custom HTML code.
	 * 
	 * @param mail the message to generate HTML from.
	 * @return the generated HTML code.
	 */
	private HTML buildHTMLFromMessage(MailRepresentation mail) {
		StringBuilder htmlContent = new StringBuilder();
		
		// Add date
		htmlContent.append("<span class=\"cil-mail-date\">");
		htmlContent.append(DateTimeFormat.getFormat("dd.MM.yyyy").format(mail.getDate()));
		htmlContent.append("</span>");
		
		// Add from
		htmlContent.append("<span class=\"cil-mail-from\">");
		htmlContent.append(mail.getFrom());
		if (mail.getAttachments() != null && mail.getAttachments().length > 0) {
			htmlContent.append("<img src=\"attachment.png\" class=\"attachment\" />");
		}
		htmlContent.append("</span>");
		
		// Add subject
		htmlContent.append("<span class=\"cil-mail-subject\">");
		htmlContent.append(mail.getSubject());
		htmlContent.append("</span>");
		
		// Add content preview
		htmlContent.append("<span class=\"cil-mail-teaser\">");
		
		// Make string a maximum length of MAX_TEASER_LENGTH
		String contentTeaser = mail.getContent();
		if (contentTeaser.length() > MAX_TEASER_LENGTH) {
			// Find last whitespace backwards starting from 250th character
			int index = MAX_TEASER_LENGTH;
			while (contentTeaser.charAt(index) != ' ' && index >= 0) {
				index--;
			}
			
			// Cut the string off and add "..."
			if (index > 0) {
				contentTeaser = contentTeaser.substring(0, index);
			} else {
				// If the first 250 contain no whitespace just make a hard cut
				contentTeaser = contentTeaser.substring(0, MAX_TEASER_LENGTH);
			}
			contentTeaser += "...";
		}
		
		htmlContent.append(contentTeaser);
		htmlContent.append("</span>");
		
		return new HTML(htmlContent.toString());
	}
}
