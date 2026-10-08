package com.govscheme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GovSchemeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovSchemeApplication.class, args);
        System.out.println("==========================================================");
        System.out.println(" Government Scheme Recommendation System Started Successfully ");
        System.out.println(" Backend Running on: http://localhost:8080");
        System.out.println(" Rule-Based Recommendation Engine Initialized ");
        System.out.println("==========================================================");
    }
}
