package com.bookmystayapp.inventory;

import java.util.*;

public class BookingService {

    private Set<String> bookedRooms = new HashSet<>();
    private Map<String, Set<String>> roomAssignments = new HashMap<>();
    private InventoryService inventory;

    public BookingService(InventoryService inventory) {
        this.inventory = inventory;
    }

    public void confirmReservation(Reservation r) {

        String type = r.getRoomType();
        int available = inventory.getAvailableRooms(type);

        if(available == 0) {
            System.out.println("No rooms available");
            return;
        }

        String roomId = type + "-" + (available);

        bookedRooms.add(roomId);

        roomAssignments
                .computeIfAbsent(type, k -> new HashSet<>())
                .add(roomId);

        inventory.updateRoomCount(type, available - 1);

        System.out.println("Reservation confirmed. Room ID: " + roomId);
    }
}