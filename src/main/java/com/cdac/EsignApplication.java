package com.cdac;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@SpringBootApplication
public class EsignApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(EsignApplication.class, args);
	}
	

}
