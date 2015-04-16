package concrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyCrawler implements Crawler {

    /** Maximal number of visited urls per request. */
    private static final int MAX_VISITS = 20;

	@Override
	public List<String> crawl(String startURL) {
        /* Contains the already visited urls. */
        Set<String> urlsVisited = new ConcurrentSkipListSet<String>();
        /* Task Counter */
        AtomicInteger tasks = new AtomicInteger(1);

        Executor executor = Executors.newFixedThreadPool(20);
        executor.execute(new Worker(startURL, urlsVisited, executor, tasks));
        while(tasks.get() > 0){
        	try { Thread.sleep(10);  }
        	catch (InterruptedException e) { }
        }
        return new ArrayList<String>(urlsVisited);
	}

	private static class Worker implements Runnable {

		private final String visit;
		private final Set<String> visited;
		private final Executor executor;
		private final AtomicInteger tasks;
		
		/**
		 * @param visited Thread safe set, in which crawled urls can be added
		 * @param tasks 
		 */
		public Worker(String visit, Set<String> visited, Executor executor, AtomicInteger tasks) {
			this.visit = visit;
			this.visited = visited;
			this.executor = executor;
			this.tasks = tasks;
		}

		@Override
		public void run() {
			try{
				if(visited.size() >= MAX_VISITS){ return; }
				visited.add(visit);
				try {
					Document doc = Jsoup.parse(Jsoup.connect(visit)
							.userAgent("ConCrawler/0.1 Mozilla/5.0")
							.timeout(3000)
							.get().html());
	
					Elements links = doc.select("a[href]");
					for (Element link : links) {
						String linkString = link.absUrl("href");
						if ((!visited.contains(linkString)) && linkString.startsWith("http")) {
							tasks.incrementAndGet();
							executor.execute(new Worker(linkString, visited, executor, tasks));
						}
					}
	
				} catch (Exception e) {
					System.out.println("Problem reading '" + visit + "'. Message: " + e.getMessage());
				}
			} finally {
				tasks.decrementAndGet();
			}
		}
		
	}
}
