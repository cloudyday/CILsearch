package de.cil2012.cilsearch.benchmark;

import de.cil2012.cilsearch.server.GlobalPropertyStore;
import de.cil2012.cilsearch.server.MailSearchServiceImpl;
import de.cil2012.cilsearch.shared.model.MailSearchRequest;
import de.cil2012.cilsearch.shared.model.MailSearchResultRepresentation;
import de.cil2012.cilsearch.shared.model.QueryParam;
import de.cil2012.cilsearch.shared.model.QueryParam.SearchField;

public class Benchmarks {
	final String[] nouns = {
			"time",
			"person",
			"year",
			"way",
			"day",
			"thing",
			"man",
			"world",
			"life",
			"hand",
			"part",
			"child",
			"eye",
			"woman",
			"place",
			"work",
			"week",
			"case",
			"point",
			"government",
			"company",
			"number",
			"group",
			"problem",
			"fact"
	};
	
	final String[] adjectives = {
			"good",
			"new",
			"first",
			"last",
			"long",
			"great",
			"little",
			"own",
			"other",
			"old",
			"right",
			"big",
			"high",
			"different",
			"small",
			"large",
			"next",
			"early",
			"young",
			"important",
			"few",
			"public",
			"bad",
			"same",
			"able"
	};
	
	String[] words = null;
	
	final String[] names = {"Smith",
			"Johnson",
			"Williams",
			"Brown",
			"Jones",
			"Miller",
			"Davis",
			"Garcia",
			"Rodriguez",
			"Wilson",
			"Martinez",
			"Anderson",
			"Taylor",
			"Thomas",
			"Hernandez",
			"Moore",
			"Martin",
			"Jackson",
			"Thompson",
			"White",
			"Lopez",
			"Lee",
			"Gonzalez",
			"Harris",
			"Clark",
			"Lewis",
			"Robinson",
			"Walker",
			"Perez",
			"Hall",
			"Young",
			"Allen",
			"Sanchez",
			"Wright",
			"King",
			"Scott",
			"Green",
			"Baker",
			"Adams",
			"Nelson",
			"Hill",
			"Ramirez",
			"Campbell",
			"Mitchell",
			"Roberts",
			"Carter",
			"Phillips",
			"Evans",
			"Turner",
			"Torres",
			"Parker",
			"Collins",
			"Edwards",
			"Stewart",
			"Flores",
			"Morris",
			"Nguyen",
			"Murphy",
			"Rivera",
			"Cook",
			"Rogers",
			"Morgan",
			"Peterson",
			"Cooper",
			"Reed",
			"Bailey",
			"Bell",
			"Gomez",
			"Kelly",
			"Howard",
			"Ward",
			"Cox",
			"Diaz",
			"Richardson",
			"Wood",
			"Watson",
			"Brooks",
			"Bennett",
			"Gray",
			"James",
			"Reyes",
			"Cruz",
			"Hughes",
			"Price",
			"Myers",
			"Long",
			"Foster",
			"Sanders",
			"Ross",
			"Morales",
			"Powell",
			"Sullivan",
			"Russell",
			"Ortiz",
			"Jenkins",
			"Gutierrez",
			"Perry",
			"Butler",
			"Barnes",
			"Fisher"};
	
	/**
	 * Benchmarks 100 searches.
	 * @param the keyword.
	 */
	private void benchmarkSearchTimeTotal(String filename) {
		final MailSearchServiceImpl service = new MailSearchServiceImpl();
		
		Benchmark benchmark = new Benchmark();
		for (String word : words) {
			final MailSearchRequest request = new MailSearchRequest(word);
			MailSearchResultRepresentation result = service.findMessages(request);
			benchmark.addMeasurement(word, result.getSearchTimeService(), result.getTotalHits(), 0);
		}
		benchmark.save(BenchmarkUtil.getFilePrefix() + filename + "_all");
	}
	
