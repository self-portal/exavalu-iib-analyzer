package com.exavalu.iib.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExavaluIibAnalyzerApplication {
	private static final Logger log = LoggerFactory.getLogger(ExavaluIibAnalyzerApplication.class);

	public static void main(String[] args) {
		log.info("Exavalu IIB Analyzer server has been started...");
		SpringApplication.run(ExavaluIibAnalyzerApplication.class, args);
	}
}
