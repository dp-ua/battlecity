package com.codenjoy.dojo.battlecity.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBService {
    Connection connection;


    public void insert(String name, String command, int point, double coef) {
        if (isConnected()) {
            String sql = "INSERT INTO detectors(name,command,point,coef) VALUES(?,?,?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setString(2, command);
                pstmt.setInt(3, point);
                pstmt.setDouble(4, coef);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public boolean isConnected() {
        return (connection != null);
    }

    public DBService() {
        init();
    }

    private void init() {
        String currentDir = System.getProperty("user.dir");
        String jdbc = "jdbc:sqlite:" + currentDir + "/db/battle-city.db";
        Connect connect = new Connect();
        try {
            Connection connection = connect.getConnection(jdbc);
            this.connection = connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
