package de.cil2012.cilsearch.client.handler;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

import de.cil2012.cilsearch.client.ExtendedSearchWidget;
import de.cil2012.cilsearch.client.MailPreviewWidget;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;

public class ExtendedSearchHandler extends AbstractSearchHandler {
	private ExtendedSearchWidget widget;
	
	public ExtendedSearchHandler(ExtendedSearchWidget widget, Button searchButton,
			MailPreviewWidget resultTable, Label resultLabel) {
		super(searchButton, resultTable, resultLabel);
		this.widget = widget;
	}
	
	@Override
	public MailSearchRequest getRequest() {
		return widget.getRequest();
	}
}
