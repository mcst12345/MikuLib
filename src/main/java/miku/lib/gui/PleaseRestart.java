package miku.lib.gui;

import javax.swing.*;
import java.awt.*;

public class PleaseRestart extends JPanel {
    public PleaseRestart(){
        JTextArea jt2=new JTextArea();   //new一个文本区
        JFrame jf=new JFrame("Please restart.");
        jf.setVisible(true);                     //窗体可见
        jf.setSize(100, 100);      //窗体大小
        jf.setLayout(new BorderLayout());    //边界布局
        jt2.setBackground(Color.WHITE);
        jt2.setText("MikuLib has just extracted the sqlite module of it. Please restart it.");
    }
}
