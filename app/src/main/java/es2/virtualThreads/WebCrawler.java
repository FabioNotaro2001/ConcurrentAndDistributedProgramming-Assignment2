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

public class WebCrawler implements Callable<Map<String, Integer>> {
    private final ExecutorService executor;
    private final String webAddress;
    private final int currentDepth;
    private final int maxDepth;
    private final String word;
    private final List<String> alreadyExploredPages;
    public WebCrawler(ExecutorService ex, String webAddress, int currentDepth, int maxDepth, String word, List<String> alreadyExploredPages){
        this.executor = ex;
        this.webAddress = webAddress;
        this.currentDepth = currentDepth;
        this.maxDepth = maxDepth;
        this.word = word;
        this.alreadyExploredPages = alreadyExploredPages;
    }
    @Override
    public Map<String, Integer> call() throws Exception {
        this.alreadyExploredPages.add(this.webAddress);
        String outputPadding = " ".repeat(currentDepth - 1);
        System.out.println(outputPadding + "Exploring page " + this.webAddress + " with depth " + this.currentDepth);
        Map<String, Integer> results = new HashMap<>();
        try {
            Document document = Jsoup.connect(webAddress).get();

            // Check if the keyword is present in the current page
            String text = document.toString();
            int occurrences = text.split(this.word).length - 1;

            if(currentDepth < maxDepth){
                // Find all links on the page and recursively crawl them
                Elements links = document.select("a[href]");
                List<Future<Map<String, Integer>>> futures = new ArrayList<>();  // List of futures of its children.

                for (Element link : links) {
                    String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                    String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                    if((nextUrl.startsWith("https://") || nextUrl.startsWith("http://")) && !this.alreadyExploredPages.contains(noQueryStringUrl)){
                        this.alreadyExploredPages.add(noQueryStringUrl);
                        futures.add(this.executor.submit(new WebCrawler(executor, nextUrl, currentDepth + 1, maxDepth, word, new ArrayList<>(this.alreadyExploredPages))));
                    }
                }
                futures.forEach(f -> {
                    try {
                        results.putAll(f.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
                futures.clear();
            }
            results.put(webAddress, occurrences);
            System.out.println(outputPadding + "Ended exploring " + this.webAddress);
        } catch (IOException e) {
            System.out.println(outputPadding + "Failed exploring " + this.webAddress);
        }
        return results;
    }
}
