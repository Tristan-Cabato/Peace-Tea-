package com.mycompany.esystem;
import java.sql.*;


public class ESystem {
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    public static String currentDB;
    public static String currentUser;
    
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
                "jdbc:mysql://10.4.44.47:3306/%s?zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&allowPublicKeyRetrieval=true",
                db
            );
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useSSL", "false"); // idk what this is
            
            con = DriverManager.getConnection(url, props);
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
