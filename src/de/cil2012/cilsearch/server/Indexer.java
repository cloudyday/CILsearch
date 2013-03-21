package de.cil2012.cilsearch.server;

import java.util.List;
import java.util.Set;

import de.cil2012.cilsearch.server.model.MailObject;
import de.cil2012.cilsearch.server.model.exceptions.ChangeIndexException;

public interface Indexer {
	

	/**
	 * adds a list of mail objects to the search index
	 * 
	 * @param mails - A list of MailObjects
	 * @throws ChangeIndexException (if something goes wrong with the request)
	 */
	public void addToIndex(List<MailObject> mails)
			throws ChangeIndexException;
	
	/**
	 * removes messages from the search index
	 * 
	 * @param messageIds - a Set of Message Ids
	 * @throws ChangeIndexException (if something goes wrong with the request)
	 */
	public void removeFromIndex(Set<String> messageIds)
			throws ChangeIndexException;

}
