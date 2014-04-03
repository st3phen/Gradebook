package ca.uwo.csd.cs2212.team10;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.*;

public class TestStudent{
    private Student student;
    private Deliverable asn1;
    private Deliverable asn2;
    private Deliverable exam1;
    private Deliverable exam2;
    
    @Before
    public void setup(){
        //create the new student
        student = new Student("John", "Doe", "1234567890", "jdoe@example.com");
        //IMPORTANT: the Student should be left empty (ie. no grades inside) after each test
        
        //create some deliverables
        asn1 = new Deliverable("Asn1", Deliverable.ASSIGNMENT_TYPE, 5);
        asn2 = new Deliverable("Asn2", Deliverable.ASSIGNMENT_TYPE, 10);
        exam1 = new Deliverable("Midterm", Deliverable.EXAM_TYPE, 35);
        exam2 = new Deliverable("Final", Deliverable.EXAM_TYPE, 50);
    }
    
    @Test
    public void testConstructorSetsAttributes(){
        //set the attributes through the constructor
        student = new Student("John", "Doe", "1234567890", "jdoe@example.com");
        
        //ensure all attributes were set correctly
        assertEquals("John", student.getFirstName());
        assertEquals("Doe", student.getLastName());
        assertEquals("1234567890", student.getNum());
        assertEquals("jdoe@example.com", student.getEmail());
    }
    
    @Test
    public void testSetFirstName(){
        //ensure the first name can be set
        student.setFirstName("Jane");
        assertEquals("Jane", student.getFirstName());
    }
    
    @Test
    public void testSetLastName(){
        //ensure the last name can be set
        student.setLastName("Foo");
        assertEquals("Foo", student.getLastName());
    }
    
    @Test
    public void testSetEmail(){
        //ensure the email can be set
        student.setEmail("jfoo@example.com");
        assertEquals("jfoo@example.com", student.getEmail());
    }
    
    @Test
    public void testSetStudentNumber(){
        //ensure the student number can be set
        student.setNum("987654321");
        assertEquals("987654321", student.getNum());
    }
    
    @Test
    public void testSetGrade(){
        //add a grade and ensure it is returned
        student.setGrade(asn1, 90.0);
        assertEquals(90.0, student.getGrade(asn1));
        
        student.removeGrade(asn1);
    }
    
    @Test
    public void testRemoveGrade(){
        student.setGrade(asn1, 90.0);
        
        //remove a grade and ensure null is returned
        student.removeGrade(asn1);
        assertNull(student.getGrade(asn1));
    }
    
    @Test
    public void testCalcAveragesWithNoGrades(){
        //the average should be 0 if there are no grades
        assertEquals(Student.NO_GRADE, student.calcAverage());
        assertEquals(Student.NO_GRADE, student.calcAverage(Deliverable.ASSIGNMENT_TYPE));
        assertEquals(Student.NO_GRADE, student.calcAverage(Deliverable.EXAM_TYPE));
    }
    
    @Test
    public void testCalcAveragesWithGrades(){
        //add some grades
        student.setGrade(asn1, 80.1);
        student.setGrade(asn2, 72.4);
        student.setGrade(exam1, 86.5);
        student.setGrade(exam2, 92.0);
        
        //ensure the average calculations match the formula given in the spec
        assertEquals((80.1*5+72.4*10+86.5*35+92.0*50)/(5+10+35+50), student.calcAverage());
        assertEquals((80.1*5+72.4*10)/(10+5), student.calcAverage(Deliverable.ASSIGNMENT_TYPE));
        assertEquals((86.5*35+92.0*50)/(35+50), student.calcAverage(Deliverable.EXAM_TYPE));
        
        student.removeGrade(asn1);
        student.removeGrade(asn2);
        student.removeGrade(exam1);
        student.removeGrade(exam2);
    }
    
    @Test
    public void testToString(){
    	assertTrue(student.toString().contains(student.getFirstName()));
    	assertTrue(student.toString().contains(""+student.getLastName()));
    	assertTrue(student.toString().contains(""+student.getNum()));
    	assertTrue(student.toString().contains(""+student.getEmail()));
    	
    }
}
