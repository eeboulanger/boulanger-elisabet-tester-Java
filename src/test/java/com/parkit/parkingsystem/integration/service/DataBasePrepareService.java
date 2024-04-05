package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void createTestDatabase() {
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //create test database
            connection.prepareStatement("CREATE DATABASE IF NOT EXISTS test").execute();
            connection.prepareStatement("USE test").execute();

            //create table parking
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS parking (parking_number INT NOT NULL PRIMARY KEY,"+
                    "type VARCHAR(20) NOT NULL," +
                    "available VARCHAR(20) NOT NULL)").execute();

            //populate parking
            connection.prepareStatement("INSERT IGNORE INTO parking (parking_number, type, available) VALUES" +
                            "(1, 'CAR', true)," +
                            "(2, 'BIKE', true)").execute();

            //create table ticket
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS ticket (id INT AUTO_INCREMENT, " +
                    "parking_number INT NULL, " +
                    "vehicle_reg_number VARCHAR(255) NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "in_time DATETIME NOT NULL, " +
                    "out_time DATETIME," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY(parking_number) REFERENCES parking(parking_number) ON DELETE CASCADE)").execute();

        }catch(Exception e){
            clearDataBaseEntries(); //
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
}
