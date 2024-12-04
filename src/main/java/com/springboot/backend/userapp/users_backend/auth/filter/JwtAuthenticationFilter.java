package com.springboot.backend.userapp.users_backend.auth.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.backend.userapp.users_backend.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static com.springboot.backend.userapp.users_backend.auth.TokenJwtConfig.*;

// 3. JwtAuthenticationFilter
// Un filtro personalizado que gestiona la autenticación con tokens JWT.

// PROPOSITO:
// Interceptar las solicitudes de inicio de sesión para autenticar al usuario y generar un token JWT si las credenciales son válidas.

// PUNTOS CLAVE:

// 3.1 Método attemptAuthentication: 
// Lee las credenciales del cuerpo de la solicitud (JSON con userName y password).
// Crea un token de autenticación (UsernamePasswordAuthenticationToken).
// Usa el AuthenticationManager para autenticar al usuario con las credenciales proporcionadas.

// 3.2 Método successfulAuthentication:
// Genera un token JWT si la autenticación es exitosa:
// Contiene información del usuario (como roles y nombre).
// Usa la clave secreta (SECRET_KEY) para firmar el token.
// Agrega el token al encabezado de la respuesta HTTP (Authorization: Bearer <token>).
// Devuelve una respuesta JSON con el token y un mensaje de éxito.

// 3.3 Método unsuccessfulAuthentication:
// Responde con un error (401) si las credenciales son incorrectas.
// No se da información específica sobre qué falló (nombre de usuario o contraseña), por razones de seguridad.

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // 1. Atributo: Este es el AuthenticationManager que se utilizará para autenticar al usuario.
    //AuthenticationManager se utiliza cuando hay que autenticar.
    private AuthenticationManager authenticationManager;

    // 2. Constructor: Este constructor recibe el AuthenticationManager y lo asigna al atributo.
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // 3. attemptAuthentication: Este es el método más importante. Se ejecuta cuando se recibe una solicitud de autenticación.
    // Aquí es donde leemos las credenciales del usuario (nombre de usuario y contraseña) y las verificamos.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String userName = null;
        String password = null;

        // 4. Intentamos leer el cuerpo de la solicitud HTTP, que contiene el nombre de
        // usuario y la contraseña enviados en formato JSON.
        try {
            // Usamos ObjectMapper para deserializar el cuerpo de la solicitud en un objeto User.
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            userName = user.getUserName(); // Extraemos el nombre de usuario del objeto User.
            password = user.getPassword(); // Extraemos la contraseña del objeto User.
        } catch (StreamReadException e) {
            e.printStackTrace(); // Manejo de excepciones específicas de lectura del stream de datos.
        } catch (DatabindException e) {
            e.printStackTrace(); // Manejo de excepciones al procesar el mapeo de datos JSON a la clase User.
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de excepciones generales relacionadas con entrada/salida.
        }

        // 5. Creamos un token de autenticación con las credenciales extraídas (nombre de usuario y contraseña).
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName,password);
        // 6. Usamos el AuthenticationManager para autenticar al usuario con el token de autenticación creado anteriormente.
        // Esto verificará las credenciales contra las implementaciones de autenticación configuradas.
        return this.authenticationManager.authenticate(authenticationToken);
    }

    // 7. successfulAuthentication: Este método se ejecuta si la autenticación fue exitosa.
    // Aquí es donde normalmente generaríamos el JWT y lo devolveríamos al cliente,pero no está implementado en este caso.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws IOException, ServletException {

        // Obtenemos el usuario autenticado desde el objeto `authentication`.
        // `authentication.getPrincipal()` devuelve el principal asociado al usuario autenticado,
        // que en este caso es un objeto `User` de Spring Security.
        //realizamos un cast para decirle al programa que el principal será de tipo User
        //y asi acceder a sus metodos (getUsername()..)
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication
                .getPrincipal();

        // Extraemos el nombre de usuario del objeto User.
        String userName = user.getUsername();

        //getAuthorities(): Es un método de la interfaz Authentication que devuelve los roles asociados al usuario autenticado.
        //estos roles se representan con GrandedAuthority
        Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();
        boolean isAdmin = roles.stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        // Los claims son una forma segura de enviar datos del usuario en el token.
        Claims claims = Jwts
                .claims() // Crea un contenedor vacío para los claims.
                .add("authorities", new ObjectMapper().writeValueAsString(roles)) 
                // Los pasamos a string JSON, para enviarlo dentro del token
                .add("username", userName)// Añade el nombre de usuario al claim "username".
                .add("isAdmin" , isAdmin) //Añadimos la validacion del true o el false.
                .build(); // Construimos el claim

        // Generamos un token JWT utilizando la librería JJWT.
        // Aquí configuramos los valores esenciales del token:
        String jwt = Jwts.builder()
                .subject(userName) // Asignamos el nombre de usuario como sujeto del token.
                .claims(claims)
                .signWith(SECRET_KEY) // Firmamos el token con la clave secreta (SECRET_KEY).
                .issuedAt(new Date()) // Indicamos la fecha/hora en que el token fue emitido.
                .expiration(new Date(System.currentTimeMillis() + 3600000)) 
                // Establecemos el tiempo de expiración del token (1 hora).
                .compact(); 
                // Compactamos la configuración para obtener el JWT final como una cadena String.

        // Añadimos el token generado al encabezado de la respuesta HTTP. Se utiliza el
        // formato estándar de "Authorization: Bearer <token>".
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + jwt);

        // Creamos un mapa para construir un cuerpo de respuesta en formato JSON.
        Map<String, String> body = new HashMap<>();  
        body.put("token", jwt);
        body.put("username", userName);
        body.put("message", String.format("Hola %s has iniciado sesión con éxito", userName));
        // ObjectMapper convierte objetos de Java (como mapas o listas) en una cadena JSON.
        // writeValueAsString genera esta cadena JSON.
        // response.getWriter().write(...) envía la cadena JSON al cliente como el cuerpo de la respuesta HTTP.
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        // Configuramos el tipo de contenido de la respuesta como JSON.
        response.setContentType(CONTENT_TYPE);
        // Configuramos el estado HTTP de la respuesta.
        response.setStatus(200);
    }

    // 8. unsuccessfulAuthentication: Este método se ejecuta si la autenticación falla.
    // Aquí es donde podríamos devolver un código de error, como 401 (No autorizado).
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {

        Map<String, String> body = new HashMap<>();  
        body.put("message", String.format("Error en la autenticación con username y password incorrecto!"));    // no indicar nunca que es lo que esta incorrecto.
        body.put("error", failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(CONTENT_TYPE);
        response.setStatus(401);
    }
}
