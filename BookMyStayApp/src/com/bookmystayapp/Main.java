//author @ Suhas T G
//version 1.0

package com.bookmystayapp;

import com.bookmystayapp.inventory.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();

        inventory.addRoomType("Single", 5, 2000);
        inventory.addRoomType("Double", 3, 3500);
        inventory.addRoomType("Suite", 2, 6000);

        System.out.println("Room Inventory:");
        inventory.displayInventory();

        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("\nPress Enter to skip updates, or type a room type to update: ");
            String type = sc.nextLine().trim();
            if (!type.isEmpty()) {
                System.out.print("New count for " + type + ": ");
                String countStr = sc.nextLine().trim();
                try {
                    int count = Integer.parseInt(countStr);
                    inventory.updateRoomCount(type, count);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number. Skipping update.");
                }
            }
        }

        System.out.println("\nFinal Inventory:");
        inventory.displayInventory();
    }
}
