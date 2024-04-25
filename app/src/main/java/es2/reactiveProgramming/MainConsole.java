package es2.reactiveProgramming;


import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;

public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;

        var o = Observable.just(webAddress);

        o.blockingSubscribe(v -> {
            System.out.println("Entrato!");
            new WebCrawlerReactiveProgramming().crawl(v, 1, depth, word);
        });
        System.out.println("Uscito!");


        /*Observable<Integer> src2 = Observable.just(100)
                .map(v -> { log("map 1 " + v); return v * v; })		// by the current thread (main thread)
                .observeOn(Schedulers.computation()) 			// => use RX comp thread(s) downstream
                .map(v -> { log("map 2 " + v); return v + 1; });		// by the RX comp thread;

        src2.subscribe(v -> {						// by the RX comp thread
            log("sub 1 " + v);
        });

        src2.subscribe(v -> {						// by the RX comp thread
            log("sub 2 " + v);
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }
    /*static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }*/
}
