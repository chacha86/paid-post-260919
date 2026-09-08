package com.rest1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Rest1Application {

    public static void main(String[] args) {
        SpringApplication.run(Rest1Application.class, args);
    }

}
