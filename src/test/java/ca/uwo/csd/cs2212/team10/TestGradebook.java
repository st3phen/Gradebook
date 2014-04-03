package ca.uwo.csd.cs2212.team10;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.*;

public class TestGradebook{
    private Gradebook gradebook;
    private Course course;
    
    @Before
    public void setup(){
        this.gradebook = new Gradebook();
        this.course = new Course("foo", "bar", "daz");
    }
    
    @Test
    public void testCourseListInitiallyEmpty(){
        gradebook = new Gradebook();
        assertTrue(gradebook.getCourseList().isEmpty());
    }
    
    @Test
    public void testActiveCourseInitiallyNull(){
        gradebook = new Gradebook();
        assertNull(gradebook.getActiveCourse());
    }
    
    @Test
    public void testAddAndRemoveCourse(){
        gradebook.addCourse(course);
        assertTrue(gradebook.getCourseList().contains(course));
        
        gradebook.removeCourse(course);
        assertFalse(gradebook.getCourseList().contains(course));
    }
    
    @Test
    public void testSetActiveCourse(){
        gradebook.addCourse(course);
        gradebook.setActiveCourse(course);
        
        assertSame(course, gradebook.getActiveCourse());
        
        gradebook.removeCourse(course);
    }
    
    @Test
    public void testSetActiveCourseToNull(){
        gradebook.setActiveCourse(null);
        
        assertNull(gradebook.getActiveCourse());
    }
    
    @Test(expected = DuplicateObjectException.class)
    public void testValidateCourseModificationFail() throws DuplicateObjectException{
        Course course2 = new Course("moo", "ma", "maz");
        gradebook.addCourse(course);
        gradebook.addCourse(course2);
        gradebook.validateCourseModification(course2, "bar", "daz");
    }
    
    @Test
    public void testValidateCourseModificationSuccess() throws DuplicateObjectException{
        Course course2 = new Course("moo", "ma", "maz");
        gradebook.addCourse(course);
        gradebook.addCourse(course2);
        gradebook.validateCourseModification(course2, "baar", "daaz");
    }
}
