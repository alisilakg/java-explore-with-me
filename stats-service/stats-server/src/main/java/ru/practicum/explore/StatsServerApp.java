package ru.practicum.explore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StatsServerApp {

    public static void main(String[] args) {
        //System.setProperty("server.port", "9090");
        SpringApplication.run(StatsServerApp.class, args);
    }

}