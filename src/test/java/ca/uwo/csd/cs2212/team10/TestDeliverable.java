package ca.uwo.csd.cs2212.team10;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.*;

public class TestDeliverable{
    private Deliverable deliverable;
    
    @Before
    public void setup(){
        deliverable = new Deliverable("Foo", Deliverable.ASSIGNMENT_TYPE, 0);
    }
    
    @Test
    public void testConstructorSetsAttributes(){
        deliverable = new Deliverable("Assignment 1", Deliverable.ASSIGNMENT_TYPE, 25);
        
        assertEquals("Assignment 1", deliverable.getName());
        assertEquals(Deliverable.ASSIGNMENT_TYPE, deliverable.getType());
        assertEquals(25, deliverable.getWeight());
    }
    
    @Test
    public void testSetName(){
        deliverable.setName("Exam 1");
        assertEquals("Exam 1", deliverable.getName());
    }
    
    @Test
    public void testSetType(){
        deliverable.setType(Deliverable.EXAM_TYPE);
        assertEquals(Deliverable.EXAM_TYPE, deliverable.getType());
    }
    
    @Test
    public void testSetWeight(){
        deliverable.setWeight(50);
        assertEquals(50, deliverable.getWeight());
    }
    
    @Test
    public void testEqualSameName(){
        Deliverable d2 = new Deliverable(deliverable.getName(), Deliverable.ASSIGNMENT_TYPE, 0);
        
        assertTrue(deliverable.equals(d2));
    }
    
    @Test
    public void testEqualDifferentName(){
        Deliverable d2 = new Deliverable(deliverable.getName() + "x", Deliverable.ASSIGNMENT_TYPE, 0);
        
        assertFalse(deliverable.equals(d2));
    }
    
    @Test
    public void testToStringContainsNameAndWeight(){
    	assertTrue(deliverable.toString().contains(deliverable.getName()));
    	assertTrue(deliverable.toString().contains(""+deliverable.getWeight()));
    }
}
