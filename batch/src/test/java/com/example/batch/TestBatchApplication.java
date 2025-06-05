package com.example.batch;

import org.springframework.boot.SpringApplication;

public class TestBatchApplication {

	public static void main(String[] args) {
		SpringApplication.from(BatchApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
