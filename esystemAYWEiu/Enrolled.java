package com.mycompany.esystem;

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
        String checkQuery = "SELECT COUNT(*) FROM Enroll WHERE studid = " + studid + " AND subjid = " + subjid;
        try {
            ESystem.rs = ESystem.st.executeQuery(checkQuery);
            if (ESystem.rs.next() && ESystem.rs.getInt(1) > 0) {
                return "Student is already enrolled in this subject";
            }
            
            int nextEid = getEnrollmentId();
            String query = "INSERT INTO Enroll (eid, studid, subjid) VALUES (" + nextEid + ", " + studid + ", " + subjid + ")";
            ESystem.st.executeUpdate(query);
            return "Student enrolled successfully";
        } catch (Exception ex) {
            return "Failed to enroll student: " + ex.getMessage();
        }
    }
    
    public String dropSubject(int studid) {
        String query = "DELETE FROM Enroll WHERE studid = " + studid + " AND subjid = " + subjid;
        try {
            int rowsAffected = ESystem.st.executeUpdate(query);
            if (rowsAffected == 0) {
                return "No matching enrollment found to drop";
            }
            return "Subject dropped successfully";
        } catch (Exception ex) {
            return "Failed to drop subject: " + ex.getMessage();
        }
    }
}
