package com.springboot.backend.userapp.users_backend.controller;

import org.springframework.web.bind.annotation.RestController;
import com.springboot.backend.userapp.users_backend.entities.User;
import com.springboot.backend.userapp.users_backend.services.UserService;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@CrossOrigin(origins = { "http://localhost:4200" }) // Permite que el frontend en Angular, que corre en localhost:4200,
                                                    // se comunique con el backend
@RestController // Define que esta clase es un controlador REST
@RequestMapping("/api/users") // Define que todas las rutas que maneja este controlador comienzan con
                              // "/api/users"
public class UserController {

    @Autowired
    // Inyecta el servicio de usuarios que contiene la lógica de negocio
    // (interacciones con la base de datos)
    // Hay que inyectar lo mas generico posible
    private UserService service;

    // Método para obtener todos los usuarios (GET /api/users)
    @GetMapping
    public List<User> list() {
        // Llama al servicio para obtener todos los usuarios y los devuelve como una lista
        return service.findAll();                            
    }

    // Maneja solicitudes GET en la ruta "/api/users/page/{page}"
    @GetMapping("/page/{page}")
    public Page<User> listPageable(@PathVariable Integer page) {
        // Crea un objeto Pageable para solicitar la página especificada con 5 usuarios por página
        Pageable pageable = PageRequest.of(page, 5);
        // Llama al servicio para obtener la lista de usuarios de la página solicitada
        return service.findAll(pageable);
    }

    // Método para buscar un usuario por su ID (GET /api/users/{id})
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        // Busca el usuario por ID
        Optional<User> userOptional = service.findById(id);
        // Si el usuario está presente, devuelve el usuario con estado HTTP 200 (OK)
        if (userOptional.isPresent()) {
            // orElseThrow() controla en caso de que userOptional no contenga nada
            // en este contexto no es necesario ya que en el if ya se controla si esta
            // presente o no
            // El ResponseEntity convierte el objeto en JSON
            return ResponseEntity.status(HttpStatus.OK).body(userOptional.orElseThrow());
        }
        // Si el usuario no se encuentra, devuelve un mensaje de error con estado HTTP
        // 404 (NOT FOUND)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "El usuario no se ha encontrado, id: " + id));
    }

    // Método para crear un nuevo usuario (POST /api/users)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    // Método para actualizar un usuario existente (PUT /api/users/{id})
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody User user, BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return validation(result);
        }
        // Busca el usuario por ID
        Optional<User> userOptional = service.findById(id);
        // Si el usuario está presente, lo actualiza con los nuevos datos
        if (userOptional.isPresent()) {
            User userBd = userOptional.get(); // Obtiene el usuario de la base de datos
            userBd.setName(user.getName()); // Actualiza el nombre
            userBd.setLastName(user.getLastName()); // Actualiza el apellido
            userBd.setEmail(user.getEmail()); // Actualiza el email
            userBd.setUserName(user.getUserName()); // Actualiza el nombre de usuario
            userBd.setPassword(user.getPassword()); // Actualiza la contraseña
            return ResponseEntity.ok(service.save(userBd)); // Guarda el usuario actualizado y responde con estado HTTP
                                                            // 200 (OK)
        }
        // Si no se encuentra el usuario, devuelve estado HTTP 404 (NOT FOUND)
        return ResponseEntity.notFound().build();
    }

    // Método para eliminar un usuario por ID (DELETE /api/users/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // Busca el usuario por ID
        Optional<User> userOptional = service.findById(id);
        // Si el usuario está presente, lo elimina
        if (userOptional.isPresent()) {
            service.deleteById(id); // Elimina el usuario
            return ResponseEntity.noContent().build(); // Responde con estado HTTP 204 (NO CONTENT) sin cuerpo
        }
        // Si no se encuentra el usuario, devuelve estado HTTP 404 (NOT FOUND)
        return ResponseEntity.notFound().build();
    }

    // Método privado que se encarga de validar los resultados de la vinculación de
    // datos
    // y devuelve una respuesta HTTP que incluye los errores de validación si los
    // hay.
    private ResponseEntity<?> validation(BindingResult result) {
        // Crea un nuevo mapa que almacenará los mensajes de error de validación
        // 'Map<String, String>' indica que el mapa contendrá pares clave-valor,
        // donde la clave es un String (nombre del campo) y el valor es otro String
        // (mensaje de error).
        Map<String, String> errors = new HashMap<>();
        // Itera sobre cada error de campo obtenido del resultado de la validación
        // 'result.getFieldErrors()' devuelve una lista de objetos 'FieldError'
        // que representan cada error en los campos de un objeto.
        result.getFieldErrors().forEach(error -> {
            // 'error.getField()' devuelve el nombre del campo que falló en la validación.
            // 'error.getDefaultMessage()' devuelve el mensaje de error predeterminado
            // asociado a esa validación fallida.
            // Agrega un nuevo par clave-valor al mapa de errores
            // La clave es el nombre del campo que falló en la validación
            // (error.getField()).
            // El valor es un mensaje que indica el problema con ese campo
            // (error.getDefaultMessage()).
            errors.put(error.getField(), "El campo " + error.getField() + " " + error.getDefaultMessage());
        });
        // Devuelve una respuesta HTTP con el estado 400 (Bad Request) y el cuerpo
        // conteniendo los errores. Esto permite al cliente (frontend) recibir
        // información sobre qué salió mal en la solicitud.
        return ResponseEntity.badRequest().body(errors);
    }

}