	/**
	 * Benchmarks 100 searches in parallel mode.
	 * @param the keyword.
	 */
	private void benchmarkSearchTimeTotalParallel(String filename) {
		SearchAllThread[] threads = new SearchAllThread[words.length];
		
		// Init threads
		for (int i=0; i<threads.length; i++) {
			threads[i] = new SearchAllThread(words[i]);
		}
		
		// Start all threads
		for (Thread thread : threads) {
			thread.start();
		}
		
		// Wait for all threads to stop
		boolean stopped;
		do {
			stopped = true;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					stopped = false;
				}
			}
		} while (!stopped);
		
		// Get all thread results
		Benchmark benchmark = new Benchmark();
		for (SearchAllThread thread : threads) {
			benchmark.addMeasurement(thread.getKeyword(), thread.getTime(), thread.getCount(), thread.getErrors());
		}
		benchmark.save(BenchmarkUtil.getFilePrefix() + filename + "_parallel_all");
	}
	
	/**
	 * Benchmarks 100 searches in to-field.
	 * @param the keyword.
	 */
	private void benchmarkSearchTimeTotalForTo(String filename) {
		final MailSearchServiceImpl service = new MailSearchServiceImpl();
		
		Benchmark benchmark = new Benchmark();
		for (String name : names) {
			MailSearchRequest request = new MailSearchRequest();
			request.addQueryArgument(QueryParam.include(SearchField.RECIPIENTS_TO, name));
			MailSearchResultRepresentation result = service.findMessages(request);
			benchmark.addMeasurement(name, result.getSearchTimeService(), result.getTotalHits(), 0);
		}
		benchmark.save(BenchmarkUtil.getFilePrefix() + filename + "_to");
	}
	
	/**
	 * Benchmarks 100 searches in to-field in parallel mode.
	 * @param the keyword.
	 */
	private void benchmarkSearchTimeTotalForToParallel(String filename) {
		SearchToThread[] threads = new SearchToThread[names.length];
		
		// Init threads
		for (int i=0; i<threads.length; i++) {
			threads[i] = new SearchToThread(names[i]);
		}
		
		// Start all threads
		for (Thread thread : threads) {
			thread.start();
		}
		
		// Wait for all threads to stop
		boolean stopped;
		do {
			stopped = true;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					stopped = false;
				}
			}
		} while (!stopped);
		
		// Get all thread results
		Benchmark benchmark = new Benchmark();
		for (SearchToThread thread : threads) {
			benchmark.addMeasurement(thread.getKeyword(), thread.getTime(), thread.getCount(), thread.getErrors());
		}
		benchmark.save(BenchmarkUtil.getFilePrefix() + filename + "_parallel_to");
	}

	class SearchAllThread extends Thread {
		private String keyword = null;
		private long time;
		private long count;
		private long errors;
		
		public SearchAllThread(String keyword) {
			this.keyword = keyword;
		}
		
		@Override
		public void run() {
			final MailSearchServiceImpl service = new MailSearchServiceImpl();
			final MailSearchRequest request = new MailSearchRequest(keyword);
			MailSearchResultRepresentation result =  null;
			long errors = -1;
			long startTimeService = System.currentTimeMillis();
			while(result == null) {
				result = service.findMessages(request);
				errors++;
			}
			long runTimeService = System.currentTimeMillis() - startTimeService;
			if (errors > 0) {
				// For comparison purposes we take the time until we get a valid response
				// in case of error.
				this.time = runTimeService;
			} else {
				this.time = result.getSearchTimeService();
			}
			this.count = result.getTotalHits();
			this.errors = errors;
		}
		
		public String getKeyword() {
			return keyword;
		}
		
		public long getTime() {
			return time;
		}
		
		public long getCount() {
			return count;
		}
		
		public long getErrors() {
			return errors;
		}
	}
	
	class SearchToThread extends Thread {
		private String keyword = null;
		private long time;
		private long count;
		private long errors;
		
		public SearchToThread(String keyword) {
			this.keyword = keyword;
		}
		
		@Override
		public void run() {
			final MailSearchServiceImpl service = new MailSearchServiceImpl();
			final MailSearchRequest request = new MailSearchRequest();
			request.addQueryArgument(QueryParam.include(SearchField.RECIPIENTS_TO, keyword));
			MailSearchResultRepresentation result =  null;
			long errors = -1;
			long startTimeService = System.currentTimeMillis();
			while(result == null) {
				result = service.findMessages(request);
				errors++;
			}
			long runTimeService = System.currentTimeMillis() - startTimeService;
			if (errors > 0) {
				// For comparison purposes we take the time until we get a valid response
				// in case of error.
				this.time = runTimeService;
			} else {
				this.time = result.getSearchTimeService();
			}
			this.count = result.getTotalHits();
			this.errors = errors;
		}
		
		public String getKeyword() {
			return keyword;
		}
		
		public long getTime() {
			return time;
		}
		
		public long getCount() {
			return count;
		}
		
		public long getErrors() {
			return errors;
		}
	}
	
	public void initWords() {
		words = new String[nouns.length * adjectives.length];
		int index = 0;
		for (String adjective : adjectives) {
			for (String noun : nouns) {
				words[index] = adjective + " " + noun;
				index++;
			}
		}
	}
	
	public static void main(String[] args) {
		Benchmarks benchmarks = new Benchmarks();
		benchmarks.initWords();
		
		SearchServiceConfiguration[] configurations = {
//				SearchServiceConfiguration.createCloudsearchConfiguration(
//						"fabibench3", "aan5vthw2tr4gk4d5rmcb2xwcq", "us-east-1"),
//				SearchServiceConfiguration.createCloudsearchConfiguration(
//						"fabibench4", "p5yquwovacjr37y265qom5yjdy", "us-east-1"),
//				SearchServiceConfiguration.createCloudsearchConfiguration(
//						"fabibench5", "ohth5572fg5uqzqyvzb7fcoyie", "us-east-1"),
				SearchServiceConfiguration.createElasticsearchConfiguration(
						"fabibench1", "ec2-184-72-161-163.compute-1.amazonaws.com:9300,ec2-23-20-46-52.compute-1.amazonaws.com:9300", "elasticsearch"),
				SearchServiceConfiguration.createElasticsearchConfiguration(
						"fabibench2", "ec2-184-72-161-163.compute-1.amazonaws.com:9300,ec2-23-20-46-52.compute-1.amazonaws.com:9300", "elasticsearch"),
				SearchServiceConfiguration.createElasticsearchConfiguration(
						"fabibench3", "ec2-184-72-161-163.compute-1.amazonaws.com:9300,ec2-23-20-46-52.compute-1.amazonaws.com:9300", "elasticsearch")
		};
		
		for (SearchServiceConfiguration configuration : configurations) {
			GlobalPropertyStore.setActiveService(configuration.getService());
			GlobalPropertyStore.setActiveServiceProperties(configuration.getProperties());
			
			benchmarks.benchmarkSearchTimeTotal(configuration.toString());
			benchmarks.benchmarkSearchTimeTotalForTo(configuration.toString());
			benchmarks.benchmarkSearchTimeTotalParallel(configuration.toString());
			benchmarks.benchmarkSearchTimeTotalForToParallel(configuration.toString());
		}
	}
}
