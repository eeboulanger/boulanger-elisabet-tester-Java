package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Discount;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        double duration = (double) (outHour - inHour) / (60 * 60 * 1000);

        if (duration < 0.5) {
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(roundTwoDecimals(duration * Fare.CAR_RATE_PER_HOUR));
                    break;
                }
                case BIKE: {
                    ticket.setPrice(roundTwoDecimals(duration * Fare.BIKE_RATE_PER_HOUR));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
            if (discount) {
                ticket.setPrice(roundTwoDecimals(ticket.getPrice() * Discount.DISCOUNT)); //Apply 5% discount
            }
        }
    }

    public double roundTwoDecimals(double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}