package de.cil2012.cilsearch.server.amazon;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.mortbay.log.Log;

import com.searchtechnologies.cloudsearch.api.CloudSearchClient;
import com.searchtechnologies.cloudsearch.api.CloudSearchDocResult;
import com.searchtechnologies.cloudsearch.api.CloudSearchQuery;
import com.searchtechnologies.cloudsearch.api.CloudSearchQueryException;
import com.searchtechnologies.cloudsearch.api.CloudSearchResult;

import de.cil2012.cilsearch.server.DefaultSearcher;
import de.cil2012.cilsearch.server.Searcher;
import de.cil2012.cilsearch.server.model.MailContent;
import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.MailSearchResult;
import de.cil2012.cilsearch.server.model.exceptions.SearchException;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.QueryParam;
import de.cil2012.cilsearch.shared.model.MailSearchRequest.DateOrder;
import de.cil2012.cilsearch.shared.model.MailSearchRequest.ResultField;
import de.cil2012.cilsearch.shared.model.QueryParam.SearchField;

/**
 * This class implements the mail search feature using the searchtechnologies.com AWS search library
 * @see Searcher
 */
public class AWSSearch extends DefaultSearcher {

	private CloudSearchClient searchService;

	public AWSSearch(String domainName, String domainId, String region) {
		searchService = AWSCloudSearch.getSearchServiceClientInstance(domainName, domainId, region);
	}


