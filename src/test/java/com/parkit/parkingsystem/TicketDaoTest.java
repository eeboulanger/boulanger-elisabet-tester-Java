package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
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
public class TicketDaoTest {

    @Mock
    DataBaseConfig config;
    @InjectMocks
    private TicketDAO ticketDAO = new TicketDAO();

    private final Ticket ticket = new Ticket();

    @BeforeEach
    public void setUp() throws SQLException, ClassNotFoundException {
        when(config.getConnection()).thenThrow(SQLException.class);
    }

    @Test
    public void saveTicketTest() {

        boolean result = ticketDAO.saveTicket(ticket);

        verify(config).closeConnection(any());
        assertFalse(result);
    }

    @Test
    public void getTicketTest() {

        Ticket result = ticketDAO.getTicket("ABCDEF");

        verify(config).closeConnection(any());
        assertNull(result);
    }

    @Test
    public void updateTicketTest() {

        boolean result = ticketDAO.updateTicket(ticket);

        verify(config).closeConnection(any());
        assertFalse(result);
    }

    @Test
    public void getNbTicketsTest() {

        int result = ticketDAO.getNbTicket("ABCDEF");

        verify(config).closeConnection(any());
        assertEquals(0, result);
    }
}
