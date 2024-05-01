package es2.eventLoop;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.*;


public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;
        Vertx vertx = Vertx.vertx();

        // deployVerticle execute a verticle, which runs asynchronously.

        // Version 1 (one single verticle).
        /*
        vertx.deployVerticle(new VerticleSearch(webAddress, depth, word, (res) -> {
            System.out.println("[In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]"); // Ogni occorenza viene stampata
        }))
        .onComplete(res -> System.exit(0));
        */

        // Version 2 (multiple verticles).
        // We need to pass explicitly the shared collections as they must be shared among all verticles.
        final Set<String> alreadyVisitedPages = new ConcurrentSkipListSet<>();
        final AtomicBoolean isStopped = new AtomicBoolean(false); // Atomic for the GUI.
        final AtomicInteger remainingSearches = new AtomicInteger(1); // Number of searches to be done, useful to know if the search is over.

        List<Future<String>> listOfFutures = new ArrayList<>();

        int nVerticle = 5;

        for(int i = 0; i < nVerticle; i++){
            listOfFutures.add(vertx.deployVerticle(new VerticleSearch_v2(webAddress, depth, word, (res) -> {
                System.out.println("Verticle: "+ res.id() +" -> [In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]");
            }, i, nVerticle, alreadyVisitedPages, isStopped, remainingSearches)));
        }

        // Only one of the verticles will complete the promise because only one will read remainingSearches == 0.
        Future.any(listOfFutures).onComplete((res) -> System.exit(0));
    }
}