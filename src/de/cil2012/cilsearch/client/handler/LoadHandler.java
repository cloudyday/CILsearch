package de.cil2012.cilsearch.client.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.cil2012.cilsearch.client.InformationMessageDialog;
import de.cil2012.cilsearch.client.MailDisplayDialog;
import de.cil2012.cilsearch.client.services.MailDisplayService;
import de.cil2012.cilsearch.client.services.MailDisplayServiceAsync;
import de.cil2012.cilsearch.shared.model.MailDisplayServiceResult;
import de.cil2012.cilsearch.shared.model.MailRepresentation;

/**
 * Called when the "Display" button beside a message is clicked.
 */
public class LoadHandler implements ClickHandler {
	private MailRepresentation mailMessage;
	
	/**
	 * Create a remote service proxy to talk to the server-side MailDisplayService.
	 */
	private final MailDisplayServiceAsync mailSearchService = GWT
			.create(MailDisplayService.class);
	
	
	public LoadHandler(MailRepresentation mailMessage) {
		this.mailMessage = mailMessage;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		// Get the complete representation from the server
		mailSearchService.loadCompleteMessageData(mailMessage, 
				new AsyncCallback<MailDisplayServiceResult>() {
					@Override
					public void onSuccess(MailDisplayServiceResult result) {
						// Show our dialog
						MailDisplayDialog displayDialog = new MailDisplayDialog();
						displayDialog.showMessage(result.getMessage(), result.getLoadTime());
					}
					
					@Override
					public void onFailure(Throwable caught) {
						InformationMessageDialog messageDialog = new InformationMessageDialog();
						messageDialog.showMessageModal("Error while searching for messages", caught.getLocalizedMessage());
					}
				});
	}

}
