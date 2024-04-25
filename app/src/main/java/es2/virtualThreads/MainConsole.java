package es2.virtualThreads;

public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;
        WordCounter counter = new WordCounterImpl((res) -> {
            System.out.println("[In '" + res.webAddress() + "' local occurrences: " + res.depth() + "]");
        });
        counter.getWordOccurrences(webAddress, word, depth);
    }
}