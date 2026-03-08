package com.bookmystayapp.inventory;

public class SearchService {

    private final InventoryService inventory;

    public SearchService(InventoryService inventory) {
        this.inventory = inventory;
    }

    // UC2: Search availability for a given room type (read-only)
    public void searchRoom(String type) {
        if (type == null || type.isBlank()) {
            System.out.println("Please enter a valid room type.");
            return;
        }
        String t = type.trim();

        int available = inventory.getAvailableRooms(t);
        double price  = inventory.getRoomPrice(t);

        if (available > 0) {
            System.out.println(t + " rooms available: " + available + " | Price: " + price);
        } else {
            System.out.println("No rooms available for " + t);
        }
    }

    // UC2: List available room types with price (read-only)
    public void listAvailableRooms() {
        System.out.println("\nAvailable Rooms:");
        boolean any = false;

        String[] types = {"Single", "Double", "Suite"};
        for (String t : types) {
            int count = inventory.getAvailableRooms(t);
            if (count > 0) {
                any = true;
                System.out.println(t + " → " + count + " rooms | Price: " + inventory.getRoomPrice(t));
            }
        }

        if (!any) {
            System.out.println("No rooms available right now.");
        }
    }

    // UC2 helper: prevent booking when unavailable
    public boolean isAvailable(String type) {
        return type != null && !type.isBlank()
                && inventory.getAvailableRooms(type.trim()) > 0;
    }
}