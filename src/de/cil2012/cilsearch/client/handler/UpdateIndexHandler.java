package de.cil2012.cilsearch.client.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.cil2012.cilsearch.client.InformationMessageDialog;
import de.cil2012.cilsearch.client.services.UpdateIndexService;
import de.cil2012.cilsearch.client.services.UpdateIndexServiceAsync;

/**
 * Called to start the update process of our index at our search service.
 */
public class UpdateIndexHandler implements ClickHandler {
	private InformationMessageDialog dialog;
	
	public UpdateIndexHandler(InformationMessageDialog dialog) {
		this.dialog = dialog;
	}
	
	/**
	 * Create a remote service proxy to talk to the server-side UpdateIndexService.
	 */
	private final UpdateIndexServiceAsync updateIndexService = GWT
			.create(UpdateIndexService.class);
	
	@Override
	public void onClick(ClickEvent event) {
		// Display a model message when starting the update process
		dialog.showMessageModalWithoutButton("Updating index...", "Update index process is running.");
		
		// Run the actual service
		updateIndexService.updateIndex(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				dialog.hide();
				dialog.showMessageModal("Success", "Successfully updated our index!");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				dialog.hide();
				dialog.showMessageModal("ERROR!", "Something very bad happened!");	
			}
		});
	}

}