	@Override
	public MailSearchResult search(MailSearchRequest searchRequest, int offset, int size) throws SearchException {
		// Keep start time in mind
		long startTime = System.currentTimeMillis();
		
		CloudSearchQuery query = new CloudSearchQuery();
		query.setStart(offset);
		query.setSize(size);
		
		
		// add the search query
		if(searchRequest.getSearchString() != null) {
			query.setQuery(searchRequest.getSearchString());
		}
		
		// add the boolean AND queries
		List<QueryParam> queryAnd = searchRequest.getQueryAndParameter();
		if(!queryAnd.isEmpty()) {
			for(QueryParam queryParam : queryAnd) {
				query.addFilter(queryParam.getField().toString().toLowerCase(), queryParam.getTerm(), queryParam.isExclude(), false);
			}	
		}

		
		if(searchRequest.getResultFields().isEmpty()) {
			// by default all fields are result fields
			
			for(ResultField field : ResultField.values()) {
				query.addResultField(field.toString().toLowerCase());
			}

		} else {
			for(ResultField field : searchRequest.getResultFields()) {
				query.addResultField(field.toString().toLowerCase());
			}
		}
		
		
		query.addSort("date", (searchRequest.getDateOrder() == DateOrder.DESCENDING) ? false : true);

		
		if(searchRequest.getDateRangeEnd() != null & searchRequest.getDateRangeStart() != null) {
			query.addFilter("date", searchRequest.getDateRangeStart().getTime() / 1000 + ".." + searchRequest.getDateRangeEnd().getTime(), false, true);
		}

		// make the call to cloudsearch
		long runTimeService = 0;
		CloudSearchResult queryResult = null;
		try {
			long startTimeService = System.currentTimeMillis();
			queryResult = searchService.search(query);
			runTimeService = System.currentTimeMillis() - startTimeService;
		} catch (CloudSearchQueryException e1) {
			Log.warn("Search request failed");
			throw new AWSSearchException(e1.getMessage());
		}

		// convert the results to MailObjects and add them to a list
		List<MailObject> messages = new LinkedList<MailObject>();
		for (int i = 0; i < queryResult.getNumResultsReturned(); i++) {
			MailObject mail = new MailObject();

			CloudSearchDocResult docResult = queryResult.getDoc(i);
			// message id
			mail.setMessageId(docResult.getId());
			
			// subject, date, folder, from
			ArrayList<String> field;
			field = docResult.getFieldValues("subject");
			if(field != null && !field.isEmpty()) {
				String subject = docResult.getFieldValues("subject").get(0);
				mail.setSubject(subject);
			}
			field = docResult.getFieldValues("date");
			if(field != null && !field.isEmpty()) {
				mail.setDate(new Date(Long.parseLong(docResult.getFieldValues("date").get(0)) * 1000));
			}
			field = docResult.getFieldValues("folder");
			if(field != null && !field.isEmpty()) {
				mail.setFolder(docResult.getFieldValues("folder").get(0));
			}
			field = docResult.getFieldValues("from");
			if(field != null && !field.isEmpty()) {
				// From
				InternetAddress fromAddress = new InternetAddress();
				if (docResult.getFieldValues("from").get(0).contains("@")) {
					try {
						fromAddress = new InternetAddress(docResult.getFieldValues("from").get(0));
					} catch (AddressException e) {
						// Ignore because we can't do anything if we somehow get a invalid mail address.
						Log.debug("Invalid mail address: " + docResult.getFieldValues("from").get(0));
					}
				} else {
					try {
						fromAddress.setPersonal(docResult.getFieldValues("from").get(0));
						if (docResult.getFieldValues("from").size() >= 2)
							fromAddress.setAddress(docResult.getFieldValues("from").get(1));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				mail.setFrom(fromAddress);
			}
	
			

			// get recipient addresses
			String[] recipientType = { "recipients_to", "recipients_cc", "recipients_bcc" };

			for (int j = 0; j < recipientType.length; j++) {
				ArrayList<String> recipients = docResult.getFieldValues(recipientType[j]);
				if (recipients == null) {
					continue;
				}
				List<Address> recp = new LinkedList<Address>();
				for (String s : recipients) {
					InternetAddress addr = new InternetAddress();
					if (s.contains("@")) {
						try {
							addr = new InternetAddress(s);
						} catch (AddressException e) {
							// Ignore because we can't do anything if we somehow get a invalid mail address.
							Log.debug("Invalid mail address: " + s);
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
			
			field = docResult.getFieldValues("content_main_preview");
			if(field != null && !field.isEmpty()) {
				MailContent main = new MailContent();
				main.setContent(docResult.getFieldValues("content_main_preview").get(0));
				main.setMainContent(true);
				contents.add(main);
			}


			ArrayList<String> contentOtherNames = docResult.getFieldValues("content_other_names");
			if (contentOtherNames != null) {
				for (String s : contentOtherNames) {
					MailContent content = new MailContent();
					content.setName(s);
					contents.add(content);
				}
			}
			if(!contents.isEmpty()) {
				mail.setContents(contents);
			}

			messages.add(mail);
		}
		
		// Calculate runtime
		long runTime = System.currentTimeMillis() - startTime;
		
		// create our search result
		MailSearchResult searchResult = new MailSearchResult(messages,
				queryResult.getTotalHits(),
				runTimeService, runTime);

		return searchResult;
	}
	
	/**
	 * Get all mails with id and date fields stored in Amazon CloudSearch.
	 * @return all mails with id and date fields.
	 */
	@Override
	public MailSearchResult getAllMailsIdAndDate(int offset, int size) throws SearchException {
		MailSearchRequest searchRequest = new MailSearchRequest();
		HashSet<ResultField> resultFields = new HashSet<MailSearchRequest.ResultField>();
		resultFields.add(ResultField.DATE);
		searchRequest.setResultFields(resultFields);
		searchRequest.addQueryArgument(QueryParam.include(SearchField.FOLDER, "*"));
		
		return this.search(searchRequest, offset, size);
	}
	

	@Override
	public MailSearchResult getMailByMessageId(String messageId) throws SearchException {
		MailSearchRequest searchRequest = new MailSearchRequest();
		searchRequest.addQueryArgument(QueryParam.include(SearchField.ID_SEARCH, "search_"+messageId));
		
		MailSearchResult result;
		result = this.search(searchRequest, 0, 1);
		
		return result;
	}
	

}
