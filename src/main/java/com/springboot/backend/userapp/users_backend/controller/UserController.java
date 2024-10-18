package com.springboot.backend.userapp.users_backend.controller;

import org.springframework.web.bind.annotation.RestController;
import com.springboot.backend.userapp.users_backend.entities.User;
import com.springboot.backend.userapp.users_backend.services.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@CrossOrigin(origins = {"http://localhost:4200"})  // Permite que el frontend en Angular, que corre en localhost:4200, se comunique con el backend
@RestController  // Define que esta clase es un controlador REST
@RequestMapping("/api/users")  // Define que todas las rutas que maneja este controlador comienzan con "/api/users"
public class UserController {

    @Autowired  // Inyecta el servicio de usuarios que contiene la lógica de negocio (interacciones con la base de datos)
    private UserService service;

    // Método para obtener todos los usuarios (GET /api/users)
    @GetMapping
    public List<User> list(){
        return service.findAll();  // Llama al servicio para obtener todos los usuarios y los devuelve como una lista
    }

    // Método para buscar un usuario por su ID (GET /api/users/{id})
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        // Busca el usuario por ID
        Optional<User> userOptional = service.findById(id);
        
        // Si el usuario está presente, devuelve el usuario con estado HTTP 200 (OK)
        if(userOptional.isPresent()){
           return ResponseEntity.status(HttpStatus.OK).body(userOptional.orElseThrow());
        }
        // Si el usuario no se encuentra, devuelve un mensaje de error con estado HTTP 404 (NOT FOUND)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Collections.singletonMap("error", "El usuario no se ha encontrado, id: " + id));
    }

    // Método para crear un nuevo usuario (POST /api/users)
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user){
        // Guarda el usuario y devuelve el usuario creado con estado HTTP 201 (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    // Método para actualizar un usuario existente (PUT /api/users/{id})
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        // Busca el usuario por ID
        Optional<User> userOptional = service.findById(id);

        // Si el usuario está presente, lo actualiza con los nuevos datos
        if(userOptional.isPresent()){
           User userBd = userOptional.get();  // Obtiene el usuario de la base de datos
           userBd.setName(user.getName());  // Actualiza el nombre
           userBd.setLastName(user.getLastName());  // Actualiza el apellido
           userBd.setEmail(user.getEmail());  // Actualiza el email
           userBd.setUserName(user.getUserName());  // Actualiza el nombre de usuario
           userBd.setPassword(user.getPassword());  // Actualiza la contraseña
           return ResponseEntity.ok(service.save(userBd));  // Guarda el usuario actualizado y responde con estado HTTP 200 (OK)
        }
        // Si no se encuentra el usuario, devuelve estado HTTP 404 (NOT FOUND)
        return ResponseEntity.notFound().build();
    }

    // Método para eliminar un usuario por ID (DELETE /api/users/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        // Busca el usuario por ID
        Optional<User> userOptional = service.findById(id);        
        // Si el usuario está presente, lo elimina
        if(userOptional.isPresent()){
            service.deleteById(id);  // Elimina el usuario
            return ResponseEntity.noContent().build();  // Responde con estado HTTP 204 (NO CONTENT) sin cuerpo
        }
        // Si no se encuentra el usuario, devuelve estado HTTP 404 (NOT FOUND)
        return ResponseEntity.notFound().build();
    }
}
