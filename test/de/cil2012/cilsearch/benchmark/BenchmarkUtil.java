package de.cil2012.cilsearch.benchmark;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utitlity package for benchmarking.
 */
public class BenchmarkUtil {
	private final static SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmm");
	
	// Private because we don't want this class to be intatiated
	private BenchmarkUtil() {
	}
	
	/**
	 * Returns a file prefixe build from the current date and time.
	 * @return prefix with form 'yyMMddHHmm_'
	 */
	public static String getFilePrefix() {
		return format.format(new Date()) + "_";
	}
	
	/**
	 * Escapes spaces in the give string to be better used as a filename.
	 * @param keyword the string (e.g. keyword) to be escaped.
	 * @return the escaped string
	 */
	public static String escapeSpaces(String val) {
		return val.replaceAll(" ", "_");
	}
}
