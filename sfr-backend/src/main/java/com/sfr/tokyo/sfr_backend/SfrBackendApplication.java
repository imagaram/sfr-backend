package com.sfr.tokyo.sfr_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

import com.sfr.tokyo.sfr_backend.config.FileStorageProperties;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "SFR Backend API", version = "v1", description = "SFR プロトコル (ガバナンス/報酬/暗号資産) の公式バックエンド API 仕様", contact = @Contact(name = "SFR Team", email = "dev@sfr.tokyo"), license = @License(name = "Proprietary")), servers = {
        @Server(url = "http://localhost:8080", description = "Local Dev"),
        @Server(url = "https://api.sfr.tokyo", description = "Production")
})
@EnableConfigurationProperties({
        FileStorageProperties.class
})
@EnableScheduling
@EntityScan(basePackages = {
        "com.sfr.tokyo.sfr_backend.entity",
        "com.sfr.tokyo.sfr_backend.entity.crypto",
        "com.sfr.tokyo.sfr_backend.user"
})
public class SfrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfrBackendApplication.class, args);
    }

}
