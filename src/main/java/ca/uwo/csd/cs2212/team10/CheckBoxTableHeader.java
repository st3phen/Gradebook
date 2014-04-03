package ca.uwo.csd.cs2212.team10;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import javax.swing.table.*;
import java.awt.event.*;
import javax.swing.border.Border;

/**
 * Implements a custom renderer to header to show a Checkbox in dialog's tables
 * By clicking in the checkbox on the header the user can Select All/Deselect All Students of the Table
 * 
 * @author team10
 */
class CheckBoxTableHeader extends JCheckBox
        implements TableCellRenderer, MouseListener {

    protected CheckBoxTableHeader rendererComponent;
    protected int column;
    protected boolean mousePressed = false;

    /*Constructor*/
    public CheckBoxTableHeader(ItemListener itemListener) {
        rendererComponent = this;
        rendererComponent.addItemListener(itemListener);
        rendererComponent.setSelected(true);
    }

    /*Overridden Method from the TableCellRenderer class*/
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                rendererComponent.setForeground(header.getForeground());
                rendererComponent.setBackground(header.getBackground());
                rendererComponent.setFont(header.getFont());
                rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
                header.addMouseListener(rendererComponent);
            }
        }
        Border border = UIManager.getBorder("TableHeader.cellBorder");
        rendererComponent.setBorder(border);
        rendererComponent.setBorderPainted(true);
        return rendererComponent;
    }

    /*Overridden Method from the MouseListener class */
    
    //Necessary to implement the Select All/Deselect All functionality
    @Override
    public void mouseClicked(MouseEvent e) {
        if (mousePressed) {
            mousePressed = false;
            JTableHeader header = (JTableHeader) (e.getSource());
            JTable tableView = header.getTable();
            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);

            if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
                doClick();
            }
        }
        ((JTableHeader) e.getSource()).repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    //Not necessary to implement, methods are not used
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
