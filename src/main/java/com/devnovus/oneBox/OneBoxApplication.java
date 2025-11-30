package com.devnovus.oneBox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OneBoxApplication {

	public static void main(String[] args) {
		SpringApplication.run(OneBoxApplication.class, args);
	}

}
