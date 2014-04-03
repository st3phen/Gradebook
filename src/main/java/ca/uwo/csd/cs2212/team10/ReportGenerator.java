package ca.uwo.csd.cs2212.team10;

import java.io.*;
import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.xml.*;
import org.apache.velocity.*;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * To generate the PDF Reports
 * 
 * @author Team 10
 */
public class ReportGenerator {
    private final static String REPORT_FILENAME = "grade_report.jrxml";

    /* Attributes */
    private JasperReport report;

    public static class JavaBean {
        /* Attributes */
        private Double deliverableAvg;
        private Double deliverableGrade;
        private String deliverableName;
        
        public JavaBean(String deliverableName, Double grade, Double deliverableAvg) {
            this.deliverableName = deliverableName;
            this.deliverableGrade = grade;
            this.deliverableAvg = deliverableAvg;
        }

        public String getDeliverableName() {
            return deliverableName;
        }

        public Double getDeliverableGrade() {
            return deliverableGrade;
        }

        public Double getDeliverableAvg() {
            return deliverableAvg;
        }
    }
    
    public ReportGenerator() throws JRException {
        InputStream reportStream = ReportGenerator.class.getClassLoader().getResourceAsStream(REPORT_FILENAME);
        JasperDesign jasperDesign = JRXmlLoader.load(reportStream);
        report = JasperCompileManager.compileReport(jasperDesign);
    }
    
    public JasperReport getReport(){
        return report;
    }
    
    public JasperPrint fillReport(Course course, Student student) throws JRException {
        Collection<JavaBean> beans = new ArrayList<JavaBean>();
        List<Deliverable> deliverables = course.getDeliverableList();
        
        for (Deliverable d : deliverables){
            if (student.getGrade(d) != Student.NO_GRADE)
                beans.add(new JavaBean(d.toString(), student.getGrade(d), course.calcAverage(d)));
        }
        
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(beans);
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("courseDetails", course.toString());
        parameters.put("studentDetails", student.toString());
        parameters.put("average", CommonFunctions.formatGrade(student.calcAverage()));
        parameters.put("asnAverage", CommonFunctions.formatGrade(student.calcAverage(Deliverable.ASSIGNMENT_TYPE)));
        parameters.put("examAverage", CommonFunctions.formatGrade(student.calcAverage(Deliverable.EXAM_TYPE)));

        return JasperFillManager.fillReport(report, parameters, beanColDataSource);
    }

    public Message exportToEmailMessage(String smtpServer, String smtpPort, final String username, 
            final String password, String fromAddress, Course course, Student s)
            throws AddressException, MessagingException, JRException {
        
        //getr properties
        Properties props = new Properties();
 
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        //authenticate username & password
        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            }
        );
        
        //initialize new message
        Message msg = new MimeMessage(session);
        
        //set sender's address
        Address sender = new InternetAddress(fromAddress);
        msg.setFrom(sender);

        //add recipient
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(s.getEmail()));

        //set subject
        msg.setSubject("Grade Report");

        //load templates
        Multipart multiPart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(loadTemplate("email.text.vm", s.getFirstName()), "utf-8");

        //attach pdf report
        MimeBodyPart fileAttachmentPart = new MimeBodyPart();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(fillReport(course, s), output);
        DataSource source = new ByteArrayDataSource(output.toByteArray(), "application/pdf");
        
        fileAttachmentPart.setDataHandler(new DataHandler(source));
        fileAttachmentPart.setFileName("grade_report.pdf");

        multiPart.addBodyPart(textPart);
        multiPart.addBodyPart(fileAttachmentPart);

        msg.setContent(multiPart);

        return msg;
    } 
    
    private static String loadTemplate(String filename, String name) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        
        //get template
        Template template = ve.getTemplate(filename);
        
        //put student name in template
        VelocityContext context = new VelocityContext();
        context.put("studentName", name);
        
        StringWriter out = new StringWriter();
        template.merge(context, out);
        
        return out.toString();
    }
}
