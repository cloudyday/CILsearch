package de.cil2012.cilsearch.client;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchRequest.DateFilter;
import de.cil2012.cilsearch.shared.model.QueryParam;
import de.cil2012.cilsearch.shared.model.QueryParam.SearchField;

/**
 * A widget that contains extended options for searching mail messages
 * just as choosing a date range or excluded keywords.
 */
public class ExtendedSearchWidget extends Composite {
	private VerticalPanel mainPanel = new VerticalPanel();
	private Label fromLabel = new Label("From");
	private HorizontalPanel fromPanel = new HorizontalPanel();
	private ToggleButton fromToggle = new ToggleButton("INCLUDES", "EXCLUDES");
	private TextBox fromField = new TextBox();
	private Label toLabel = new Label("To");
	private HorizontalPanel toPanel = new HorizontalPanel();
	private ToggleButton toToggle = new ToggleButton("INCLUDES", "EXCLUDES");
	private TextBox toField = new TextBox();
	private Label subjectLabel = new Label("Subject");
	private HorizontalPanel subjectPanel = new HorizontalPanel();
	private ToggleButton subjectToggle = new ToggleButton("INCLUDES", "EXCLUDES");
	private TextBox subjectField = new TextBox();
	private Label containsLabel = new Label("Contains");
	private TextBox containsField = new TextBox();
	private Label excludesLabel = new Label("Excludes");
	private TextBox excludesField = new TextBox();
	private Label dateRangeLabel = new Label("Date range");
	private HorizontalPanel dateRangePanel = new HorizontalPanel();
	private TextBox durationField = new TextBox();
	private ListBox durationUnitList = new ListBox();
	private Button searchButton = new Button("Search");
	
	/**
	 * Creates a new search widget that contains extended options for 
	 * searching mail messages.
	 */
	public ExtendedSearchWidget() {
		initInterface();
		initWidget(mainPanel);
		
		// Set main style class
		this.setStyleName("extendedSearchPanel");
	}
	
	/**
	 * Inits the interface of our extended search widget.
	 */
	private void initInterface() {
		// Stack everything together
		fromPanel.add(fromField);
		fromPanel.add(fromToggle);
		
		toPanel.add(toField);
		toPanel.add(toToggle);
		
		subjectPanel.add(subjectField);
		subjectPanel.add(subjectToggle);
				
		mainPanel.add(fromLabel);
		mainPanel.add(fromPanel);
		mainPanel.add(toLabel);
		mainPanel.add(toPanel);
		mainPanel.add(subjectLabel);
		mainPanel.add(subjectPanel);
		mainPanel.add(containsLabel);
		mainPanel.add(containsField);
		mainPanel.add(excludesLabel);
		mainPanel.add(excludesField);
		mainPanel.add(dateRangeLabel);
		
		dateRangePanel.add(durationField);
		dateRangePanel.add(durationUnitList);
		dateRangePanel.add(searchButton);
		mainPanel.add(dateRangePanel);
		
		// Init our duration list
		durationUnitList.setVisibleItemCount(1); // Make it a combobox
		durationUnitList.addItem("Days");
		durationUnitList.addItem("Weeks");
		durationUnitList.addItem("Months");
		durationUnitList.addItem("Years");
		
		// Ensure duration is always a number
		durationField.addKeyPressHandler(new NumbersOnlyHandler());
		
		// Add css for nice styling
		fromField.addStyleName("toogleField");
		toField.addStyleName("toogleField");
		subjectField.addStyleName("toogleField");
		durationField.addStyleName("durationField");
		durationUnitList.addStyleName("durationList");
		searchButton.addStyleName("searchButton");
	}
	
	/**
	 * Create a request from the current input data.
	 * @return the request.
	 */
	public MailSearchRequest getRequest() {
		MailSearchRequest request = new MailSearchRequest();
		
		if (!fromField.getText().trim().isEmpty()) {
			if (!fromToggle.isDown()) {
				request.addQueryArgument(QueryParam.include(SearchField.FROM, fromField.getText().trim()));
			} else {
				request.addQueryArgument(QueryParam.exclude(SearchField.FROM, fromField.getText().trim()));
			}
		}
		if (!toField.getText().trim().isEmpty()) {
			if (!toToggle.isDown()) {
				request.addQueryArgument(QueryParam.include(SearchField.RECIPIENTS_TO, toField.getText().trim()));
			} else {
				request.addQueryArgument(QueryParam.exclude(SearchField.RECIPIENTS_TO, toField.getText().trim()));
			}
		}
		if (!subjectField.getText().trim().isEmpty()) {
			if (!subjectToggle.isDown()) {
				request.addQueryArgument(QueryParam.include(SearchField.SUBJECT, subjectField.getText().trim()));
			} else {
				request.addQueryArgument(QueryParam.exclude(SearchField.SUBJECT, subjectField.getText().trim()));
			}
		}
		if (!containsField.getText().trim().isEmpty()) {
			request.setSearchString(containsField.getText().trim());
		}
		if (!excludesField.getText().trim().isEmpty()) {
			// Add a minus before all keywords to negotiate the request
			String currentSearch = request.getSearchString();
			String excludes = excludesField.getText().trim();
			String newSearch = currentSearch;
			
			if (currentSearch != null && !currentSearch.isEmpty()) {
				newSearch += " ";
			} else {
				newSearch = "";
			}
			
			newSearch += "-" + excludes.replaceAll(" ", " -");
			request.setSearchString(newSearch);
		}
		if (!durationField.getText().isEmpty()) {
			// Figure the unit out
			DateFilter filterUnit = null;
			switch (durationUnitList.getSelectedIndex()) {
			case 0: // Days
				filterUnit = DateFilter.DAY;
				break;
			case 1: // Weeks
				filterUnit = DateFilter.WEEK;
				break;
			case 2: // Months
				filterUnit = DateFilter.MONTH;
				break;
			case 3: // Years
				filterUnit = DateFilter.YEAR;
				break;
			}
			
			request.setDateSinceQuery(filterUnit, Integer.parseInt(durationField.getText()));
		}
		
		return request;
	}
	
	/**
	 * Exposes the search button so we can add a click handler from outside.
	 * @return the search button.
	 */
	public Button getSearchButton() {
		return searchButton;
	}
	
	/**
	 * Handler for allowing only numeric input in text field.
	 */
	class NumbersOnlyHandler implements KeyPressHandler {
        @Override
        public void onKeyPress(KeyPressEvent event) {
            if(!Character.isDigit(event.getCharCode()))
                ((IntegerBox)event.getSource()).cancelKey();
        }
    }
}
