package es2;

import java.util.Map;

public interface WebCrawler {
    record Result(String webAddress, int depth, int occurrences) {}

    void crawl() throws Exception;

}