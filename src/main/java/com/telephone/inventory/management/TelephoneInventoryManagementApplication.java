package com.telephone.inventory.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.common.models.model")
@EnableJpaRepositories(basePackages = "com.telephone.inventory.management.repository")
public class TelephoneInventoryManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelephoneInventoryManagementApplication.class, args);
	}
}
