package es2.virtualThreads;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        String webAddress = "https://virtuale.unibo.it";
        String word = "html";
        int depth = 4;
        //counter.getWordOccurrences(webAddress, word, depth);
        Gui gui = new Gui();
        gui.setVisible(true);
        
    }
}
