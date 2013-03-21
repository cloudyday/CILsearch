package de.cil2012.cilsearch.shared;

/**
 * Service class proving helpful functions when working with mails.
 */
public abstract class MailHelper {
	/**
	 * Given an array of internet addresses in string format
	 * this method concatenates the addresses by an ", ".
	 * 
	 * Returns null if the given argument is either null or empty.
	 * 
	 * @param addresses the addresses to concatenate.
	 * @return the concatenated addresses.
	 */
	public static String concatAddresses(String[] addresses) {
		if (addresses == null || addresses.length < 1) {
			return null;
		}
		
		StringBuilder addressesBuilder = new StringBuilder(addresses[0]);
		for (int toIndex = 1; toIndex < addresses.length; toIndex++) {
			addressesBuilder.append(", " + addresses[toIndex]);
		}
		
		return addressesBuilder.toString();
	}
	
	/**
	 * Encodes the given string to HTML.
	 * 
	 * @param text the text to encode.
	 * @return the encoded string.
	 */
	public static String encodeToHTML(String text) {
		return text.replaceAll("\n", "<br/>");
	}
	
	/**
	 * Escapes the given messageId to be used as a cloud search id.
	 * 
	 * @param id the given messageId.
	 * @return the escaped messageId.
	 */
	public static String escapeMessageId(String id) {
		if(id.length() > 128) {
			id = id.substring(0,128);
		}
		id = id.replaceFirst("<", "").replaceAll("\\W", "_").toLowerCase();
		
		return id;
	}
}
