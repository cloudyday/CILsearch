package de.cil2012.cilsearch.benchmark;

import de.cil2012.cilsearch.server.UpdateIndexServiceImpl;

public class IndexBenchmark {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		UpdateIndexServiceImpl service = new UpdateIndexServiceImpl();
		service.updateIndex();
	}

}
