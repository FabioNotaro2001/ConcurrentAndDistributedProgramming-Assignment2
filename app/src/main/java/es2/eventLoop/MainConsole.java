package es2.eventLoop;

import es2.virtualThreads.WordCounter;
import es2.virtualThreads.WordCounterImpl;
import io.vertx.core.Vertx;

public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 2;
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VerticleSearch(webAddress, depth, word))
                .onComplete(res -> System.exit(0));
    }
}