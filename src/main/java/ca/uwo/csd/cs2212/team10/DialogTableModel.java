package ca.uwo.csd.cs2212.team10;

import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Class to set the Model of the Generate Report and Email dialog boxes Table
 * 
 * @author team10
 */

public class DialogTableModel extends AbstractTableModel {
    private final static int IDX_CHECKBOX = 0;
    private final static int IDX_NAME = 1;
    private final static int IDX_NUMBER = 2;
    private final static int IDX_EMAIL = 3;
    private final static int COLUMN_COUNT = 4;
    private final List<Student> students;
    private Boolean[] checkBoxes;

    /*Constructor*/
    public DialogTableModel(List<Student> studentsList) {
        students = studentsList;
        checkBoxes = new Boolean[students.size()];
        Arrays.fill(checkBoxes, true);
    }
    
    /*Overridden Methods*/
    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == IDX_CHECKBOX)
            return Boolean.class;
        else 
            return String.class;
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == IDX_NAME)
            return "Student";
        else if (columnIndex == IDX_NUMBER)
            return "Number";
        else if (columnIndex == IDX_EMAIL)
            return "Email";
        else if (columnIndex == IDX_CHECKBOX)
            return "Select Student";
        else 
            return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student selectedStudent = students.get(rowIndex);
        
        if (columnIndex == IDX_NAME)
            return (selectedStudent.getLastName() + ", " + selectedStudent.getFirstName());
        else if (columnIndex == IDX_NUMBER)
            return selectedStudent.getNum();
        else if (columnIndex == IDX_EMAIL)
            return selectedStudent.getEmail();
        else if (columnIndex == IDX_CHECKBOX)
            return checkBoxes[rowIndex];
        else 
            return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        checkBoxes[rowIndex] = (boolean) aValue;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == IDX_CHECKBOX)
            return true;
        else
            return false;
    }
 
}
