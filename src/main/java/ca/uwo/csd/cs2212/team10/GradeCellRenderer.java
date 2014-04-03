package ca.uwo.csd.cs2212.team10;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Implements the Cell Renderer used in the JTable
 * Set's the grade format to be displayed correctly
 *
 * @author team10
 */
public class GradeCellRenderer extends DefaultTableCellRenderer {
    public GradeCellRenderer(){
        super();
        setHorizontalAlignment(SwingConstants.RIGHT);
    }
    
    @Override
    public void setValue(Object aValue) {
        super.setValue(CommonFunctions.formatGrade((Double)aValue));
    }
}
