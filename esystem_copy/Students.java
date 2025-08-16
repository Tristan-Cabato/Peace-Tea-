/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem_copy;
import java.sql.ResultSet;

/**
 *
 * @author tcabato
 */
public class Students extends ESystem { // SQL Variables should change if required
    int Studid,Contact,YearLevel;
    String Name,Address,Gender;
    
    public Students() { 
        connectDB(); 

        try {
            String query = "SELECT MAX(ID) + 1 FROM students";
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                int nextId = rs.getInt(1);
                Studid = nextId < 1001 ? 1001 : nextId;
            }
        } catch (Exception ex) {
            System.out.println("Error getting next student ID: " + ex.getMessage());
            Studid = 1001; 
        }
    }
    public void connectDB() { DBConnect(); }
    
    public void SaveRecord(int Studid, String Name, String Address, int Contact, String Gender, int YearLevel){
        String query = "INSERT INTO students VALUES(" + Studid + ",'" + Name + "','" + Address + "'," + Contact + ",'" + Gender + "'," + YearLevel + ")";
        try {
            st.executeUpdate(query);
            System.out.println("Student Record Saved");	
        } catch (Exception ex) { System.out.println("Failed to save student record: " + ex.getMessage()); }
    }
    
    public void UpdateRecord(int Studid, String Name, String Address, int Contact, String Gender, int YearLevel) {
        String query = "UPDATE students SET " +
                      "Name = '" + Name + "', " +
                      "Address = '" + Address + "', " +
                      "Contact = " + Contact + ", " +
                      "Gender = '" + Gender + "', " +
                      "YearLevel = " + YearLevel + " " +
                      "WHERE ID = " + Studid;
        try {
            st.executeUpdate(query);
            System.out.println("Student Record Updated");
        } catch (Exception ex) { System.out.println("Failed to update student record: " + ex.getMessage()); } 
    }
    
    public void DeleteRecord(int Studid) {
        String query = "DELETE FROM students WHERE ID = " + Studid;
        try {
            st.executeUpdate(query);
            System.out.println("Student Record Deleted");
        } catch (Exception ex) { System.out.println("Failed to delete student record: " + ex.getMessage()); }
    }
}
