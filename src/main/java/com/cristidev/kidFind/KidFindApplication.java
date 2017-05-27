package com.cristidev.kidFind;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.cristidev.kidFind.configuration.StorageProperties;
import com.cristidev.kidFind.service.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class KidFindApplication {

	public static void main(String[] args) {
		SpringApplication.run(KidFindApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}
}
