package com.bookmystayapp.tests;

import com.bookmystayapp.inventory.*;

import java.util.Set;

public class SimpleTests {

    // Minimal check helpers
    private static int PASS = 0, FAIL = 0;
    private static void check(boolean condition, String message) {
        if (condition) {
            PASS++;
            System.out.println("PASS: " + message);
        } else {
            FAIL++;
            System.out.println("FAIL: " + message);
        }
    }
    
    private static void header(String title) {
        System.out.println("\n==== " + title + " ====");
    }
    
    private static void summary() {
        System.out.println("\n==================================");
        System.out.println("Summary -> Passed: " + PASS + " | Failed: " + FAIL);
        System.out.println("==================================\n");
    }

    public static void main(String[] args) {
        testInventoryService();     // UC1
        testSearchService();        // UC2
        testBookingQueueService();  // UC3
        testBookingService();       // UC4
        testServiceManager();       // UC5
        testMiniFlow();             // UC3+UC4 integration

        summary();
    }

    // ---------------- UC1: InventoryService ----------------
    private static void testInventoryService() {
        header("UC1: InventoryService");

        InventoryService inv = new InventoryService();

        inv.addRoomType("Single", 5, 2000.0);
        check(inv.getAvailableRooms("Single") == 5, "addRoomType: count stored");
        check(Math.abs(inv.getRoomPrice("Single") - 2000.0) < 1e-6, "addRoomType: price stored");

        check(inv.getAvailableRooms("Unknown") == 0, "unknown type count default 0");
        check(Math.abs(inv.getRoomPrice("Unknown") - 0.0) < 1e-6, "unknown type price default 0.0");

        inv.addRoomType("Suite", 2, 6000.0);
        inv.updateRoomCount("Suite", 1);
        check(inv.getAvailableRooms("Suite") == 1, "updateRoomCount: replaces value");

        inv.addRoomType("Double", 3, 3500.0);
        inv.addRoomType("Double", 10, 4200.0);
        check(inv.getAvailableRooms("Double") == 10, "overwrite count for same type");
        check(Math.abs(inv.getRoomPrice("Double") - 4200.0) < 1e-6, "overwrite price for same type");

        inv.addRoomType("Economy", 1, 1500.0);
        Set<String> types = inv.getRoomTypes();
        check(types.contains("Economy") && types.contains("Single") && types.contains("Suite") && types.contains("Double"),
                "getRoomTypes contains added keys");

        // Note: negative price allowed by current code (no validation)
        inv.addRoomType("ErrorType", 1, -100.0);
        check(Math.abs(inv.getRoomPrice("ErrorType") + 100.0) < 1e-6, "negative price currently allowed (known gap)");
    }

    // ---------------- UC2: SearchService ----------------
    private static void testSearchService() {
        header("UC2: SearchService");

        InventoryService inv = new InventoryService();
        inv.addRoomType("Single", 5, 2000.0);
        inv.addRoomType("Suite", 0, 6000.0);

        SearchService search = new SearchService(inv);

        // We verify behavior using isAvailable (returns boolean)
        check(search.isAvailable("Single"), "isAvailable true for positive counts");
        check(!search.isAvailable("Suite"), "isAvailable false for zero counts");
        check(!search.isAvailable(null), "isAvailable false for null");
        check(!search.isAvailable("  "), "isAvailable false for blank");

        // Optional manual eyeballing (no assertions) — simple console feedback
        System.out.println("Manual check (no assert): following lines should describe availability & price:");
        search.searchRoom("Single");       // expect: available 5, price 2000.0
        search.searchRoom("Suite");        // expect: no rooms available
        search.searchRoom("   ");          // expect: 'Please enter a valid room type.'
        search.searchRoom("  Single  ");   // expect: trims and shows same as Single

        System.out.println("Manual check (known limitation): listAvailableRooms uses static types (won't show 'Economy')");
        inv.addRoomType("Economy", 4, 1000.0);
        search.listAvailableRooms();       // won't list "Economy" in current implementation
    }

