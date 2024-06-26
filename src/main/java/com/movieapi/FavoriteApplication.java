package com.movieapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FavoriteApplication {
	private static final Logger logger = LoggerFactory.getLogger(FavoriteApplication.class);
	public static void main(String[] args) {
        SpringApplication.run(FavoriteApplication.class, args);
	}
}
