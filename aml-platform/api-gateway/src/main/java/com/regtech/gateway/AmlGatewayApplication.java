package com.regtech.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// The scanBasePackages tells the Gateway to look for Spring Beans 
// in all of your other modules, not just the gateway module!
@SpringBootApplication(scanBasePackages = "com.regtech")
public class AmlGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmlGatewayApplication.class, args);
    }
}