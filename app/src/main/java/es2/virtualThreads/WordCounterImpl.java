package es2.virtualThreads;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class WordCounterImpl implements WordCounter{

    public void getWordOccurrences(final String webAddress, final String word, final int depth){
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submitting a task to a virtual thread executor
            var res = executor.submit(new WebCrawler(executor, webAddress, 1, depth, word, new ArrayList<>()));
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
    }
}
