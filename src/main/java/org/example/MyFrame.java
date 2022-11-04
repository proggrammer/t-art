package org.example;

import javax.swing.*;
import java.util.List;

public class MyFrame extends JFrame {
    MyPanel panel;

    MyFrame(List<Circle> circles, int w, int h){
        panel = new MyPanel(circles, w, h);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
