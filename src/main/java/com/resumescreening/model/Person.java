package com.resumescreening.model;
// Abstract base class to be extended by sub classes
// Applied concepts: Abstraction and Encapsulation
public abstract class Person {
    private String name;
    private String email;
    private String phone;

    public Person (String name, String email, String phone){
        this.name = name;
        this.email =email;
        this.phone = phone;
    }

    // Abstract method to be implemented in the subclasses
    public abstract double calculateScore();

    //Setters and Getters to encapsulate my super class data
    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name =name;
    }

    public String getPhone (){
        return phone;
    }

    public void setPhone (String phone){
        this.phone =phone;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }
}
