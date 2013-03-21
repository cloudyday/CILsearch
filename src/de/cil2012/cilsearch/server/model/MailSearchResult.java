package de.cil2012.cilsearch.server.model;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.cil2012.cilsearch.shared.model.MailRepresentation;
import de.cil2012.cilsearch.shared.model.MailSearchResultRepresentation;

/**
 * This class is used to wrap the search results obtained by the search service 
 * that was used. To give search results to the client side, you can use the
 * <code>getRepresentation</code> method.
 */
public class MailSearchResult implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<MailObject> results;
	private long totalHits;
	private long searchTimeService;
	private long searchTimeTotal;

	/**
	 * Create a new MailSearchResult with the given results, the given amount
	 * of total hits, the search time on amazon and the total search time.
	 * @param results the results to contain.
	 * @param totalHits the number of total hits.
	 * @param searchTimeService the search time returned by the search service.
	 * @param searchTimeTotal the total time to perform the search request.
	 */
	public MailSearchResult(List<MailObject> results, long totalHits,
			long searchTimeService, long searchTimeTotal) {
		setResults(results);
		setTotalHits(totalHits);
		setSearchTimeService(searchTimeService);
		setSearchTimeTotal(searchTimeTotal);
	}

	/**
	 * Creates a <code>MailSearchResultRepresentation</code> out of this object.
	 * The representation can be used to send result data to the client.
	 * @return the corresponding representation.
	 */
	public MailSearchResultRepresentation getRepresentation() {
		// Guava function for converting mail objects to representations
		Function<MailObject, MailRepresentation> convert = 
				new Function<MailObject, MailRepresentation>() {
					@Override
					public MailRepresentation apply(MailObject object) {
						return object.getRepresentation();
					}
				};
		
		// Create our representation object
		MailSearchResultRepresentation representation = 
				new MailSearchResultRepresentation();
		representation.setMessages(
				Lists.transform(results, convert).toArray(new MailRepresentation[0]));
		representation.setSearchTimeService(searchTimeService);
		representation.setSearchTimeTotal(searchTimeTotal);
		representation.setTotalHits(totalHits);
		
		return representation;
	}
	
	/**
	 * Get the results returned by the query.
	 * @return the returned results.
	 */
	public List<MailObject> getResults() {
		return results;
	}

	// Result should not be modified after the object has been created.
	private void setResults(List<MailObject> results) {
		this.results = results;
	}
	
	/**
	 * Get the time used by the search service for processing the request.
	 * @return the time used by the search service.
	 */
	public long getSearchTimeService() {
		return searchTimeService;
	}
	
	// Result should not be modified after the object has been created.
	private void setSearchTimeService(long searchTimeService) {
		this.searchTimeService = searchTimeService;
	}
	
	/** 
	 * Get the total time used by our methods between calling the search
	 * service and getting the results back.
	 * @return the total amount of time used.
	 */
	public long getSearchTimeTotal() {
		return searchTimeTotal;
	}
	
	// Results should not be modified after the object has been created.
	private void setSearchTimeTotal(long searchTimeTotal) {
		this.searchTimeTotal = searchTimeTotal;
	}

	/**
	 * Get the number of total hits for our search request. This number must
	 * not nescessarily be equal to the size of our results array, if the
	 * result was set to contain a maximum nuber of results.
	 * @return the total number of hits found by the search service.
	 */
	public long getTotalHits() {
		return totalHits;
	}

	// Results should not be modified after the object has been created.
	private void setTotalHits(long totalHits) {
		this.totalHits = totalHits;
	}

}
