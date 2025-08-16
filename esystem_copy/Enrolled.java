package com.mycompany.esystem_copy;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Enrolled extends ESystem {
    private int subjid;
    
    public void setsubjid(int sid) { this.subjid = sid; }
    public int getsubjid() { return subjid; }
    
    public int getEnrollmentId() {
        try {
            String query = "SELECT MAX(eid) + 1 FROM Enroll";
            ESystem.rs = ESystem.st.executeQuery(query);
            if (ESystem.rs.next()) {
                int nextId = ESystem.rs.getInt(1);
                return ESystem.rs.wasNull() ? 1 : nextId;
            }
            return 1;
        } catch (Exception ex) {
            System.out.println("Error getting next enrollment ID: " + ex.getMessage());
            return 1; 
        }
    }
    
    public String enrollStudent(int studid) {
        // Validate input
        if (studid <= 0) {
            return "Invalid student ID";
        }
        
        if (subjid <= 0) {
            return "Invalid subject ID";
        }
        
        // Check if student exists
        String checkStudent = "SELECT COUNT(*) FROM students WHERE studid = " + studid;
        // Check if subject exists
        String checkSubject = "SELECT COUNT(*) FROM subjects WHERE subjid = " + subjid;
        // Check if already enrolled
        String checkEnrollment = "SELECT COUNT(*) FROM Enroll WHERE studid = " + studid + " AND subjid = " + subjid;
        
        try {
            // Check if student exists
            ESystem.rs = ESystem.st.executeQuery(checkStudent);
            if (!ESystem.rs.next() || ESystem.rs.getInt(1) == 0) {
                return "Student with ID " + studid + " does not exist";
            }
            
            // Check if subject exists
            ESystem.rs = ESystem.st.executeQuery(checkSubject);
            if (!ESystem.rs.next() || ESystem.rs.getInt(1) == 0) {
                return "Subject with ID " + subjid + " does not exist";
            }
            
            // Check if already enrolled
            ESystem.rs = ESystem.st.executeQuery(checkEnrollment);
            if (ESystem.rs.next() && ESystem.rs.getInt(1) > 0) {
                return "Student is already enrolled in this subject";
            }
            
            // If all checks pass, proceed with enrollment
            int nextEid = getEnrollmentId();
            String query = "INSERT INTO Enroll (eid, studid, subjid) VALUES (?, ?, ?)";
            
            // Using PreparedStatement to prevent SQL injection
            try (PreparedStatement pstmt = ESystem.con.prepareStatement(query)) {
                pstmt.setInt(1, nextEid);
                pstmt.setInt(2, studid);
                pstmt.setInt(3, subjid);
                pstmt.executeUpdate();
                return "Student enrolled successfully";
            }
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }
    
    public String dropSubject(int studid) {
        // Validate input
        if (studid <= 0) {
            return "Invalid student ID";
        }
        
        if (subjid <= 0) {
            return "Invalid subject ID";
        }
        
        String query = "DELETE FROM Enroll WHERE studid = ? AND subjid = ?";
        
        try (PreparedStatement pstmt = ESystem.con.prepareStatement(query)) {
            pstmt.setInt(1, studid);
            pstmt.setInt(2, subjid);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                return "No matching enrollment found to drop";
            }
            return "Subject dropped successfully";
        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }
}
