package es2.eventLoop;
import es2.WebCrawler;
import io.vertx.core.*;

import java.io.IOException;
import java.util.*;
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
    private int remainingSearches; // Numero di ricerche ancora da fare per sapere se abbiamo finito
    private final Set<String> alreadyVisitedPages;;
    private final Consumer<WebCrawler.Result> onPageVisited; // Dice cosa fare dopo che ho visitato una pagina (mano a mano che i risultati sonoo disponibili) a seconda di console e GUI
    private final AtomicBoolean isStopped; // Atomico per la gui

    public VerticleSearch(String webAddress, int maxDepth, String word, Consumer<WebCrawler.Result> onPageVisited) {
        this.webAddress = webAddress;
        this.maxDepth = maxDepth;
        this.word = word;
        this.remainingSearches = 1; // 1 because of the initial web address to be visited.
        this.alreadyVisitedPages = new ConcurrentSkipListSet<>();
        this.onPageVisited = onPageVisited;
        this.isStopped = new AtomicBoolean(false);
    }

    // Esso viene chiamato implicitamente da solo alla creazione del vertical.
    public void start(Promise<Void> promise){
        System.out.println("Verticle started!");
        // Prima dico a chi come comportarmi al'arrivo di un evento. Consumer permette di leggere dalla coda di eventi dell'eventloop.
        vertx.eventBus().consumer("my-topic", message -> {
            //System.out.println("Event received: " + message.body().toString());
            crawl(message.body().toString(), promise);
        });
        // Poi pubblichiamo sull'event loop un evento, in questo caso con il prossimo indirizzo da visitare (essendo a depth 1), con depth e web address
        vertx.eventBus().publish("my-topic", 1 + ";" + webAddress); // We put on the event bus a formatted string composed of two parts.
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
        // Viene fatto eseguire dall'event loop in modo asincrono in modo tale da non farlo bloccare.
        getVertx().runOnContext((id) -> {
            this.remainingSearches --;
            try {
                if(isStopped.get()) {
                    return;
                }
                this.alreadyVisitedPages.add(webAddr);
                Document document = Jsoup.connect(webAddr).timeout(2000).get(); // Time out per siti che non rispondono
                String text = document.toString();
                int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.
                if(occurrences > 0){
                    // Chiama il consumer col risultato.
                    this.onPageVisited.accept(new WebCrawler.Result(webAddr, currentDepth, occurrences));
                }

                if(currentDepth < maxDepth){
                    Elements links = document.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                        String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                        if(!this.alreadyVisitedPages.contains(noQueryStringUrl) && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://"))){//
                            this.remainingSearches++;
                            this.alreadyVisitedPages.add(noQueryStringUrl);
                            vertx.eventBus().publish("my-topic", (currentDepth + 1) + ";" + nextUrl); // Pubblichiamo sull'event bus l'evento
                        }
                    }
                }
            } catch (IOException | IllegalArgumentException ignored) {
            } finally {
                if(remainingSearches == 0){    // Event bus is empty.
                    promise.complete();
                }
            }
        });
    }
}