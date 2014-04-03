package ca.uwo.csd.cs2212.team10;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Implements the Cell Editor used in the JTable
 *
 * @author team10
 */
public class GradeCellEditor extends DefaultCellEditor {
    private JTextField textField;

    /*Contructor*/
    public GradeCellEditor() {
        super(new JTextField());
        textField = (JTextField)getComponent();
        textField.setHorizontalAlignment(JTextField.RIGHT);
    }
    
    /*Overridden methods*/
    @Override
    public boolean stopCellEditing() {
        //Prevents user from stop editing if the entered data is not a positive number
        try {
            double value = Double.valueOf(textField.getText());
            if (value < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            textField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            return false;
        }
        return super.stopCellEditing();
    }
    
    @Override
    public Object getCellEditorValue() {
        return Double.valueOf(textField.getText());
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
               
        textField.setBorder(BorderFactory.createEmptyBorder());
        if ((Double)value == Student.NO_GRADE)
            textField.setText("0.0");

        //Necessary to select all the content on cell when before starts editing a grade
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                textField.requestFocus();
                textField.selectAll();
            }
        });
        
        return textField;
    }
}
