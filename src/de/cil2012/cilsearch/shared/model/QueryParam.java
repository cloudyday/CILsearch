package de.cil2012.cilsearch.shared.model;

import java.io.Serializable;


/**
 * This class is used to express a boolean query parameter such as
 * subject='Hello' or from='Oliver'.
 */
public class QueryParam implements Serializable  {

	private static final long serialVersionUID = 2651913163025460221L;

	private SearchField field;
	private String term;
	private boolean isExclude;

	/**
	 * Default private constructor to make GWT serialization work. Who knows this?
	 * See: https://developers.google.com/web-toolkit/doc/latest/tutorial/RPC#serialize
	 */
	private QueryParam() {}
	
	/**
	 * Constructs a query param where you have the ability to choose if you want the param to be a "NOT param" by
	 * setting ixExclude to true.
	 * 
	 * @param field the field to contain term.
	 * @param term the term to search for.
	 * @param isExclude if the query should be negotiated.
	 */
	private QueryParam(SearchField field, String term, boolean isExclude) {
		// Check for non empty arguments
		if (field == null) {
			throw new NullPointerException("Parameter 'field' for QueryParam should not be null");
		}
		if (term == null) {
			throw new NullPointerException("Parameter 'term' for QueryParam should not be null");
		}
		
		// Set values of our object
		setField(field);
		setTerm(term);
		setExclude(isExclude);
	}
	
	/**
	 * Create a new include {@link QueryParam} for given field and term.
	 * @param field the field to contain term.
	 * @param term the term to search for.
	 * @return the {@link QueryParam} object.
	 */
	public static QueryParam include(SearchField field, String term) {
		return new QueryParam(field, term, false);
	}
	
	/**
	 * Create a new exclude {@link QueryParam} for given field and term.
	 * @param field the field not to contain term.
	 * @param term the term field should not contain.
	 * @return the {@link QueryParam} object.
	 */
	public static QueryParam exclude(SearchField field, String term) {
		return new QueryParam(field, term, true);
	}
	
	/**
	 * Get the field to look in for a certain term.
	 * @return the field.
	 */
	public SearchField getField() {
		return field;
	}

	// Query param should be immutable.
	private void setField(SearchField field) {
		this.field = field;
	}
	
	/**
	 * Get the term to look for.
	 * @return the term.
	 */
	public String getTerm() {
		return term;
	}
	
	// Query param should be immutable.
	private void setTerm(String term) {
		this.term = term;
	}

	/**
	 * Get wheather this query should be negotiated.
	 * @return true if the query is negotiated.
	 */
	public boolean isExclude() {
		return isExclude;
	}

	// Query param should be immutable.
	private void setExclude(boolean isExclude) {
		this.isExclude = isExclude;
	}
	
	/**
	 * Specifies the field to search in.
	 */
	public enum SearchField {
		FOLDER, FROM, RECIPIENTS_BCC, RECIPIENTS_CC, RECIPIENTS_TO, SUBJECT, CONTENT, ID_SEARCH
	}

}
