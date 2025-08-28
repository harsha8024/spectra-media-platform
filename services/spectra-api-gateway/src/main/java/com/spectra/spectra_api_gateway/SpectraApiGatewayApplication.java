package com.spectra.spectra_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpectraApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpectraApiGatewayApplication.class, args);
    }
}
