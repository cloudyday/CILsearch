package de.cil2012.cilsearch.server.elasticsearch;

import de.cil2012.cilsearch.server.model.exceptions.SearchException;

/**
 * This exception is thrown, if anything goes wrong whilst trying to search with ESSearch
 */
public class ESSearchException extends SearchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ESSearchException() {
	}

	public ESSearchException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ESSearchException(String arg0) {
		super(arg0);
	}

	public ESSearchException(Throwable arg0) {
		super(arg0);
	}

}
