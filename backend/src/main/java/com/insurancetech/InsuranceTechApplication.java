package com.insurancetech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for InsuranceTech Claims Platform
 */
@SpringBootApplication
@EnableJpaAuditing
public class InsuranceTechApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceTechApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("InsuranceTech Claims Platform Started!");
        System.out.println("===========================================");
        System.out.println("API Documentation: http://localhost:8080/swagger-ui.html");
        System.out.println("Health Check: http://localhost:8080/actuator/health");
        System.out.println("===========================================\n");
    }
}
