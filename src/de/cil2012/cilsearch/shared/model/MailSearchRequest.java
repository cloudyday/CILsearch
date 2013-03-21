package de.cil2012.cilsearch.shared.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.datepicker.client.CalendarUtil;



/**
 * This class is used to construct an mail search request,
 * independent from a concrete search service.
 */
public class MailSearchRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String searchString;
	private List<QueryParam> queryAndParameters;
	private DateOrder dateOrder;
	private Set<ResultField> resultFields;
	private Date dateRangeStart;
	private Date dateRangeEnd;

	/**
	 * Creates a new empty search request.
	 */
	public MailSearchRequest() {
		setSearchString(null);
		setQueryAnd(new LinkedList<QueryParam>());
		setResultFields(new HashSet<MailSearchRequest.ResultField>());
		setDateOrder(DateOrder.DESCENDING);
		dateRangeStart = null;
		dateRangeEnd = null;
	}
	
	/**
	 * Create a search request which searches ALL fields with the specified string.
	 * @param searchString the string to search.
	 */
	public MailSearchRequest(String searchString) {
		this();
		setSearchString(searchString);
	}
	
	/**
	 * Add a queryParam to the request. All query params are connected with AND.
	 * @param queryParam the {@link QueryParam} object containing the field and term.
	 */
	public void addQueryArgument(QueryParam queryParam) {
		queryAndParameters.add(queryParam);
	}
	
	/**
	 * Convenience method for creating a "get all SINCE" date-query.
	 * You can add a date filter for a duration which starts with emails that
	 * have arrived NOW and ends with $duration $durationScope e.g. emails from
	 * last 2 weeks: $durationScope = DateFilter.WEEK, $duration = 2.
	 * @param durationScope the unit in which duration is specified.
	 * @param duration the duration in the given unit.
	 */
	public void setDateSinceQuery(DateFilter durationScope, int duration) {
		// Set end time to current time
		dateRangeEnd = new Date();
		
		// Substract the duration from endTime to get startTime
		dateRangeStart = new Date(dateRangeEnd.getTime());
		switch(durationScope) {
		case DAY:
			CalendarUtil.addDaysToDate(dateRangeStart, -duration);
			break;
		case WEEK:
			CalendarUtil.addDaysToDate(dateRangeStart, -duration * 7);
			break;
		case MONTH:
			CalendarUtil.addMonthsToDate(dateRangeStart, -duration);
			break;
		case YEAR:
			CalendarUtil.addMonthsToDate(dateRangeStart, -duration * 12);
			break;
		}
	}
		
	/**
	 * Construct a date range query.
	 * @param start the earliest message date to look for.
	 * @param end the latest message date to look for.
	 */
	public void setDateRangeQuery(Date start, Date end) {
		if(start.after(end)) {
			throw new IllegalArgumentException("Illegal MailSearchRequest: start > end");
		}

		dateRangeStart = start;
		dateRangeEnd = end;
	}
	
	/**
	 * Adds a result field to our query. Note that if this there are no
	 * result fields added to the request, the service will automatically
	 * pull all message fields. As soon as you add one result field, only
	 * this (and addionally added fields) will be pulled.
	 * @param field the result field to add.
	 */
	public void addResultField(ResultField field) {
		resultFields.add(field);
	}

	
	
	/**
	 * Gets the search string that is used for an all fields query.
	 * @return the search string.
	 */
	public String getSearchString() {
		return searchString;
	}
	
	/**
	 * Sets the search string that is used for an all fields query.
	 * @param searchString the search string.
	 */
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	/**
	 * Gets the date ordering used for the results.
	 * @return the date ordering.
	 */
	public DateOrder getDateOrder() {
		return dateOrder;
	}
	
	/**
	 * Sets the date ordering used for the results.
	 * @param dateOrder the date ordering.
	 */
	public void setDateOrder(DateOrder dateOrder) {
		this.dateOrder = dateOrder;
	}
	
	/**
	 * Gets the start date of the date range to look for mails in.
	 * @return the start date.
	 */
	public Date getDateRangeStart() {
		return dateRangeStart;
	}

	/**
	 * Gets the end date of the date range to look for mails in.
	 * @return the end date.
	 */
	public Date getDateRangeEnd() {
		return dateRangeEnd;
	}

	/**
	 * Gets the message fields that should be contained in our result.
	 * @return the fields contained in the result messages.
	 */
	public Set<ResultField> getResultFields() {
		return resultFields;
	}

	/**
	 * Sets the message fields that should be contained in our result.
	 * @param resultFields the fields contained in the result messages.
	 */
	public void setResultFields(Set<ResultField> resultFields) {
		if (resultFields == null) {
			this.resultFields = new HashSet<MailSearchRequest.ResultField>();
		} else {
			this.resultFields = resultFields;
		}
	}

	/**
	 * Gets all {@link QueryParam}s that are contained in this request.
	 * @return this parameters contained in the request.
	 */
	public List<QueryParam> getQueryAndParameter() {
		return queryAndParameters;
	}

	/**
	 * Sets all {@link QueryParam}s that are contained in this request.
	 * @param queryAndParameters this parameters contained in the request.
	 */
	public void setQueryAnd(List<QueryParam> queryAndParameters) {
		this.queryAndParameters = queryAndParameters;
	}
	
	
	
	/**
	 * Specifies the time frame to look for messages in.
	 */
	public enum DateFilter {
		ALL, YEAR, MONTH, WEEK, DAY
	}

	/**
	 * Specifies the fields that should be loaded for the messages.
	 */
	public enum ResultField {
		FROM, RECIPIENTS_BCC, RECIPIENTS_CC, RECIPIENTS_TO, SUBJECT, CONTENT_MAIN_PREVIEW, CONTENT_OTHER_NAMES, FOLDER, DATE
	}
	
	/**
	 * Specifies whether the results are order ascending or descending by date.
	 */
	public enum DateOrder {
		ASCENDING(1), DESCENDING(0);
		private int value;
		
		private DateOrder(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}

}
