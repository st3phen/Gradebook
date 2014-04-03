package ca.uwo.csd.cs2212.team10;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * Represents the main Gradebook of the project
 * @author Team 10
 */
public class Gradebook implements Serializable{
    /* Constants */
    private static final long serialVersionUID = 1L; //for serializing

    /* Attributes */
    private Course activeCourse;
    private List<Course> courses; //the courses in the Gradebook

    /* Constructor */
    public Gradebook(){
        courses = new ArrayList<Course>();
    }

    public static Gradebook fromObjectInputStream(ObjectInputStream in) throws IOException, ClassNotFoundException{
        return (Gradebook)in.readObject();
    }

    /* Public Methods */

    public void addCourse(Course course){
        courses.add(course);
    }

    public void removeCourse(Course course){
        courses.remove(course);
    }
    
    public List<Course> getCourseList(){
        return courses;
    }
    
    public Course getActiveCourse(){
        return activeCourse;
    }

    public void setActiveCourse(Course course){
        activeCourse = course;
    }
    
    public void toObjectOutputStream(ObjectOutputStream out) throws IOException{
        out.writeObject(this);
    }
    
    public void validateCourseModification(Course course, String newCode, String newTerm) throws DuplicateObjectException{
        for (Course c : courses)
            if (c != course && c.equals(new Course("", newCode, newTerm)))
                throw new DuplicateObjectException();
    }
}
