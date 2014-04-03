package ca.uwo.csd.cs2212.team10;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.*;
import net.sf.jasperreports.engine.JasperPrint;
import javax.mail.Message;

public class TestReportGenerator{
    private ReportGenerator reportGenerator;
    private ReportGenerator.JavaBean bean;
    
    private Course course;
    private Student student;
    private Deliverable asn1;
    private Deliverable asn2;
    private Deliverable exam1;
    private Deliverable exam2;
    
    @Before
    public void setup() throws Exception{
        //create the report generator
        reportGenerator = new ReportGenerator();
        
        //create the new course
        course = new Course("Calculus", "1000", "Winter 2014");
        
        //create the new student
        student = new Student("John", "Doe", "1234567890", "jdoe@example.com");
        
        //add the student to the course
        course.addStudent(student);
        
        //create some deliverables
        asn1 = new Deliverable("Asn1", Deliverable.ASSIGNMENT_TYPE, 5);
        asn2 = new Deliverable("Asn2", Deliverable.ASSIGNMENT_TYPE, 10);
        exam1 = new Deliverable("Midterm", Deliverable.EXAM_TYPE, 35);
        exam2 = new Deliverable("Final", Deliverable.EXAM_TYPE, 50);
        
        //add them to the course
        course.addDeliverable(asn1);
        course.addDeliverable(asn2);
        course.addDeliverable(exam1);
        course.addDeliverable(exam2);
        
        //assign grades for some of them
        student.setGrade(asn1, 90.0);
        student.setGrade(asn2, 90.0);
    }
    
    @Test
    public void testJavaBeanConstructorSetsAttributes(){
        //set the attributes through the constructor
        bean = new ReportGenerator.JavaBean("foo", 1.0, 0.0);
        
        //ensure all attributes were set correctly
        assertEquals("foo", bean.getDeliverableName());
        assertEquals(1.0, bean.getDeliverableGrade());
        assertEquals(0.0, bean.getDeliverableAvg());
    }
    
    @Test
    public void testConstructorReadsJRXMLFile(){
        assertNotNull(reportGenerator.getReport());
    }
    
    @Test
    public void testFillReport() throws Exception{
        JasperPrint printer = reportGenerator.fillReport(course, student);
    }
    
    @Test
    public void testEmailExport() throws Exception{
        String smtpServer = "example.com";
        String smtpPort = "1000";
        String username = "user"; 
        String password = "pass";
        String fromAddress = "mail@example.com";
        
        Message msg = reportGenerator.exportToEmailMessage(smtpServer, smtpPort, username, password, 
                        fromAddress, course, student);
    }
}
