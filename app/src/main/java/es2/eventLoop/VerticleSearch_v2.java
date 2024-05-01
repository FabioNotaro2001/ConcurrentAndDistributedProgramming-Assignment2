package es2.eventLoop;
import io.vertx.core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.concurrent.Callable;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VerticleSearch_v2 extends AbstractVerticle {

    // Record useful to link the result with the verticle that found it.
    record Result(String webAddress, int depth, int occurrences, int id) {}

    private final String webAddress;
    private final int maxDepth;
    private final String word;
    private AtomicInteger remainingSearches;
    private final Set<String> alreadyVisitedPages;
    // Defines the action to be done after visiting a page (as results become available) depending on console or GUI.
    private final Consumer<Result> onPageVisited;
    private final AtomicBoolean isStopped; // Atomic for the GUI.
    private final int verticleId;
    private final int nVerticles;   // Wished number of verticles.

    public VerticleSearch_v2(String webAddress, int maxDepth, String word, Consumer<Result> onPageVisited, int id, int nVerticle, Set<String> alreadyVisitedPages, AtomicBoolean isStopped, AtomicInteger remainingSearches) {
        this.webAddress = webAddress;
        this.maxDepth = maxDepth;
        this.word = word;
        this.remainingSearches = remainingSearches; // 1 because of the initial web address to be visited.
        this.alreadyVisitedPages = alreadyVisitedPages;
        this.onPageVisited = onPageVisited;
        this.isStopped = isStopped;
        this.verticleId = id;
        this.nVerticles = nVerticle;
    }

    // This method is called implicitly with the creation of the verticle.
    public void start(Promise<Void> promise){
        // As the event bus is shared, every verticles reads from a different topic.
        String topic = "my-topic-" + verticleId;

        // Defines what to do after reading something on the event bus.
        vertx.eventBus().consumer(topic, message -> {
            crawl(message.body().toString(), promise);
        });

        // Publication of the first link to be explored on the event bus (formatted).
        if(verticleId == 0){
            vertx.eventBus().publish("my-topic-0", 1 + ";" + webAddress); // We put on the event bus a formatted string composed of two parts.
        }
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
            return Jsoup.connect(webAddr).timeout(2000).get(); // Time out per siti che non rispondono                
        };

        getVertx().executeBlocking(call)    // Executes call from another thread in order not to block the main one (as the connection is heavy).
        .onComplete(res -> {
            try{
                this.remainingSearches.decrementAndGet();

                Document document = res.result();
                if(document == null){
                    return;
                }

                String text = document.toString();
                int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.    

                if(occurrences > 0){
                    // Call the consumer passing to it the results computed.
                    this.onPageVisited.accept(new Result(webAddr, currentDepth, occurrences, this.verticleId));
                }

                // If the max depth has not been reached we must find all the links in this page.
                if(currentDepth < maxDepth){
                    Elements links = document.select("a[href]");
                    int index = 0;
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                        String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                        if(!this.alreadyVisitedPages.contains(noQueryStringUrl) && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://"))){//
                            this.remainingSearches.incrementAndGet();
                            this.alreadyVisitedPages.add(noQueryStringUrl);

                            // For each link found compute the correct topic on which it must be published.
                            String topicToPublish = "my-topic-" + (index % nVerticles);
                            index = index + 1;

                            // Publication on the event bus of all the links found in the current page.
                            vertx.eventBus().publish(topicToPublish, (currentDepth + 1) + ";" + nextUrl); // Pubblichiamo sull'event bus l'evento
                        }
                    }
                }
            } finally {
                if(remainingSearches.get() == 0){
                    promise.complete();
                }
            }
        });
    }
}