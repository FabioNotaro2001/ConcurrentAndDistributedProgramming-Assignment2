package es2.eventLoop;

import es2.virtualThreads.WordCounter;
import es2.virtualThreads.WordCounterImpl;
import io.vertx.core.Vertx;


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

        int nVerticle = 3;
        vertx.deployVerticle(new VerticleSearch_v2(webAddress, depth, word, (res) -> {
            System.out.println("Vertical: "+ res.id() +" -> [In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]"); // Ogni occorenza viene stampata
        }, 0, nVerticle))
        .onComplete(res -> System.exit(0));
        
    }
}