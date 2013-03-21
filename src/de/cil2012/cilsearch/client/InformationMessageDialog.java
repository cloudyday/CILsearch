package de.cil2012.cilsearch.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InformationMessageDialog extends DialogBox {
	private VerticalPanel dialogPanel = new VerticalPanel();
	private Label messageLabel = new Label();
	private Button closeButton = new Button("Close");
	
	public InformationMessageDialog() {
		// Stack controls together
		dialogPanel.add(messageLabel);
		dialogPanel.add(closeButton);
		this.setWidget(dialogPanel);
		
		closeButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				InformationMessageDialog.this.hide();
			}
		});
	}
	
	/**
	 * Show a message dialog modal. Shortcut for <code>showMessage(title, text, true, true)</code>.
	 * 
	 * @param title the title of the dialog to display.
	 * @param text the text to show.
	 */
	public void showMessageModal(String title, String text) {
		showMessage(title, text, true, true);
	}
	
	/**
	 * Show a message dialog modal and without buttons.
	 * Shortcut for <code>showMessage(title, text, true, false)</code>.
	 * 
	 * @param title the title of the dialog to display.
	 * @param text the text to show.
	 */
	public void showMessageModalWithoutButton(String title, String text) {
		showMessage(title, text, true, false);
	}
	
	/**
	 * Show a message dialog modal. Specify weather the dialog is modal and
	 * the buttons of the dialog are shown.
	 * 
	 * @param title the title of the dialog to display.
	 * @param text the text to show.
	 * @param modal sets if the dialog is modal.
	 * @param showButton specifies if a "Close"-Button is shown.
	 */
	public void showMessage(String title, String text, boolean modal, boolean showButton) {
		this.setText(title);
		messageLabel.setText(text);
		
		// Set button visibilty
		if (showButton) {
			closeButton.setVisible(true);
		} else {
			closeButton.setVisible(false);
		}
		
		// Set modality
		if (showButton) {
			this.setModal(false);
		} else {
			this.setModal(true);
		}
		
		this.center();
	}
}
