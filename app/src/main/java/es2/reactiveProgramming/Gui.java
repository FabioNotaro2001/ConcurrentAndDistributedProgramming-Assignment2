package es2.reactiveProgramming;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import es2.virtualThreads.WebCrawlerWithVirtualThread;

import java.awt.*;
import java.awt.event.*;

public class Gui extends JFrame {
    private JTextField txtWebAddress, txtWord, txtDepth;
    private JButton buttonSearch, buttonStop;
    private DefaultTableModel model;
    private JTable table;

    private String webAddress;
    private int depth;
    private String word;

    private WebCrawlerWithReactiveProgramming webCrawler;

    public Gui() {

        setTitle("Esempio GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        // Creazione dei campi di testo
        JLabel labelWebAddress = new JLabel("Web address:");
        JLabel labelWord = new JLabel("Word:");
        JLabel labelDepth = new JLabel("Depth:");
        txtWebAddress = new JTextField("https://virtuale.unibo.it", 30);
        txtWord = new JTextField("virtuale", 30);
        txtDepth = new JTextField("2", 30);

        Object[][] data = {};
        String[] columnNames = {"Address", "Depth", "Num"};
        model = new DefaultTableModel(data, columnNames);

        table = new JTable(model);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(60);
        var center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        
        
        // Creazione dei pulsanti
        buttonSearch = new JButton("Search");
        buttonStop = new JButton("Stop");
        buttonStop.setEnabled(false);
        
        int borderSize = 10;

        // Aggiunta degli elementi al content pane
        Container container = getContentPane();
        container.setLayout(new BorderLayout(0, borderSize));

        // Pannello per gli input
        JPanel topPanel = new JPanel(new GridLayout(3, 2));
        topPanel.setPreferredSize(new Dimension(300, 80));
        topPanel.add(labelWebAddress);
        topPanel.add(txtWebAddress);
        topPanel.add(labelWord);
        topPanel.add(txtWord);
        topPanel.add(labelDepth);
        topPanel.add(txtDepth);

        // Pannello per la tabella
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        
        // Pannello per i pulsanti
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(buttonSearch);
        bottomPanel.add(buttonStop);

        container.add(topPanel, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);

        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonSearch.setText("Searching...");

                model.setRowCount(0);
                webAddress = txtWebAddress.getText();
                if(!(webAddress.startsWith("https://") || webAddress.startsWith("http://"))) {
                    webAddress = "https://" + webAddress;
                }
                word = txtWord.getText();
                depth = Integer.parseInt(txtDepth.getText());
                buttonSearch.setEnabled(false);
                buttonStop.setEnabled(true);

                webCrawler = new WebCrawlerWithReactiveProgramming(webAddress, word, depth);

                webCrawler.crawl()
                    .doOnComplete( () ->
                            SwingUtilities.invokeLater(() -> {
                                buttonSearch.setText("Search");
                                buttonSearch.setEnabled(true);
                                buttonStop.setText("Stop");
                                buttonStop.setEnabled(false);
                            })
                    )
                    .subscribe(res -> {
                        SwingUtilities.invokeLater(() -> updateTextArea(res));
                    }); 
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                webCrawler.requestStop();
                buttonStop.setEnabled(false);
                buttonStop.setText("Stopping...");
            }
        });
    }

    public void updateTextArea(WebCrawlerWithVirtualThread.Result res) {
        Object[] rowData = {res.webAddress(), res.depth(), res.occurrences()};
        model.addRow(rowData);
        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
    }
}
