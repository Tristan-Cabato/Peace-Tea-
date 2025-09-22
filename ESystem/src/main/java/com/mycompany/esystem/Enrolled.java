package com.mycompany.esystem;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Enrolled extends ESystem {
    private int subjid;
    
    public void setsubjid(int sid) { this.subjid = sid; }
    public int getsubjid() { return subjid; }
    
    public String enrollStudent(int studid) {
        // Validate input
        if (studid <= 0) {
            return "Invalid student ID";
        }
        
        if (subjid <= 0) {
            return "Invalid subject ID";
        }
        
        // Check if student exists
        String checkStudent = "SELECT COUNT(*) FROM students WHERE ID = ?";
        // Check if subject exists
        String checkSubject = "SELECT COUNT(*) FROM subjects WHERE ID = ?";
        // Check if already enrolled
        String checkEnrollment = "SELECT COUNT(*) FROM Enroll WHERE studid = ? AND subjid = ?";
        
        try {
            // Check if student exists
            try (PreparedStatement ps = ESystem.con.prepareStatement(checkStudent)) {
                ps.setInt(1, studid);
                ESystem.rs = ps.executeQuery();
                if (!ESystem.rs.next() || ESystem.rs.getInt(1) == 0) {
                    return "Student with ID " + studid + " does not exist";
                }
            }
            
            // Check if subject exists
            try (PreparedStatement ps = ESystem.con.prepareStatement(checkSubject)) {
                ps.setInt(1, subjid);
                ESystem.rs = ps.executeQuery();
                if (!ESystem.rs.next() || ESystem.rs.getInt(1) == 0) {
                    return "Subject with ID " + subjid + " does not exist";
                }
            }
            
            // Check if already enrolled
            try (PreparedStatement ps = ESystem.con.prepareStatement(checkEnrollment)) {
                ps.setInt(1, studid);
                ps.setInt(2, subjid);
                ESystem.rs = ps.executeQuery();
                if (ESystem.rs.next() && ESystem.rs.getInt(1) > 0) {
                    return "Student is already enrolled in this subject";
                }
            }
            
            // If all checks pass, proceed with enrollment
            int nextEid;
            try {
                String query = "SELECT MAX(eid) + 1 FROM Enroll";
                ESystem.rs = ESystem.st.executeQuery(query);
                if (ESystem.rs.next()) {
                    nextEid = ESystem.rs.getInt(1);
                    if (ESystem.rs.wasNull()) {
                        nextEid = 1; // If no records exist yet, start with 1
                    }
                } else {
                    nextEid = 1; // If no records exist yet, start with 1
                }
            } catch (SQLException e) {
                return "Error generating enrollment ID: " + e.getMessage();
            }
            
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
