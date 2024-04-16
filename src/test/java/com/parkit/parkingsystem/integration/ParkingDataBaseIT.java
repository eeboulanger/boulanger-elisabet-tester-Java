package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Discount;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;

        dataBasePrepareService = new DataBasePrepareService();
        dataBasePrepareService.createTestDatabase();
        dataBasePrepareService.createAndPopulateTableParking();
        dataBasePrepareService.createTableTicket();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    @DisplayName("Given there is an available parking spot, when incoming vehicle, then save new ticket and update available parking spot")
    public void testParkingACar(){
        //Given there is available parking spot
        when(inputReaderUtil.readSelection()).thenReturn(1);

        //When incoming vehicle
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        //Then save a ticket with in time, reg number and parking spot in DB and update parking spot to not available
        Ticket ticket = ticketDAO.getTicket("ABCDEF"); //get ticket for reg nr
        int parkingSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertNotNull(ticket.getInTime());
        assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
        assertEquals(1, ticket.getParkingSpot().getId()); //parking spot has number 1
        assertEquals(0, parkingSpot); //there was only one free parking spot for cars
    }

    @Test
    @DisplayName("Given a car has been parked for 1 hour, when checking out, then the fare should be for 1 hour and out time saved")
    public void testParkingLotExit(){
        //Given there is a ticket with an in time 1 hour ago
        dataBasePrepareService.populateWithTicketInTimeOneHourAgo();

        //When checking out
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        //Then the fare should be for one hour and the out time be set with current time
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime outTime = ticket.getOutTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.MINUTES);

        assertEquals(Fare.CAR_RATE_PER_HOUR * 1, ticket.getPrice() );
        assertNotNull(ticket.getOutTime());
        assertEquals(now, outTime);
    }

    @Test
    @DisplayName("Given a user has discount, when exiting vehicle, then 5% discount should be applied to the price")
    public void testParkingLotExitRecurringUser(){
        //Given a customer is eligible for discount (1 previous ticket)
        dataBasePrepareService.populateTicketToDiscountCustomer();

        //When exiting vehicle with discount
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        //Then there should be a minimum of tickets with same reg nr to be eligible for discount, and the price should be reduced with 5% for the last ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        int eligibleForDiscount = Discount.MIN_PASSAGES +1; //2

        double discountPrice = Fare.CAR_RATE_PER_HOUR * Discount.DISCOUNT;
        double expectedPrice = BigDecimal.valueOf(discountPrice).setScale(2, RoundingMode.HALF_UP).doubleValue();

        assertEquals(eligibleForDiscount,ticketDAO.getNbTicket("ABCDEF"));
        assertEquals(expectedPrice, ticket.getPrice());
    }
}
