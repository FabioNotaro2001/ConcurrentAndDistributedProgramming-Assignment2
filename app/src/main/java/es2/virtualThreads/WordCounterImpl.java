package es2.virtualThreads;

import es2.WebCrawlerResult;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class WordCounterImpl implements WordCounter{
    private Consumer<WebCrawlerResult.Result> consumer; // Defines the action to be done once the result is ready.
    private ExecutorService executor;

    public WordCounterImpl(Consumer<WebCrawlerResult.Result> consumer){
        this.consumer = consumer;
    }
    
    public void getWordOccurrences(final String webAddress, final String word, final int depth){
        try {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();    // Virtual thread executor.

            // Here we submit the crawler task to the executor.
            Future<Void> res = this.executor.submit(new WebCrawlerWithVirtualThread(this.executor, consumer, webAddress, 1, depth, word, new ConcurrentSkipListSet<>()));
            try {
                res.get();  // Waits the completion of the executor task.
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } finally {
            this.executor.shutdown();
        }
    }

    @Override
    public void stop() {
        if (this.executor != null) {
            this.executor.shutdown();
        } else {
            throw new IllegalStateException("Not running");
        }
    }
}
