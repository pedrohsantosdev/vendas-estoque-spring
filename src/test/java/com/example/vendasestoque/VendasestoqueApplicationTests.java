package com.example.vendasestoque;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest(
		classes = VendasestoqueApplication.class,
		properties = "spring.jpa.hibernate.ddl-auto=create"
)
@ActiveProfiles("test")
@Testcontainers
class VendasestoqueApplicationTests {

	@Container
	@ServiceConnection
	static MySQLContainer mysql = new MySQLContainer("mysql:8.0.36")
			.withDatabaseName("inicializacao_test")
			.withUsername("test")
			.withPassword("test");

	@Test
	void contextLoads() {

	}
}