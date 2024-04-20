package es2.eventLoop;
import es2.WebCrawler;
import io.vertx.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import io.vertx.core.buffer.Buffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VerticleSearch extends AbstractVerticle {
    private record Search(int currentDepth, String webAddress){}

    private final String webAddress;
    private final int maxDepth;
    private final String word;
    private int remainingSearches;
    private final List<String> alreadyVisitedPages;

    public VerticleSearch(String webAddress, int maxDepth, String word) {
        this.webAddress = webAddress;
        this.maxDepth = maxDepth;
        this.word = word;
        this.remainingSearches = 1; // 1 because of the initial web address to be visited.
        this.alreadyVisitedPages = new ArrayList<>();
    }

    public void start(Promise<Void> promise){
        System.out.println("Verticle started!");
        vertx.eventBus().consumer("my-topic", message -> {
            //System.out.println("Event received: " + message.body().toString());
            crawl(message.body().toString(), promise);
        });
        vertx.eventBus().publish("my-topic", 1 + ";" + webAddress); // We put on the event bus a formatted string composed of two parts.
    }

    public void stop(){
        
    }
    
    private void crawl(String info, Promise<Void> promise){
        var depthStr = info.split(";.*")[0];
        int currentDepth = Integer.parseInt(depthStr);
        String webAddr = info.substring(depthStr.length() + 1);
        getVertx().runOnContext((id) -> { //equivalte al setTimeout(delay=0)
            this.remainingSearches --;
            Document document = new Document("");
            try {
                this.alreadyVisitedPages.add(webAddr);
                document = Jsoup.connect(webAddr).timeout(2000).get();
                String text = document.toString();
                int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.
                if(occurrences > 0){
                    System.out.println("[In '" + webAddr + "' local occurrences: " + occurrences + "]");
                }

                if(currentDepth < maxDepth){
                    Elements links = document.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                        String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");
                        if(!this.alreadyVisitedPages.contains(noQueryStringUrl) && (nextUrl.startsWith("https://") || nextUrl.startsWith("http://"))){//
                            this.remainingSearches++;
                            this.alreadyVisitedPages.add(noQueryStringUrl);
                            vertx.eventBus().publish("my-topic", (currentDepth + 1) + ";" + nextUrl);   // Quando mettiamo qualcosa in lista lo segnaliamo con questo
                        }
                    }
                }
            } catch (IOException | IllegalArgumentException e) {

            } finally {
                if(remainingSearches == 0){    // Event bus is empty.
                    promise.complete();
                }
            }
        });
    }
}