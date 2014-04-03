package ca.uwo.csd.cs2212.team10;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.*;
import au.com.bytecode.opencsv.CSVReader;
import java.io.*;

public class TestCourse{
    private Course course;
    private Student student;
    private Deliverable deliverable;
    
    @Before
    public void setup(){
        course = new Course("foo", "bar", "daz");
        student = new Student("foo", "bar", "1", "daz");
        deliverable = new Deliverable("Foo", Deliverable.ASSIGNMENT_TYPE, 0);
    }
    
    @Test
    public void testConstructorSetsAttributesAndCreatesEmptyLists(){
        course = new Course("foo", "bar", "daz");
        
        assertEquals("foo", course.getTitle());
        assertEquals("bar", course.getCode());
        assertEquals("daz", course.getTerm());
        
        assertTrue(course.getStudentList().isEmpty());
        assertTrue(course.getDeliverableList().isEmpty());
    }
    
    @Test
    public void testSetTitle(){
        course.setTitle("title");
        assertEquals("title", course.getTitle());
    }
    
    @Test
    public void testSetCode(){
        course.setCode("code");
        assertEquals("code", course.getCode());
    }
    
    @Test
    public void testSetTerm(){
        course.setTerm("term");
        assertEquals("term", course.getTerm());
    }
    
    @Test
    public void testAddStudent(){
        course.addStudent(student);
        assertTrue(course.getStudentList().contains(student));
        
        course.removeStudent(student);
    }
    
    @Test
    public void testRemoveStudent(){
        course.addStudent(student);
        
        course.removeStudent(student);
        assertFalse(course.getStudentList().contains(student));
    }
    
    @Test
    public void testAddDeliverable(){
        course.addDeliverable(deliverable);
        assertTrue(course.getDeliverableList().contains(deliverable));
        
        course.removeDeliverable(deliverable);
    }
    
    @Test
    public void testRemoveDeliverable(){
        course.addDeliverable(deliverable);
        
        course.removeDeliverable(deliverable);
        assertFalse(course.getDeliverableList().contains(deliverable));
    }
    
    @Test
    public void testAddStudentAlsoAddsGrades(){
        course.addDeliverable(deliverable);
        course.addStudent(student);
        
        assertEquals(Student.NO_GRADE, student.getGrade(deliverable));
        
        course.removeStudent(student);
        course.removeDeliverable(deliverable);
    }
    
    @Test
    public void testAddDeliverableAlsoAddsGradesToStudents(){
        course.addStudent(student);
        course.addDeliverable(deliverable);
        
        assertEquals(Student.NO_GRADE, student.getGrade(deliverable));
        
        course.removeDeliverable(deliverable);
        course.removeStudent(student);
    }
    
    @Test
    public void testRemoveDeliverableAlsoRemovesGradesFromStudents(){
        course.addStudent(student);
        course.addDeliverable(deliverable);
        
        course.removeDeliverable(deliverable);
        
        assertNull(student.getGrade(deliverable));
    }
    
    @Test(expected = DuplicateObjectException.class)
    public void testNewDuplicateStudentFailsValidation() throws DuplicateObjectException{
        course.addStudent(student);
        
        course.validateStudentModification(null, student.getEmail(), student.getNum());
    }
    
    @Test(expected = DuplicateObjectException.class)
    public void testExistingDuplicateStudentFailsValidation() throws DuplicateObjectException{
        Student student2 = new Student("foo", "bar", "2", "quux");
        
        course.addStudent(student);
        course.addStudent(student2);
        
        course.validateStudentModification(student2, student.getEmail(), student.getNum());
    }
    
    @Test(expected = DuplicateObjectException.class)
    public void testStudentModificationWithExistingNumber() throws DuplicateObjectException{
        Student student2 = new Student("foo", "bar", "2", "quux");
        
        course.addStudent(student);
        course.addStudent(student2);
        
        course.validateStudentModification(student2, student2.getEmail(), student.getNum());
    }
    
    @Test
    public void testValidateDeliverableMod() throws DuplicateObjectException{
        Deliverable deliverable2 = new Deliverable("Doe", Deliverable.ASSIGNMENT_TYPE, 0);
        course.addDeliverable(deliverable);
        course.addDeliverable(deliverable2);
        course.validateDeliverableModification(deliverable2, "Doe");
        assertEquals("Doe", deliverable2.getName());
    }
    
    @Test(expected = DuplicateObjectException.class)
    public void testValidateDeliverableModWithSameNameAsExistingDeliverable() throws DuplicateObjectException{
        Deliverable deliverable2 = new Deliverable("Doe", Deliverable.ASSIGNMENT_TYPE, 0);
        course.addDeliverable(deliverable);
        course.addDeliverable(deliverable2);
        course.validateDeliverableModification(deliverable2, "Foo");
    }
    
