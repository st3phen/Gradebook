package ca.uwo.csd.cs2212.team10;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.mail.internet.*;
import java.util.List;

/**
 * Class to handle all dialogs when the user is requested to enter some data or choose some options
 * Validates data entered by the user in each dialog
 * 
 * @author Team 10
 */
public class UserEntryPrompter{
    public static final int OK_PRESSED = 0;
    public static final int CANCEL_PRESSED = 1;
    public static final int DELETE_PRESSED = 2;
    public static final int WINDOW_CLOSED = JOptionPane.CLOSED_OPTION;
    
    private static final int ADD_TYPE = 1;
    private static final int EDIT_TYPE = 2;
    
    private static final int MAX_ERROR_WIDTH = 220;
    
    private int retval;
    private Object[] output;
    
    public int getReturnValue(){
        return retval;
    }
    
    public Object[] getOutput(){
        return output;
    }
    
    public void showEmailDialog (Component parent, List<Student> studentsList) {
        EmailDialog dialog = new EmailDialog(getParentFrame(parent), true, studentsList);
        
        retval = dialog.showDialog();
        
        if (retval == OK_PRESSED)
            output = dialog.getOutput();
        
        dialog.dispose();
    }
    
    public void showReportDialog (Component parent, List<Student> studentsList) {
        ReportDialog dialog = new ReportDialog(getParentFrame(parent), true, studentsList);
        
        retval = dialog.showDialog();

        if (retval == OK_PRESSED)
            output = dialog.getOutput();
        
        dialog.dispose();
    }
    
    public void showAddCourseDialog(Component parent, Gradebook containingGradebook){
        showCourseDialog(parent, ADD_TYPE, null, null, null, containingGradebook, null);
    }
    
    public void showEditCourseDialog(Component parent, Course oldCourse, Gradebook containingGradebook){
        showCourseDialog(parent, EDIT_TYPE, oldCourse.getTitle(), oldCourse.getCode(), oldCourse.getTerm(), containingGradebook, oldCourse);
    }
    
    public void showAddDeliverableDialog(Component parent, Course containingCourse){
        showDeliverableDialog(parent, ADD_TYPE, null, 0, null, containingCourse, null);
    }
    
    public void showEditDeliverableDialog(Component parent, Deliverable oldDeliverable, Course containingCourse){
        showDeliverableDialog(parent, EDIT_TYPE, oldDeliverable.getName(), oldDeliverable.getType(), ""+oldDeliverable.getWeight(), containingCourse, oldDeliverable);
    }
    
    public void showAddStudentDialog(Component parent, Course containingCourse){
        showStudentDialog(parent, ADD_TYPE, null, null, null, null, containingCourse, null);
    }
    
    public void showEditStudentDialog(Component parent, Student oldStudent, Course containingCourse){
        showStudentDialog(parent, EDIT_TYPE, oldStudent.getFirstName(), oldStudent.getLastName(), oldStudent.getNum(), oldStudent.getEmail(), containingCourse, oldStudent);
    }
    
