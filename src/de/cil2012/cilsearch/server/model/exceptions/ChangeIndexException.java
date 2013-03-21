package de.cil2012.cilsearch.server.model.exceptions;

/**
 * A subclass of this exception should be thrown if a request to change the search index has failed
 */
public abstract class ChangeIndexException extends Throwable {

	private static final long serialVersionUID = 1L;

	public ChangeIndexException() {
		super();
	}

	public ChangeIndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChangeIndexException(String message) {
		super(message);
	}

	public ChangeIndexException(Throwable cause) {
		super(cause);
	}


	
}
