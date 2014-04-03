package ca.uwo.csd.cs2212.team10;

/**
 * Test for Duplicate objects
 * Used to prevent duplicate Students or Deliverables
 * 
 * @author Team 10
 */
public class DuplicateObjectException extends Exception{
    public static final int DUP_NUMBER = 1;
    public static final int DUP_EMAIL = 2;
    
    private int reason;
    
    public DuplicateObjectException(){
    
    }
    
    public DuplicateObjectException(int reason){
        super();
        this.reason = reason;
    }
    
    public int getReason(){
        return reason;
    }
}
