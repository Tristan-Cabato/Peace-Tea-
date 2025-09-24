/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tcabato
 */
public class Grade extends ESystem {

    public Grade() {
        // Constructor - no specific initialization needed
    }

    /**
     * Loads subjects for the current teacher into the provided table model
     * @param model The DefaultTableModel to populate with subject data
     */
    public void loadSubjects(DefaultTableModel model) {
        model.setRowCount(0);

        try {
            String currentUser = ESystem.currentUser;
            String teacherIdStr = "";

            int i = 0;
            while (i < currentUser.length() && Character.isDigit(currentUser.charAt(i))) {
                teacherIdStr += currentUser.charAt(i);
                i++;
            }
            int teacherId = Integer.parseInt(teacherIdStr);

            String query = "SELECT s.ID, s.Code, s.Description, s.Units, s.Schedule, " +
                         "(SELECT COUNT(*) FROM Enroll e WHERE e.subjid = s.ID) as StudentCount " +
                         "FROM subjects s " +
                         "JOIN Assign a ON s.ID = a.subid " +
                         "WHERE a.tid = " + teacherId;

            ESystem.rs = ESystem.st.executeQuery(query);

            while (ESystem.rs.next()) {
                model.addRow(new Object[]{
                    ESystem.rs.getInt("ID"),
                    ESystem.rs.getString("Code"),
                    ESystem.rs.getString("Description"),
                    ESystem.rs.getInt("Units"),
                    ESystem.rs.getString("Schedule"),
                    ESystem.rs.getInt("StudentCount")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error loading subjects: " + ex.getMessage());
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid teacher ID format in username: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Loads students for a specific subject into the provided table model
     * @param model The DefaultTableModel to populate with student data
     * @param subjectId The ID of the subject to load students for
     */
    public void loadStudentsForSubject(DefaultTableModel model, int subjectId) {
        model.setRowCount(0);

        try {
            String query = "SELECT s.ID, s.Name, " +
                         "(SELECT g.Prelim FROM Grades g WHERE g.eid = e.eid) as Prelim, " +
                         "(SELECT g.Midterm FROM Grades g WHERE g.eid = e.eid) as Midterm, " +
                         "(SELECT g.Prefinal FROM Grades g WHERE g.eid = e.eid) as Prefinal, " +
                         "(SELECT g.Final FROM Grades g WHERE g.eid = e.eid) as Final " +
                         "FROM students s, Enroll e " +
                         "WHERE s.ID = e.studid AND e.subjid = ?";

            try (PreparedStatement pstmt = ESystem.con.prepareStatement(query)) {
                pstmt.setInt(1, subjectId);
                ESystem.rs = pstmt.executeQuery();

                while (ESystem.rs.next()) {
                    model.addRow(new Object[]{
                        ESystem.rs.getInt("ID"),
                        ESystem.rs.getString("Name"),
                        ESystem.rs.getString("Prelim"),
                        ESystem.rs.getString("Midterm"),
                        ESystem.rs.getString("Prefinal"),
                        ESystem.rs.getString("Final")
                    });
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error loading students: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Saves grades for a specific student and subject
     * @param studentId The ID of the student
     * @param subjectId The ID of the subject
     * @param prelim The prelim grade
     * @param midterm The midterm grade
     * @param prefinal The prefinal grade
     * @param finalGrade The final grade
     * @return true if successful, false otherwise
     */
    public boolean saveGrades(int studentId, int subjectId, String prelim, String midterm, String prefinal, String finalGrade) {
        try {
            // Get the eid from Enroll table
            String getEidQuery = "SELECT eid FROM Enroll WHERE studid = ? AND subjid = ?";
            int eid = -1;

            try (PreparedStatement pstmt = ESystem.con.prepareStatement(getEidQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, subjectId);
                ESystem.rs = pstmt.executeQuery();

                if (ESystem.rs.next()) {
                    eid = ESystem.rs.getInt("eid");
                } else {
                    System.err.println("No enrollment found for the selected student and subject");
                    return false;
                }
            }

            // Check if grades exist
            String checkGrades = "SELECT COUNT(*) FROM Grades WHERE eid = ?";
            boolean gradesExist = false;

            try (PreparedStatement pstmt = ESystem.con.prepareStatement(checkGrades)) {
                pstmt.setInt(1, eid);
                ESystem.rs = pstmt.executeQuery();

                if (ESystem.rs.next()) {
                    gradesExist = ESystem.rs.getInt(1) > 0;
                }
            }

            // Insert or update grades
            String query;
            if (gradesExist) {
                query = "UPDATE Grades SET " +
                       "Prelim = ?, " +
                       "Midterm = ?, " +
                       "Prefinal = ?, " +
                       "Final = ? " +
                       "WHERE eid = ?";
            } else {
                query = "INSERT INTO Grades (Prelim, Midterm, Prefinal, Final, eid) " +
                       "VALUES (?, ?, ?, ?, ?)";
            }

            try (PreparedStatement pstmt = ESystem.con.prepareStatement(query)) {
                // Set parameters for the prepared statement
                pstmt.setString(1, prelim == null ? null : prelim);
                pstmt.setString(2, midterm == null ? null : midterm);
                pstmt.setString(3, prefinal == null ? null : prefinal);
                pstmt.setString(4, finalGrade == null ? null : finalGrade);
                pstmt.setInt(5, eid);

                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error saving grades: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}
