/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem;
import java.sql.*;

/**
 *
 * @author tcabato
 */
public class Teacher extends ESystem {
    int Tid;
    String Tname, Tadd, Tcontact, Tdept;

    public Teacher() {
        try {
            String query = "SELECT MAX(ID) + 1 FROM teachers";
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                int nextId = rs.getInt(1);
                Tid = nextId < 3001 ? 3001 : nextId;
            }
        } catch (Exception ex) {
            System.out.println("Error getting next teacher ID: " + ex.getMessage());
            Tid = 3001; 
        } 
    }

    public void SaveRecord(int Tid, String Tname, String Tadd, int Tcontact, String Tdept){
        String query = "Insert into teachers values(" + Tid + ",'" + Tname + "','" + Tadd + "'," + Tcontact + ",'" + Tdept + "')";
        try {
            st.executeUpdate(query);
            System.out.println("Teacher Record Saved");	
        } catch (Exception ex) { System.out.println("Failed to save teacher record: " + ex.getMessage()); }
    }
    
    public void UpdateRecord(int Tid, String Tname, String Tadd, int Tcontact, String Tdept) {
        String query = "UPDATE teachers SET " +
                      "Name = '" + Tname + "', " +
                      "Address = '" + Tadd + "', " +
                      "Contact = " + Tcontact + ", " +
                      "Department = '" + Tdept + "' " +
                      "WHERE ID = " + Tid;
        try {
            st.executeUpdate(query);
            System.out.println("Update Success!!!");
        } catch (Exception ex) { System.out.println("Failed to Update: " + ex.getMessage()); } 
    }
    
    public void DeleteRecord(int Tid) {
        String query = "DELETE FROM teachers WHERE ID = " + Tid;
        try {
            st.executeUpdate(query);
            System.out.println("Delete Success!!!");
        } catch (Exception ex) { System.out.println("Failed to Delete" + ex.getMessage()); }
    }
}

