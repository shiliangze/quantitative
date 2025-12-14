package com.stu.quantitative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.stu.quantitative.jpa")  // repository 所在的包
@EntityScan(basePackages = "com.stu.quantitative.entity") // 实体所在的包
@EnableScheduling
public class QuantitativeApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuantitativeApplication.class, args);
	}

}
