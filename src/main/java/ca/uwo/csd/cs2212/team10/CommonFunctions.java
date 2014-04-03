package ca.uwo.csd.cs2212.team10;

import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Class to group together methods frequently used in many classes
 * 
 * @author Team 10
 */
public class CommonFunctions{
    private static final DecimalFormat formatter = new DecimalFormat("0.##'%'");
    
    private CommonFunctions(){
        // This class may not be instantiated
    }

    public static String formatGrade(double grade){
        if (grade == Student.NO_GRADE)
            return "--.--%";
        else
            return formatter.format(grade);
    }
    
    public static void showErrorMessage(Component parent, String text){
        JOptionPane.showMessageDialog(parent, text, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showWarningMessage(Component parent, String text){
        JOptionPane.showMessageDialog(parent, text, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}