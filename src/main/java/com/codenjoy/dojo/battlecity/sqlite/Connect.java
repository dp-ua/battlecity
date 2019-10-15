package com.codenjoy.dojo.battlecity.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author sqlitetutorial.net
 */
public class Connect {
    /**
     * Connect to a sample database
     */
    public Connection getConnection(String url) throws SQLException {
        Connection conn = null;
            conn = DriverManager.getConnection(url);
        return conn;
    }
}