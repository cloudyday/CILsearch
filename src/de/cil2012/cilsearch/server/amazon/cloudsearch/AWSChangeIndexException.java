package de.cil2012.cilsearch.server.amazon.cloudsearch;

import de.cil2012.cilsearch.server.model.exceptions.ChangeIndexException;

/**
 * The AWS subclass exception for @link ChangeIndexException
 */
public class AWSChangeIndexException extends ChangeIndexException {


	private static final long serialVersionUID = 1L;

	public AWSChangeIndexException() {
		super();
	}

	public AWSChangeIndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public AWSChangeIndexException(String message) {
		super(message);
	}

	public AWSChangeIndexException(Throwable cause) {
		super(cause);
	}

	
	

}
