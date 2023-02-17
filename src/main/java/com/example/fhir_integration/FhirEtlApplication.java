package com.example.fhir_integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class FhirEtlApplication extends SpringBootServletInitializer  {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(FhirEtlApplication.class, args);
	}
}