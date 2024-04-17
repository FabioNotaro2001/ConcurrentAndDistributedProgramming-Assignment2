package es2.virtualThreads;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;



public class WebCrawler implements Callable<Map<String, Integer>> {

    public record Result(String webAddress, int depth, int occurrences) {}

    private final ExecutorService executor;
    private Consumer<WebCrawler.Result> consumer;
    private final String webAddress;
    private final int currentDepth;
    private final int maxDepth;
    private final String word;
    private final List<String> alreadyExploredPages;

    public WebCrawler(final ExecutorService ex, final Consumer<WebCrawler.Result> consumer, final String webAddress, final int currentDepth, final int maxDepth, final String word, final List<String> alreadyExploredPages){
        this.executor = ex;
        this.consumer = consumer;
        this.webAddress = webAddress;
        this.currentDepth = currentDepth;
        this.maxDepth = maxDepth;
        this.word = word;
        this.alreadyExploredPages = alreadyExploredPages; // Per non esplorare pagine già visitate
    }

    private boolean pageAlreadyVisited(String webAddress){
        synchronized(this.alreadyExploredPages){
            return this.alreadyExploredPages.contains(webAddress);
        }
    }

    private void addVisitedPage(String webAddress){
        synchronized(this.alreadyExploredPages){
            this.alreadyExploredPages.add(webAddress);
        }
    }
    // Method representing the task of crawling a web page.
    @Override
    public Map<String, Integer> call() throws Exception {
        this.addVisitedPage(this.webAddress); // Marking the current page explored.
        
        // String outputPadding = " ".repeat(currentDepth - 1);
        // System.out.println(outputPadding + "Exploring page " + this.webAddress + " with depth " + this.currentDepth);
        Map<String, Integer> results = new HashMap<>();
        try {
            Document document = Jsoup.connect(webAddress).timeout(3000).get(); // Fetching the HTML content of the web page.

            // https://virtuale.unibo.it

            String text = document.toString();
            int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.
            
            if (occurrences > 0) {
                consumer.accept(new Result(webAddress, currentDepth, occurrences));
            }

            // If the current depth is less than the maximum depth, continue exploring links on the page
            if(currentDepth < maxDepth){
                // Find all links on the page and recursively crawl them
                Elements links = document.select("a[href]");
                List<Future<Map<String, Integer>>> futures = new ArrayList<>();  // List of futures of its children.

                for (Element link : links) {
                    // Clear the Url with regex syntax.
                    String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                    String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");

                    // If the link is not already explored and is a valid URL (https or http), submit it for crawling 
                    if(!executor.isShutdown() && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://")) && !this.pageAlreadyVisited(noQueryStringUrl)){
                        this.addVisitedPage(noQueryStringUrl);
                        futures.add(this.executor.submit(new WebCrawler(executor, this.consumer, nextUrl, currentDepth + 1, maxDepth, word, this.alreadyExploredPages)));
                    }
                }

                // Collect results from child crawlers
                futures.forEach(f -> {
                    try {
                        results.putAll(f.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
                futures.clear();
            }
            
            // Storing occurrences of the keyword in the current page
            results.put(webAddress, occurrences);
            // System.out.println(outputPadding + "Ended exploring " + this.webAddress);
        } catch (IOException e) {
            // System.out.println("Failed exploring " + this.webAddress);
        }
        return results;
    }
}
