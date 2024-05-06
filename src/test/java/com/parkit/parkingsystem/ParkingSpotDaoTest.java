package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static junit.framework.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingSpotDaoTest {
    @Mock
    private DataBaseConfig config;
    @InjectMocks
    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    public void setUp() throws SQLException, ClassNotFoundException {
        when(config.getConnection()).thenThrow(SQLException.class);
    }

    @Test
    public void getNextAvailableParkingSpotTest() {

        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);

        verify(config).closeConnection(any());
        assertEquals(-1, result);
    }

    @Test
    public void updateParkingSpotTest() {

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, true);
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        verify(config).closeConnection(any());
        assertFalse(result);
    }
}