    private void showCourseDialog(Component parent, int dialogType, String oldTitle, String oldCode, 
                                    String oldTerm, final Gradebook containingGradebook, 
                                    final Course oldCourse){
        
        final JTextField title = new JTextField(oldTitle);
        final JTextField code = new JTextField(oldCode);
        final JTextField term = new JTextField(oldTerm);
        final JLabel errorMsg = new JLabel();
        
        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");
        final JButton delete = new JButton("Delete Course");
        
        errorMsg.setForeground(Color.RED);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (title.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a title."));
                    getParentDialog((Component)e.getSource()).pack();
                } else if (code.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a course code."));
                    getParentDialog((Component)e.getSource()).pack();
                } else if (term.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a term."));
                    getParentDialog((Component)e.getSource()).pack();
                } else{
					try {
                        containingGradebook.validateCourseModification(oldCourse, code.getText(), term.getText());
                    } catch (DuplicateObjectException ex) {
                        errorMsg.setText(formatForErrorLabel(
                            "The code and term entered are already used by another course."));
                        getParentDialog((Component)e.getSource()).pack();
                        return;
                    }
					
                    getParentOptionPane((Component)e.getSource()).setValue(ok);
                }
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getParentOptionPane((Component)e.getSource()).setValue(cancel);
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getParentOptionPane((Component)e.getSource()).setValue(delete);
            }
        });
        
        Object[] message = {
            "Title:", title,
            "Course code:", code,
            "Term:", term,
            errorMsg
        };
        
        Object[] options = null;
        String dialogTitle = null;
        if (dialogType == ADD_TYPE){
            options = new Object[]{ok, cancel};
            dialogTitle = "Add Course";
        } else if (dialogType == EDIT_TYPE){
            options = new Object[]{ok, cancel, delete};
            dialogTitle = "Edit Course";
        }
        
        retval = JOptionPane.showOptionDialog(parent, message, dialogTitle, JOptionPane.DEFAULT_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE, null, options, ok);
        
        if (retval == OK_PRESSED)
            output = new String[]{title.getText(), code.getText(), term.getText()};
    }
    
    private void showDeliverableDialog(Component parent, int dialogType, String oldName, int oldType, 
                                        String oldWeight, final Course containingCourse,
                                        final Deliverable oldDeliverable){
        
        final JTextField name = new JTextField(oldName);
        final JComboBox type = new JComboBox(Deliverable.TYPES);
        final JTextField weight = new JTextField(oldWeight);
        final JLabel errorMsg = new JLabel();
        
        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");
        final JButton delete = new JButton("Delete Deliverable");
        
        if (oldWeight == null)
            oldWeight = "0";
        final Integer oldWeightVal = new Integer(oldWeight);
        
        type.setSelectedIndex(oldType);
        errorMsg.setForeground(Color.RED);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (name.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                            "You must enter a name."));
                    getParentDialog((Component)e.getSource()).pack();
                } else {
                    try {
						//check for duplicate name
						containingCourse.validateDeliverableModification(oldDeliverable, name.getText());
						
                        //check that the weight is a positive integer
                        int newWeightVal = Integer.parseInt(weight.getText());
                        if (newWeightVal <= 0)
                            throw new NumberFormatException();
                        
                        //check that the weights add up to less than 100
                        int total = 0;
                        for (Deliverable d : containingCourse.getDeliverableList())
                            total += d.getWeight();
                        if (total + newWeightVal - oldWeightVal > 100)
                            throw new IllegalArgumentException();
                        
                    } catch (DuplicateObjectException ex) {
                        errorMsg.setText(formatForErrorLabel(
                            "The name entered is already used by another deliverable."));
                        getParentDialog((Component)e.getSource()).pack();
                        return;
                    } catch (NumberFormatException ex) {
                        errorMsg.setText(formatForErrorLabel(
                            "You must enter a positive integer for the weight."));
                        getParentDialog((Component)e.getSource()).pack();
                        return;
                    } catch (IllegalArgumentException ex){
                        errorMsg.setText(formatForErrorLabel(
                            "The total weights in the course cannot add up to more than 100%."));
                        getParentDialog((Component)e.getSource()).pack();
                        return;
                    }
                    
                    getParentOptionPane((Component)e.getSource()).setValue(ok);
                }
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getParentOptionPane((Component)e.getSource()).setValue(cancel);
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getParentOptionPane((Component)e.getSource()).setValue(delete);
            }
        });
        
        Object[] message = {
            "Name:", name,
            "Type:", type,
            "Weight:", weight,
            errorMsg
        };
        
        Object[] options = null;
        String dialogTitle = null;
        if (dialogType == ADD_TYPE){
            options = new Object[]{ok, cancel};
            dialogTitle = "Add Deliverable";
        } else if (dialogType == EDIT_TYPE){
            options = new Object[]{ok, cancel, delete};
            dialogTitle = "Edit Deliverable";
        }
        
        retval = JOptionPane.showOptionDialog(parent, message, dialogTitle, JOptionPane.DEFAULT_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE, null, options, ok);
        
        if (retval == OK_PRESSED)
            output = new Object[]{name.getText(), type.getSelectedIndex(), Integer.parseInt(weight.getText())};
    }

    private void showStudentDialog(Component parent, int dialogType, String oldFirstName, String oldLastName, 
                                    String oldNum, String oldEmail, final Course containingCourse, 
                                    final Student oldStudent){
        
        final JTextField firstName = new JTextField(oldFirstName);
        final JTextField lastName = new JTextField(oldLastName);
        final JTextField number = new JTextField(oldNum);
        final JTextField email = new JTextField(oldEmail);
        final JLabel errorMsg = new JLabel();
        
        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");
        final JButton delete = new JButton("Delete Student");
        
        errorMsg.setForeground(Color.RED);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstName.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a first name."));
                    getParentDialog((Component)e.getSource()).pack();
                } else if (lastName.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a last name."));
                    getParentDialog((Component)e.getSource()).pack();
                } else if (number.getText().trim().isEmpty()) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a student number."));
                    getParentDialog((Component)e.getSource()).pack();
                } else if (!isValidEmail(email.getText())) {
                    errorMsg.setText(formatForErrorLabel(
                        "You must enter a valid email address."));
                    getParentDialog((Component)e.getSource()).pack();
                } else {
                    try {
                        containingCourse.validateStudentModification(oldStudent, email.getText(), number.getText());
                    } catch (DuplicateObjectException ex) {
                        if (ex.getReason() == DuplicateObjectException.DUP_NUMBER){
                            errorMsg.setText(formatForErrorLabel(
                                "The student number entered is already used by another student."));
                            getParentDialog((Component)e.getSource()).pack();
                        } else if (ex.getReason() == DuplicateObjectException.DUP_EMAIL){
                            errorMsg.setText(formatForErrorLabel(
                                "The email address entered is already used by another student."));
                            getParentDialog((Component)e.getSource()).pack();
                        }
                        return;
                    }
                    
                    getParentOptionPane((Component)e.getSource()).setValue(ok);
                }
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getParentOptionPane((Component)e.getSource()).setValue(cancel);
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getParentOptionPane((Component)e.getSource()).setValue(delete);
            }
        });
        
        Object[] message = {
            "First name:", firstName,
            "Last name:", lastName,
            "Student number:", number,
            "Email:", email,
            errorMsg
        };
        
        Object[] options = null;
        String dialogTitle = null;
        if (dialogType == ADD_TYPE){
            options = new Object[]{ok, cancel};
            dialogTitle = "Add Student";
        } else if (dialogType == EDIT_TYPE){
            options = new Object[]{ok, cancel, delete};
            dialogTitle = "Edit Student";
        }
        
        retval = JOptionPane.showOptionDialog(parent, message, dialogTitle, JOptionPane.DEFAULT_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE, null, options, ok);
        
        if (retval == OK_PRESSED)
            output = new String[]{firstName.getText(), lastName.getText(), number.getText(), email.getText()};
    }
    
    private static JOptionPane getParentOptionPane(Component c) {
        while (c != null) {
            if (c instanceof JOptionPane) {
                return (JOptionPane) c;
            }
            c = c.getParent();
        }
        return null;
    }
    
    private static JDialog getParentDialog(Component c) {
        while (c != null) {
            if (c instanceof JDialog) {
                return (JDialog) c;
            }
            c = c.getParent();
        }
        return null;
    }
    
    private static JFrame getParentFrame (Component c) {
        while (c != null) {
            if (c instanceof JFrame) {
                return (JFrame) c;
            }
            c = c.getParent();
        }
        return null;
    }
    
    private static boolean isValidEmail(String email) {
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException e) { }
        return false;
    }
    
    private static String formatForErrorLabel(String text){
        return String.format("<html><div WIDTH=%d>%s</div><html>", MAX_ERROR_WIDTH, text);
    }
}