package com.bookmystayapp.inventory;

import java.util.*;

public class BookingQueueService {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addBookingRequest(Reservation r) {
        queue.offer(r);
        System.out.println("Booking request added: " + r.getReservationId());
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }
}