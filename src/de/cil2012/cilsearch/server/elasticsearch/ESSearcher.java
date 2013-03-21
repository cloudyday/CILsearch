package de.cil2012.cilsearch.server.elasticsearch;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.sort.SortOrder;

import com.allen_sauer.gwt.log.client.Log;

import de.cil2012.cilsearch.server.DefaultSearcher;
import de.cil2012.cilsearch.server.model.MailContent;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.MailSearchResult;
import de.cil2012.cilsearch.server.model.exceptions.SearchException;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchRequest.DateOrder;
import de.cil2012.cilsearch.shared.model.MailSearchRequest.ResultField;
import de.cil2012.cilsearch.shared.model.QueryParam;

/**
 * This class implements the search functionality specified by @see Searcher for
 * the elasticsearch service
 */
public class ESSearcher extends DefaultSearcher {

	private final String TYPE = "mail";

	private String domainName;
	private TransportClient client;

	/**
	 * 
	 * @param domainName
	 *            - the name of the index
	 */
	public ESSearcher(String domainName) {
		this.client = ESSearch.createClient();
		this.domainName = domainName;
	}

	@Override
	public MailSearchResult search(MailSearchRequest searchRequest, int offset,
			int size) throws SearchException {
		// Meassure start time
		long startTime = System.currentTimeMillis();		
		
		QueryBuilder q = null;

		SearchRequestBuilder request = client.prepareSearch(domainName);
		request.setFrom(offset);
		request.setSize(size);
		
		request.addSort(
				"date",
				(searchRequest.getDateOrder() == DateOrder.DESCENDING) ? SortOrder.DESC
						: SortOrder.ASC);
		
		
		// if an all-fields-query-string is set AND and-queries are set => build
		// a QueryString-query with filters
		if (searchRequest.getSearchString() != null
				&& !searchRequest.getQueryAndParameter().isEmpty()) {

			QueryStringQueryBuilder query = QueryBuilders
					.queryString(searchRequest.getSearchString()).defaultOperator(Operator.AND);

			BoolFilterBuilder filter = new BoolFilterBuilder();

			for (QueryParam queryParam : searchRequest.getQueryAndParameter()) {
				if (queryParam.isExclude()) {
					filter.mustNot(new TermFilterBuilder(queryParam.getField()
							.toString().toLowerCase(), queryParam.getTerm()));
				} else {
					filter.must(new TermFilterBuilder(queryParam.getField()
							.toString().toLowerCase(), queryParam.getTerm()));
				}
			}
			
			
			if (searchRequest.getDateRangeEnd() != null
					&& searchRequest.getDateRangeStart() != null) {

				RangeFilterBuilder range = new RangeFilterBuilder("date");
				range.from(searchRequest.getDateRangeStart().getTime());
				range.to(searchRequest.getDateRangeEnd().getTime());
				
				filter.must(range);
			}
			
			request.setFilter(filter);
			q = query;

		} else if (searchRequest.getSearchString() != null
				&& searchRequest.getQueryAndParameter().isEmpty()) {
			QueryStringQueryBuilder query = QueryBuilders
					.queryString(searchRequest.getSearchString()).defaultOperator(Operator.AND);
			
			if (searchRequest.getDateRangeEnd() != null
					&& searchRequest.getDateRangeStart() != null) {

				RangeFilterBuilder range = new RangeFilterBuilder("date");
				range.from(searchRequest.getDateRangeStart().getTime());
				range.to(searchRequest.getDateRangeEnd().getTime());
				
				request.setFilter(range);
			}

			q = query;

			// if just AND-queries are set, build a BooleanQuery
		} else if (searchRequest.getSearchString() == null
				&& !searchRequest.getQueryAndParameter().isEmpty()) {

			BoolQueryBuilder query = QueryBuilders.boolQuery();
			for (QueryParam queryParam : searchRequest.getQueryAndParameter()) {
				if (queryParam.isExclude()) {
					query.mustNot(QueryBuilders.textQuery(queryParam.getField()
							.toString().toLowerCase(), queryParam.getTerm()));
				} else {
					query.must(QueryBuilders.textQuery(queryParam.getField()
							.toString().toLowerCase(), queryParam.getTerm()));
				}

			}

			if (searchRequest.getDateRangeEnd() != null
					&& searchRequest.getDateRangeStart() != null) {

				RangeFilterBuilder filter = new RangeFilterBuilder("date");
				filter.from(searchRequest.getDateRangeStart().getTime());
				filter.to(searchRequest.getDateRangeEnd().getTime());
				request.setFilter(filter);
			}

			q = query;

		} else {
			Log.error("MailSearchRequest is insufficiently defined");
			throw new ESSearchException(
					"MailSearchRequest is insufficiently defined");
		}

		
		request.setQuery(q);
//		System.out.println(request.toString());

		// SET THE RESULT FIELDS
		// by default all fields are result fields
		if (searchRequest.getResultFields().isEmpty()) {
			for (ResultField field : ResultField.values()) {
				request.addField(field.toString().toLowerCase());
			}

		} else {
			for (ResultField field : searchRequest.getResultFields()) {
				request.addField(field.toString().toLowerCase());
			}
		}

		// Execute the search request against elasticsearch
		long runTimeService = 0;
		SearchResponse response = null;
		try {
			long startTimeService = System.currentTimeMillis();
			response = request.execute().actionGet();
			runTimeService = System.currentTimeMillis() - startTimeService;
		} catch (Exception e) {
			Log.warn("Search request failed");
			throw (new ESSearchException(e.getMessage()));
		}

		return this.transformResults(response, startTime, runTimeService);
	}

