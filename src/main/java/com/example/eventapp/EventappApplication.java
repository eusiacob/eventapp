package com.example.eventapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EventappApplication {

	public static void main(String[] args) {

		SpringApplication.run(EventappApplication.class, args);
	}

}