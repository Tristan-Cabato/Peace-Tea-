/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem;

import java.sql.ResultSet;

/**
 *
 * @author tcabato
 */
public class Subject extends ESystem {
    int Subjid, Subjunits;
    String Subjcode, Subjdesc, sched;
    
    public Subject() { 
        try {
            String query = "SELECT MAX(ID) + 1 FROM subjects";
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                int nextId = rs.getInt(1);
                Subjid = nextId < 2001 ? 2001 : nextId;
            }
        } catch (Exception ex) {
            System.out.println("Error getting next subject ID: " + ex.getMessage());
            Subjid = 2001; 
        } 
    }
    
    public void SaveRecord(int Subjid, String Subjcode, String Subjdesc, int Subjunits, String sched){
        String query = "Insert into subjects values(" + Subjid + ",'" + Subjcode + "','" + Subjdesc + "'," + Subjunits + ",'" + sched + "')";
        try {
            st.executeUpdate(query);
            System.out.println("Subject record saved");	
        } catch (Exception ex) { System.out.println("Failed to save subject record: " + ex.getMessage()); }
    }
    
    public void UpdateRecord(int Subjid, String Subjcode, String Subjdesc, int Subjunits, String sched) {
        String query = "UPDATE subjects SET " +
                      "Code = '" + Subjcode + "', " +
                      "Description = '" + Subjdesc + "', " +
                      "Units = " + Subjunits + ", " +
                      "Schedule = '" + sched + "' " +
                      "WHERE ID = " + Subjid;
        try {
            st.executeUpdate(query);
            System.out.println("Subject record updated");
        } catch (Exception ex) { System.out.println("Failed to update subject record: " + ex.getMessage()); } 
    }
    
    public void DeleteRecord(int Subjid) {
        String query = "DELETE FROM subjects WHERE ID = " + Subjid;
        try {
            st.executeUpdate(query);
            System.out.println("Delete Success!!!");
        } catch (Exception ex) { System.out.println("Failed to Delete" + ex.getMessage()); }
    }
}
