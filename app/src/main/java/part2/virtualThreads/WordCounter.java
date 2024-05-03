package es2.virtualThreads;

public interface WordCounter {
    void getWordOccurrences(final String webAddress, final String word, final int depth);
    void stop();
}