    // ---------------- UC3: BookingQueueService ----------------
    private static void testBookingQueueService() {
        header("UC3: BookingQueueService (FIFO)");

        BookingQueueService q = new BookingQueueService();
        q.addBookingRequest(new Reservation("R1", "Single"));
        q.addBookingRequest(new Reservation("R2", "Double"));
        q.addBookingRequest(new Reservation("R3", "Suite"));

        Reservation a = q.getNextRequest();
        Reservation b = q.getNextRequest();
        Reservation c = q.getNextRequest();
        Reservation d = q.getNextRequest();

        check(a != null && "R1".equals(a.getReservationId()), "FIFO #1 == R1");
        check(b != null && "R2".equals(b.getReservationId()), "FIFO #2 == R2");
        check(c != null && "R3".equals(c.getReservationId()), "FIFO #3 == R3");
        check(d == null, "Queue empty returns null");

        // Duplicate IDs allowed (queue semantics)
        q.addBookingRequest(new Reservation("R1", "Single"));
        q.addBookingRequest(new Reservation("R1", "Single"));
        check("R1".equals(q.getNextRequest().getReservationId()) &&
              "R1".equals(q.getNextRequest().getReservationId()) &&
               q.getNextRequest() == null,
              "Duplicates preserved in FIFO order");
    }

    // ---------------- UC4: BookingService ----------------
    private static void testBookingService() {
        header("UC4: BookingService");

        // BKG-01: success path decrements inventory
        InventoryService inv1 = new InventoryService();
        inv1.addRoomType("Single", 5, 2000.0);
        BookingService svc1 = new BookingService(inv1);

        svc1.confirmReservation(new Reservation("A", "Single")); // prints: Room ID: Single-5
        check(inv1.getAvailableRooms("Single") == 4, "confirmReservation decrements by 1");

        // BKG-02: zero stock -> no change
        InventoryService inv2 = new InventoryService();
        inv2.addRoomType("Suite", 0, 6000.0);
        BookingService svc2 = new BookingService(inv2);

        svc2.confirmReservation(new Reservation("B", "Suite"));   // prints: No rooms available
        check(inv2.getAvailableRooms("Suite") == 0, "no decrement when 0 stock");

        // BKG-03: multiple confirms result in counts to 0
        InventoryService inv3 = new InventoryService();
        inv3.addRoomType("Double", 3, 3500.0);
        BookingService svc3 = new BookingService(inv3);

        svc3.confirmReservation(new Reservation("C1", "Double")); // expected ID Double-3
        svc3.confirmReservation(new Reservation("C2", "Double")); // expected ID Double-2
        svc3.confirmReservation(new Reservation("C3", "Double")); // expected ID Double-1
        check(inv3.getAvailableRooms("Double") == 0, "three confirms exhaust inventory");

        // BKG-04: unknown type behaves like zero stock
        InventoryService inv4 = new InventoryService();
        BookingService svc4 = new BookingService(inv4);
        svc4.confirmReservation(new Reservation("X", "Presidential"));
        check(inv4.getAvailableRooms("Presidential") == 0, "unknown type remains 0");

        // BKG-06: case sensitivity (current behavior)
        InventoryService inv5 = new InventoryService();
        inv5.addRoomType("Single", 1, 2000.0);
        BookingService svc5 = new BookingService(inv5);
        svc5.confirmReservation(new Reservation("low", "single")); // lower-case
        check(inv5.getAvailableRooms("Single") == 1, "case-sensitive types (no change for 'single')");
    }

    // ---------------- UC5: ServiceManager ----------------
    private static void testServiceManager() {
        header("UC5: ServiceManager");

        ServiceManager mgr = new ServiceManager();
        mgr.addService("R100", new Service("Breakfast", 300.0));
        mgr.addService("R100", new Service("Airport Pickup", 1200.0));

        System.out.println("Manual check (no assert): should list two services for R100:");
        mgr.showServices("R100"); // Expect two lines: "Breakfast 300.0" and "Airport Pickup 1200.0"

        System.out.println("Manual check (no assert): unknown reservation prints nothing:");
        mgr.showServices("UNKNOWN");
    }

    // ---------------- Tiny end-to-end flow (UC3 + UC4) ----------------
    private static void testMiniFlow() {
        header("Mini Flow: Queue -> Confirm -> Inventory");

        InventoryService inv = new InventoryService();
        inv.addRoomType("Single", 2, 2000.0);
        inv.addRoomType("Suite", 1, 6000.0);

        BookingQueueService q = new BookingQueueService();
        q.addBookingRequest(new Reservation("R1", "Single"));
        q.addBookingRequest(new Reservation("R2", "Suite"));
        q.addBookingRequest(new Reservation("R3", "Single"));

        BookingService booking = new BookingService(inv);

        Reservation r;
        while ((r = q.getNextRequest()) != null) {
            booking.confirmReservation(r);
        }

        check(inv.getAvailableRooms("Single") == 0, "Single exhausted after two confirmations");
        check(inv.getAvailableRooms("Suite") == 0, "Suite exhausted after one confirmation");
        check(q.getNextRequest() == null, "Queue is empty at the end");
    }
}