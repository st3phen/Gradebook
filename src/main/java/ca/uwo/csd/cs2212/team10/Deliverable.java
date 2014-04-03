package ca.uwo.csd.cs2212.team10;

import java.io.Serializable;

/**
 * Represents a gradeable item to be completed by a student in a course
 * @author Team 10
 */
public class Deliverable implements Serializable{
    /* Constants */
    private static final long serialVersionUID = 1L; //for serializing
    
    public static final String[] TYPES = {"Assignment", "Exam"};
    public static final int ASSIGNMENT_TYPE = 0;
    public static final int EXAM_TYPE = 1;

    /* Attributes */
    private String name;
    private int type;
    private int weight;
    

    /* Constructor */
    public Deliverable(String name, int type, int weight){
        this.name = name;
        this.type = type;
        this.weight = weight;
    }
    
    /* Public Methods */
    
    public String getName(){
        return name;
    }
    
    public int getType(){
        return type;
    }
    
    public int getWeight(){
        return weight;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setType(int type){
        this.type = type;
    }
    
    public void setWeight(int weight){
        this.weight = weight;
    }
    
    public boolean equals(Deliverable d){
        return name.equals(d.getName());
    }
    
    @Override
    public String toString(){
        return name + " (" + weight + "%)";
    }
}
