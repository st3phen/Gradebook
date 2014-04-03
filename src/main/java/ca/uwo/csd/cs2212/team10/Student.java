package ca.uwo.csd.cs2212.team10;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Class representing a student in the course
 * @author Team10
 */
public class Student implements Serializable {
    /* Constants */
    public static final double NO_GRADE = -1;
    private static final long serialVersionUID = 1L; //for serializing
    
    /* Attributes */
    private String firstName;
    private String lastName;
    private String num;
    private String email;
    private HashMap<Deliverable, Double> grades;
    
    /* Constructor */
    public Student(String firstName, String lastName, String num, String email){
        this.firstName = firstName;
        this.lastName = lastName;
        this.num = num;
        this.email = email;
        
        //the list of grades should be initially empty
        grades = new HashMap<Deliverable, Double>();
    }
    
    /* Public Methods */
    
    /**
     * Sets the first name of the student
     */
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    
    /**
     * Sets the last name of the student
     */
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    
    /**
     * Sets the number of the student
     */
    public void setNum(String num){
        this.num = num;
    }
    
    /**
     * Sets the email of the student
     */
    public void setEmail(String email){
        this.email = email;
    }
    
    /**
     * Returns the first name of the student
     */
    public String getFirstName(){
        return firstName;
    }
    
    /**
     * Returns the last name of the student
     */
    public String getLastName(){
        return lastName;
    }
    
    /**
     * Returns the number of the student
     */
    public String getNum(){
        return num;
    }
    
    /**
     * Returns the email of the student
     */
    public String getEmail(){
        return email;
    }
    
    /**
     * Removes a grade assigned to a given deliverable
     * @param deliverable the deliverable whose grade should be deleted
     */
    public void removeGrade(Deliverable deliverable){
        grades.remove(deliverable);
    }
    
    /**
     * Returns the grade assigned to a given deliverable, or null if there is no grade assigned
     * @param deliverable the deliverable whose grade should be returned
     */
    public Double getGrade(Deliverable deliverable){
        return grades.get(deliverable);
    }
    
    /**
     * Adds a grade for a given deliverable
     * @param deliverable the deliverable whose grade should be modified
     */
    public void addGrade(Deliverable deliverable){
        grades.put(deliverable, NO_GRADE);
    }
    
    /**
     * Edits the grade for a given deliverable
     * @param deliverable the deliverable whose grade should be modified
     * @param grade the grade to assign
     */
    public void setGrade(Deliverable deliverable, Double grade){
        grades.put(deliverable, grade);
    }
    
    /**
     * Calculates the overall weighted average of the student
     */
    public double calcAverage(){    
        double total = 0;
        int weights = 0;
        
        //loop through each grade
        for (Entry<Deliverable, Double> grade : grades.entrySet()){
            if (grade.getValue() != NO_GRADE){
                //keep a running total of the grades and the weights
                total += grade.getValue() * grade.getKey().getWeight();
                weights += grade.getKey().getWeight();
            }
        }
        
        if (weights == 0)
            return NO_GRADE;
        else
            return total/weights; //return the weighted average
    }
    
    /**
     * Calculates the weighted average of the student for a given deliverable type
     * @param type the deliverable type: see Deliverable.TYPES
     */
    public double calcAverage(int type){
        double total = 0;
        int weights = 0;
        
        //loop through each grade
        for (Entry<Deliverable, Double> grade : grades.entrySet()){
            //if the type matches the desired one and there's a grade there...
            if (grade.getValue() != NO_GRADE && grade.getKey().getType() == type){
                //keep a running total of the grades and the weights
                total += grade.getValue() * grade.getKey().getWeight();
                weights += grade.getKey().getWeight();
            }
        }
        
        if (weights == 0)
            return NO_GRADE;
        else
            return total/weights; //return the weighted average
    }
    
    @Override
    public String toString(){
        return firstName + " " + lastName + " - " + num + " - " + email;
    }
}
