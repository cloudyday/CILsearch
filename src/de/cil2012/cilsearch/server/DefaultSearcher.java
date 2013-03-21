package de.cil2012.cilsearch.server;

import java.util.List;

import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.MailSearchResult;
import de.cil2012.cilsearch.server.model.exceptions.SearchException;

/**
 * Provides standard method implementations that are common for most 
 * search services and therefore can be refactored.
 */
public abstract class DefaultSearcher implements Searcher {

	// The maximum amount of messages that can be loaded once at a time
	private final int MAX_HIT_SIZE = 100;
	
	/**
	 * Gets the id and date fiels of all messages saved to the search service.
	 * @return the list of all message ids and dates.
	 */
	@Override
	public MailSearchResult getAllMailsIdAndDate() throws SearchException {
		// Load the first results until MAX_HIT_SIZE
		MailSearchResult partResult = this.getAllMailsIdAndDate(0, MAX_HIT_SIZE);
		List<MailObject> mails = partResult.getResults();
		long addedSearchTimeService = partResult.getSearchTimeService();
		long addedSearchTimeTotal = partResult.getSearchTimeTotal();
		
		// Check if there are more messages than MAX_HIT_SIZE
		// fitting to the search request
		if (partResult.getTotalHits() > MAX_HIT_SIZE) {
			// Calculate how many times we have to load MAX_HIT_SIZE
			// messages to get all messages loaded
			int totalHits = (int) partResult.getTotalHits();
			int times = (totalHits - 1) / MAX_HIT_SIZE;
			
			// Perform the request the calculate amount of times and
			// save results into our mails list
			MailSearchResult nextResult;
			int offset = 0;
			for (int i = 0; i < times; i++) {
				offset += MAX_HIT_SIZE;
				nextResult = this.getAllMailsIdAndDate(offset, MAX_HIT_SIZE);
				mails.addAll(nextResult.getResults());
				addedSearchTimeService += nextResult.getSearchTimeService();
				addedSearchTimeTotal += nextResult.getSearchTimeTotal();
			}	
		}
		
		return new MailSearchResult(mails, partResult.getTotalHits(),
				addedSearchTimeService, addedSearchTimeTotal);
	}

}
