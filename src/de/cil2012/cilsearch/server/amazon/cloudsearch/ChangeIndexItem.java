package de.cil2012.cilsearch.server.amazon.cloudsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * SDF representation class of a single item
 */
public class ChangeIndexItem {
	
	private String type;
	private String id;
	private int version;
	private String lang;
	
	private Fields fields;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public Fields getFields() {
		return fields;
	}
	public void setFields(Fields fields) {
		this.fields = fields;
	}
	
	public String toSDF() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//Gson gson = new Gson();

		String json = gson.toJson(this);
//		System.out.println(json);
		return json;
	}


	

}
