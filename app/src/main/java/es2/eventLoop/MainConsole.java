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

        // L'event loop è il motore principale di Vert.x,
        // responsabile della gestione degli eventi e dell'esecuzione dei verticle.
        // Quando un evento arriva, viene messo in coda nell'event loop e i verticle associati
        // vengono chiamati per gestire l'evento.
        // deployVerticle lanciare un programma async che interagisce con l'event loop.
        
        /*
        vertx.deployVerticle(new VerticleSearch(webAddress, depth, word, (res) -> {
            System.out.println("[In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]"); // Ogni occorenza viene stampata
        }))
        .onComplete(res -> System.exit(0));
        */
        final Set<String> alreadyVisitedPages = new ConcurrentSkipListSet<>();
        final AtomicBoolean isStopped = new AtomicBoolean(false); // Atomico per la gui
        final AtomicInteger remainingSearches = new AtomicInteger(1); // Numero di ricerche ancora da fare per sapere se abbiamo finito

        List<Future<String>> liFuture = new ArrayList<>();

        int nVerticle = 5;

        for(int i = 0; i < nVerticle; i++){
            liFuture.add(vertx.deployVerticle(new VerticleSearch_v2(webAddress, depth, word, (res) -> {
                System.out.println("Verticle: "+ res.id() +" -> [In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]"); // Ogni occorenza viene stampata
            }, i, nVerticle, alreadyVisitedPages, isStopped, remainingSearches)));
        }

        Future.any(liFuture).onComplete((res) -> System.exit(0));
    }
}