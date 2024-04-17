package es2.virtualThreads;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class WordCounterImpl implements WordCounter{
    private Map<String, Integer> getResult;
    private Consumer<WebCrawler.Result> consumer;
    private WrapperMonitor<Boolean> stopped = new WrapperMonitor<>(true);

    public WordCounterImpl(Consumer<WebCrawler.Result> consumer){
        this.consumer = consumer;
    }
    
    public void getWordOccurrences(final String webAddress, final String word, final int depth){
        stopped.setValue(false);;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submitting a task to a virtual thread executor
            var res = executor.submit(new WebCrawler(executor, consumer, webAddress, 1, depth, word, new ArrayList<>(), stopped));
            try {
                // Getting the result from the task, counting the total word values
                int totalOccurrences = res.get().values().stream().mapToInt(Integer::intValue).sum();
                
                // Printing local occurrences number for each URL 
                res.get().forEach((x, y) -> {
                    System.out.println("[In " + x + " local occurrences: " + y + "]");
                });
                System.out.println("Total occurrences: " + totalOccurrences);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        stopped.setValue(true);;
    }
    @Override
    public void stop() {
        if (!stopped.getValue()) {
            stopped.setValue(true);
        } else {
            throw new IllegalStateException("Not running");
        }
    }
}
