package de.cil2012.cilsearch.client.handler;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import de.cil2012.cilsearch.client.MailPreviewWidget;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;

/**
 * Implements the handler for a standard search event.
 */
public class StandardSearchHandler extends AbstractSearchHandler {
	private TextBox searchField;
	
	public StandardSearchHandler(TextBox searchField, Button searchButton,
			MailPreviewWidget resultTable, Label resultLabel) {
		super(searchButton, resultTable, resultLabel);
		this.searchField = searchField;
	}
	
	@Override
	public MailSearchRequest getRequest() {
		return new MailSearchRequest(searchField.getText());
	}
}
