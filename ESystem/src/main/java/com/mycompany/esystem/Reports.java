/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.awt.GraphicsEnvironment;

/**
 * Records class for generating PDF reports
 *
 * @author tcabato
 */
public class Reports {

    private static final com.itextpdf.text.Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final com.itextpdf.text.Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final com.itextpdf.text.Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/main/java/com/mycompany/esystem/";

    /**
     * Generates a student record PDF for StudentsForm
     * @param studentId the student ID
     * @return the generated PDF file
     */

     public static String generateTime(int studentId) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY); 
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
    
        return String.format("%d_%02d%04d%02d_%02d%02d%02d", 
                            studentId, month, year, day, hour, minute, second);
    }

    public static File generateStudentRecord(int studentId) throws Exception {
        String fileName = FILE_PATH + "record.pdf";
        return createStudentPDF(studentId, fileName, "STUDENT GRADE SHEET");
    }

    /**
     * Generates a grade report PDF for student login
     * @param studentId the student ID
     * @return the generated PDF file
     */
    public static File generateGradeReport(int studentId) throws Exception {
        String fileName = FILE_PATH + generateTime(studentId) + ".pdf";
        return createGradePDF(studentId, fileName);
    }

    private static File createStudentPDF(int studentId, String fileName, String title) throws Exception {
        String studentQuery = "SELECT * FROM students WHERE ID = " + studentId;
        ESystem.rs = ESystem.st.executeQuery(studentQuery);

        if (!ESystem.rs.next()) {
            throw new Exception("Student not found in database");
        }

        String studentID = ESystem.rs.getString("ID");
        String studentName = ESystem.rs.getString("Name");
        String yearLevel = ESystem.rs.getString("YearLevel");
        String course = ESystem.rs.getString("Address");
        String contact = ESystem.rs.getString("Contact");

        // Create PDF document
        Document document = new Document(PageSize.A4);
        File file = new File(fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, fos);
            document.open();
    
            // Add logo at the very top
            try {
                String logoPath = System.getProperty("user.dir") + "/src/main/resources/logo.png";
                Image logo = Image.getInstance(logoPath);
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(PageSize.A4.getWidth(), 100);
                logo.setSpacingAfter(10);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo not found: " + e.getMessage());
            }
    
            // Add title below the logo
            Paragraph titlePara = new Paragraph(title, TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20);
            document.add(titlePara);

            // Create a table with 2 columns for student info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Left column
            PdfPTable leftTable = new PdfPTable(1);
            leftTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            leftTable.addCell(new Phrase("Student ID: " + studentId, NORMAL_FONT));
            leftTable.addCell(new Phrase("Student Name: " + studentName, NORMAL_FONT));
            leftTable.addCell(new Phrase("Student Year: " + yearLevel, NORMAL_FONT));

            // Right column
            PdfPTable rightTable = new PdfPTable(1);
            rightTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            rightTable.addCell(new Phrase("School Year: " + ESystem.currentDB, NORMAL_FONT));
            rightTable.addCell(new Phrase("Student Course: " + (course != null ? course : "N/A"), NORMAL_FONT));

            // Add both tables to the main table
            infoTable.addCell(leftTable);
            infoTable.addCell(rightTable);

            // Add the table to the document
            document.add(infoTable);

            // Add enrolled subjects with grades
            PdfPTable table = createSubjectsTable(studentId);
            document.add(table);

            // Create a table with 2 columns for the footer
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setSpacingBefore(15f);  // Add some space before the footer
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Left cell - Total Subjects
            PdfPCell leftCell = new PdfPCell(new Phrase("Total Subjects Listed: " + (table.getRows().size() - 1), NORMAL_FONT));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            footerTable.addCell(leftCell);

            // Right cell - Empty for the first row
            PdfPCell rightCell = new PdfPCell(new Phrase(""));
            rightCell.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(rightCell);

            // Add the footer table to the document
            document.add(footerTable);

            // Add the signature on a new line
            Paragraph signaturePara = new Paragraph();
            signaturePara.setAlignment(Element.ALIGN_RIGHT);
            signaturePara.add(new Phrase("_________________", NORMAL_FONT));
            signaturePara.add(Chunk.NEWLINE);
            signaturePara.add(new Phrase("Teacher's Signature", NORMAL_FONT));
            document.add(signaturePara);

            document.close();

            return file;
        }
    }

    private static PdfPTable createGradeTable(int studentId) throws Exception {
        int subjectCount = 0;
        // Create table with 5 columns
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        // Set column widths (wider first column)
        float[] columnWidths = {2f, 2f, 5f, 1f, 1f};
        table.setWidths(columnWidths);
    
        // Add headers with bottom border only
        PdfPCell header1 = new PdfPCell(new Phrase("Subject ID", HEADER_FONT));
        PdfPCell header2 = new PdfPCell(new Phrase("Subject Code", HEADER_FONT));
        PdfPCell header3 = new PdfPCell(new Phrase("Description", HEADER_FONT));
        PdfPCell header4 = new PdfPCell(new Phrase("Final", HEADER_FONT));
        PdfPCell header5 = new PdfPCell(new Phrase("Credit", HEADER_FONT));
        
        // Set header styling
        for (PdfPCell header : new PdfPCell[]{header1, header2, header3, header4, header5}) {
            header.setBorder(Rectangle.BOTTOM);
            header.setPadding(5);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        
        table.addCell(header1);
        table.addCell(header2);
        table.addCell(header3);
        table.addCell(header4);
        table.addCell(header5);
    
        // Query to get subjects with grades, grouped by database
        String query = "SELECT s.ID, s.Code, s.Description, " +
                     "COALESCE(g.Final, 'N/A') as Final, s.Units as Credit, " +
                     "DATABASE() as DatabaseName " +
                     "FROM subjects s " +
                     "LEFT JOIN Enroll e ON s.ID = e.subjid " +
                     "LEFT JOIN Grades g ON e.eid = g.GradeID " +
                     "WHERE e.studid = " + studentId + " " +
                     "ORDER BY DatabaseName, s.Code";
    
        ESystem.rs = ESystem.st.executeQuery(query);
    
        String currentDb = null;
        
        // Add data rows
        while (ESystem.rs.next()) {
            String dbName = ESystem.rs.getString("DatabaseName");
            
            // Add database header if it's a new database
            if (!dbName.equals(currentDb)) {
                currentDb = dbName;
                
                // Add empty row for spacing
                addEmptyRow(table, 5);
                
                // Add database label with bottom border
                PdfPCell dbHeader = new PdfPCell(new Phrase("Database: " + dbName, NORMAL_FONT));
                dbHeader.setColspan(5);
                dbHeader.setBorder(Rectangle.BOTTOM);
                dbHeader.setPadding(5);
                table.addCell(dbHeader);
            } subjectCount++;
            
            // Add subject data with no borders
            addCellNoBorder(table, ESystem.rs.getString("ID"));
            addCellNoBorder(table, ESystem.rs.getString("Code"));
            addCellNoBorder(table, ESystem.rs.getString("Description"));
            addCellNoBorder(table, ESystem.rs.getString("Final"));
            addCellNoBorder(table, ESystem.rs.getString("Credit"));
        }
    
        return new Object[]{table, subjectCount};
    }
    
    // Helper method to add cells with no borders
    private static void addCellNoBorder(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    // Helper method to add empty row
    private static void addEmptyRow(PdfPTable table, int cols) {
        PdfPCell emptyCell = new PdfPCell(new Phrase(" "));
        emptyCell.setColspan(cols);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setFixedHeight(10); // Adjust height as needed
        table.addCell(emptyCell);
    }

    private static File createGradePDF(int studentId, String fileName) throws Exception {
        String studentQuery = "SELECT * FROM students WHERE ID = " + studentId;
        ESystem.rs = ESystem.st.executeQuery(studentQuery);
    
        if (!ESystem.rs.next()) {
            throw new Exception("Student not found in database");
        }
    
        String studentID = ESystem.rs.getString("ID");
        String studentName = ESystem.rs.getString("Name");
        String course = ESystem.rs.getString("Address"); // Using Address as Course
        String gender = ESystem.rs.getString("Gender");
    
        // Create PDF document
        Document document = new Document(PageSize.A4);
        File file = new File(fileName);
    
        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            
            // Add logo at the very top
            try {
                String logoPath = System.getProperty("user.dir") + "/src/main/resources/logo.png";
                Image logo = Image.getInstance(logoPath);
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(PageSize.A4.getWidth(), 100);
                logo.setSpacingAfter(10);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo not found: " + e.getMessage());
            }
    
            // Add title
            Paragraph titlePara = new Paragraph("OFFICIAL TRANSCRIPT OF RECORDS", TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20);
            document.add(titlePara);
    
            // Student info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    
            // Left column
            PdfPTable leftTable = new PdfPTable(1);
            leftTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            leftTable.addCell(new Phrase("Student ID: " + studentID, NORMAL_FONT));
            leftTable.addCell(new Phrase("Student Course: " + (course != null ? course : "N/A"), NORMAL_FONT));
    
            // Right column
            PdfPTable rightTable = new PdfPTable(1);
            rightTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            rightTable.addCell(new Phrase("Student Name: " + studentName, NORMAL_FONT));
            rightTable.addCell(new Phrase("Gender: " + (gender != null ? gender : "N/A"), NORMAL_FONT));
    
            infoTable.addCell(leftTable);
            infoTable.addCell(rightTable);
            document.add(infoTable);
    
            // Add subjects table and get subject count
            Object[] result = createGradeTable(studentId);
            PdfPTable gradeTable = (PdfPTable) result[0];
            int subjectCount = (int) result[1];
            document.add(gradeTable);
    
            // Add footer with total subjects
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setSpacingBefore(15f);
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    
            // Left cell - Total Subjects
            PdfPCell leftCell = new PdfPCell(new Phrase("Total Subjects Listed: " + subjectCount, NORMAL_FONT));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            footerTable.addCell(leftCell);
    
            // Right cell - Empty
            PdfPCell rightCell = new PdfPCell(new Phrase(""));
            rightCell.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(rightCell);
    
            document.add(footerTable);
    
            // Add signature
            Paragraph signaturePara = new Paragraph();
            signaturePara.setAlignment(Element.ALIGN_RIGHT);
            signaturePara.add(new Phrase("_________________", NORMAL_FONT));
            signaturePara.add(Chunk.NEWLINE);
            signaturePara.add(new Phrase("Teacher's Signature", NORMAL_FONT));
            document.add(signaturePara);
    
            document.close();
            return file;
        }
    }

    private static PdfPTable createSubjectsTable(int studentId) throws Exception {
        // Updated to 5 columns: SUBJECT CODE, DESCRIPTION, PRELIM, MIDTERM, FINAL
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Add headers
        addTableHeader(table, "SUBJECT CODE");
        addTableHeader(table, "DESCRIPTION");
        addTableHeader(table, "PRELIM");
        addTableHeader(table, "MIDTERM");
        addTableHeader(table, "FINAL");

        // Query enrolled subjects with grades
        //right here
        String query = "SELECT s.ID, s.Description, " +
                     "COALESCE(g.Prelim, 'N/A') as Prelim, " +
                     "COALESCE(g.Midterm, 'N/A') as Midterm, " +
                     "COALESCE(g.Final, 'N/A') as Final " +
                     "FROM subjects s " +
                     "LEFT JOIN Enroll e ON s.ID = e.subjid " +
                     "LEFT JOIN Grades g ON e.eid = g.GradeID " +
                     "WHERE e.studid = " + studentId + " " +
                     "ORDER BY s.ID";

        ESystem.rs = ESystem.st.executeQuery(query);

        // Add data rows
        while (ESystem.rs.next()) {
            String subjectCode = ESystem.rs.getString("ID");
            String description = ESystem.rs.getString("Description");
            String prelim = ESystem.rs.getString("Prelim");
            String midterm = ESystem.rs.getString("Midterm");
            String finalGrade = ESystem.rs.getString("Final");

            // Add cells to table
            addTableCell(table, subjectCode);
            addTableCell(table, description);
            addTableCell(table, prelim);
            addTableCell(table, midterm);
            addTableCell(table, finalGrade);
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
