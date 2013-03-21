package de.cil2012.cilsearch.server.amazon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.google.common.collect.Lists;

/**
 * 
 * This class handles interactions with Amazon SimpleDB
 * 
 */
public class SimpleDBHandler {

	private String domainName;
	private AmazonSimpleDB sdb;

	/**
	 * Initialize an AmazonSimpleDBClient
	 * 
	 * @param domainName
	 *            SimpleDB-Domain
	 * @throws IOException
	 */
	public SimpleDBHandler(String domainName) throws IOException {
		if (domainName == null) {
			throw new NullArgumentException("domainName");
		}
		this.domainName = domainName;

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("server_resources/AwsCredentials.properties");
		PropertiesCredentials credentials = new PropertiesCredentials(in);
		sdb = new AmazonSimpleDBClient(credentials);
	}

	/**
	 * Add items to the SimpleDB-Domain
	 * 
	 * @param items
	 *            List of ReplacebleItems
	 */
	public void addMultipleItems(List<ReplaceableItem> items) {
		if (items.size() > 0) {
			for (List<ReplaceableItem> sublist : Lists.partition(items, 25)) {
				sdb.batchPutAttributes(new BatchPutAttributesRequest(domainName, sublist));
			}
		}
	}

	/**
	 * Add items - that are generated from Strings - to the SimpleDB-Domain
	 * 
	 * @param items
	 *            List of Strings
	 */
	public void addMultipleItemsFromStrings(List<String> items) {
		if (items.size() > 0) {
			ArrayList<ReplaceableItem> itemList = new ArrayList<ReplaceableItem>();
			for (String itemName : items) {
				itemList.add(new ReplaceableItem(itemName).withAttributes(new ReplaceableAttribute("Timestamp", new Date().toString(), true)));
			}
			for (List<ReplaceableItem> sublist : Lists.partition(itemList, 25)) {
				sdb.batchPutAttributes(new BatchPutAttributesRequest(domainName, sublist));
			}
		}
	}

	/**
	 * Get items from the SimpleDB-Domain
	 * 
	 * @param returnFields
	 *            Fields that are returned
	 * @param condition
	 *            Query-Condition
	 * @return List of items
	 */
	public List<Item> getItems(String returnFields, String condition) {
		String selectExpression = "select " + returnFields + " from `" + domainName + "` where " + condition;
		SelectRequest selectRequest = new SelectRequest(selectExpression);

		return sdb.select(selectRequest).getItems();
	}

	/**
	 * Get a list of all the item names (hashes) from the SimpleDB-Domain
	 * 
	 * @return LinkedList of hashes
	 */
	public LinkedList<String> getAllItemHashes() {
		String selectExpression = "select * from `" + domainName + "`";
		SelectRequest selectRequest = new SelectRequest(selectExpression);
		LinkedList<String> result = new LinkedList<String>();
		for (Item item : sdb.select(selectRequest).getItems()) {
			result.add(item.getName());
		}
		return result;
	}

	/**
	 * Delete item from the SimpleDB-Domain
	 * 
	 * @param itemName
	 *            Name of the item
	 */
	public void deleteItem(String itemName) {
		sdb.deleteAttributes(new DeleteAttributesRequest(domainName, itemName));
	}

	/**
	 * Delete multiple items from the SimpleDB-Domain
	 * 
	 * @param items
	 *            List of the items
	 */
	public void deleteMultipleItems(List<String> items) {
		if (items.size() > 0) {
			ArrayList<DeletableItem> itemList = new ArrayList<DeletableItem>();
			for (String itemName : items) {
				itemList.add(new DeletableItem(itemName, null));
			}

			for (List<DeletableItem> sublist : Lists.partition(itemList, 25)) {
				sdb.batchDeleteAttributes(new BatchDeleteAttributesRequest(domainName, sublist));
			}
		}
	}
}