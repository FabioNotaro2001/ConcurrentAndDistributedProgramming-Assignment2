package es2.reactiveProgramming;

import es2.WebCrawlerResult;
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


public class WebCrawlerWithReactiveProgramming {
    private record Search(String webAddress, int currentDepth) {}   // Record for the next search to be done.
    private final String startingWebAddress;
    private final String word;
    private final int maxDepth;
    private final Set<String> alreadyVisitedPages;
    private final AtomicBoolean isStopped; // Atomic for GUI.

    public WebCrawlerWithReactiveProgramming(final String webAddress, final String word, final int maxDepth) {
        this.startingWebAddress = webAddress;
        this.word = word;
        this.maxDepth = maxDepth;
        this.alreadyVisitedPages = new ConcurrentSkipListSet<>();
        this.isStopped = new AtomicBoolean(false);
    }

    public void requestStop(){
        this.isStopped.set(true);
    }

    public Observable<WebCrawlerResult.Result> crawl() {
        return Observable.<WebCrawlerResult.Result> create(resultEmitter -> {
            // Action to be done on each flow (single link to visit).
            Function<Search, Flowable<Search>> crawler = (Search src) -> {
                if(isStopped.get()) {
                    return Flowable.empty();
                }

                List<Search> linksFound = new ArrayList<>();
                String webAddress = src.webAddress;
                int currentDepth = src.currentDepth;

                this.alreadyVisitedPages.add(webAddress); // Marking the current page as explored.

                try {
                    Document document = Jsoup.connect(webAddress).timeout(3000).get();
                    String text = document.toString();
                    int occurrences = text.split("\\b(" + this.word + ")\\b").length - 1; // Take the occurrences number in the page.

                    if (occurrences > 0) {
                        // Emit a new item on the flow.
                        resultEmitter.onNext(new WebCrawlerResult.Result(webAddress, currentDepth, occurrences));
                    }

                    // If the current depth is less than the maximum depth, continue exploring links on the page.
                    if(currentDepth < this.maxDepth){
                        // Find all links on the page.
                        Elements links = document.select("a[href]");

                        for (Element link : links) {
                            // Clear the Url with regex syntax.
                            String nextUrl = link.absUrl("href").split("#")[0].replaceAll("/+$", "");
                            String noQueryStringUrl = nextUrl.split("\\?")[0].replaceAll("/+$", "");

                            // If the link is not already explored and is a valid URL (https or http), we add it the list of links found.
                            if((nextUrl.startsWith("https://") || nextUrl.startsWith("http://")) && !this.alreadyVisitedPages.contains(noQueryStringUrl)){
                                this.alreadyVisitedPages.add(noQueryStringUrl);
                                linksFound.add(new Search(nextUrl, currentDepth + 1));
                            }
                        }
                    }
                } catch (IOException e) {
                }
                // After processing a page, we return a flow with the links found there.
                return Flowable.fromIterable(linksFound);
            };

            var flowSearches = Flowable.just(new Search(this.startingWebAddress, 1));
            // For each depth, take the current flow and apply to it a flat map that for each item creates a new flow on which it applies the flat map with the crawl function.
            for (int i = 0; i < maxDepth; i++) {
                flowSearches = flowSearches.flatMap(src ->  // Parallel execution of the search.
                    Flowable.just(src)
                            .subscribeOn(Schedulers.io())
                            .flatMap(crawler)
                );
            }
            flowSearches.blockingSubscribe();   // Useful for waiting the results.
            resultEmitter.onComplete(); // Signal the end of the search to the caller.
        }).subscribeOn(Schedulers.io());    // The whole flow must be executed asynchronously.
    }

}
