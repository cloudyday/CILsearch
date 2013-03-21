package de.cil2012.cilsearch.server.model.exceptions;

/**
 * A subclass of this exception should be thrown if a search request has failed
 */
public abstract class SearchException extends Throwable {

	private static final long serialVersionUID = 1L;

	public SearchException() {
		super();
	}

	public SearchException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SearchException(String arg0) {
		super(arg0);
	}

	public SearchException(Throwable arg0) {
		super(arg0);
	}


	
}
