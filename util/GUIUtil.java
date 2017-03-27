package util;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUIUtil {
    public static void setlf() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException |
            IllegalAccessException | UnsupportedLookAndFeelException ex) {
                System.err.println("Couldn't set system look and feel");
        }
    }

    public static void makeGUI(String title, JPanel gridPanel, JPanel buttonPanel, JPanel statusPanel) {
        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run() {
                setlf();
                JFrame frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                if(gridPanel != null)
                    frame.add(gridPanel, BorderLayout.CENTER);
                if(buttonPanel != null)
                    frame.add(buttonPanel, BorderLayout.EAST);
                if(statusPanel != null)
                    frame.add(statusPanel, BorderLayout.SOUTH);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
