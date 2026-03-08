package com.bookmystayapp.inventory;

import java.util.*;

public class ServiceManager {

    private Map<String, List<Service>> services = new HashMap<>();

    public void addService(String reservationId, Service service) {

        services
        .computeIfAbsent(reservationId, k -> new ArrayList<>())
        .add(service);

        System.out.println("Service added: " + service.getName());
    }

    public void showServices(String reservationId) {

        List<Service> list = services.get(reservationId);

        if(list == null) return;

        for(Service s : list)
            System.out.println(s.getName() + " " + s.getPrice());
    }
}