package ca.uwo.csd.cs2212.team10;

import javax.swing.SwingUtilities;

/**
 * The entry point of the gradebook program
 * @author Team 10
 */
public class AppLauncher{
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true); //spawn the main window
            }
        });
    }
}
