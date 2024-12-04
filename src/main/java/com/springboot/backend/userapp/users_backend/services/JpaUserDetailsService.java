package com.springboot.backend.userapp.users_backend.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.backend.userapp.users_backend.entities.User;
import com.springboot.backend.userapp.users_backend.repositories.UserRepository;

// 1. JpaUserDetailsService
// Esta clase implementa UserDetailsService, una interfaz central en Spring Security
// para cargar datos de usuario durante el proceso de autenticación.
// PROPOSITO:
// Permitir que Spring Security obtenga la información del usuario desde la base de datos (por medio de UserRepository).
// PUNTOS CLAVE:
// Inyección de UserRepository: Se usa para buscar usuarios en la base de datos por su nombre de usuario.

//UserDetailsService se utiliza para cargar los detalles de un usuario (como nombre, contraseña y roles) durante el proceso de autenticación.
//Necesario inyectar la dependencia de UserDetailsService
@Service 
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repository; 

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        // Busca al usuario en la base de datos utilizando el método findByUsername del repositorio.
        // Usar Optional te obliga a manejar explícitamente la posibilidad de que el valor no exista, 
        // evitando errores en tiempo de ejecución.
        Optional<User> optionalUser = repository.findByUserName(userName);

        // Si el usuario no existe en la base de datos, lanza una excepción con un mensaje personalizado.
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(
                    //string.format tiene otras especificaciones... %s texto / %d enteros etc...                  
                    String.format("El usuario con username '%s' no existe en el sistema", userName));
        }
        // Si el usuario existe, lo extraemos del Optional.o orElseThrow es lo mismo que .get()
        User user = optionalUser.orElseThrow(); // Esto nunca falla aquí porque ya verificamos si está vacío.

        // Convertimos los roles del usuario en una lista de GrantedAuthority.
        // GrantedAuthority es una interfaz que representa permisos o roles en Spring Security.     
        // user.getRoles().stream(): convierte la lista de roles del usuario (user.getRoles()) en un Stream.
        // el stream() permite aplicar operaciones funcionales sobre los roles. en este caso .map()
        List<GrantedAuthority> authorities = user.getRoles()
                .stream()
                //new SimpleGrantedAuthority(...): Crea un objeto de tipo SimpleGrantedAuthority,
                //que es la implementación de la interfaz GrantedAuthority utilizada por Spring Security.
                .map(role -> new SimpleGrantedAuthority(role.getName())) 
                // Colecta los resultados en una lista. (convierte los resultados en una lista de roles).
                .collect(Collectors.toList()); 

        // Creamos un objeto UserDetails utilizando la implementación proporcionada por Spring Security.
        // Este objeto será usado por Spring Security para gestionar la autenticación.
        return new org.springframework.security.core.userdetails.User(
                userName,                   // El nombre de usuario.
                user.getPassword(),         // La contraseña del usuario (normalmente cifrada).
                true,               // Indica si la cuenta está habilitada.
                true,     // Indica si la cuenta no ha expirado.
                true, // Indica si las credenciales no han expirado.
                true,      // Indica si la cuenta no está bloqueada.
                authorities);    
    }
}

