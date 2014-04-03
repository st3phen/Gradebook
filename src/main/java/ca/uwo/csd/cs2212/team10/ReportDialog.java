package ca.uwo.csd.cs2212.team10;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.TableColumn;
import java.util.List;
import java.util.ArrayList;
import org.jdesktop.swingx.JXTable;

/**
 * Dialog to handle the Generate Report Action
 *
 * @author team10
 */
public class ReportDialog extends JDialog {
    private JButton cancel;
    private JScrollPane jScrollPane1;
    private JButton ok;
    private JButton pathBtn;
    private JLabel pathLabel;
    private JTextField pathTxt;
    private JXTable studentsTbl;

    private int retval;
    private Object[] output;
    private static List<Student> students;

    /*Constructor*/
    public ReportDialog(Frame parent, boolean modal, List<Student> studentsList) {
        super(parent, modal);
        students = studentsList;
        initComponents();
        initTable();
    }

    /*Private Methods*/
    
    //Initialize Components and set the layout of the dialog
    private void initComponents() {
        pathLabel = new JLabel();
        jScrollPane1 = new JScrollPane();
        studentsTbl = new JXTable();
        ok = new JButton();
        cancel = new JButton();
        pathTxt = new JTextField();
        pathBtn = new JButton();

        setTitle("Generate PDF Reports");
        setMinimumSize(new Dimension(100, 100));
        setResizable(false);

        pathLabel.setText("Select destination folder:");

        jScrollPane1.setViewportView(studentsTbl);

        ok.setText("Save");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okActionPerformed(evt);
            }
        });

        cancel.setText("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        pathTxt.setEditable(false);


        pathBtn.setIcon(new ImageIcon(getClass().getResource("/folder.png")));
        pathBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                pathBtnActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 372, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ok)
                        .addGap(18, 18, 18)
                        .addComponent(cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pathLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathTxt, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pathBtn, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(pathBtn)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(pathLabel, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                        .addComponent(pathTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel)
                    .addComponent(ok))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    //Initialize JTable
    private void initTable() {
        DialogTableModel tblModel = new DialogTableModel(students);
        studentsTbl.setModel(tblModel);

        studentsTbl.setAutoCreateRowSorter(true);
        studentsTbl.setRowSelectionAllowed(true);
        studentsTbl.getRowSorter().toggleSortOrder(1);
        studentsTbl.setGridColor(Color.gray);
        studentsTbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        studentsTbl.setRowHeight(22);

        TableColumn tc = studentsTbl.getColumnModel().getColumn(0);
        tc.setCellEditor(studentsTbl.getDefaultEditor(Boolean.class));
        tc.setCellRenderer(studentsTbl.getDefaultRenderer(Boolean.class));
        tc.setHeaderRenderer(new CheckBoxTableHeader(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object source = e.getSource();
                if (source instanceof AbstractButton == false) {
                    return;
                }
                boolean checked = e.getStateChange() == ItemEvent.SELECTED;
                for (int x = 0, y = studentsTbl.getRowCount(); x < y; x++) {
                    studentsTbl.setValueAt(new Boolean(checked), x, 0);
                }
            }
        }));
    }

    /*Button Actions*/
    
    private void okActionPerformed(ActionEvent evt) {
        if (pathTxt.getText().trim().isEmpty()) {
            CommonFunctions.showErrorMessage(this, "You must choose a folder.");
            return;
        }
        
        retval = 0;
        
        List<Student> selectedStudents = new ArrayList();
        for (int i = 0; i < studentsTbl.getRowCount(); i++) {
            if ((boolean)studentsTbl.getValueAt(i, 0))
                selectedStudents.add(students.get(studentsTbl.convertRowIndexToModel(i)));
        }
        
        output = new Object[] {pathTxt.getText(), selectedStudents};
        setVisible(false);
    }

    private void cancelActionPerformed(ActionEvent evt) {
        retval = 1;
        setVisible(false);
    }

    private void pathBtnActionPerformed(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int option = chooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            pathTxt.setText(file.getAbsolutePath());
        }

    }

    /*Public Methods*/
    
    //Set dialog visible, only return when setVisible is set to false in one of the buttons actions
    //this way it keeps the dialog on the screen untill the user is finished and returns retval
    //indicating if user has finished action or canceled it
    public int showDialog() {
        setVisible(true);
        return retval;
    }
    
    //Outputs the path to save file and the list of Selected Students
    public Object[] getOutput() {
        return output;
    }
}
