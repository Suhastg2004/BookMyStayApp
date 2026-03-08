//author @ Suhas T G
//version 6.0 

package com.bookmystayapp;

import com.bookmystayapp.inventory.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {

            // ---- UC1: Inventory ----
            InventoryService inventory = new InventoryService();
            inventory.addRoomType("Single", 5, 2000);
            inventory.addRoomType("Double", 3, 3500);
            inventory.addRoomType("Suite", 2, 6000);

            System.out.println("Room Inventory:");
            inventory.displayInventory();

            // ---- UC2: Search (optional, read-only) ----
            SearchService search = new SearchService(inventory);
            System.out.print("\nEnter Room Type to Search (press Enter to skip): ");
            String type = sc.nextLine().trim();
            if (!type.isEmpty()) {
                search.searchRoom(type);
            }

            // ---- UC3: Booking Queue (FIFO) ----
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

                Reservation reservation = new Reservation(id, roomType);
                queue.addBookingRequest(reservation);
            }

            // ---- UC4: Reservation Confirmation ----
            BookingService bookingService = new BookingService(inventory);

            // ---- UC6: Booking History ----
            BookingHistory history = new BookingHistory();
            List<Reservation> confirmed = new ArrayList<>(); // for simple reporting

            System.out.println("\nProcessing Booking Requests (FIFO):");
            Reservation r;
            while ((r = queue.getNextRequest()) != null) {
                System.out.println("\nProcessing Reservation: " + r.getReservationId());

                // Check before & after to determine success (no change to BookingService required)
                String rt = r.getRoomType().trim();
                int before = inventory.getAvailableRooms(rt);

                bookingService.confirmReservation(r);

                int after = inventory.getAvailableRooms(rt);
                boolean confirmedNow = after < before; // inventory decreased -> confirmed

                if (confirmedNow) {
                    history.addReservation(r);
                    confirmed.add(r);
                }

                try {
                    Thread.sleep(1000); // small simulated delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("\nUpdated Inventory:");
            inventory.displayInventory();

            // ---- UC5: Add-On Service Selection (optional) ----
            ServiceManager manager = new ServiceManager();
            System.out.println("\nAdd Services to a Reservation");

            System.out.print("Enter Reservation ID (press Enter to skip): ");
            String reservationId = sc.nextLine().trim();

            double addOnTotal = 0.0;
            if (!reservationId.isEmpty()) {
                while (true) {
                    System.out.print("Enter Service Name (press Enter to finish): ");
                    String serviceName = sc.nextLine().trim();
                    if (serviceName.isEmpty()) break;

                    System.out.print("Enter Service Price: ");
                    String priceStr = sc.nextLine().trim();
                    double price;
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid price. Skipping this service.");
                        continue;
                    }

                    Service service = new Service(serviceName, price);
                    manager.addService(reservationId, service);
                    addOnTotal += price;
                }

                System.out.println("\nServices for Reservation " + reservationId + ":");
                manager.showServices(reservationId);
                System.out.println("Total Add-on Cost: " + addOnTotal);
            }

            // ---- UC6: History & Reporting ----
            System.out.println("\nBooking History (confirmed only):");
            history.showHistory();

            // Simple report: counts per room type
            Map<String, Integer> counts = new HashMap<>();
            for (Reservation res : confirmed) {
                counts.put(res.getRoomType(),
                           counts.getOrDefault(res.getRoomType(), 0) + 1);
            }

            System.out.println("\nReport: Confirmed Bookings by Room Type");
            if (counts.isEmpty()) {
                System.out.println("No confirmed bookings yet.");
            } else {
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    System.out.println(e.getKey() + " : " + e.getValue());
                }
                System.out.println("Total Confirmed Bookings: " + confirmed.size());
            }
        }
    }
}