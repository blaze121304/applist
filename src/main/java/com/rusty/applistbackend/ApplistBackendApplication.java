package com.rusty.applistbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ApplistBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplistBackendApplication.class, args);
    }

}
