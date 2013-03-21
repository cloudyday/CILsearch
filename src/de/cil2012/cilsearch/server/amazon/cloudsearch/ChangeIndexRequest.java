package de.cil2012.cilsearch.server.amazon.cloudsearch;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * creates a SDF representation of a complete indexing request
 */
public class ChangeIndexRequest {
	
	private List<ChangeIndexItem> batch;
	
	public ChangeIndexRequest() {
		batch = new LinkedList<ChangeIndexItem>();
	}
			
	/**
	 * 
	 * @return the batch request as the final SDF string
	 */
	public String toSDF() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//Gson gson = new Gson();

		ChangeIndexItem[] request = batch.toArray(new ChangeIndexItem[0]);
		String json = gson.toJson(request);
//		System.out.println(json);
		return json;
	}


	/**
	 * 
	 * @return the batch request as a list of ChangeIndexItem's
	 */
	public List<ChangeIndexItem> getBatch() {
		return batch;
	}

	public void setBatch(List<ChangeIndexItem> batch) {
		this.batch = batch;
	}
	
	/**
	 * Adds a ChangeIndexItem to the request
	 * @param item
	 */
	public void addToBatch(ChangeIndexItem item) {
		if(item == null) {
			throw new NullArgumentException("item");
		}
		batch.add(item);
	}


}
