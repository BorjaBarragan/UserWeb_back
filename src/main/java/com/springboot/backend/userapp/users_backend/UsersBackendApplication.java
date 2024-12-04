package com.springboot.backend.userapp.users_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 4. UsersBackendApplication
// La clase principal que inicia la aplicación Spring Boot.

// PROPOSITO:
// Es el punto de entrada de la aplicación. Al ejecutar el método main, se lanza el servidor embebido y la configuración de Spring Boot.

@SpringBootApplication
public class UsersBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsersBackendApplication.class, args);
	}

}


