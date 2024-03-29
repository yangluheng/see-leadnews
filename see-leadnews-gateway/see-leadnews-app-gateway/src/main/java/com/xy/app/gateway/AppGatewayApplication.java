package com.xy.app.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *
 * @author 杨路恒
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AppGatewayApplication {
    public static void main( String[] args ) {
        SpringApplication.run(AppGatewayApplication.class, args);
    }
}
