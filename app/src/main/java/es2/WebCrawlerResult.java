package es2;

// Common interface that defines the result type of web crawler.
public interface WebCrawlerResult {
    record Result(String webAddress, int depth, int occurrences) {}
}