package es2.virtualThreads;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WordCounterImpl implements WordCounter{
    private Map<String, Integer> getResult;
    private Consumer<WebCrawlerVirtualThread.Result> consumer;
    private ExecutorService executor;

    public WordCounterImpl(){
        this.consumer = res -> { };
    }

    public WordCounterImpl(Consumer<WebCrawlerVirtualThread.Result> consumer){
        this.consumer = consumer;
    }
    
    public void getWordOccurrences(final String webAddress, final String word, final int depth){
        try {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();

            // Submitting a task to a virtual thread executor
            var res = this.executor.submit(new WebCrawlerVirtualThread(this.executor, consumer, webAddress, 1, depth, word, new ArrayList<>()));
            try {                
                // Printing local occurrences number for each URL 
                // res.get().forEach((x, y) -> {
                //     System.out.println("[In " + x + " local occurrences: " + y + "]");
                // });
                res.get();
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
