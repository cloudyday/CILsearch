package de.cil2012.cilsearch.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for generating data files in CSV format when
 * benchmarking our different services.
 */
public class Benchmark {
	private final String BENCHMARK_FOLDER = "data/benchmark";
	
	private List<BenchmarkEntry> entries;
	
	/**
	 * Creates a new benchmark that can later be saved to a file.
	 */
	public Benchmark() {
		entries = new LinkedList<Benchmark.BenchmarkEntry>();
	}
	
	/**
	 * Adds a measured value to the benchmark.
	 * @param keyword the values were measured for.
	 * @param time the time to search.
	 * @param count the number of results.
	 * @param errors the number of errors.
	 */
	public void addMeasurement(String keyword, long time, long count, long errors) {
		entries.add(new BenchmarkEntry(keyword, time, count, errors));
	}
	
	/**
	 * Saves the benchmark to the given filename.
	 * @param filename the filename without extension.
	 */
	public void save(String filename) {
		// Build up our file data
		StringBuilder builder = new StringBuilder("keyword,time,count,errors\n");
		for (BenchmarkEntry entry : entries) {
			builder.append(entry.getKeyword() + "," + entry.getTime() + "," + entry.getCount() + "," + entry.getErrors() + "\n");
		}
		
		// Create the folder if not existent
		File benchmarkFolder = new File(BENCHMARK_FOLDER);
		benchmarkFolder.mkdirs();
		
		// Now save the data to the given file
		File benchmarkFile = new File(BENCHMARK_FOLDER + "/" + filename + ".csv");
		try {
			// Create new file if needed and open it for writing
			benchmarkFile.createNewFile();
			FileWriter fileWriter = new FileWriter(benchmarkFile);
			
			// Save the data to that file
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(builder.toString());
			bufferedWriter.close();
		} catch (IOException e) {
			System.err.println("[ERROR] Error occured while writing benchmark file!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Represents a single benchmark Entry. Only used internally to
	 * save the different points in time.
	 */
	private class BenchmarkEntry {
		private String keyword;
		private long time;
		private long count;
		private long errors;
		
		/**
		 * Creates a new benchmark entry.
		 * @param point where/when the value was measured.
		 * @param value the value.
		 */
		public BenchmarkEntry(String keyword, long time, long count, long errors) {
			this.keyword = keyword;
			this.time = time;
			this.count = count;
			this.errors = errors;
		}
		
		/**
		 * The keyword the value was measured.
		 * @return the keyword.
		 */
		public String getKeyword() {
			return keyword;
		}
		
		/**
		 * The time that was measured for the specified keyword.
		 * @return the time.
		 */
		public long getTime() {
			return time;
		}
		
		/**
		 * The number of elements that were returned.
		 * @return the count.
		 */
		public long getCount() {
			return count;
		}
		
		/**
		 * The number of errors while searching for this keyword.
		 * @return the number of errors.
		 */
		public long getErrors() {
			return errors;
		}
	}
}
