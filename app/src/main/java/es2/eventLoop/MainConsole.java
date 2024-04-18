package es2.eventLoop;

public class MainConsole {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "virtuale";
        int depth = 3;
        WordCounter counter = new WordCounterImpl((res) -> {
            System.out.println("[In '" + res.webAddress() + "' local occurrences: " + res.occurrences() + "]");
        });
        counter.getWordOccurrences(webAddress, word, depth);
    }
}