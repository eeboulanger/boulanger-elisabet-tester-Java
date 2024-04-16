package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void createTestDatabase() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();

            //create test database
            connection.prepareStatement("CREATE DATABASE IF NOT EXISTS test").execute();
            connection.prepareStatement("USE test").execute();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

    public void createAndPopulateTableParking() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();

            //create table parking
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS parking (parking_number INT NOT NULL PRIMARY KEY," +
                    "type VARCHAR(20) NOT NULL," +
                    "available TINYINT(1) NOT NULL)").execute();

            //populate parking
            PreparedStatement preparedStatement = connection.prepareStatement
                    ("INSERT IGNORE INTO parking (parking_number, type, available) VALUES (?,?,?)");

            //CAR
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "CAR");
            preparedStatement.setInt(3, 1);
            preparedStatement.addBatch();

            //BIKE
            preparedStatement.setInt(1, 2);
            preparedStatement.setString(2, "BIKE");
            preparedStatement.setInt(3, 1);
            preparedStatement.addBatch();

            preparedStatement.executeBatch();

            dataBaseTestConfig.closePreparedStatement(preparedStatement);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }


    public void createTableTicket() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();

            //create table ticket
            PreparedStatement preparedStatement =
                    connection.prepareStatement("CREATE TABLE IF NOT EXISTS ticket (id INT AUTO_INCREMENT PRIMARY KEY NOT NULL, " +
                            "parking_number INT NOT NULL, " +
                            "vehicle_reg_number VARCHAR(255) NOT NULL, " +
                            "price DECIMAL(10,2) NOT NULL, " +
                            "in_time DATETIME NOT NULL, " +
                            "out_time DATETIME NULL," +
                            "FOREIGN KEY(parking_number) REFERENCES parking(parking_number) ON DELETE CASCADE)");

            preparedStatement.execute();

            dataBaseTestConfig.closePreparedStatement(preparedStatement);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

    public void populateWithTicketInTimeOneHourAgo() {
        Connection connection = null;

        try {
            connection = dataBaseTestConfig.getConnection();

            //save ticket with in time one hour ago
            Timestamp inTime = new Timestamp(new Date(System.currentTimeMillis() - (60 * 60 * 1000)).getTime());

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ticket(parking_number, vehicle_reg_number, price, in_time, out_time) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "ABCDEF");
            preparedStatement.setDouble(3, 0.0);
            preparedStatement.setTimestamp(4, inTime);
            preparedStatement.setTimestamp(5, null);

            preparedStatement.execute();

            dataBaseTestConfig.closePreparedStatement(preparedStatement);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            dataBaseTestConfig.closeConnection(connection);

        }
    }

    public void populateTicketToDiscountCustomer() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();

            //save old ticket
            Timestamp inTime = new Timestamp(new Date(System.currentTimeMillis() - (180 * 60 * 1000)).getTime());
            Timestamp outTime = new Timestamp(new Date(System.currentTimeMillis() - (120 * 60 * 1000)).getTime());

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ticket(parking_number, vehicle_reg_number, price, in_time, out_time) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "ABCDEF");
            preparedStatement.setDouble(3, 1.5);
            preparedStatement.setTimestamp(4, inTime);
            preparedStatement.setTimestamp(5, outTime);
            preparedStatement.addBatch();

            //create new ticket without checkout
            inTime = new Timestamp(new Date(System.currentTimeMillis() - (60 * 60 * 1000)).getTime());
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "ABCDEF");
            preparedStatement.setDouble(3, 0.00);
            preparedStatement.setTimestamp(4, inTime);
            preparedStatement.setTimestamp(5, null);
            preparedStatement.addBatch();

            preparedStatement.executeBatch();

            dataBaseTestConfig.closePreparedStatement(preparedStatement);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

    public void clearDataBaseEntries() {
        Connection connection = null;
        try {
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
}
