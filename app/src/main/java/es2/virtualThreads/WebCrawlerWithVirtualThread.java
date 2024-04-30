package es2.virtualThreads;

import es2.WebCrawlerResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.*;

// Class that represents the crawling task to be done.
// It implements Callable because it is called inside the submit() of the executor.
// Call method is the first implicitly called.
public class WebCrawlerWithVirtualThread implements Callable<Void>, WebCrawlerResult {
    private final ExecutorService executor; // Same caller executor for submitting new tasks.
    private final Consumer<Result> consumer;
    private final String webAddress;
    private final int currentDepth;
    private final int maxDepth;
    private final String word;
    private final Set<String> alreadyExploredPages; // Useful to avoid exploring the same page twice.

    public WebCrawlerWithVirtualThread(final ExecutorService ex, final Consumer<WebCrawlerResult.Result> consumer, final String webAddress, final int currentDepth, final int maxDepth, final String word, final Set<String> alreadyExploredPages){
        this.executor = ex;
        this.consumer = consumer;
        this.webAddress = webAddress;
        this.currentDepth = currentDepth;
        this.maxDepth = maxDepth;
        this.word = word;
        this.alreadyExploredPages = alreadyExploredPages;
    }

    // Method representing the task of crawling a web page.
    @Override
    public Void call() throws Exception {
        this.crawl();
        return null;
    }

    public void crawl() throws Exception{
        this.alreadyExploredPages.add(this.webAddress); // Marking the current page as explored.

        try {
            Document document = Jsoup.connect(webAddress).timeout(3000).get(); // Get the content of the web page.
            String text = document.toString();

            // Find all occurrences using a regex expression.
            int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1;

            if (occurrences > 0) {
                // Call to the consumer action passing the result found.
                consumer.accept(new Result(webAddress, currentDepth, occurrences));
            }

            // If the current depth is less than the maximum depth, continue exploring links on the page.
            if(currentDepth < maxDepth){
                // Find all links on the page and recursively crawl them.
                Elements links = document.select("a[href]");
                List<Future<Void>> futures = new ArrayList<>();  // List of futures to save the futures of the executor.

                for (Element link : links) {
                    // Clear the Url with regex syntax.
                    String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                    String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");

                    // If the link is not already explored and is a valid URL (https or http), submit it for crawling.
                    if(!executor.isShutdown() && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://")) && !this.alreadyExploredPages.contains(noQueryStringUrl)){
                        this.alreadyExploredPages.add(noQueryStringUrl);
                        futures.add(this.executor.submit(new WebCrawlerWithVirtualThread(executor, this.consumer, nextUrl, currentDepth + 1, maxDepth, word, this.alreadyExploredPages)));
                    }
                }

                // Once submitting all the crawling of the children link waits for the results.
                futures.forEach(f -> {
                    try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
                futures.clear();
            }
        } catch (IOException e) {
        }
    }
}