	/**
	 * internal
	 * 
	 * @param response
	 * @return MailSearchResult
	 */
	private MailSearchResult transformResults(SearchResponse response, long startTime, long runTimeService) {
		// Transform all objects to MailObjects
		List<MailObject> messages = new LinkedList<MailObject>();
		for (SearchHit hit : response.getHits().getHits()) {
			MailObject mail = new MailObject();

			Map<String, SearchHitField> hitFields = hit.getFields();

			// message id
			mail.setMessageId(hit.getId());

			// subject, date, folder, from
			if (hitFields.containsKey("subject")) {
				mail.setSubject((String) hitFields.get("subject").getValue());
			}
			if (hitFields.containsKey("date")) {
				mail.setDate(new Date((Long) hitFields.get("date").getValue()));
			}
			if (hitFields.containsKey("folder")) {
				mail.setFolder((String) hitFields.get("folder").getValue());
			}
			if (hitFields.containsKey("from")) {

				Iterator<Object> it = hitFields.get("from").iterator();

				String from1 = (String) it.next();

				// From
				InternetAddress fromAddress = new InternetAddress();
				if (from1.contains("@")) {
					try {
						fromAddress = new InternetAddress(from1);
					} catch (AddressException e) {
						e.printStackTrace();
					}
				} else {
					try {
						fromAddress.setPersonal(from1);
						try {
							fromAddress.setAddress((String) it.next());
						} catch (NoSuchElementException e) {
							// Ignore it
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				mail.setFrom(fromAddress);
			}

			// get recipient addresses
			String[] recipientType = { "recipients_to", "recipients_cc",
					"recipients_bcc" };

			for (int j = 0; j < recipientType.length; j++) {
				if (!hitFields.containsKey(recipientType[j])) {
					continue;
				}

				Iterator<Object> it = hitFields.get(recipientType[j])
						.iterator();

				List<Address> recp = new LinkedList<Address>();
				while (it.hasNext()) {
					String s = (String) it.next();

					InternetAddress addr = new InternetAddress();
					if (s.contains("@")) {
						try {
							addr = new InternetAddress(s);
						} catch (AddressException e) {
							e.printStackTrace();
						}
					} else {
						try {
							addr.setPersonal(s);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					recp.add(addr);
				}

				switch (j) {
				case 0:
					mail.setRecipientsTO(recp);
					break;
				case 1:
					mail.setRecipientsCC(recp);
					break;
				case 2:
					mail.setRecipientsBCC(recp);
					break;
				}
			}

			// content preview and other names
			List<MailContent> contents = new LinkedList<MailContent>();

			if (hitFields.containsKey("content_main_preview")) {
				MailContent main = new MailContent();
				main.setContent((String) hitFields.get("content_main_preview")
						.getValue());
				main.setMainContent(true);
				contents.add(main);
			}

			if (hitFields.containsKey("content_other_names")) {
				Iterator<Object> it = hitFields.get("content_other_names")
						.iterator();
				while (it.hasNext()) {
					MailContent content = new MailContent();
					content.setName((String) it.next());
					contents.add(content);
				}
			}

			if (!contents.isEmpty()) {
				mail.setContents(contents);
			}

			messages.add(mail);
		}

		// Get total time
		long runTime = System.currentTimeMillis() - startTime;
		
		// Create our result object
		MailSearchResult searchResult = new MailSearchResult(messages, response
				.getHits().getTotalHits(), runTimeService, runTime);

		return searchResult;
	}

	@Override
	public MailSearchResult getAllMailsIdAndDate(int offset, int size)
			throws SearchException {
		long startTime = System.currentTimeMillis();
		
		QueryBuilder q = QueryBuilders.matchAllQuery();

		SearchRequestBuilder request = client.prepareSearch(domainName);
		request.setFrom(offset);
		request.setSize(size);
		request.addSort("date", SortOrder.ASC);
		request.addField("date");
		request.setQuery(q);

		long runTimeService = 0;
		SearchResponse response = null;
		try {
			long startTimeService = System.currentTimeMillis();
			response = request.execute().actionGet();
			runTimeService = System.currentTimeMillis() - startTimeService;
		} catch (Exception e) {
			Log.warn("Search request failed");
			throw (new ESSearchException(e.getMessage()));
		}

		return this.transformResults(response, startTime, runTimeService);
	}

	@Override
	public MailSearchResult getMailByMessageId(String messageId)
			throws SearchException {
		long startTime = System.currentTimeMillis();
		
		QueryBuilder q = QueryBuilders.idsQuery(TYPE).addIds(messageId);

		SearchRequestBuilder request = client.prepareSearch(domainName);
		request.addField("date");
		request.setQuery(q);

		long runTimeService = 0;
		SearchResponse response = null;
		try {
			long startTimeService = System.currentTimeMillis();
			response = request.execute().actionGet();
			runTimeService = System.currentTimeMillis() - startTimeService;
		} catch (Exception e) {
			Log.warn("Search request failed");
			throw (new ESSearchException(e.getMessage()));
		}
		
		return this.transformResults(response, startTime, runTimeService);
	}

}
