package de.cil2012.cilsearch.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.cil2012.cilsearch.shared.MailHelper;
import de.cil2012.cilsearch.shared.model.MailRepresentation;

public class MailDisplayDialog extends DialogBox {
	// Controls needed for displaying
	private VerticalPanel dialogPanel = new VerticalPanel();
	private VerticalPanel headerPanel = new VerticalPanel();
	private HorizontalPanel fromPanel = new HorizontalPanel();
	private HorizontalPanel toPanel = new HorizontalPanel();
	private HorizontalPanel ccPanel = new HorizontalPanel();
	private HorizontalPanel bccPanel = new HorizontalPanel();
	private HorizontalPanel subjectPanel = new HorizontalPanel();
	private HorizontalPanel datePanel = new HorizontalPanel();
	private HorizontalPanel footerPanel = new HorizontalPanel();
	private ScrollPanel contentScrollPanel = new ScrollPanel();
	private Label fromDescLabel = new Label("From:");
	private Label fromValueLabel = new Label();
	private Label toDescLabel = new Label("To:");
	private Label toValueLabel = new Label();
	private Label ccDescLabel = new Label("CC:");
	private Label ccValueLabel = new Label();
	private Label bccDescLabel = new Label("BCC:");
	private Label bccValueLabel = new Label();
	private Label subjectDescLabel = new Label("Subject:");
	private Label subjectValueLabel = new Label();
	private Label dateDescLabel = new Label("Date:");
	private Label dateValueLabel = new Label();
	private Label loadTimeLabel = new Label();
	private HTML contentHTML = new HTML();
	private HTML[] downloadLinks;
	private Button closeDialogButton = new Button("Close");
	
	public MailDisplayDialog() {
		// Set style information
		dialogPanel.addStyleName("dialogPanel");
		headerPanel.addStyleName("headerPanel");
		fromDescLabel.addStyleName("descLabel");
		subjectDescLabel.addStyleName("descLabel");
		dateDescLabel.addStyleName("descLabel");
		toDescLabel.addStyleName("descLabel");
		ccDescLabel.addStyleName("descLabel");
		bccDescLabel.addStyleName("descLabel");
		loadTimeLabel.addStyleName("loadTimeLabel");
		closeDialogButton.addStyleName("closeDialogButton");
		
		// Build header information
		fromPanel.add(fromDescLabel);
		fromPanel.add(fromValueLabel);
		
		subjectPanel.add(subjectDescLabel);
		subjectPanel.add(subjectValueLabel);
		
		datePanel.add(dateDescLabel);
		datePanel.add(dateValueLabel);
		
		toPanel.add(toDescLabel);
		toPanel.add(toValueLabel);
		
		ccPanel.add(ccDescLabel);
		ccPanel.add(ccValueLabel);
		
		bccPanel.add(bccDescLabel);
		bccPanel.add(bccValueLabel);
		
		footerPanel.add(loadTimeLabel);
		footerPanel.add(closeDialogButton);
		
		// Add scroll panel to htmlContent conrtol
		contentScrollPanel.add(contentHTML);
		contentScrollPanel.setHeight("400px");
		
		// Build dialog panel
		headerPanel.add(fromPanel);
		headerPanel.add(subjectPanel);
		headerPanel.add(datePanel);
		headerPanel.add(toPanel);
		headerPanel.add(ccPanel);
		headerPanel.add(bccPanel);
		dialogPanel.add(headerPanel);
		dialogPanel.add(contentScrollPanel);
		dialogPanel.add(footerPanel);
		
		// Set the close handler for our button
		closeDialogButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				MailDisplayDialog.this.hide();
			}
		});
		
		// Add interface to dialog
		this.setWidget(dialogPanel);
	}
	
	/**
	 * Resets all display labels to empty text.
	 */
	private void resetFields() {
		this.fromValueLabel.setText("");
		this.toValueLabel.setText("");
		this.ccValueLabel.setText("");
		this.bccValueLabel.setText("");
		this.subjectValueLabel.setText("");
		this.dateValueLabel.setText("");
		this.contentHTML.setText("");
	}
	
	/**
	 * Sets the contents of this dialog to show the contents
	 * of the given message.
	 * 
	 * @param message the message to show.
	 */
	public void showMessage(MailRepresentation message, long loadTime) {
		// Reset all fields to not show a message from before
		resetFields();
		
		// Set the title of the box
		this.setText("Message from " + message.getFrom());
		
		// Set the easy to set fields
		this.fromValueLabel.setText(message.getFrom());
		this.toValueLabel.setText(MailHelper.concatAddresses(message.getRecipientsTO()));
		this.ccValueLabel.setText(MailHelper.concatAddresses(message.getRecipientsCC()));
		this.bccValueLabel.setText(MailHelper.concatAddresses(message.getRecipientsBCC()));
		this.subjectValueLabel.setText(message.getSubject());
		this.contentHTML.setHTML(MailHelper.encodeToHTML(message.getContent()));
		this.loadTimeLabel.setText("Message loaded in " + 
				((Double)(Math.round(loadTime/10.0)/100.0)).toString() +
				" seconds");
		
		// Only show cc and bcc if set
		if (message.getRecipientsCC() == null || message.getRecipientsCC().length == 0) {
			this.ccPanel.setVisible(false);
		} else {
			this.ccPanel.setVisible(true);
		}
		
		if (message.getRecipientsBCC() == null || message.getRecipientsBCC().length == 0) {
			this.bccPanel.setVisible(false);
		} else {
			this.bccPanel.setVisible(true);
		}
		
		// Format our date to look nicely
		this.dateValueLabel.setText(
				DateTimeFormat.getFormat("dd MMM yyyy HH:mm:ss Z").format(message.getDate()));
		
		// Display links
		if (downloadLinks != null) {
			for (HTML downloadLink : downloadLinks) {
				dialogPanel.remove(downloadLink);
			}
		}
		downloadLinks = new HTML[message.getAttachments().length];
		for (int index=0; index < downloadLinks.length; index++) {
			downloadLinks[index] = new HTML("<img src=\"attachment.png\" class=\"attachment\" />" +
					"<a href=\"/cilsearch/download?folder=" + 
					message.getFolder() + "&msg=" + MailHelper.escapeMessageId(message.getMessageId()) +
					"&num=" + (index+1) + "\">" + message.getAttachments()[index] + 
					"</a>");
			dialogPanel.insert(downloadLinks[index], dialogPanel.getWidgetIndex(footerPanel));
			
		}
		
		
		// Display our dialog
		this.center();
	}
}
