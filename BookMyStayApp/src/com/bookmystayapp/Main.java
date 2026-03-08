//author @ Suhas T G
//version 3.0

package com.bookmystayapp;

import com.bookmystayapp.inventory.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();

        // UC1: Initialize inventory
        inventory.addRoomType("Single", 5, 2000);
        inventory.addRoomType("Double", 3, 3500);
        inventory.addRoomType("Suite", 2, 6000);

        System.out.println("Room Inventory:");
        inventory.displayInventory();

        try (Scanner sc = new Scanner(System.in)) {
            // UC2: Simple search (optional)
            SearchService search = new SearchService(inventory);
            System.out.print("\nEnter Room Type to Search (press Enter to skip): ");
            String typeToSearch = sc.nextLine().trim();
            if (!typeToSearch.isEmpty()) {
                search.searchRoom(typeToSearch);
            }

            // UC3: Collect booking requests (FIFO)
            BookingQueueService queue = new BookingQueueService();

            System.out.print("\nEnter number of booking requests: ");
            int n = 0;
            try {
                n = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. No requests added.");
            }

            for (int i = 1; i <= n; i++) {
                System.out.println("\nBooking Request " + i);

                System.out.print("Enter Reservation ID: ");
                String id = sc.nextLine().trim();

                System.out.print("Enter Room Type: ");
                String roomType = sc.nextLine().trim();

                Reservation r = new Reservation(id, roomType);
                queue.addBookingRequest(r);
            }

            // -------- UC4 : Reservation Confirmation --------
            BookingService bookingService = new BookingService(inventory);

            System.out.println("\nProcessing Booking Requests (FIFO):");
            Reservation r;
            while ((r = queue.getNextRequest()) != null) {
                System.out.println("\nProcessing Reservation: " + r.getReservationId());
                bookingService.confirmReservation(r);

                try {
                    Thread.sleep(3000); // simulate processing delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Processing interrupted.");
                }
            }

            System.out.println("\nUpdated Inventory:");
            inventory.displayInventory();
        }
    }
}