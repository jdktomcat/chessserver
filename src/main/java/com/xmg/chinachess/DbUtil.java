package com.xmg.chinachess;

import java.sql.*;

public class DbUtil {

    private String driver;
    private String path;
    private String user;
    private String pwd;
    private Connection conn;

    public static void main(String[] args) {
        User user = new User("小明哥", "3353255", "1");
        UserDao udDao = new UserDao();
        try {
            udDao.sign(user);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public DbUtil() {
        driver = "com.mysql.jdbc.Driver";
        path = "jdbc:mysql://127.0.0.1:3306/chinachess?useUnicode=true&characterEncoding=utf8";
        user = "root";
        pwd = "123456";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
        }
        try {
            conn = DriverManager.getConnection(path, user, pwd);
        } catch (SQLException e) {
        }
    }

    public ResultSet executeQuerry(String sql) throws Exception {
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        return rs;
    }

    public void execute(String sql) throws Exception {
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.execute();
    }

    public void close() {
        try {
            conn.close();
        } catch (Exception e) {
        }
    }
}
