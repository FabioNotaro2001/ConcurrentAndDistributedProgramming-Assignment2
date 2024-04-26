package es2.reactiveProgramming;

import es2.WebCrawler;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;


public class WebCrawlerReactiveProgramming {
    private record Search(String webAddress, int currentDepth) {}
    private final String startWebAddress;
    private final String word;
    private final int maxDepth;
    private final Set<String> alreadyVisitedPages;
    private final AtomicBoolean isStopped; // Atomico per la gui

    public WebCrawlerReactiveProgramming(final String webAddress, final String word, final int maxDepth) {
        this.startWebAddress = webAddress;
        this.word = word;
        this.maxDepth = maxDepth;
        this.alreadyVisitedPages = new ConcurrentSkipListSet<>();
        this.isStopped = new AtomicBoolean(false);
    }

    public void requestStop(){
        this.isStopped.set(true);
    }

    public Observable<WebCrawler.Result> crawl() {
        return Observable.<WebCrawler.Result>create(resultEmitter -> {
            Function<Search, Flowable<Search>> crawler = (Search src) -> {
                if(isStopped.get()) {
                    return Flowable.empty();
                }
                List<Search> linksFound = new ArrayList<>();

                String webAddress = src.webAddress;
                int currentDepth = src.currentDepth;

                this.alreadyVisitedPages.add(webAddress); // Marking the current page explored.
                try {
                    Document document = Jsoup.connect(webAddress).timeout(3000).get(); // Fetching the HTML content of the web page.

                    String text = document.toString();
                    int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.

                    if (occurrences > 0) {
//                        System.out.println("On thread " + Thread.currentThread().getName() + " emitting result...");
                        resultEmitter.onNext(new WebCrawler.Result(webAddress, currentDepth, occurrences));
                    }

                    // If the current depth is less than the maximum depth, continue exploring links on the page
                    if(currentDepth < this.maxDepth){
                        // Find all links on the page and recursively crawl them
                        Elements links = document.select("a[href]");

                        for (Element link : links) {
                            // Clear the Url with regex syntax.
                            String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                            String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");

                            // If the link is not already explored and is a valid URL (https or http), submit it for crawling
                            if((nextUrl.startsWith("https://") || nextUrl.startsWith("http://")) && !this.alreadyVisitedPages.contains(noQueryStringUrl)){
                                this.alreadyVisitedPages.add(noQueryStringUrl);
                                linksFound.add(new Search(nextUrl, currentDepth + 1));
                            }
                        }
                    }
                    // System.out.println(outputPadding + "Ended exploring " + this.webAddress);
                } catch (IOException e) {
                    // System.out.println("Failed exploring " + this.webAddress);
                }
                return Flowable.fromIterable(linksFound);
            };

            var flowSearches = Flowable.just(new Search(this.startWebAddress, 1));
            for (int i = 0; i < maxDepth; i++) {
                flowSearches = flowSearches.flatMap(src ->  // Parallel execution of the search.
                    Flowable.just(src)
                            .subscribeOn(Schedulers.io())
                            .flatMap(crawler)
                );
            }
            flowSearches.blockingSubscribe();
            resultEmitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

}
