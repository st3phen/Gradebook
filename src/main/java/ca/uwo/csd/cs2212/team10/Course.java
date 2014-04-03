package ca.uwo.csd.cs2212.team10;

import java.util.*;
import au.com.bytecode.opencsv.*;
import java.io.*;
import javax.mail.internet.*;

/**
 * Represents a course in the Gradebook
 *
 * @author Team 10
 */
public class Course implements Serializable {
    /* Constants */

    private static final long serialVersionUID = 1L; //for serializing

    /* Attributes */
    private String code;
    private String term;
    private String title;
    private List<Student> students;
    private List<Deliverable> deliverables;

    /* Constructor */
    public Course(String title, String code, String term) {
        this.title = title;
        this.code = code;
        this.term = term;

        students = new ArrayList<Student>();
        deliverables = new ArrayList<Deliverable>();
    }

    /* Public Methods */
    public void setTitle(String title) {
        this.title = title;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public String getTerm() {
        return term;
    }

    public List<Student> getStudentList() {
        return students;
    }

    public List<Deliverable> getDeliverableList() {
        return deliverables;
    }

    public void validateStudentModification(Student student, String newEmail, String newNum) throws DuplicateObjectException{
    	//Loop through student list
        for (Student s : students)
        	//If the current student "s" does not equal the student passed in
            if (s != student){
            	//If the new e-mail address the old e-mail address, throw exception
                if (newEmail.equals(s.getEmail()))
                    throw new DuplicateObjectException(DuplicateObjectException.DUP_EMAIL);
                //If the new student number equals the old student number, throw exception
                else if (newNum.equals(s.getNum()))
                    throw new DuplicateObjectException(DuplicateObjectException.DUP_NUMBER);
            }
    }
    
    public void validateDeliverableModification(Deliverable deliverable, String newName) throws DuplicateObjectException{
    	//Loop through deliverables list
        for (Deliverable d : deliverables)
        	//If the current deliverable does not equal the deliverable passed in AND
        	//if the current deliverable equals the parameters of the modified deliverable
            if (d != deliverable && d.equals(new Deliverable(newName, Deliverable.ASSIGNMENT_TYPE, 0)))
            	//throw duplicate object exception
                throw new DuplicateObjectException();
    }
    
    public void addStudent(Student student){
        students.add(student);

        //Add each deliverable to the grade list
        for (Deliverable deliverable : deliverables)
            student.addGrade(deliverable);
    }

    public void removeStudent(Student student) {
        students.remove(student);
    }

    public void addDeliverable(Deliverable deliverable) {
        deliverables.add(deliverable);
        
        //Add the deliverable to the grade list of each student
        for (Student student : students)
            student.addGrade(deliverable);
    }

    public void removeDeliverable(Deliverable deliverable) {
        deliverables.remove(deliverable);
        
        //Remove the deliverable from the grade list of each student
        for (Student student : students)
            student.removeGrade(deliverable);
    }
    
    public double calcAverage(){
        double total = 0;
        int count = 0;
        double currAverage;
        
        //Loop through student list
        for (Student s : students){
        	//Calculate average of current student
            currAverage = s.calcAverage();
            
            //If the current average does not equal a no_grade
            if (currAverage != Student.NO_GRADE){
            	//Add the current average to the total
                total += currAverage;
                count++;
            }
        }
        //If the count is 0, return no grade
        if (count == 0)
            return Student.NO_GRADE;
        else
        	//Return the average
            return total/count;
    }
    
    //Method that calculates average (similar to method above)
    //but the average calculation is based on type
    //ie. assignment average, exam average
    public double calcAverage(int type){
        double total = 0;
        int count = 0;
        double currAverage;
        
        for (Student s : students){
            currAverage = s.calcAverage(type);
            
            if (currAverage != Student.NO_GRADE){
                total += currAverage;
                count++;
            }
        }
        
        if (count == 0)
            return Student.NO_GRADE;
        else
            return total/count;
    }
    
    //Method that calculates average (similar to method above)
    //but the average calculation is based on deliverable
    //ie. Assignment 1 or Midterm or Assignment 2 or Final, etc.
    public double calcAverage(Deliverable deliverable){
        double total = 0;
        int count = 0;
        double currGrade;
        
        for (Student s : students){
            currGrade = s.getGrade(deliverable);
            
            if (currGrade != Student.NO_GRADE){
                total += currGrade;
                count++;
            }
        }
        
        if (count == 0)
            return Student.NO_GRADE;
        else
            return total/count;
    }
    
    public void importStudents(CSVReader reader) throws IOException, CSVException{
        String[] line;
        String firstName, lastName, num, email;
        int invalidLines = 0;
        
        while ((line = reader.readNext()) != null){
            if (line.length != 14){
                invalidLines++;
                continue;
            }
            
            firstName = line[10];
            lastName = line[9];
            num = line[8];
            email = line[13];
            
            try{
                if (firstName.trim().isEmpty() || lastName.trim().isEmpty() || num.trim().isEmpty())
                    throw new IllegalArgumentException();
                
                new InternetAddress(email).validate();
                
                validateStudentModification(null, email, num);
            } catch (IllegalArgumentException | AddressException | DuplicateObjectException e){
                invalidLines++;
                continue;
            }
            
            addStudent(new Student(firstName, lastName, num, email));
        }
        
        if (invalidLines > 0)
            throw new CSVException(invalidLines);
    }
    
    public void importGrades(CSVReader reader) throws IOException, CSVException{
        String[] line;
        ArrayList<Deliverable> dList = new ArrayList<Deliverable>();
        Student currStudent;
        
        if((line = reader.readNext()) != null){
            if (line.length < 1 || !line[0].equals("Student Number")){
                throw new CSVException(CSVException.BAD_FORMAT);
            }
            
            for (int i = 1; i < line.length; i++){
                for (Deliverable d : deliverables){
                    if (d.getName().equals(line[i])){
                        dList.add(d);
                        break;
                    }
                }
                if (dList.size() != i)
                    dList.add(null);
            }
        }
        
        int firstLineLength = line.length;
        int invalidLines = 0;
        double currGrade;
        boolean errorOnCurrLine;
        while((line = reader.readNext()) != null){
            if (line.length != firstLineLength){
                invalidLines++;
                continue;
            }
            
            currStudent = null;
            for (Student s : students){
                if (s.getNum().equals(line[0])){
                    currStudent = s;
                    break;
                }
            }
            if (currStudent == null){
                invalidLines++;
                continue;
            }
            
            errorOnCurrLine = false;
            for (int i = 1; i < line.length; i++){
                if (dList.get(i-1) == null)
                    continue;
                
                try{
                    currGrade = Double.parseDouble(line[i])*100;
                    if (currGrade < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e){
                    errorOnCurrLine = true;
                    continue;
                }
                
                currStudent.setGrade(dList.get(i-1), currGrade);
            }
            if (errorOnCurrLine)
                invalidLines++;
        }
        
        if (invalidLines > 0)
            throw new CSVException(invalidLines);
    }
    
    public void exportGrades(CSVWriter writer){
        ArrayList<String> currLine;
        
        //write the header
        currLine = new ArrayList<String>();
        
        currLine.add("First Name");
        currLine.add("Last Name");
        currLine.add("Student Number");
        currLine.add("Email");
        for(Deliverable d : deliverables)
            currLine.add(d.getName());
        
        writer.writeNext(currLine.toArray(new String[0]));
        
        //write each student
        for (Student s : students){
            currLine = new ArrayList<String>();
            
            currLine.add(s.getFirstName());
            currLine.add(s.getLastName());
            currLine.add(s.getNum());
            currLine.add(s.getEmail());
            for(Deliverable d : deliverables){
                if (s.getGrade(d) == Student.NO_GRADE)
                    currLine.add("(no grade)");
                else
                    currLine.add(String.valueOf(s.getGrade(d)/100));
            }
            
            writer.writeNext(currLine.toArray(new String[0]));
        }
    }

    @Override
    public String toString() {
        return title + " - " + code + " - " + term;
    }
    
    public boolean equals(Course c) {
        return code.equals(c.getCode()) && term.equals(c.getTerm());
    }
}
