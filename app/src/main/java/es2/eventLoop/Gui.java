package es2.eventLoop;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

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
    private final WordCounter wordCounter;

    public Gui() {
        this.wordCounter = new WordCounterImpl(res -> SwingUtilities.invokeLater(() -> {
            updateTextArea(res);
        }));

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
                
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
    }

    public void updateTextArea(WebCrawlerVirtualThread.Result res) {
        Object[] rowData = {res.webAddress(), res.depth(), res.occurrences()};
        model.addRow(rowData);
        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
    }
}
