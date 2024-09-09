package com.fploptimizer.fplai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class FplaiApplication {

    public static void main(String[] args) {SpringApplication.run(FplaiApplication.class, args);
    }

}
