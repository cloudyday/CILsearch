package de.cil2012.cilsearch.benchmark;

import java.io.IOException;

public class CountMessages {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		int delay = 10000;
//		int messageCount_old = 0;
//		int threshold = 50;
//		int i = 0;
//		
//		while(i < threshold) {
//			int messageCount = new GetMessagesInIndex().countMessages();
//			if(messageCount == messageCount_old) {
//				i++;
//			} else {
//				i = 0;
//			}
//			
//			messageCount_old = messageCount;
//			
//			try {
//				Thread.sleep(delay);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//		}
//		
//		System.out.println("Alle Mails im Index bei timestamp: "+(new Date().getTime()-threshold*delay));
		
		new GetMessagesInIndex().countMessages();

	}

}
