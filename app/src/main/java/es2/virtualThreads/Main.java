package es2.virtualThreads;

public class Main {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "html";
        int depth = 4;
        WordCounter counter = new WordCounterImpl();
        counter.getWordOccurrences(webAddress, word, depth);

    }
}
