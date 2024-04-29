package es2.eventLoop;
import io.vertx.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VerticleSearch_v2 extends AbstractVerticle {

    record Result(String webAddress, int depth, int occurrences, int id) {}

    private final String webAddress;
    private final int maxDepth;
    private final String word;
    private int remainingSearches; // Numero di ricerche ancora da fare per sapere se abbiamo finito
    private final List<String> alreadyVisitedPages;
    private final Consumer<Result> onPageVisited; // Dice cosa fare dopo che ho visitato una pagina (mano a mano che i risultati sonoo disponibili) a seconda di console e GUI
    private final AtomicBoolean isStopped; // Atomico per la gui
    private final int id;
    private final int nVerticle;

    public VerticleSearch_v2(String webAddress, int maxDepth, String word, Consumer<Result> onPageVisited, int id, int nVerticle) {
        this.webAddress = webAddress;
        this.maxDepth = maxDepth;
        this.word = word;
        this.remainingSearches = 1; // 1 because of the initial web address to be visited.
        this.alreadyVisitedPages = new ArrayList<>();
        this.onPageVisited = onPageVisited;
        this.isStopped = new AtomicBoolean(false);
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
        
        if( id == 0){
            vertx.eventBus().publish("my-topic-0", 1 + ";" + webAddress); // We put on the event bus a formatted string composed of two parts.

            for( int i=1; i<nVerticle; i++){
                    vertx.deployVerticle(new VerticleSearch_v2(webAddress, maxDepth, word, (res) -> {
                        System.out.println("Vertical: "+ res.id() +" -> [In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]"); // Ogni occorenza viene stampata
                    }, i, nVerticle))
                    .onComplete(res -> System.exit(0));
            }
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
        // Viene fatto eseguire dall'event loop in modo asincrono in modo tale da non farlo bloccare.
        getVertx().runOnContext((id_) -> {
            //System.out.println("Crawl: " + this.id);
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
                    this.onPageVisited.accept(new Result(webAddr, currentDepth, occurrences, this.id));
                }

                if(currentDepth < maxDepth){
                    Elements links = document.select("a[href]");
                    int index = 0;
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                        String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                        if(!this.alreadyVisitedPages.contains(noQueryStringUrl) && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://"))){//
                            this.remainingSearches++;
                            this.alreadyVisitedPages.add(noQueryStringUrl);
                            String topicToPublish = "my-topic-" + (index % nVerticle);
                            index = index + 1;
                            vertx.eventBus().publish(topicToPublish, (currentDepth + 1) + ";" + nextUrl); // Pubblichiamo sull'event bus l'evento
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