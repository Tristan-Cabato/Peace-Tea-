package com.mycompany.esystem_copy; // GotChanged
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ESystem {
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    static String db;
    static String uname;
    static String pswd;
       
    public static void main(String[] args) {
        DBConnect();
        StudentsForm SForm = new StudentsForm();
        SForm.setVisible(true);
        SForm.showRecords();
    }
    public static void DBConnect() {
        try {
            db = "2ndSem_Sy2025_2026"; // GotChanged
            uname = "root";
            pswd = "safi";
            // Use the actual password, this messed me up
            
            Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://192.168.1.193:3306/" + db + "?ZeroDateTimeBehavior=convertToNull", uname, pswd);
                // con is not finished, and the IP used is the VM IP, just look to the left on Services
                // GotChanged
                st = con.createStatement();
                System.out.println("Connected");
        } catch (Exception ex) { System.out.println("Failed to Connect: " + ex); }
    }
}
