package ca.uwo.csd.cs2212.team10;

import org.junit.Test;
import org.junit.Before;
import static junit.framework.Assert.*;

public class TestCommonFunctions {	
	@Test
	public void testFormatGradeNoGrade(){
		assertFalse(CommonFunctions.formatGrade(Student.NO_GRADE).contains(""+Student.NO_GRADE));
	}
	
	@Test
	public void testFormatGrade(){
		double grade = 78.5;
        
		assertTrue(CommonFunctions.formatGrade(grade).contains(""+grade));
	}
}
