package de.cil2012.cilsearch.server;

import de.cil2012.cilsearch.server.model.MailSearchResult;
import de.cil2012.cilsearch.server.model.exceptions.SearchException;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;

public interface Searcher {
	
	/**
	 * Executes a search request against the search service
	 * The search parameters are specified by MailSearchRequest, an offset and a size parameter
	 * 
	 * 
	 * @param searchRequest
	 * @param offset
	 * @param size
	 * @return a MailSearchResult object with mails in MailObject
	 *         representation. From/Recipient information is lossy, i.e. the
	 *         Address representation does not conform to the original imap
	 *         message. Content information is lossy as well.
	 *         
	 *         Variables in the MailObject, that are not part of the result(fields) will be NULL by default
	 *         Variables that are part of the result, will be set to NULL if empty
	 *         
	 * @throws SearchException
	 */
	public MailSearchResult search(MailSearchRequest searchRequest, int offset, int size) throws SearchException;
	
	/**
	 * Retrieves ALL mails from the search service, just retrieving the id and date and saving it to the MailObject
	 * 
	 * @return MailSearchResult object
	 * @throws SearchException
	 */
	public MailSearchResult getAllMailsIdAndDate() throws SearchException;
	
	/**
	 * Retrieves SOME mails (specified by offset and size) 
	 * from the search service, just retrieving the id and date and saving it to the MailObject
	 * 
	 * @param offset
	 * @param size
	 * @return MailSearchResult object
	 * @throws SearchException
	 */
	public MailSearchResult getAllMailsIdAndDate(int offset, int size) throws SearchException;
	
	
	/**
	 * Convenience method for retrieving a mail by its ESCAPED message id
	 * 
	 * @param messageId (ESCAPED)
	 * @return MailSearchResult object
	 * @throws SearchException
	 */
	public MailSearchResult getMailByMessageId(String messageId) throws SearchException;

}
