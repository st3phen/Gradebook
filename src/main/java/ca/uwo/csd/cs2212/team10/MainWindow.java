package ca.uwo.csd.cs2212.team10;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*; 
import java.util.List;
import java.util.ArrayList;
import au.com.bytecode.opencsv.*;
import java.text.DecimalFormat;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.mail.internet.AddressException;
import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Transport;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperExportManager;
import org.jdesktop.swingx.*;

/**
 * The main window of the gradebook program
 * 
 * @author Team 10
 */
public class MainWindow extends JFrame {
    /* Constants */
    private final String DATA_FILENAME = "gradebook.dat";
    private final String BACKUP_FILENAME = "gradebook.dat.bak";
    private final int COLUMN_PADDING = 5;
    
    /* Attributes */
    private Gradebook gradebook;
    private ReportGenerator reportGenerator;
    private boolean callFirstStart = false;

    private JScrollPane jScrollPane1;
    private JXTable mainTable;
    private JComboBox dropDownCourses;
    private JPanel statusPanel;
    private JLabel statusLabel, courseAvgTxtLabel, courseAvgLabel,
            assignmentAvgTxtLabel, assignmentAvgLabel, examAvgTxtLabel, examAvgLabel;
    private JButton addStudentBtn, addDeliverableBtn,
            emailBtn, genRepBtn;
    private JPopupMenu studentTblPopup, deliverableTblPopup;
    private JMenuItem editStudentPopupMenu, delStudentPopupMenu,
            editDeliverablePopupMenu, delDeliverablePopupMenu;
    private JMenuBar jMenuBar;
    private JMenu fileMenu, coursesMenu, studentsMenu, deliverablesMenu, importMenu, exportMenu;
    private JMenuItem exitMenuItem, addMenuItem, editMenuItem, delMenuItem,
            addStudentMenuItem, editStudentMenuItem, delStudentMenuItem,
            addDeliverableMenuItem, editDeliverableMenuItem, delDeliverableMenuItem, impStudentsMenuItem,
            impGradesMenuItem, expGradesMenuItem, emailMenuItem, genRepMenuItem;

    /* Constructor */
    public MainWindow() {
        loadGradebook();
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initTable();
        setVisible(true);
        
        try{
            reportGenerator = new ReportGenerator();
        } catch (JRException e){
            CommonFunctions.showErrorMessage(this, "<html>The report generator could not be initialized.<br>" + 
                                                "PDF export and email functionality will not be available.</html>");
        }
        
        if (callFirstStart)
            firstStartAction();
    }

    /* Private methods */
    
