package es2.eventLoop;
import es2.WebCrawlerResult;
import io.vertx.core.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentSkipListSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VerticleSearch extends AbstractVerticle {
    private final String webAddress;
    private final int maxDepth;
    private final String word;
    private int remainingSearches;
    private final Set<String> alreadyVisitedPages;
    // Defines the action to be done after visiting a page (as results become available) depending on console or GUI.
    private final Consumer<WebCrawlerResult.Result> onPageVisited;
    private final AtomicBoolean isStopped; // Atomic for the GUI.

    public VerticleSearch(String webAddress, int maxDepth, String word, Consumer<WebCrawlerResult.Result> onPageVisited) {
        this.webAddress = webAddress;
        this.maxDepth = maxDepth;
        this.word = word;
        this.remainingSearches = 1; // 1 because of the initial web address to be visited.
        this.alreadyVisitedPages = new ConcurrentSkipListSet<>();
        this.onPageVisited = onPageVisited;
        this.isStopped = new AtomicBoolean(false);
    }

    // This method is called implicitly with the creation of the verticle.
    public void start(Promise<Void> promise){
        System.out.println("Verticle started!");

        // Defines what to do after reading something on the event bus.
        vertx.eventBus().consumer("my-topic", message -> {
            crawl(message.body().toString(), promise);
        });

        // Publication of the first link to be explored on the event bus (formatted).
        vertx.eventBus().publish("my-topic", 1 + ";" + webAddress); // We put on the event bus a formatted string composed of two parts.
    }

    public void stop(){
    }
    
    private void crawl(String messageReadFromBus, Promise<Void> promise){
        String depthStr = messageReadFromBus.split(";.*")[0];
        int currentDepth = Integer.parseInt(depthStr);
        String webAddr = messageReadFromBus.substring(depthStr.length() + 1);

        Callable<Document> call = () -> {
            if(isStopped.get()) {
                return null;
            }
            this.alreadyVisitedPages.add(webAddr);
            return Jsoup.connect(webAddr).timeout(2000).get();
        };

        getVertx().executeBlocking(call)    // Executes call from another thread in order not to block the main one (as the connection is heavy).
        .onComplete(res -> {
            this.remainingSearches --;

            try {
                Document document = res.result();
                if(document == null) {
                    return;
                }

                String text = document.toString();
                int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.

                if(occurrences > 0){
                    // Call the consumer passing to it the results computed.
                    this.onPageVisited.accept(new WebCrawlerResult.Result(webAddr, currentDepth, occurrences));
                }

                // If the max depth has not been reached we must find all the links in this page.
                if(currentDepth < maxDepth){
                    Elements links = document.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                        String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                        if(!this.alreadyVisitedPages.contains(noQueryStringUrl) && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://"))){//
                            this.remainingSearches++;
                            this.alreadyVisitedPages.add(noQueryStringUrl);

                            // Publication on the event bus of all the links found in the current page.
                            vertx.eventBus().publish("my-topic", (currentDepth + 1) + ";" + nextUrl);
                        }
                    }
                }
            } finally {
                if(remainingSearches == 0){
                    promise.complete();
                }
            }
        });
    }
}