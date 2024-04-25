package es2.reactiveProgramming;

import es2.WebCrawler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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


public class WebCrawlerReactiveProgramming {
    private final List<String> alreadyVisitedPages = new ArrayList<>();

    private boolean pageAlreadyVisited(String webAddress){
        synchronized(this.alreadyVisitedPages){
            return this.alreadyVisitedPages.contains(webAddress);
        }
    }
    private void addVisitedPage(String webAddress){
        synchronized(this.alreadyVisitedPages){
            this.alreadyVisitedPages.add(webAddress);
        }
    }

    public void crawl(final String webAddress, final int currentDepth, final int maxDepth, final String word) throws Exception {
        addVisitedPage(webAddress); // Marking the current page explored.
        try {
            Document document = Jsoup.connect(webAddress).timeout(3000).get(); // Fetching the HTML content of the web page.

            String text = document.toString();
            int occurrences = text.split("\\b(" + word + ")\\b").length - 1; // Take the occurrences number in the page.

            //if(occurrences > 0){
                System.out.println("[In '" + webAddress + "' local occurrences: " + occurrences + "at depth: " + currentDepth + "]");
            //}

            // If the current depth is less than the maximum depth, continue exploring links on the page
            if(currentDepth < maxDepth){
                // Find all links on the page and recursively crawl them
                Iterable<Element> links = document.select("a[href]");

                for(Element v : links){
                    String nextAddress = v.absUrl("href").split("#")[0].replaceAll("/+$", "");
                    String nextAddressFormatted = nextAddress.split("\\?")[0].replaceAll("/+$", "");
                    //System.out.println(nextAddress);
                    if ((nextAddress.startsWith("https://") || nextAddress.startsWith("http://")) && !this.pageAlreadyVisited(nextAddressFormatted)) {
                        addVisitedPage(nextAddressFormatted);
                        crawl(nextAddress, currentDepth + 1, maxDepth, word);
                    }
                }

                /*Observable.fromIterable(links)
                    .blockingSubscribe(v -> {

                    });*/
            }
            System.out.println("Ended exploring " + webAddress);
        } catch (IOException e) {
            //System.out.println("Failed exploring " + webAddress);
        }
    }

}
