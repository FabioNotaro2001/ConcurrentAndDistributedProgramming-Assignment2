package es2.virtualThreads;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;

public class Gui extends JFrame {
    private JTextField textField1, textField2, textField3;
    private JButton button1, button2;
    private DefaultTableModel model;
    private JTable table;

    private String webAddress;
    private int depth;
    private String word;

    public Gui() {
        setTitle("Esempio GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        // Creazione dei campi di testo
        JLabel label1 = new JLabel("Web address:");
        JLabel label2 = new JLabel("Word:");
        JLabel label3 = new JLabel("Depth:");
        textField1 = new JTextField(30);
        textField2 = new JTextField(30);
        textField3 = new JTextField(30);

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
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(350, 200));

        // Creazione dei pulsanti
        button1 = new JButton("Search");
        button2 = new JButton("Stop");

        // Aggiunta degli elementi al content pane
        Container container = getContentPane();
        container.setLayout(new FlowLayout());
        JPanel topPanel = new JPanel(new GridLayout(3, 2));
        topPanel.setPreferredSize(new Dimension(300, 80));;
        topPanel.add(label1);
        topPanel.add(textField1);
        topPanel.add(label2);
        topPanel.add(textField2);
        topPanel.add(label3);
        topPanel.add(textField3);

        // Panel per i pulsanti
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(button1);
        bottomPanel.add(button2);

        container.add(topPanel, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setRowCount(0);
                webAddress = textField1.getText();
                word = textField2.getText();
                depth = Integer.parseInt(textField3.getText());
                new Thread(() -> {
                    new WordCounterImpl(res -> SwingUtilities.invokeLater(() -> {
                        updateTextArea(res);
                    })).getWordOccurrences(webAddress, word, depth);
                }).start();
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    public void updateTextArea(WebCrawler.Result res){
        Object[] rowData = {res.webAddress(), res.depth(), res.occurrences()};
        model.addRow(rowData);}
}
