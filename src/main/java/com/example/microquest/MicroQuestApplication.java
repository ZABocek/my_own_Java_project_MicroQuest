package com.example.microquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the MicroQuest Spring Boot application.
 * <p>
 * {@code @SpringBootApplication} triggers component scanning, auto-configuration,
 * and {@code @Configuration} processing for the entire {@code com.example.microquest}
 * package tree. Spring Boot then bootstraps an embedded Tomcat server and starts
 * serving HTTP traffic on the port defined in {@code application.properties}
 * (default 8080).
 * </p>
 */
@SpringBootApplication
public class MicroQuestApplication {

    /**
     * JVM entry point — delegates immediately to Spring Boot's launcher.
     *
     * @param args optional command-line arguments forwarded to the Spring
     *             {@code ApplicationContext} (e.g. {@code --server.port=9090})
     */
    public static void main(String[] args) {
        SpringApplication.run(MicroQuestApplication.class, args);
    }
}
