package org.ithub.mediastorageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MediaStorageServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MediaStorageServiceApplication.class, args);
	}
}
