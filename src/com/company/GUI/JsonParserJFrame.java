package com.company.GUI;

import javax.swing.*;
import java.awt.*;

public class JsonParserJFrame {
    static JFrame frame;
    static Container container = new app().panelMain;

    public JsonParserJFrame() {
        frame = new JFrame("Json Parser");
        frame.setContentPane(container);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    public static void SetInvisible(){
        frame.setVisible(false);
    }
}