    @Test
    public void testToStringReturnsAString(){
        assertNotNull(course.toString());
    }
    
    @Test
    public void testCalcAverageNoStudents(){
    	course.getStudentList().clear();
    	assertEquals(Student.NO_GRADE,course.calcAverage());
    	assertEquals(Student.NO_GRADE,course.calcAverage(deliverable));
    	assertEquals(Student.NO_GRADE,course.calcAverage(Deliverable.ASSIGNMENT_TYPE));
    	assertEquals(Student.NO_GRADE,course.calcAverage(Deliverable.EXAM_TYPE));
    }
    
    @Test
    public void testCalcAverageWithStudents(){
    	course.getStudentList().clear();
    	Student student1 = new Student("Foo", "Bar", "1", "daz");
    	Student student2 = new Student("Foo", "Bar", "2", "jaz");
    	course.addStudent(student1);
    	course.addStudent(student2);
    	deliverable = new Deliverable("Asn1",Deliverable.ASSIGNMENT_TYPE,100);
    	course.addDeliverable(deliverable);
    	student1.setGrade(deliverable, 75.5);
    	student2.setGrade(deliverable,83.4);
    	Double averageAsn = (75.5 + 83.4)/2;
    	assertEquals(averageAsn, course.calcAverage());
    	assertEquals(averageAsn,course.calcAverage(Deliverable.ASSIGNMENT_TYPE));
    	assertEquals(averageAsn,course.calcAverage(deliverable));
    	student2.setGrade(deliverable,Student.NO_GRADE);
    	assertEquals(75.5,course.calcAverage());
    	assertEquals(75.5,course.calcAverage(Deliverable.ASSIGNMENT_TYPE));
    	assertEquals(75.5,course.calcAverage(deliverable));
    	
    }
    
    @Test
    public void testCourseEquals(){
        Course newCourse = new Course("Foo", "bar", "daz");
        assertTrue(course.equals(newCourse));
    }
    
    @Test
    public void testCourseEqualsFalse(){
        Course newCourse = new Course("Foo", "bar", "daaz");
        assertFalse(course.equals(newCourse));
    }

    @Test
    public void testDuplicateObjectException() throws Exception{
    	Student student2 = new Student("Foo", "Bar", "2", "haz");
    	try{
    		course.getStudentList().clear();
        	course.addStudent(student);
        	course.addStudent(student2);
        	student.setEmail("haz");
        	course.validateStudentModification(student, "haz", "1");	
    	} catch (DuplicateObjectException e){
    		assertEquals(DuplicateObjectException.DUP_EMAIL, e.getReason());
    	}
    }
    
    @Test
    public void testImportStudentsGood() throws Exception{
        CSVReader reader = new CSVReader(new InputStreamReader(TestCourse.class.getClassLoader().getResourceAsStream("students_test1.csv")));
        course.importStudents(reader);
        
        reader.close();
    }
    
    @Test
    public void testImportStudentsBad() throws Exception{
        course.addStudent(new Student("John", "Smith", "250000000", "foo1@uwo.ca"));
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(TestCourse.class.getClassLoader().getResourceAsStream("students_test2.csv")))){
            course.importStudents(reader);
        } catch (CSVException e){
            assertEquals(6, e.getNumBadLines());
        }
    }
    
    @Test
    public void testImportGradesGood() throws Exception{
        course.addStudent(new Student("John", "Smith", "1234", "foo2@uwo.ca"));
        
        CSVReader reader = new CSVReader(new InputStreamReader(TestCourse.class.getClassLoader().getResourceAsStream("grades_test1.csv")));
        course.importGrades(reader);
        
        reader.close();
    }
    
    @Test
    public void testImportGradesBad() throws Exception{
        course.addStudent(new Student("John", "Smith", "2345", "foo3@uwo.ca"));
        course.addDeliverable(new Deliverable("DL1", Deliverable.EXAM_TYPE, 1));
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(TestCourse.class.getClassLoader().getResourceAsStream("grades_test2.csv")))){
            course.importGrades(reader);
        } catch (CSVException e){
            assertEquals(2, e.getNumBadLines());
        }
    }
    
    @Test
    public void testImportGradesBadHeader() throws Exception{
        try (CSVReader reader = new CSVReader(new InputStreamReader(TestCourse.class.getClassLoader().getResourceAsStream("grades_test3.csv")))){
            course.importGrades(reader);
        } catch (CSVException e){
            assertEquals(CSVException.BAD_FORMAT, e.getNumBadLines());
        }
    }
}
