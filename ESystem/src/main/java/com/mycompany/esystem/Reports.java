/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.esystem;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.awt.Desktop;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
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
    private static final com.itextpdf.text.Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10); // Add this line
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/main/java/com/mycompany/esystem/Docs/";

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
            leftTable.addCell(createBoldLabel("Student ID", studentID));
            leftTable.addCell(createBoldLabel("Student Name", studentName));
            leftTable.addCell(createBoldLabel("Student Year", yearLevel));

            // Right column
            PdfPTable rightTable = new PdfPTable(1);
            rightTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            rightTable.addCell(createBoldLabel("School Year", ESystem.currentDB));
            rightTable.addCell(createBoldLabel("Student Course", (course != null ? course : "N/A")));

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
            Font boldLargeFont = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT.getSize() + 2, Font.BOLD);
            PdfPCell leftCell = new PdfPCell(new Phrase("Number of Subjects Listed: " + (table.getRows().size() - 1), boldLargeFont));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            footerTable.addCell(leftCell);

            // Right cell - Empty for the first row
            PdfPCell rightCell = new PdfPCell(new Phrase(""));
            rightCell.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(rightCell);

            // Add the footer table to the document
            document.add(footerTable);

            // Add separator line with custom styling
            LineSeparator line = new LineSeparator();
            line.setLineWidth(1.5f);  // Make the line thicker
            line.setLineColor(BaseColor.BLACK);  // Ensure it's black
            line.setPercentage(80);  // Make it 80% of the page width
            line.setAlignment(Element.ALIGN_CENTER);  // Center the line

            Paragraph separator = new Paragraph();
            separator.add(new Chunk(line));
            separator.setSpacingAfter(15f);
            separator.setSpacingBefore(10f);
            document.add(separator);

            // Then the line break
            document.add(Chunk.NEWLINE);

            // Add the signature on a new line
            Paragraph signaturePara = new Paragraph();
            signaturePara.setAlignment(Element.ALIGN_RIGHT);
            signaturePara.add(new Phrase("_________________", NORMAL_FONT));
            signaturePara.add(Chunk.NEWLINE);
            signaturePara.add(new Phrase("Registrar", NORMAL_FONT));
            document.add(signaturePara);

            document.close();

            return file;
        }
    }

    private static Object[] createGradeTable(int studentId) throws Exception {
        int subjectCount = 0;
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        float[] columnWidths = {1.5f, 2f, 4f, 1.5f, 1f};
        table.setWidths(columnWidths);
    
        // Add headers
        addTableHeader(table, "Subject ID");
        addTableHeader(table, "Code");
        addTableHeader(table, "Descriptive Title");
        addTableHeader(table, "Final");
        addTableHeader(table, "Credit");
    
        // Get list of all databases that match the pattern
        String getDbsQuery = "SHOW DATABASES WHERE `Database` LIKE '%_Sy%' OR `Database` LIKE '%1stSem%' OR `Database` LIKE '%2ndSem%' OR `Database` LIKE '%summer%'";
        String currentDb = ESystem.con.getCatalog();
        ResultSet dbs = null;
        
        try {
            // Get all database names
            dbs = ESystem.st.executeQuery(getDbsQuery);
            List<String> databaseNames = new ArrayList<>();
            
            // First collect all database names to avoid ResultSet conflicts
            while (dbs.next()) {
                databaseNames.add(dbs.getString(1));
            }
            
            // Close the ResultSet after collecting database names
            if (dbs != null) {
                try { dbs.close(); } catch (SQLException e) { /* ignore */ }
            }
    
            // Now process each database
            for (String dbName : databaseNames) {
                ResultSet checkRs = null;
                ResultSet rs = null;
                try {
                    // Switch to the database
                    ESystem.st.execute("USE " + dbName);
                    
                    // Check if student has any enrollments in this database
                    String checkQuery = "SELECT COUNT(*) as count FROM Enroll WHERE studid = " + studentId;
                    checkRs = ESystem.st.executeQuery(checkQuery);
                    if (checkRs.next() && checkRs.getInt("count") == 0) {
                        continue;  // Skip if no enrollments
                    }
    
                    // Update the query in the createGradeTable method to:
                    String query = "SELECT " +
                                "s.ID as 'Subject ID', " +
                                "s.Code as Code, " +
                                "s.Description as 'Descriptive Title', " +
                                "IFNULL(g.Final, 'No Grade') as Final, " +
                                "s.Units as Credit " +
                                "FROM subjects s " +
                                "INNER JOIN Enroll e ON s.ID = e.subjid " +
                                "LEFT JOIN Grades g ON e.eid = g.GradeID " +
                                "WHERE e.studid = " + studentId + " " +
                                "ORDER BY s.Code";
    
                    rs = ESystem.st.executeQuery(query);
                    boolean hasRecords = false;
    
                    // Add data to table
                    while (rs.next()) {
                        if (!hasRecords) {
                            // Add database header only if we have records
                            addEmptyRow(table, 5);
                            Font underlineFont = new Font(HEADER_FONT.getBaseFont(), HEADER_FONT.getSize(), Font.UNDERLINE);
                            PdfPCell dbHeader = new PdfPCell(new Phrase(dbName, underlineFont));
                            dbHeader.setColspan(5);
                            dbHeader.setBorder(Rectangle.NO_BORDER);
                            dbHeader.setPadding(5);
                            table.addCell(dbHeader);
                            hasRecords = true;
                        }
                        
                        subjectCount++;
                        addCellNoBorder(table, rs.getString("Subject ID"));
                        addCellNoBorder(table, rs.getString("Code"));
                        addCellNoBorder(table, rs.getString("Descriptive Title"));
                        addCellNoBorder(table, rs.getString("Final"));
                        addCellNoBorder(table, rs.getString("Credit"));
                    }
                } catch (SQLException e) {
                    System.err.println("Error processing database " + dbName + ": " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Close ResultSets
                    if (checkRs != null) try { checkRs.close(); } catch (SQLException e) { /* ignore */ }
                    if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
                }
            }
        } finally {
            // Close the main ResultSet if it's still open
            if (dbs != null) {
                try { dbs.close(); } catch (SQLException e) { /* ignore */ }
            }
            // Switch back to the original database
            if (currentDb != null) {
                ESystem.st.execute("USE " + currentDb);
            }
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
    
            // ADD THE LINE SEPARATOR HERE - right after total subjects and before grading systems
            LineSeparator line = new LineSeparator();
            line.setLineWidth(1f);
            line.setLineColor(BaseColor.BLACK);
            line.setPercentage(90);
            line.setAlignment(Element.ALIGN_CENTER);
    
            Paragraph separator = new Paragraph();
            separator.add(new Chunk(line));
            separator.setSpacingAfter(15f);
            separator.setSpacingBefore(10f);
            document.add(separator);
    
            // Add line break after total subjects
            document.add(Chunk.NEWLINE);
    
            // Add grading system tables
            PdfPTable gradingSystemsTable = new PdfPTable(2);
            gradingSystemsTable.setWidthPercentage(100);
            gradingSystemsTable.setSpacingAfter(10f);
            gradingSystemsTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    
            // Left grading system
            String leftGrading = "Grading System up to SY 1981-82\n\n" +
                               "95-100 = 1.0 = Excellent\n" +
                               "90-94  = 1.5 = Very Good\n" +
                               "85-89  = 2.0 = Good\n" +
                               "80-84  = 2.5 = Fair\n" +
                               "75-79  = 3.0 = Passed\n" +
                               "Below 75 = 5.0 = Failed";
    
            // Right grading system
            String rightGrading = "New Grading System(1st Sem. SY1982-83 & up)\n\n" +
                                "95-100 = 1.0 = Excellent\n" +
                                "90-94  = 1.5 = Very Good\n" +
                                "85-89  = 2.0 = Good\n" +
                                "80-84  = 2.5 = Fair\n" +
                                "75-79  = 3.0 = Passed\n" +
                                "Below 75 = 5.0 = Failed";
    
            PdfPCell leftGradingCell = createGradingSystemCell(leftGrading);
            PdfPCell rightGradingCell = createGradingSystemCell(rightGrading);
    
            gradingSystemsTable.addCell(leftGradingCell);
            gradingSystemsTable.addCell(rightGradingCell);
            document.add(gradingSystemsTable);
    
            // Add FCF Grades information
            Paragraph fcfPara = new Paragraph("FOR FCF Grades: O - Outstanding, HS - Highly Satisfactory, MS - Moderately Satisfactory, S - Satisfactory, F - Fair, P - Poor", NORMAL_FONT);
            fcfPara.setAlignment(Element.ALIGN_LEFT);
            fcfPara.setSpacingAfter(5f);
            document.add(fcfPara);
    
            // Add Quality Point information
            Paragraph qualityPara = new Paragraph("Quality Point Equivalent: 1.0 = 4.0, 1.5 = 3.5, 2.0 = 3.0, 2.5 = 2.5, 3.0 = 2.0", NORMAL_FONT);
            qualityPara.setAlignment(Element.ALIGN_LEFT);
            qualityPara.setSpacingAfter(15f);
            document.add(qualityPara);
    
            // Add "NOT VALID WITHOUT SCHOOL SEAL"
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Paragraph sealPara = new Paragraph("NOT VALID WITHOUT SCHOOL SEAL", boldFont);
            sealPara.setAlignment(Element.ALIGN_LEFT);
            sealPara.setSpacingAfter(20f);
            document.add(sealPara);
    
            // Add signature tables
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(10f);
            signatureTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    
            // Left signature table - Prepared By
            PdfPTable leftSignature = new PdfPTable(1);
            leftSignature.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            
            Font underlineFont = new Font(Font.FontFamily.HELVETICA, 10, Font.UNDERLINE);
            
            leftSignature.addCell(new Phrase("Prepared By: SAFIRE X. ZIKALY", underlineFont));
            leftSignature.addCell(new Phrase(" ")); // Empty space
            leftSignature.addCell(new Phrase("Date: " + getCurrentDate(), NORMAL_FONT));
            
            PdfPCell leftSigCell = new PdfPCell(leftSignature);
            leftSigCell.setBorder(Rectangle.NO_BORDER);
            leftSigCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    
            // Right signature table - Checked By and Registrar
            PdfPTable rightSignature = new PdfPTable(1);
            rightSignature.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            
            rightSignature.addCell(new Phrase("Checked By: TETO KASANE", underlineFont));
            rightSignature.addCell(new Phrase("Registrar: DR. BIWA HAYAHIDE", underlineFont));
            
            PdfPCell rightSigCell = new PdfPCell(rightSignature);
            rightSigCell.setBorder(Rectangle.NO_BORDER);
            rightSigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    
            signatureTable.addCell(leftSigCell);
            signatureTable.addCell(rightSigCell);
            document.add(signatureTable);
    
            document.close();
            return file;
        }
    }
    
    // Helper method to create grading system cells
    private static PdfPCell createGradingSystemCell(String content) {
        Paragraph paragraph = new Paragraph();
        String[] lines = content.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            Font font = (i == 0) ? HEADER_FONT : NORMAL_FONT; // First line is header
            paragraph.add(new Phrase(lines[i] + (i < lines.length - 1 ? "\n" : ""), font));
        }
        
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5f);
        return cell;
    }
    
    // Helper method to get current date in MM/DD/YYYY format
    private static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        return String.format("%02d/%02d/%04d", month, day, year);
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
        "IFNULL(g.Prelim, 'NULL') as Prelim, " +
        "IFNULL(g.Midterm, 'NULL') as Midterm, " +
        "IFNULL(g.Final, 'NULL') as Final " +
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

    private static Phrase createBoldLabel(String label, String value) {
        Phrase phrase = new Phrase();
        
        // Create bold font explicitly
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Chunk labelChunk = new Chunk(label + ": ", boldFont);
        phrase.add(labelChunk);
        
        // Create normal font explicitly
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Chunk valueChunk = new Chunk(value != null ? value : "N/A", normalFont);
        phrase.add(valueChunk);
        
        return phrase;
    }
}
