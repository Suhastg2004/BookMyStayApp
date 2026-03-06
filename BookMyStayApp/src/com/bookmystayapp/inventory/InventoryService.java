package com.bookmystayapp.inventory;

import java.util.*;

public class InventoryService {

    private Map<String, Integer> roomCount = new HashMap<>();
    private Map<String, Double> roomPrice = new HashMap<>();

    public void addRoomType(String type, int count, double price) {
        roomCount.put(type, count);
        roomPrice.put(type, price);
    }

    public void updateRoomCount(String type, int count) {
        roomCount.put(type, count);
    }

    public int getAvailableRooms(String type) {
        return roomCount.getOrDefault(type, 0);
    }

    public double getRoomPrice(String type) {
        return roomPrice.getOrDefault(type, 0.0);
    }

    public void displayInventory() {
        for(String type : roomCount.keySet()) {
            System.out.println(type + " Rooms: " + roomCount.get(type)
                    + " Price: " + roomPrice.get(type));
        }
    }
}
