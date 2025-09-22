/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.*;
import java.sql.*;
import javax.swing.*;
import java.awt.*;

/**
 * Records class for generating PDF reports
 *
 * @author tcabato
 */
public class Reports {

    private static final com.itextpdf.text.Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final com.itextpdf.text.Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final com.itextpdf.text.Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);

    /**
     * Generates a student record PDF for StudentsForm
     * @param studentId the student ID
     * @return the generated PDF file
     */
    public static File generateStudentRecord(int studentId) throws Exception {
        String fileName = System.getProperty("user.dir") + "/StudentRecord_" + studentId + ".pdf";
        return createStudentPDF(studentId, fileName, "STUDENT RECORD");
    }

    /**
     * Generates a grade report PDF for student login
     * @param studentId the student ID
     * @return the generated PDF file
     */
    public static File generateGradeReport(int studentId) throws Exception {
        String fileName = System.getProperty("user.dir") + "/GradeReport_" + studentId + "_" + System.currentTimeMillis() + ".pdf";
        return createGradePDF(studentId, fileName);
    }

    private static File createStudentPDF(int studentId, String fileName, String title) throws Exception {
        // Get student information
        String studentQuery = "SELECT * FROM students WHERE ID = " + studentId;
        ESystem.rs = ESystem.st.executeQuery(studentQuery);

        if (!ESystem.rs.next()) {
            throw new Exception("Student not found in database");
        }

        String studentName = ESystem.rs.getString("Name");
        String yearLevel = ESystem.rs.getString("YearLevel");
        String studentInfo = "Name: " + studentName + " | Year Level: " + yearLevel + " | ID: " + studentId;

        // Create PDF document
        Document document = new Document(PageSize.A4);
        File file = new File(fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Add title
            Paragraph titlePara = new Paragraph(title, TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20);
            document.add(titlePara);

            // Add student info
            Paragraph info = new Paragraph(studentInfo, NORMAL_FONT);
            info.setSpacingAfter(15);
            document.add(info);

            // Add enrolled subjects with grades
            PdfPTable table = createSubjectsTable(studentId);
            document.add(table);

            document.close();

            return file;
        }
    }

    private static File createGradePDF(int studentId, String fileName) throws Exception {
        return createStudentPDF(studentId, fileName, "STUDENT GRADE REPORT");
    }

    private static PdfPTable createSubjectsTable(int studentId) throws Exception {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Add headers
        addTableHeader(table, "SUBJECT CODE");
        addTableHeader(table, "DESCRIPTION");
        addTableHeader(table, "UNITS");
        addTableHeader(table, "SCHEDULE");
        addTableHeader(table, "PRELIM");
        addTableHeader(table, "MIDTERM");
        addTableHeader(table, "PREFINAL");
        addTableHeader(table, "FINAL");
        addTableHeader(table, "STATUS");

        // Query enrolled subjects with grades
        String query = "SELECT s.Code, s.Description, s.Units, s.Schedule, " +
                     "g.Prelim, g.Midterm, g.Prefinal, g.Final " +
                     "FROM subjects s " +
                     "JOIN Enroll e ON s.ID = e.subjid " +
                     "LEFT JOIN Grades g ON e.eid = g.eid " +
                     "WHERE e.studid = " + studentId + " " +
                     "ORDER BY s.Code";

        ESystem.rs = ESystem.st.executeQuery(query);

        // Add data rows
        while (ESystem.rs.next()) {
            String subjectCode = ESystem.rs.getString("Code");
            String description = ESystem.rs.getString("Description");
            String units = ESystem.rs.getString("Units");
            String schedule = ESystem.rs.getString("Schedule");
            String prelim = ESystem.rs.getString("Prelim");
            String midterm = ESystem.rs.getString("Midterm");
            String prefinal = ESystem.rs.getString("Prefinal");
            String finalGrade = ESystem.rs.getString("Final");
            String status = calculateStatus(prelim, midterm, prefinal, finalGrade);

            // Add cells to table
            addTableCell(table, subjectCode);
            addTableCell(table, description);
            addTableCell(table, units);
            addTableCell(table, schedule);
            addTableCell(table, prelim != null ? prelim : "N/A");
            addTableCell(table, midterm != null ? midterm : "N/A");
            addTableCell(table, prefinal != null ? prefinal : "N/A");
            addTableCell(table, finalGrade != null ? finalGrade : "N/A");

            // Color status cell based on value
            PdfPCell statusCell = new PdfPCell(new Phrase(status, NORMAL_FONT));
            if (status.equals("PASSED")) {
                statusCell.setBackgroundColor(new BaseColor(200, 255, 200)); // Light green
            } else if (status.equals("FAILED")) {
                statusCell.setBackgroundColor(new BaseColor(255, 200, 200)); // Light red
            } else if (status.equals("ONGOING")) {
                statusCell.setBackgroundColor(new BaseColor(255, 255, 200)); // Light yellow
            }
            table.addCell(statusCell);
        }

        return table;
    }

    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(new BaseColor(200, 200, 200));
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(text, HEADER_FONT));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setMinimumHeight(20);
        table.addCell(header);
    }

    private static void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static String calculateStatus(String prelim, String midterm, String prefinal, String finalGrade) {
        try {
            double prelimGrade = prelim != null && !prelim.equals("N/A") ? Double.parseDouble(prelim) : 0;
            double midtermGrade = midterm != null && !midterm.equals("N/A") ? Double.parseDouble(midterm) : 0;
            double prefinalGrade = prefinal != null && !prefinal.equals("N/A") ? Double.parseDouble(prefinal) : 0;
            double finalGrd = finalGrade != null && !finalGrade.equals("N/A") ? Double.parseDouble(finalGrade) : 0;

            if (finalGrd > 0) {
                return finalGrd >= 75.0 ? "PASSED" : "FAILED";
            } else if (prefinalGrade > 0) {
                return "ONGOING";
            } else if (midtermGrade > 0) {
                return "ONGOING";
            } else if (prelimGrade > 0) {
                return "ONGOING";
            } else {
                return "NOT STARTED";
            }
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }

    /**
     * Opens a PDF file using the default system application
     * @param file the PDF file to open
     */
    public static void openPDF(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                    "Could not open PDF file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // In Login.java or wherever student login is handled:
    private void handleStudentLogin(int studentId) {
        try {
            File pdfFile = Reports.generateGradeReport(studentId);
            JOptionPane.showMessageDialog(null, "Grade report saved to: " + pdfFile.getAbsolutePath(), 
                "Report Generated", 
                JOptionPane.INFORMATION_MESSAGE);
            Reports.openPDF(pdfFile);
            
            // Then show StudentRegistration form
            StudentRegistration studentReg = new StudentRegistration();
            studentReg.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error generating grade report: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