    //Initializes table and it's properties
    private void initTable() {
        refreshTableModel();

        //Set cell editors and renderers for grades
        mainTable.setDefaultEditor(Double.class, new GradeCellEditor());
        mainTable.setDefaultRenderer(Double.class, new GradeCellRenderer());

        mainTable.setAutoCreateRowSorter(true);
        mainTable.setCellSelectionEnabled(true);
        mainTable.setGridColor(Color.gray);
        mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        mainTable.setHorizontalScrollEnabled(true);

        //Set height for the rows
        mainTable.setRowHeight(22);

        //Sets editing a grade by pressing ENTER and deleting a Student or a Deliverable by pressing Delete
        mainTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing");
        mainTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteStudent");
        mainTable.getActionMap().put("deleteStudent", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                int selectedColumn = mainTable.convertColumnIndexToModel(mainTable.getSelectedColumn());
                if (((CustomTableModel)mainTable.getModel()).isDeliverableColumn(selectedColumn)) {
                    delDeliverableAction();
                } else if (mainTable.getSelectedRow() != -1) {
                    delStudentAction();
                }
            }
        });

        //Mouse listener to open the Popup menus on deliverables and students
        mainTable.addMouseListener(new MouseAdapter() {
            //Mouse Listener for Linux/Mac
            @Override
            public void mousePressed(MouseEvent e) {
                mainTable.clearSelection();
                int row = mainTable.rowAtPoint(e.getPoint());
                int column = mainTable.columnAtPoint(e.getPoint());
                if (row >= 0 && row < mainTable.getRowCount()) {
                    mainTable.changeSelection(row, column, false, false);
                } else {
                    mainTable.clearSelection();
                }

                if (mainTable.getSelectedRow() >= 0 && e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    int selectedColumn = mainTable.convertColumnIndexToModel(mainTable.getSelectedColumn());

                    if (((CustomTableModel)mainTable.getModel()).isStudentColumn(selectedColumn)) {
                        studentTblPopup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (((CustomTableModel)mainTable.getModel()).isDeliverableColumn(selectedColumn)) {
                        deliverableTblPopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            //Mouse Listener for Windows
            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });

        //Right-Click on a Deliverable column header open a Popup menu
        mainTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    mainTable.clearSelection();
                    int column = mainTable.columnAtPoint(e.getPoint());
                    
                    if (mainTable.convertColumnIndexToModel(column) >= 2 
                            && mainTable.convertColumnIndexToModel(column) < (mainTable.getModel().getColumnCount() - 3)) {
                        mainTable.changeSelection(-1, column, false, false);
                        deliverableTblPopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });        
    }

    //Refresh all the data in the table when necessary (mostly when active course is changed)
    private void refreshTableModel() {
        List<Student> studentsList;
        List<Deliverable> deliverablesList;

        //Get Students and Deliverables list. Creates empty lists if they don't exist
        if (gradebook.getActiveCourse() != null) {
            studentsList = gradebook.getActiveCourse().getStudentList();
            deliverablesList = gradebook.getActiveCourse().getDeliverableList();
        } else {
            deliverablesList = new ArrayList<Deliverable>();
            studentsList = new ArrayList<Student>();
        }

        CustomTableModel tblModel = new CustomTableModel(studentsList, deliverablesList);
        mainTable.setModel(tblModel);
        
        //Sort table by the last name of students
        mainTable.getRowSorter().toggleSortOrder(0);
        
        //Table Model Listener to change the display of Class average everytime data on the Table changes
        mainTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateClassAvgLabel();
            }
        });
        updateClassAvgLabel();
        updateColumnSize();
    }

    //Initialize GUI Components
    private void initComponents() {
        jScrollPane1 = new JScrollPane(mainTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainTable = new JXTable() {
            //Necessary to Override this method so all the content of a Grade cell is selected when the user starts editing
            //by pressing any key on the keyboard
            @Override
            public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if (c instanceof JTextComponent) {
                    ((JTextComponent) c).selectAll();
                }
                return c;
            }
        };
        dropDownCourses = new JComboBox(gradebook.getCourseList().toArray());
        addStudentBtn = new JButton();
        addDeliverableBtn = new JButton();
        emailBtn = new JButton();
        genRepBtn = new JButton();
        studentTblPopup = new JPopupMenu("Students Menu");
        editStudentPopupMenu = new JMenuItem();
        delStudentPopupMenu = new JMenuItem();
        deliverableTblPopup = new JPopupMenu("Deliverables Menu");
        editDeliverablePopupMenu = new JMenuItem();
        delDeliverablePopupMenu = new JMenuItem();
        jMenuBar = new JMenuBar();
        fileMenu = new JMenu();
        importMenu = new JMenu();
        exportMenu = new JMenu();
        exitMenuItem = new JMenuItem();
        coursesMenu = new JMenu();
        addMenuItem = new JMenuItem();
        editMenuItem = new JMenuItem();
        delMenuItem = new JMenuItem();
        studentsMenu = new JMenu();
        addStudentMenuItem = new JMenuItem();
        editStudentMenuItem = new JMenuItem();
        delStudentMenuItem = new JMenuItem();
        deliverablesMenu = new JMenu();
        addDeliverableMenuItem = new JMenuItem();
        editDeliverableMenuItem = new JMenuItem();
        delDeliverableMenuItem = new JMenuItem();
        impStudentsMenuItem = new JMenuItem();
        impGradesMenuItem = new JMenuItem();
        expGradesMenuItem = new JMenuItem();
        emailMenuItem = new JMenuItem();
        genRepMenuItem = new JMenuItem();
        courseAvgTxtLabel = new JLabel("Class Average:");
        courseAvgLabel = new JLabel();
        assignmentAvgTxtLabel = new JLabel("Assignments Average:");
        assignmentAvgLabel = new JLabel();
        examAvgTxtLabel = new JLabel("Exams Average:");
        examAvgLabel = new JLabel();

        //Set Properties of the Class Average labels
        courseAvgTxtLabel.setFont(new Font("Sans Serif", Font.BOLD, 12));
        assignmentAvgTxtLabel.setFont(new Font("Sans Serif", Font.BOLD, 12));
        examAvgTxtLabel.setFont(new Font("Sans Serif", Font.BOLD, 12));
        courseAvgLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
        assignmentAvgLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
        examAvgLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                exitAction();
            }
        });

        setTitle("Gradebook");

        jScrollPane1.setViewportView(mainTable);

        dropDownCourses.addItem("Add Course");
        dropDownCourses.setSelectedItem(gradebook.getActiveCourse());
        dropDownCourses.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                dropDownItemChanged(evt);
            }
        });
        dropDownCourses.setRenderer(new ListCellRenderer() {
            private ListCellRenderer delegate;
            private JPanel separatorPanel = new JPanel(new BorderLayout());
            private JSeparator separator = new JSeparator();

            //Separator before "Add Course" is rendered right
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index != -1 && "Add Course".equals(value)) {
                    separatorPanel.removeAll();
                    separatorPanel.add(separator, BorderLayout.NORTH);
                    separatorPanel.add(comp, BorderLayout.CENTER);
                    return separatorPanel;
                } else {
                    return comp;
                }
            }

            private ListCellRenderer init(ListCellRenderer delegate) {
                this.delegate = delegate;
                return this;
            }
        }.init(dropDownCourses.getRenderer()));

        /*Initialize Buttons*/
        addStudentBtn.setIcon(new ImageIcon(getClass().getResource("/addStudent.png")));
        addStudentBtn.setMnemonic(KeyEvent.VK_B);
        addStudentBtn.setToolTipText("Add a new Student to the active Course (Alt+B)");
        addStudentBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addStudentAction();
            }
        });

        addDeliverableBtn.setIcon(new ImageIcon(getClass().getResource("/addDeliverable.png")));
        addDeliverableBtn.setMnemonic(KeyEvent.VK_N);
        addDeliverableBtn.setToolTipText("Add a new Deliverable to the active Course (Alt+N)");
        addDeliverableBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addDeliverableAction();
            }
        });

        emailBtn.setIcon(new ImageIcon(getClass().getResource("/email.png")));
        emailBtn.setMnemonic(KeyEvent.VK_M);
        emailBtn.setToolTipText("Send email (ALT+M)");
        emailBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sendEmailAction();
            }
        });

        genRepBtn.setIcon(new ImageIcon(getClass().getResource("/genRep.png")));
        genRepBtn.setMnemonic(KeyEvent.VK_R);
        genRepBtn.setToolTipText("Generate grade reports (ALT+R)");
        genRepBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                genReportsAction();
            }
        });

        //The following creates the file menu and adds the appropriate menu items
        //to the appropriate menus.
        
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setText("File");

        importMenu.setMnemonic(KeyEvent.VK_I);
        importMenu.setText("Import");
        fileMenu.add(importMenu);

        exportMenu.setMnemonic(KeyEvent.VK_E);
        exportMenu.setText("Export");
        fileMenu.add(exportMenu);

        fileMenu.addSeparator();

        impStudentsMenuItem.setText("Import students from CSV file");
        impStudentsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                importStudentsAction();
            }
        });
        importMenu.add(impStudentsMenuItem);

        impGradesMenuItem.setText("Import grades from CSV file");
        impGradesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                importGradesAction();
            }
        });
        importMenu.add(impGradesMenuItem);

        expGradesMenuItem.setText("Export grades to CSV file");
        expGradesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportGradesAction();
            }
        });
        exportMenu.add(expGradesMenuItem);

        emailMenuItem.setText("Send grade reports by email");
        emailMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sendEmailAction();
            }
        });
        exportMenu.add(emailMenuItem);

        genRepMenuItem.setText("Save grade reports as PDF");
        genRepMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                genReportsAction();
            }
        });
        exportMenu.add(genRepMenuItem);

        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exitAction();
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar.add(fileMenu);

        coursesMenu.setMnemonic(KeyEvent.VK_C);
        coursesMenu.setText("Courses");

        addMenuItem.setMnemonic(KeyEvent.VK_A);
        addMenuItem.setText("Add Course");
        addMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addCourseAction();
            }
        });
        coursesMenu.add(addMenuItem);

        editMenuItem.setMnemonic(KeyEvent.VK_E);
        editMenuItem.setText("Edit Course");
        editMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editCourseAction();
            }
        });
        coursesMenu.add(editMenuItem);

        delMenuItem.setMnemonic(KeyEvent.VK_D);
        delMenuItem.setText("Delete Course");
        delMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                delCourseAction();
            }
        });
        coursesMenu.add(delMenuItem);

        jMenuBar.add(coursesMenu);

        studentsMenu.setMnemonic(KeyEvent.VK_S);
        studentsMenu.setText("Students");

        addStudentMenuItem.setMnemonic(KeyEvent.VK_A);
        addStudentMenuItem.setText("Add Student");
        addStudentMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addStudentAction();
            }
        });
        studentsMenu.add(addStudentMenuItem);

        editStudentMenuItem.setMnemonic(KeyEvent.VK_E);
        editStudentMenuItem.setText("Edit Student");
        editStudentMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editStudentAction();
            }
        });
        studentsMenu.add(editStudentMenuItem);

        delStudentMenuItem.setMnemonic(KeyEvent.VK_D);
        delStudentMenuItem.setText("Delete Student");
        delStudentMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                delStudentAction();
            }
        });
        studentsMenu.add(delStudentMenuItem);

        jMenuBar.add(studentsMenu);

        deliverablesMenu.setMnemonic(KeyEvent.VK_D);
        deliverablesMenu.setText("Deliverables");

        addDeliverableMenuItem.setMnemonic(KeyEvent.VK_A);
        addDeliverableMenuItem.setText("Add Deliverable");
        addDeliverableMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addDeliverableAction();
            }
        });
        deliverablesMenu.add(addDeliverableMenuItem);

        editDeliverableMenuItem.setMnemonic(KeyEvent.VK_E);
        editDeliverableMenuItem.setText("Edit Deliverable");
        editDeliverableMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editDeliverableAction();
            }
        });
        deliverablesMenu.add(editDeliverableMenuItem);

        delDeliverableMenuItem.setMnemonic(KeyEvent.VK_D);
        delDeliverableMenuItem.setText("Delete Deliverable");
        delDeliverableMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                delDeliverableAction();
            }
        });
        deliverablesMenu.add(delDeliverableMenuItem);

        jMenuBar.add(deliverablesMenu);

        setJMenuBar(jMenuBar);

        /*Initializes Popup menus*/
        editStudentPopupMenu.setText("Edit Student");
        editStudentPopupMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editStudentAction();
            }
        });
        studentTblPopup.add(editStudentPopupMenu);

        delStudentPopupMenu.setText("Delete Student");
        delStudentPopupMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                delStudentAction();
            }
        });

        studentTblPopup.add(delStudentPopupMenu);

        editDeliverablePopupMenu.setText("Edit Deliverable");
        editDeliverablePopupMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editDeliverableAction();
            }
        });
        deliverableTblPopup.add(editDeliverablePopupMenu);

        delDeliverablePopupMenu.setText("Delete Deliverable");
        delDeliverablePopupMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                delDeliverableAction();
            }
        });

        deliverableTblPopup.add(delDeliverablePopupMenu);

        //Set Status Bar
        statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
        updateStatusBar();

        //Set the Layout of the Main Window
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(addStudentBtn)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addDeliverableBtn)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(emailBtn)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(genRepBtn)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(courseAvgTxtLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(courseAvgLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(assignmentAvgTxtLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(assignmentAvgLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(examAvgTxtLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(examAvgLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dropDownCourses, 250, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())
                .addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, this.getWidth(), Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(addStudentBtn)
                                                .addComponent(addDeliverableBtn)
                                                .addComponent(emailBtn)
                                                .addComponent(genRepBtn)
                                                .addComponent(courseAvgTxtLabel)
                                                .addComponent(courseAvgLabel)
                                                .addComponent(examAvgTxtLabel)
                                                .addComponent(examAvgLabel)
                                                .addComponent(assignmentAvgTxtLabel)
                                                .addComponent(assignmentAvgLabel)
                                                .addComponent(dropDownCourses))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE)
                        .addGap(20)
                        .addComponent(statusPanel))
        );

        pack();
    }

    //This method will change the active course to the course selected in the drop down menu
    private void dropDownItemChanged(ItemEvent evt) {
    	//If there is no courses in the drop down menu, the active course will be set to null
        if (dropDownCourses.getSelectedItem() == null) {
            gradebook.setActiveCourse(null);

            refreshTableModel();
            updateStatusBar();
        } else if (evt.getStateChange() == ItemEvent.SELECTED) {
        	//If user clicks on Add Course, method will call addCourseAction
            if (dropDownCourses.getSelectedItem().equals("Add Course")) {
                addCourseAction();
                if (gradebook.getCourseList().size() == 0) {
                    dropDownCourses.setSelectedItem(null);
                } else {
                    dropDownCourses.setSelectedItem(gradebook.getActiveCourse());
                }

                refreshTableModel();
                updateStatusBar();
            } else {
                gradebook.setActiveCourse((Course) dropDownCourses.getSelectedItem());
            }

            refreshTableModel();
            updateStatusBar();
        }
    }

    //Method updates the Class Average that is displayed in the application
    private void updateClassAvgLabel() {
        Course activeCourse = gradebook.getActiveCourse();
        
        //If there is no active course, the course, assignment and exam averages will
        //display the default no grade
        if (activeCourse == null) {
            courseAvgLabel.setText(CommonFunctions.formatGrade(Student.NO_GRADE));
            assignmentAvgLabel.setText(CommonFunctions.formatGrade(Student.NO_GRADE));
            examAvgLabel.setText(CommonFunctions.formatGrade(Student.NO_GRADE));
            
            //Else they will be updated with the relevant class average information
        } else {
            courseAvgLabel.setText(CommonFunctions.formatGrade(activeCourse.calcAverage()));
            assignmentAvgLabel.setText(CommonFunctions.formatGrade(activeCourse.calcAverage(Deliverable.ASSIGNMENT_TYPE)));
            examAvgLabel.setText(CommonFunctions.formatGrade(activeCourse.calcAverage(Deliverable.EXAM_TYPE)));
        }
    }

    private void addCourseAction() {
        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showAddCourseDialog(this, gradebook);

        if (prompt.getReturnValue() == UserEntryPrompter.OK_PRESSED) {
            String[] output = (String[]) prompt.getOutput();

            //Create a new Course and add it to the gradebook
            Course course = new Course(output[0], output[1], output[2]);
            gradebook.addCourse(course);

            //Add the entry to the dropdown list
            dropDownCourses.addItem(course);

            //Make "Add Course" the last item in the list   
            dropDownCourses.removeItem("Add Course");
            dropDownCourses.addItem("Add Course");

            //Make the new course selected
            dropDownCourses.setSelectedItem(course);
        }
    }

    private void editCourseAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "There are no courses in the gradebook.");
            return;
        }

        Course activeCourse = gradebook.getActiveCourse();

        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showEditCourseDialog(this, activeCourse, gradebook);

        int retval = prompt.getReturnValue();
        if (retval == UserEntryPrompter.OK_PRESSED) {
            String[] output = (String[]) prompt.getOutput();

            //Set the attributes
            activeCourse.setTitle(output[0]);
            activeCourse.setCode(output[1]);
            activeCourse.setTerm(output[2]);

            //Refresh the dropdown list and status bar
            dropDownCourses.revalidate();
            updateStatusBar();
        } else if (retval == UserEntryPrompter.DELETE_PRESSED) {
            delCourseAction();
        }
    }

    private void delCourseAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "There are no courses in the gradebook.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure? This action cannot be undone.", "Delete Course", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            //get the active course
            Course activeCourse = gradebook.getActiveCourse();

            //remove it from the gradebook
            gradebook.removeCourse(activeCourse);

            //remove it from the dropdown list
            if (gradebook.getCourseList().size() == 0) {
                dropDownCourses.setSelectedItem(null);
            }
            dropDownCourses.removeItem(activeCourse);
        }
    }

    private void addStudentAction() {
        if (gradebook.getActiveCourse() == null) {
            int option = JOptionPane.showConfirmDialog(this, "You must create a course first. Create one now?", "Question", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                addCourseAction();
            }
            return;
        }

        Course activeCourse = gradebook.getActiveCourse();

        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showAddStudentDialog(this, activeCourse);

        if (prompt.getReturnValue() == UserEntryPrompter.OK_PRESSED) {
            String[] output = (String[]) prompt.getOutput();

            //Create a new Student and add it to the gradebook
            Student student = new Student(output[0], output[1], output[2], output[3]);
            gradebook.getActiveCourse().addStudent(student);

            //Update JTable
            refreshTableModel();
            updateStatusBar();
        }
    }

    private void editStudentAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "Select a student first.");
            return;
        } else if (mainTable.getSelectedRow() < 0) {
            CommonFunctions.showErrorMessage(this, "Select a student first.");
            return;
        }

        //Get the Student object
        int selectedRow = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
        Student student = gradebook.getActiveCourse().getStudentList().get(selectedRow);
        Course activeCourse = gradebook.getActiveCourse();

        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showEditStudentDialog(this, student, activeCourse);

        int retval = prompt.getReturnValue();
        if (retval == UserEntryPrompter.OK_PRESSED) {
            String[] output = (String[]) prompt.getOutput();

            //Update student info
            student.setFirstName(output[0]);
            student.setLastName(output[1]);
            student.setNum(output[2]);
            student.setEmail(output[3]);

            //Update JTable
            refreshTableModel();
        } else if (retval == UserEntryPrompter.DELETE_PRESSED) {
            delStudentAction();
        }
    }

    private void delStudentAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "Select a student first.");
            return;
        } else if (mainTable.getSelectedRow() < 0) {
            CommonFunctions.showErrorMessage(this, "Select a student first.");
            return;
        }

        //Get the Student object
        int selectedRow = mainTable.convertRowIndexToModel(mainTable.getSelectedRow());
        Student student = gradebook.getActiveCourse().getStudentList().get(selectedRow);

        int option = JOptionPane.showConfirmDialog(this, "Are you sure? This action cannot be undone.", "Delete Student", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            gradebook.getActiveCourse().removeStudent(student);
            refreshTableModel();
            updateStatusBar();
        }

    }

    private void addDeliverableAction() {
        if (gradebook.getActiveCourse() == null) {
            int option = JOptionPane.showConfirmDialog(this, "You must create a course first. Create one now?", "Question", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                addCourseAction();
            }
            return;
        }

        Course activeCourse = gradebook.getActiveCourse();

        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showAddDeliverableDialog(this, activeCourse);

        if (prompt.getReturnValue() == UserEntryPrompter.OK_PRESSED) {
            Object[] output = prompt.getOutput();

            //Create a new Deliverable and add it to the Course
            Deliverable deliverable = new Deliverable((String) output[0], (int) output[1], (int) output[2]);
            gradebook.getActiveCourse().addDeliverable(deliverable);

            //Update JTable
            refreshTableModel();
        }
    }

    private void editDeliverableAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "Select a deliverable first.");
            return;
        } 
        int selectedColumn = mainTable.convertColumnIndexToModel(mainTable.getSelectedColumn());
        if (!((CustomTableModel)mainTable.getModel()).isDeliverableColumn(selectedColumn)) {
            CommonFunctions.showErrorMessage(this, "Select a deliverable first.");
            return;
        }

        //Use deliverableIndex to get the right Deliverable from the list
        int deliverableIndex = ((CustomTableModel)mainTable.getModel()).getDeliverableIndex(selectedColumn);
        Deliverable deliverable = gradebook.getActiveCourse().getDeliverableList().get(deliverableIndex);
        Course activeCourse = gradebook.getActiveCourse();

        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showEditDeliverableDialog(this, deliverable, activeCourse);

        int retval = prompt.getReturnValue();
        if (retval == UserEntryPrompter.OK_PRESSED) {
            Object[] output = prompt.getOutput();

            //Update Deliverable
            deliverable.setName((String) output[0]);
            deliverable.setType((int) output[1]);
            deliverable.setWeight((int) output[2]);

            //Update JTable
            refreshTableModel();
        } else if (retval == UserEntryPrompter.DELETE_PRESSED) {
            delDeliverableAction();
        }
    }

    private void delDeliverableAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "Select a deliverable first.");
            return;
        } 
        int selectedColumn = mainTable.convertColumnIndexToModel(mainTable.getSelectedColumn());
        if (!((CustomTableModel)mainTable.getModel()).isDeliverableColumn(selectedColumn)) {
            CommonFunctions.showErrorMessage(this, "Select a deliverable first.");
            return;
        }

        //Use deliverableIndex to get the right Deliverable from the list 
        int deliverableIndex = ((CustomTableModel)mainTable.getModel()).getDeliverableIndex(selectedColumn);
        Deliverable deliverable = gradebook.getActiveCourse().getDeliverableList().get(deliverableIndex);

        int option = JOptionPane.showConfirmDialog(this, "Are you sure? This action cannot be undone.", "Delete Deliverable", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            gradebook.getActiveCourse().removeDeliverable(deliverable);

            refreshTableModel();
        }
    }

    //Will import student list from a CSV file into the active course and display in the table
    private void importStudentsAction() {
    	
    	//If there is no active course then we notify the user to create one first
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "You must create a course first.");
            return;
        }

        //Create a new custom file chooser
        CustomFileChooser chooser = new CustomFileChooser();
        //File filter restricts users to selecting csv files
        chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));

        //Open the open dialog and try reading the selected file
        int option = chooser.showOpenDialog(rootPane);
        if (option == JFileChooser.APPROVE_OPTION) {
            try (CSVReader reader = new CSVReader(new FileReader(chooser.getSelectedFile()))) {
            	//Import students into the active course
                gradebook.getActiveCourse().importStudents(reader);
                
                //Show pertinent error messages if required.
            } catch (IOException e) {
                CommonFunctions.showErrorMessage(this, "The selected file could not be read.");
            } catch (CSVException e) {
                int numLines = e.getNumBadLines();
                CommonFunctions.showErrorMessage(this, "Problems were encountered on " + numLines
                        + " line" + (numLines == 1 ? "" : "s")
                        + " of the file. They may not have been imported fully.");
            }

            //Refresh the table and update the status bar.
            refreshTableModel();
            updateStatusBar();
        }
    }

    //Will import grades from a CSV file into the active course and display in the table
    private void importGradesAction() {
    	
    	//If there is no active course then we notify the user to create one first
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "You must create a course first.");
            return;
        }

        //Create a new custom file chooser
        CustomFileChooser chooser = new CustomFileChooser();
        //File filter restricts users to selecting csv files
        chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));

        //Open the open dialog and try reading the selected file
        int option = chooser.showOpenDialog(rootPane);
        if (option == JFileChooser.APPROVE_OPTION) {
            try (CSVReader reader = new CSVReader(new FileReader(chooser.getSelectedFile()))) {
            	//Import grades into the active course
                gradebook.getActiveCourse().importGrades(reader);
                
                //Show pertinent error messages if required
            } catch (IOException e) {
                CommonFunctions.showErrorMessage(this, "The selected file could not be read.");
            } catch (CSVException e) {
                int numLines = e.getNumBadLines();
                if (numLines == CSVException.BAD_FORMAT) {
                    CommonFunctions.showErrorMessage(this, "The file's header is invalid. No grades could be imported.");
                } else {
                    CommonFunctions.showErrorMessage(this, "Problems were encountered on " + numLines
                            + " line" + (numLines == 1 ? "" : "s")
                            + " of the file. They may not have been imported fully.");
                }
            }

            //Refresh table
            refreshTableModel();
        }
    }

    //Will export grades and student information in the active course to a CSV file
    private void exportGradesAction() {
    	
    	//If there is no active course then we notify the user to create one first
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "You must create a course first.");
            return;
        }

        //Create a new custom file chooser
        CustomFileChooser chooser = new CustomFileChooser();
        //File filter restricts users to csv files
        chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));

      //Open the save dialog and get the selected file
        int option = chooser.showSaveDialog(rootPane);
        String file_name = chooser.getSelectedFile().toString();
        
        //Add file extension if required.
        if (!file_name.toLowerCase().endsWith(".csv"))
            file_name += ".csv";
        if (option == JFileChooser.APPROVE_OPTION) {
        	
        	//Try writing the information to file
            try (CSVWriter writer = new CSVWriter(new FileWriter(file_name))) {
                gradebook.getActiveCourse().exportGrades(writer);
                
                //Show pertinent error messages if required.
            } catch (IOException e) {
                CommonFunctions.showErrorMessage(this, "The selected file could not be written. Try a different filename.");
                return;
            }
        }
    }

    //Method that takes care of sending emails to students
    //Contains a progress monitor that appears during emailing to notify
    //user of the progress of the email.
    private void sendEmailAction() {
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "You must create a course first.");
            return;
        }

        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showEmailDialog(this, gradebook.getActiveCourse().getStudentList());

        if (prompt.getReturnValue() == UserEntryPrompter.OK_PRESSED) {
            final Object[] output = prompt.getOutput();
            final List<Student> stuList = (List<Student>) output[5];

            final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Sending reports by email",
                                                    "Initializing...", 0, stuList.size());
            progressMonitor.setMillisToPopup(0);
            progressMonitor.setMillisToDecideToPopup(0);
            progressMonitor.setProgress(0);

            new SwingWorker<Void, Void>(){
                private int errorCount = 0;
                private int progress = 0;
                
                public Void doInBackground(){
                    Message currMessage;
                    
                    for (Student s : stuList){
                        try{
                            currMessage = reportGenerator.exportToEmailMessage((String)output[1], (String)output[2], 
                                (String)output[3], (String)output[4], (String)output[0], gradebook.getActiveCourse(), s);
                            Transport.send(currMessage);
                        } catch(MessagingException | JRException e){
                            errorCount++;
                        }
                        
                        progress++;
                        publish((Void)null);
                    }
                    
                    return null;
                }
                
                protected void process(List<Void> chunks){
                    progressMonitor.setProgress(progress);
                    progressMonitor.setNote("Completed " + progress + " out of " + stuList.size());
                    if (progressMonitor.isCanceled())
                        cancel(true);
                }

                protected void done(){
                    progressMonitor.close();
                    if (errorCount > 0)
                        CommonFunctions.showErrorMessage(MainWindow.this, "<html>Problems were encountered while processing "
                            + errorCount + " student" + (errorCount == 1 ? "." : "s.")
                            + "<br>Please check your SMTP credentials and email addresses.</html>");
                }
            }.execute();
        }
    }

    //Method that takes care of generating PDF reports for students.
    //Contains a progress monitor that appears during PDF export to
    //notify users of the progress of the export.
    private void genReportsAction(){
        if (gradebook.getActiveCourse() == null) {
            CommonFunctions.showErrorMessage(this, "You must create a course first.");
            return;
        }
        
        //Dialog and data validation handled by UserEntryPrompter Class
        UserEntryPrompter prompt = new UserEntryPrompter();
        prompt.showReportDialog(this, gradebook.getActiveCourse().getStudentList());

        if (prompt.getReturnValue() == UserEntryPrompter.OK_PRESSED) {
            final Object[] output = prompt.getOutput();
            final List<Student> stuList = (List<Student>) output[1];

            final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Exporting PDF reports",
                                                    "Initializing...", 0, stuList.size());
            progressMonitor.setMillisToPopup(0);
            progressMonitor.setMillisToDecideToPopup(0);
            progressMonitor.setProgress(0);

            new SwingWorker<Void, Void>(){
                private int errorCount = 0;
                private int progress = 0;
                
                public Void doInBackground(){
                    JasperPrint currReport;
                    
                    for (Student s : stuList){
                        try{
                            currReport = reportGenerator.fillReport(gradebook.getActiveCourse(), s);
                            JasperExportManager.exportReportToPdfFile(currReport, new File((String)output[0], 
                                s.getLastName() + "-" + s.getFirstName() + "-" + s.getNum() + ".pdf").getPath());
                        } catch(JRException e){
                            errorCount++;
                        }
                        
                        progress++;
                        publish((Void)null);
                    }
                    
                    return null;
                }
                
                protected void process(List<Void> chunks){
                    progressMonitor.setProgress(progress);
                    progressMonitor.setNote("Completed " + progress + " out of " + stuList.size());
                    if (progressMonitor.isCanceled())
                        cancel(true);
                }

                protected void done(){
                    progressMonitor.close();
                    if (errorCount > 0)
                        CommonFunctions.showErrorMessage(MainWindow.this, "<html>Problems were encountered while processing "
                                + errorCount + " student" + (errorCount == 1 ? "." : "s.")
                                + "<br>Try a different folder, and check your free disk space.</html>");
                }
            }.execute();
        }
    }
    
    //Method to update the status bar with pertinent information.
    private void updateStatusBar() {
        if (gradebook.getActiveCourse() == null)
            statusLabel.setText("No course selected");
        else{
            int numStudents = gradebook.getActiveCourse().getStudentList().size();
            
            statusLabel.setText(gradebook.getActiveCourse().toString() + " | " + 
                numStudents + " student" + (numStudents == 1 ? "" : "s"));
        }
    }
    
    //Method that welcomes user for the first time and asks to create a course
    //Only occurs the first time a user ever opens the application.
    //Will not occur on subsequent opens (unless there is no information stored).
    private void firstStartAction() {
        int option = JOptionPane.showConfirmDialog(this, 
            "<html>Welcome to the gradebook program!<br>Would you like to create a new course now?</html>", 
            "Question", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            addCourseAction();
        }
    }
    
    //Called before the program is closed to store all the data on the Gradebook
    private void exitAction() {
        try{
            storeGradebook();
            System.exit(0);
        } catch (IOException e){
            int option = JOptionPane.showConfirmDialog(this, "There was a problem writing the data file. Changes were not saved. Exit anyway?", "Error", JOptionPane.YES_NO_OPTION);
        
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
    
    //Load all the data of the gradebook each time the user open the Program
    private void loadGradebook(){
        try{
            //try to read from main data file
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILENAME))){ 
                gradebook = Gradebook.fromObjectInputStream(in); //read the gradebook
            } catch (FileNotFoundException e){
                //!! primary data file doesn't exist
                if (new File(BACKUP_FILENAME).isFile()){
                    //!! backup data file exists
                    throw e; //go to the next catch block
                } else{
                    //!! first app startup
                    gradebook = new Gradebook(); //create an empty gradebook
                    callFirstStart = true; //present OOBE to the user
                }
            }
        } catch (IOException | ClassNotFoundException e){
            //!! main data file corrupt or non-existent but backup exists
            //try to read from backup data file
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(BACKUP_FILENAME))){
                //!! backup was OK
                gradebook = Gradebook.fromObjectInputStream(in); //read the gradebook
                CommonFunctions.showWarningMessage(this, "The main data file could not be read. A backup was opened instead.");
            } catch (IOException | ClassNotFoundException ex){
                //!! both data files corrupt
                gradebook = new Gradebook(); //create an empty gradebook
                CommonFunctions.showErrorMessage(this, "The data file was corrupt and could not be recovered. All data was lost.");
            }
        }
    }
    
    //Stores all the program data by serializing
    private void storeGradebook() throws IOException{
        //make a backup
        File dataFile = new File(DATA_FILENAME);
        File backupFile = new File(BACKUP_FILENAME);
        backupFile.delete(); //delete the backup first
        dataFile.renameTo(backupFile); //then make a new one
        
        //store the data
        gradebook.toObjectOutputStream(new ObjectOutputStream(new FileOutputStream(DATA_FILENAME)));
    }
    
    //Look through all the cells to find the maximun size of its contents
    //Necessary to set the optimal column width in updateColumnSize()
    private int getMaxColumnSize(int colNumber){
    	//The default width will be the size of the header.
        int width = getHeaderSize(colNumber);
        //Loop through all the cells in a column
        for(int row=0; row< mainTable.getRowCount();row++){
        	//prefWidth is the width of the renderered component within the cell
        	//(the amount of space the string within the cell takes up) + additional spacing
            int prefWidth = (int)mainTable.getCellRenderer(row, colNumber).getTableCellRendererComponent(mainTable, mainTable.getValueAt(row, colNumber), false, false, row, colNumber).getPreferredSize().getWidth() + mainTable.getIntercellSpacing().width + COLUMN_PADDING;
            //The width of the column will then be the greater of the header width, or the width of the cell with the largest string).
            width = Math.max(width, prefWidth);
        }
        return width;
    }

    //Update each column preferred size by looking at the content on every cell of that Column
    //calls getMaxColumnSize();
    private void updateColumnSize(){
    	//Loop through all columns and update the width of the columns.
        for(int col=0;col<mainTable.getColumnCount();col++){
            mainTable.getColumnModel().getColumn(col).setPreferredWidth(getMaxColumnSize(col));
        }
    }
    
    //Get the size of a Header in mainTable
    //Necessary to set the optimal column width in updateColumnSize()
    private int getHeaderSize(int colNumber){
    	//Get the header value for the specified column
        Object value = mainTable.getColumnModel().getColumn(colNumber).getHeaderValue();
        //Get the tablecellrenderer that is used to draw the header of the specified column
        TableCellRenderer renderer = mainTable.getColumnModel().getColumn(colNumber).getHeaderRenderer();
        //If the renderer is null, then we get the default renderer
        if(renderer == null){
            renderer = mainTable.getTableHeader().getDefaultRenderer();
        }
        //Here we get the rendered component within the cell
        //-1 refers to the header within the JTable
        Component comp = renderer.getTableCellRendererComponent(mainTable, value, false, false, -1, colNumber);
        //Return the width of this component (the amount of space the header string takes up) + spacing
        return (int)(comp.getPreferredSize().width + mainTable.getIntercellSpacing().width + COLUMN_PADDING);
    }
}
