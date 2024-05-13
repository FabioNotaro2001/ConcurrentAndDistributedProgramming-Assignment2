package es2.reactiveProgramming;

public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;

        new WebCrawlerWithReactiveProgramming(webAddress, word, depth).crawl()
            .blockingSubscribe(res -> {
                System.out.println(" [In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]");
            });
    }
}
