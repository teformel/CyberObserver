package com.cyber.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CyberServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CyberServerApplication.class, args);
        System.out.println(">>> CyberObserver Brain Online <<<");
    }
}
