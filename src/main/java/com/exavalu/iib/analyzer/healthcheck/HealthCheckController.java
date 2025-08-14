package com.exavalu.iib.analyzer.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 *
 * Last Updated: 2023-08-21 12:36
 *
 * Description: The `HealthCheckController` class checks the health of the
 * application and the database connection.
 */

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}" + "/health")
public class HealthCheckController {

	@Autowired
	private DatabaseHealthIndicator databaseHealthIndicator;

	// Endpoint to check the application health (including custom health indicators)
	@GetMapping
	public Health health() {
		return Health.up().withDetail("status", "Application is running").build();
	}

	// Endpoint to check the database connection
	@GetMapping("/db")
	public Health checkDatabaseConnection() {
		return databaseHealthIndicator.health();
	}

	@Component
	public static class DatabaseHealthIndicator implements HealthIndicator {

		private final EntityManager entityManager;

		public DatabaseHealthIndicator(EntityManager entityManager) {
			this.entityManager = entityManager;
		}

		@Override
		public Health health() {
			try {
				// Perform a simple query to check the database connection
				Query query = entityManager.createNativeQuery("SELECT 1");
				query.getSingleResult();
				return Health.up().build();
			} catch (Exception e) {
				// If there's an exception while querying, return the status as DOWN
				return Health.down().withDetail("error", e.getMessage()).build();
			}
		}
	}
}
