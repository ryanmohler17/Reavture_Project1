package com.ryan.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataConnector {

    private Properties props;

    public DataConnector(Properties props) {
        this.props = props;
        try {
            Class clazz = Class.forName(props.getProperty("db.driver_name"));
            Driver driver = (Driver) clazz.newInstance();
            DriverManager.registerDriver(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection newConnection() throws SQLException {
        return DriverManager.getConnection(props.getProperty("db.url"), props.getProperty("db.username"), props.getProperty("db.password"));
    }

}
