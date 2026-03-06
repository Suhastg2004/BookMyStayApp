package com.bookmystayapp.inventory;

public class Room {
    private String roomId;
    private String roomType;
    private double price;

    public Room(String roomId, String roomType, double price) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.price = price;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPrice() {
        return price;
    }
}