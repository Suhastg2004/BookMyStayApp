package com.bookmystayapp.inventory;

import java.util.*;

public class BookingHistory {

    private List<Reservation> reservations = new ArrayList<>();

    public void addReservation(Reservation r) {
        reservations.add(r);
    }

    public void showHistory() {

        for(Reservation r : reservations) {
            System.out.println("Reservation: "
                    + r.getReservationId()
                    + " Room Type: "
                    + r.getRoomType());
        }
    }
}
