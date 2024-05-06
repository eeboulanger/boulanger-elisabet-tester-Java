package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static junit.framework.Assert.*;

public class TicketDaoIT {

    private static final TicketDAO ticketDAO = new TicketDAO();
    private static Ticket ticket;
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;

    @BeforeAll
    public static void setUp() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        ticketDAO.dataBaseConfig = dataBaseTestConfig;

        dataBasePrepareService = new DataBasePrepareService();
        dataBasePrepareService.createTestDatabase();
        dataBasePrepareService.createAndPopulateTableParking();
        dataBasePrepareService.createTableTicket();
    }

    @BeforeEach
    public void setupPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void saveTicketTest() {
        ticketDAO.saveTicket(ticket);
        Ticket savedTicket = ticketDAO.getTicket("ABCDEF");

        assertEquals(1, savedTicket.getId());
        assertNull(savedTicket.getOutTime());
    }

    @Test
    public void updateTicketTest() {
        ticketDAO.saveTicket(ticket);

        Ticket updateTicket = new Ticket();
        updateTicket.setPrice(1.5);
        updateTicket.setOutTime(new Date());
        updateTicket.setId(1);

        boolean isUpdated = ticketDAO.updateTicket(updateTicket);
        Ticket result = ticketDAO.getTicket("ABCDEF");

        assertTrue(isUpdated);
        assertEquals(1.5, result.getPrice());
    }

    @Test
    public void updateTicketFailsTest() {
        ticketDAO.saveTicket(ticket);

        boolean isUpdated = ticketDAO.updateTicket(ticket);

        Ticket result = ticketDAO.getTicket("ABCDEF");
        assertFalse(isUpdated);

    }

    @Test
    public void getNbTicketsTest() {
        dataBasePrepareService.populateTicketToDiscountCustomer();

        int result = ticketDAO.getNbTicket("ABCDEF");

        assertEquals(2, result);
    }
}
