package es2.eventLoop;
import io.vertx.core.*;

import java.util.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.concurrent.Callable;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VerticleSearch_v2 extends AbstractVerticle {

    record Result(String webAddress, int depth, int occurrences, int id) {}

    private final String webAddress;
    private final int maxDepth;
    private final String word;
    private AtomicInteger remainingSearches; // Numero di ricerche ancora da fare per sapere se abbiamo finito
    private final Set<String> alreadyVisitedPages;
    private final Consumer<Result> onPageVisited; // Dice cosa fare dopo che ho visitato una pagina (mano a mano che i risultati sonoo disponibili) a seconda di console e GUI
    private final AtomicBoolean isStopped; // Atomico per la gui
    private final int id;
    private final int nVerticle;

    public VerticleSearch_v2(String webAddress, int maxDepth, String word, Consumer<Result> onPageVisited, int id, int nVerticle, Set<String> alreadyVisitedPages, AtomicBoolean isStopped, AtomicInteger remainingSearches) {
        this.webAddress = webAddress;
        this.maxDepth = maxDepth;
        this.word = word;
        this.remainingSearches = remainingSearches; // 1 because of the initial web address to be visited.
        this.alreadyVisitedPages = alreadyVisitedPages;
        this.onPageVisited = onPageVisited;
        this.isStopped = isStopped;
        this.id = id;
        this.nVerticle = nVerticle;
    }

    // Esso viene chiamato implicitamente da solo alla creazione del vertical.
    public void start(Promise<Void> promise){
        System.out.println("Verticle "+ this.id + " started");

        String topic = "my-topic-" + id;
        // Prima dico a chi come comportarmi al'arrivo di un evento. Consumer permette di leggere dalla coda di eventi dell'eventloop.
        vertx.eventBus().consumer(topic, message -> {
            //System.out.println("Event received: " + message.body().toString());
            crawl(message.body().toString(), promise);
        });
        // Poi pubblichiamo sull'event loop un evento, in questo caso con il prossimo indirizzo da visitare (essendo a depth 1), con depth e web address
        
        if(id == 0){
            vertx.eventBus().publish("my-topic-0", 1 + ";" + webAddress); // We put on the event bus a formatted string composed of two parts.
        }
    }

    public void stop(){
    }

    public void requestStop(){
        this.isStopped.set(true);
    }
    
    private void crawl(String info, Promise<Void> promise){
        var depthStr = info.split(";.*")[0];
        int currentDepth = Integer.parseInt(depthStr);
        String webAddr = info.substring(depthStr.length() + 1);
        
        Callable<Document> call = () -> {
            if(isStopped.get()) {
                return null;
            }
            this.alreadyVisitedPages.add(webAddr);
            return Jsoup.connect(webAddr).timeout(2000).get(); // Time out per siti che non rispondono                
        };
        // Viene fatto eseguire dall'event loop in modo asincrono in modo tale da non farlo bloccare.
        getVertx().executeBlocking(call)
        .onComplete(res -> {
            try{
                this.remainingSearches.decrementAndGet();

                Document document = res.result();
                if(document == null)
                    return;
                String text = document.toString();
                int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.    
                if(occurrences > 0){
                    // Chiama il consumer col risultato.
                    this.onPageVisited.accept(new Result(webAddr, currentDepth, occurrences, this.id));
                }

                if(currentDepth < maxDepth){
                    Elements links = document.select("a[href]");
                    int index = 0;
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                        String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                        if(!this.alreadyVisitedPages.contains(noQueryStringUrl) && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://"))){//
                            this.remainingSearches.incrementAndGet();
                            this.alreadyVisitedPages.add(noQueryStringUrl);
                            String topicToPublish = "my-topic-" + (index % nVerticle);
                            index = index + 1;
                            //System.out.println("public: " + topicToPublish);
                            vertx.eventBus().publish(topicToPublish, (currentDepth + 1) + ";" + nextUrl); // Pubblichiamo sull'event bus l'evento
                        }
                    }
                }
            } finally {
                if(remainingSearches.get() == 0){    // Event bus is empty.
                    System.out.println("finito:" + id);
                    promise.complete();
                }
            }
        });
    }
}