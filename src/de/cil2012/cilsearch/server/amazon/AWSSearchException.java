package de.cil2012.cilsearch.server.amazon;

import de.cil2012.cilsearch.server.model.exceptions.SearchException;

public class AWSSearchException extends SearchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AWSSearchException() {
	}

	public AWSSearchException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AWSSearchException(String arg0) {
		super(arg0);
	}

	public AWSSearchException(Throwable arg0) {
		super(arg0);
	}

}
