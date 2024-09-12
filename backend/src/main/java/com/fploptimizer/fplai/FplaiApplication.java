package com.fploptimizer.fplai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class FplaiApplication {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(FplaiApplication.class, args);
    }

    @PostConstruct
    public void printActiveProfiles() {
        System.out.println("Active profiles: " + String.join(", ", environment.getActiveProfiles()));
    }
}
