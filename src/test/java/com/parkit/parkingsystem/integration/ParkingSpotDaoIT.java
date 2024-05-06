package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static junit.framework.Assert.*;

public class ParkingSpotDaoIT {

    private static final ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static final DataBasePrepareService dataBasePrepareService = new DataBasePrepareService();

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;

        dataBasePrepareService.createTestDatabase();
        dataBasePrepareService.createAndPopulateTableParking();
    }

    @BeforeEach
    public void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void getNextAvailableSlotBikeTest() {
        ParkingType bike = ParkingType.BIKE;

        int result = parkingSpotDAO.getNextAvailableSlot(bike);

        assertEquals(2, result);
    }

    @Test
    public void getNextAvailableSlotCarTest() {
        ParkingType car = ParkingType.CAR;

        int result = parkingSpotDAO.getNextAvailableSlot(car);

        assertEquals(1, result);
    }

    @Test
    public void getNextAvailableSlotWrongTypeTest() {
        ParkingType bike = null;

        int result = parkingSpotDAO.getNextAvailableSlot(bike);

        assertEquals(-1, result);
    }


    @Test
    public void updateParkingSpotTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
    }

    @Test
    public void updateParkingSpotFailsTest() {
        ParkingSpot parkingSpot = null;

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertFalse(result);
    }
}
