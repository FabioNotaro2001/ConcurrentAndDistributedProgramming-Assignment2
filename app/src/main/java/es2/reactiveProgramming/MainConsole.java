package es2.reactiveProgramming;


public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;

        new WebCrawlerReactiveProgramming(webAddress, word, depth)
            .crawl()
            .blockingSubscribe(res -> {
                System.out.println("On thread " + Thread.currentThread().getName() + " [In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]");
            });
    }
}
