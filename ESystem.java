package com.mycompany.esystem;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ESystem {
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    public static String currentDB;
    public static String currentUser;
    public static String hostAddress = "10.4.44.47";
    public static String usedHostAddress = "10.4.44.153";
        // Check the terminal upon running
    public static int hostPort = 3306;
    
    public static void main(String[] args) {
        Login loginForm = new Login();
        loginForm.setVisible(true);
    }
    
     // In ESystem.java, modify the connection logic
    public static boolean DBConnect(String dbName, String username, String password) {
        try {
            // Close previous connection if exists
            if (con != null) con.close();
            
            // Create new connection with the provided credentials
            String url = "jdbc:mysql://" + hostAddress + ":" + hostPort + "/" + dbName;
            con = DriverManager.getConnection(url, username, password);
            st = con.createStatement();
            currentDB = dbName;
            currentUser = username;
            return true;
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }
}
