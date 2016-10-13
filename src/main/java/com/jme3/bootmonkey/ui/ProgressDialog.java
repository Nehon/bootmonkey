package com.jme3.bootmonkey.ui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by bouquet on 13/10/16.
 */
public class ProgressDialog extends JDialog implements ActionListener{

    private JTextPane log = new JTextPane();

    public ProgressDialog(JFrame parent, String title) {
        super(parent, title, true);
//        if (parent != null) {
//            Dimension parentSize = parent.getSize();
//            Point p = parent.getLocation();
//            setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
//        }

        getContentPane().setLayout(new BorderLayout());
        log.setPreferredSize(new Dimension(600, 200));
        getContentPane().add(new JScrollPane(log), BorderLayout.CENTER);
        setModal(false);

        JPanel buttonPane = new JPanel();
        JButton button = new JButton("OK");
        buttonPane.add(button);
        button.addActionListener(this);
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);

    }

    public void progress(String text, List<String> errors){
        for (String error : errors) {
            appendToPane(log, error, Color.RED);
        }
        appendToPane(log, text, Color.WHITE);
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }

    public void clear(){
        log.setText("");
    }

    public void display(){
        clear();
        setVisible(true);
    }

    private void appendToPane(JTextPane tp, String msg, Color c){
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg + "\n");
    }
}
