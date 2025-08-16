package com.mycompany.esystem_copy;

public class Assign extends ESystem {
    private int subjid;

    public void setSubjid(int subjid){ this.subjid = subjid; }
    public int getSubjid(){ return subjid; }

    public String assignSubject(int tid) {
        // First check if assignment already exists
        String checkQuery = "SELECT COUNT(*) FROM Assign WHERE subid = " + subjid + " AND tid = " + tid;
        try {
            ESystem.rs = ESystem.st.executeQuery(checkQuery);
            if (ESystem.rs.next() && ESystem.rs.getInt(1) > 0) {
                return "This subject is already assigned to the teacher";
            }
            
            // If not assigned, proceed with assignment
            String query = "INSERT INTO Assign (subid, tid) VALUES (" + subjid + ", " + tid + ")";
            ESystem.st.executeUpdate(query);
            return "Subject assigned successfully";
        } catch (Exception ex) {
            return "Failed to assign subject: " + ex.getMessage();
        }
    }

    public String deleteSubject(int tid) {
        String query = "DELETE FROM Assign WHERE subid = " + subjid + " AND tid = " + tid;
        try {
            int rowsAffected = ESystem.st.executeUpdate(query);
            if (rowsAffected == 0) {
                return "No matching assignment found to delete";
            }
            return "Subject unassigned successfully";
        } catch (Exception ex) {
            return "Failed to unassign subject: " + ex.getMessage();
        }
    }
}
