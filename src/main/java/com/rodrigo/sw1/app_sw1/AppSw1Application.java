package com.rodrigo.sw1.app_sw1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AppSw1Application {

	public static void main(String[] args) {
		SpringApplication.run(AppSw1Application.class, args);
	}

}
