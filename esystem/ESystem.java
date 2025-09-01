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
    public static String hostAddress = "192.168.1.193";
    public static String usedHostAddress = "LAPTOP-7AGILLAO";
        // Check the terminal upon running
    public static int hostPort = 3306;
    
    public static void main(String[] args) {
        Login loginForm = new Login();
        loginForm.setVisible(true);
    }
    
    public static boolean DBConnect(String db, String username, String password) {
        try {
            if (con != null && !con.isClosed()) {
                try { con.close(); } catch (SQLException e) {}
            } Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format(
                "jdbc:mysql://%s:%d/%s?zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&allowPublicKeyRetrieval=true",
                hostAddress, hostPort, db
            );
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useSSL", "false");
            
            con = DriverManager.getConnection(url, props);
                try {
                    System.out.println("Connected as: " + ESystem.con.getMetaData().getUserName());
                } catch (SQLException e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                }
            st = con.createStatement();
            st.execute("SELECT 1");
            
            currentDB = db;
            currentUser = username;
            
            System.out.println("Successfully connected to database: " + db);
            return true;
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            return false;
        }
    }
}
