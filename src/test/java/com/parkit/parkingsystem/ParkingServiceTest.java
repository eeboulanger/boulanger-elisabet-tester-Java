package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Discount;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private Ticket ticket;

    private String[] lines;

    @BeforeEach
    public void setUp(){
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

   @Nested
    public class IncomingAndExitingVehicleTests {
       @BeforeEach
       public void setUpPerTest() {
           try {
               when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

               ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
               ticket = new Ticket();
               ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
               ticket.setParkingSpot(parkingSpot);
               ticket.setVehicleRegNumber("ABCDEF");

               System.setOut(new PrintStream(outputStreamCaptor));

           } catch (Exception e) {
               e.printStackTrace();
               throw new RuntimeException("Failed to set up test mock objects");
           }
       }

       @AfterEach
       public void tearDown() {
           System.setOut(standardOut);
       }

       @Test
       public void processExitingVehicleTest() {
           when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
           when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
           when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
           when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

           parkingService.processExitingVehicle();

           lines = outputStreamCaptor.toString().split(System.lineSeparator());
           String lastLine = lines[lines.length - 1];

           verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
           verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
           verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
           verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
           assertEquals("Recorded out-time for vehicle number:ABCDEF is:" + new Date(), lastLine
                   .trim());
       }

       @Test
       public void testProcessIncomingVehicle() {
           when(inputReaderUtil.readSelection()).thenReturn(1);
           when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
           when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
           when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
           when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

           parkingService.processIncomingVehicle();

           lines = outputStreamCaptor.toString().split(System.lineSeparator());
           String lastLine = lines[lines.length - 1];

           verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
           verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
           verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
           verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
           assertEquals("Recorded in-time for vehicle number:ABCDEF is:" + new Date(), lastLine
                   .trim());
       }

       @Test
       public void testProcessIncomingVehicleWithDiscount(){
           when(inputReaderUtil.readSelection()).thenReturn(1);
           when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(Discount.MIN_PASSAGES + 1);
           when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
           when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
           when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

           parkingService.processIncomingVehicle();

           lines = outputStreamCaptor.toString().split(System.lineSeparator());

           assertEquals("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, " +
                   "vous allez obtenir une remise de 5%", lines[lines.length - 4].trim());
       }

       @Test
       public void processExitingVehicleTestUnableUpdate() {
           when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
           when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

           parkingService.processExitingVehicle();

           lines = outputStreamCaptor.toString().split(System.lineSeparator());
           String lastLine = lines[lines.length - 1];

           verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
           verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
           verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
           verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));
           assertEquals("Unable to update ticket information. Error occurred", lastLine
                   .trim());
       }
   }

   @Nested
    public class GetNextParkingNumberTests {

       @Test
       public void testGetNextParkingNumberIfAvailable(){
           when(inputReaderUtil.readSelection()).thenReturn(1);
           when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

           ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

           verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
           assertEquals(1, parkingSpot.getId());
           assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
           assertTrue(parkingSpot.isAvailable());
       }

       @Test
       public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
           when(inputReaderUtil.readSelection()).thenReturn(2);
           when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(0);

           ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

           verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.BIKE);
           assertNull(parkingSpot);
       }

       @Test
       public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
           when(inputReaderUtil.readSelection()).thenReturn(3);

           ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

           assertNull(parkingSpot); //Entered input is invalid
       }
   }
}
