package de.cil2012.cilsearch.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.cil2012.cilsearch.client.handler.ExtendedSearchHandler;
import de.cil2012.cilsearch.client.handler.StandardSearchHandler;
import de.cil2012.cilsearch.client.handler.UpdateIndexHandler;

/**
 * Starting point for the whole GTW Application.
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CILSearch implements EntryPoint {
	// Main interface elements
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel selectSearchPanel = new HorizontalPanel();
	private HorizontalPanel standardSearch = new HorizontalPanel();
	private ExtendedSearchWidget extendedSearch = new ExtendedSearchWidget();
	private Button standardSearchButton = new Button("Normal Search");
	private Button extendedSearchButton = new Button("Extended Search");
	private Label resultLabel = new Label();
	private TextBox searchField = new TextBox();
	private Button searchButton = new Button("Suchen");
	private MailPreviewWidget resultTable = new MailPreviewWidget();
	private Button updateIndexButton = new Button("Update Index");
	
	// Message box for all kind of messages
	private InformationMessageDialog messageDialog = new InformationMessageDialog();

	/**
	 * Starting point for our application
	 */
	@Override
	public void onModuleLoad() {
		// Initalize our logger
		Log.setUncaughtExceptionHandler();
		
		// Start the setup of our interface
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				createInterface();
			}
		});
	}
	
	/**
	 * Creates the applications interface.
	 */
	public void createInterface() {
		// Style elements
		selectSearchPanel.addStyleName("selectSearchPanel");
		standardSearch.addStyleName("searchPanel");
		extendedSearch.addStyleName("searchPanel");
		searchField.addStyleName("searchBox");
		searchButton.addStyleName("searchButton");
		resultLabel.addStyleName("resultLabel");
		
		// Stack together our search selection
		selectSearchPanel.add(standardSearchButton);
		selectSearchPanel.add(extendedSearchButton);
		
		// Stack together our search interface
		standardSearch.add(searchField);
		standardSearch.add(searchButton);
		
		// Stack together the main interface
		mainPanel.add(selectSearchPanel);
		mainPanel.add(standardSearch);
		mainPanel.add(extendedSearch);
		mainPanel.add(resultLabel);
		mainPanel.add(resultTable);

		// Add our main panel to our html page
		RootPanel.get("searchInterface").add(mainPanel);
		
		// Add button to update index to footer
		RootPanel.get("footerControls").add(updateIndexButton);

		// Add a handler to send the search to the server
		StandardSearchHandler standardHandler = new StandardSearchHandler(
				searchField, searchButton,
				resultTable, resultLabel);
		searchButton.addClickHandler(standardHandler);
		searchField.addKeyUpHandler(standardHandler);
		
		ExtendedSearchHandler extendedHandler = new ExtendedSearchHandler(
				extendedSearch, extendedSearch.getSearchButton(),
				resultTable, resultLabel);
		extendedSearch.getSearchButton().addClickHandler(extendedHandler);
		
		// Add a handler to update our index 
		UpdateIndexHandler updateIndexHandler = 
				new UpdateIndexHandler(messageDialog);
		updateIndexButton.addClickHandler(updateIndexHandler);
		
		// Set the default search to standard
		extendedSearch.setVisible(false);
		
		// Add handlers for switching search
		standardSearchButton.addClickHandler(new SwitchSearchTypeHandler(SearchType.STANDARD));
		extendedSearchButton.addClickHandler(new SwitchSearchTypeHandler(SearchType.EXTENDED));
	}
	
	/**
	 * Switches the displayed search type.
	 */
	private class SwitchSearchTypeHandler implements ClickHandler {
		private SearchType switchToType;
		
		/**
		 * Creates a new handler for switching search.
		 * @param switchToType the type to switch to when clicked.
		 */
		public SwitchSearchTypeHandler(SearchType switchToType) {
			this.switchToType = switchToType;
		}
		
		@Override
		public void onClick(ClickEvent event) {
			switch(switchToType) {
			case STANDARD:
				standardSearch.setVisible(true);
				extendedSearch.setVisible(false);
				break;
			case EXTENDED:
				standardSearch.setVisible(false);
				extendedSearch.setVisible(true);
				break;
			}
		}
	}
	
	/**
	 * Describes the type of search used.
	 */
	private enum SearchType {
		STANDARD, EXTENDED;
	}
}
