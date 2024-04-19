package es2.eventLoop;
import io.vertx.core.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VerticleSearch extends AbstractVerticle {

    private final String webAddress;
    private final int currentDepth;
    private final int maxDepth;
    private final String word;

    public VerticleSearch(String webAddress, int currentDepth, int maxDepth, String word) {
        this.webAddress = webAddress;
        this.currentDepth = currentDepth;
        this.maxDepth = maxDepth;
        this.word = word;
    }

    public void start(){
        searchAsync().onSuccess((res) -> {
            System.out.println(res.toString());
        });
    }

    public void stop(){
        
    }

    protected void createNewVerticle(String webAddress, int currentDepth, int maxDepth, String word){
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VerticleSearch(webAddress, currentDepth, maxDepth, word));
    }
    
    private Future<Map<String, Integer>> searchAsync(){
        Promise<Map<String, Integer>> promise = Promise.promise();
        getVertx().runOnContext((id) -> { //equivalte al setTimeout(delay=0)
            Map<String, Integer> results = new HashMap<>();
            Document document = new Document("");
            try {
                document = Jsoup.connect(webAddress).get();
                String text = document.toString();
                int occurrences = text.split(this.word).length - 1; // Take the occurrences number in the page.
                results.put(webAddress, occurrences);
            } catch (IOException e) {
                e.printStackTrace();
            } // Fetching the HTML content of the web page.
    /*
            if(currentDepth < maxDepth){
                // Find all links on the page and recursively crawl them
                Elements links = document.select("a[href]");
                //List<Future<Map<String, Integer>>> futures = new ArrayList<>();  // List of futures of its children.
    
                for (Element link : links) {
                    // Clear the Url with regex syntax.
                    String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                    String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
    
                    // If the link is not already explored and is a valid URL (https or http), submit it for crawling 
                    if((nextUrl.startsWith("https://") || nextUrl.startsWith("http://")) ){//&& !this.pageAlreadyVisited(noQueryStringUrl)){
                       // this.addVisitedPage(noQueryStringUrl);
                        //futures.add(this.executor.submit(new WebCrawlerVirtualThread(executor, this.consumer, nextUrl, currentDepth + 1, maxDepth, word, this.alreadyExploredPages)));
                        createNewVerticle(nextUrl, currentDepth, maxDepth, word);
                    }
                }
            } */
            promise.complete(results);
        });
        return promise.future();
    }

    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VerticleSearch(webAddress, depth, depth, word), deployResult -> {
            if (deployResult.succeeded()) {
                System.out.println("Search deployed successfully.");
            } else {
                System.out.println("Search deployment failed: " + deployResult.cause().getMessage());
            }
        });
    }
}