package com.sfr.tokyo.sfr_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.sfr.tokyo.sfr_backend.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class SfrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfrBackendApplication.class, args);
    }

}
