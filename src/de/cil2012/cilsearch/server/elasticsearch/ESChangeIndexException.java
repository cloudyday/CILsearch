package de.cil2012.cilsearch.server.elasticsearch;

import de.cil2012.cilsearch.server.model.exceptions.ChangeIndexException;

public class ESChangeIndexException extends ChangeIndexException {

	/**
	 * The elasticsearch subclass exception for @link ChangeIndexException
	 */
	private static final long serialVersionUID = 1L;

	public ESChangeIndexException() {
		super();
	}

	public ESChangeIndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public ESChangeIndexException(String message) {
		super(message);
	}

	public ESChangeIndexException(Throwable cause) {
		super(cause);
	}

}
