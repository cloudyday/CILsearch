package de.cil2012.cilsearch.client.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

import de.cil2012.cilsearch.client.InformationMessageDialog;
import de.cil2012.cilsearch.client.MailPreviewWidget;
import de.cil2012.cilsearch.client.services.MailSearchService;
import de.cil2012.cilsearch.client.services.MailSearchServiceAsync;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchResultRepresentation;

/**
 * Abstract handler for a search event. Must be overwritten to implement different
 * search events such as a standard or extended search.
 */
public abstract class AbstractSearchHandler implements ClickHandler, KeyUpHandler {
	
	private Label resultLabel;
	private Button searchButton;
	private MailPreviewWidget resultTable;;
	
	public AbstractSearchHandler(Button searchButton,
			MailPreviewWidget resultTable, Label resultLabel) {
		this.searchButton = searchButton;
		this.resultTable = resultTable;
		this.resultLabel = resultLabel;
	}
	
	/**
	 * Create a remote service proxy to talk to the server-side MailSearchService.
	 */
	private final MailSearchServiceAsync mailSearchService = GWT
			.create(MailSearchService.class);
	
	/**
	 * Fired when the user clicks on the searchButton.
	 */
	public void onClick(ClickEvent event) {
		startSearchProcess();
	}

	/**
	 * Fired when the user types in the searchField.
	 */
	public void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			startSearchProcess();
		}
	}

	/**
	 * Send the name from the nameField to the server and wait for a response.
	 */
	private void startSearchProcess() {
		// Get search request
		MailSearchRequest request = getRequest();

		// Then, we send the input to the server.
		searchButton.setEnabled(false);
		mailSearchService.findMessages(request,
				new AsyncCallback<MailSearchResultRepresentation>() {
					public void onFailure(Throwable caught) {
						resultLabel.setText("");
						InformationMessageDialog messageDialog = new InformationMessageDialog();
						messageDialog.showMessageModal("Error while searching for messages", caught.getLocalizedMessage());
					}

					public void onSuccess(MailSearchResultRepresentation result) {
						resultLabel.setText("Performed search request in " + 
								((Double)(Math.round(result.getSearchTimeTotal()/10.0)/100.0)).toString() + 
								" seconds");
						resultTable.setMails(result.getMessages());
					}
				});
		searchButton.setEnabled(true);
	}
	
	/**
	 * Gets the request to send to the server.
	 * @return the request.
	 */
	public abstract MailSearchRequest getRequest();
}
