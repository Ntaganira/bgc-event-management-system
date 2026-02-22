package com.bgc.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BgcEventManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BgcEventManagementSystemApplication.class, args);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("Admin@123"));
	}

}
