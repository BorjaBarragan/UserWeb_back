package com.springboot.backend.userapp.users_backend.auth;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.springboot.backend.userapp.users_backend.auth.filter.JwtAuthenticationFilter;
import com.springboot.backend.userapp.users_backend.auth.filter.JwtValidationFilter;

// 2. SpringSecurityConfig
// Esta clase es la configuración principal de seguridad para la aplicación.

// PROPOSITO:
// Establecer las reglas de seguridad para la API, como permisos, manejo de sesiones y autenticación.

// PUNTOS CLAVE:
// AuthenticationManager y PasswordEncoder:
// AuthenticationManager: Componente que se encarga de autenticar usuarios mediante las credenciales proporcionadas.
// PasswordEncoder: Cifra contraseñas utilizando BCrypt, un algoritmo robusto y estándar para seguridad.
// SecurityFilterChain: Configura cómo se manejan las solicitudes HTTP

@Configuration
public class SpringSecurityConfig {

    // Inyectamos la configuración de autenticación de Spring.
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    // Define un bean que proporciona un gestor de autenticación (AuthenticationManager).
    // Este componente gestiona cómo los usuarios son autenticados.
    @Bean //Para registrar manualmente componentes específicos en el contexto de Spring.
    AuthenticationManager authenticationManager() throws Exception{
        //El método getAuthenticationManager() construye y devuelve una instancia del AuthenticationManager configurado.
        //Es usado por Spring Security para autenticar solicitudes entrantes.
        return authenticationConfiguration.getAuthenticationManager();
    }
    // Define un bean para cifrar contraseñas utilizando el algoritmo BCrypt.
    // Este es un estándar robusto para almacenar contraseñas de forma segura.
    @Bean //Para registrar manualmente componentes específicos en el contexto de Spring.
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Define el filtro de seguridad para manejar cómo las solicitudes HTTP son procesadas.
    @Bean
    //SecurityFilterChain Es una cadena de filtros que define cómo las solicitudes HTTP deben ser manejadas por Spring Security.
    //HttpSecurity Es una clase utilizada para configurar las reglas de seguridad web.
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        //authorizeHttpRequests metodo de HttpSecurity
        return http.authorizeHttpRequests(authz -> 
            authz
            .requestMatchers(HttpMethod.GET, "/api/users","/api/users/page/{page}").permitAll()
            .requestMatchers(HttpMethod.GET,"/api/users/{id}").hasAnyRole("USER","ADMIN")
            .requestMatchers(HttpMethod.POST,"/api/users").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT,"/api/users/{id}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE,"/api/users/{id}").hasRole("ADMIN")
            //  Bloquear todas las demas solicitudes sin autenticación.
            .anyRequest().authenticated())
            .cors(cors -> cors.configurationSource(configurationSource()))
            // Añade un filtro personalizado para manejar la autenticación JWT
            //Esto asegura que Spring Security ejecute este filtro cuando llegue una solicitud a /login 
            //(o cualquier otra URL configurada por defecto en UsernamePasswordAuthenticationFilter).
            //Inyecta el AuthenticationManager en el filtro. El AuthenticationManager es necesario 
            //para validar las credenciales del usuario durante la autenticación.
            .addFilter(new JwtAuthenticationFilter(authenticationManager()))
            .addFilter(new JwtValidationFilter(authenticationManager()))
            // Desactiva CSRF, ya que no es necesario para una API REST
            .csrf(config -> config.disable())
            // Configura la gestión de sesiones como "sin estado" (stateless), ideal para APIs REST
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Construye y devuelve la configuración de seguridad
            .build();
    }

    @Bean
    CorsConfigurationSource configurationSource(){
        // Se crea una nueva configuración de CORS
        CorsConfiguration config = new CorsConfiguration();
    
        // Permite todos los orígenes (dominios) para hacer solicitudes a la API
        config.setAllowedOriginPatterns(Arrays.asList("*"));  
        // Especifica que solo el dominio 'http://localhost:4200' está permitido para hacer solicitudes
        // Esto es más restrictivo que el anterior (el * permite todos los dominios)
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Especifica los métodos HTTP que se permiten en las solicitudes
        config.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE"));
    
        // Especifica qué cabeceras se permiten en las solicitudes
        // 'Authorization' es necesario para enviar tokens de autenticación, y 'Content-Type' para especificar el tipo de contenido de la solicitud
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));   
        // Permite el uso de credenciales (cookies, cabeceras de autenticación, etc.)
        config.setAllowCredentials(true);
        // Crea una fuente de configuración de CORS basada en URLs
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Registra la configuración de CORS para todas las rutas ('/**' es cualquier ruta)
        source.registerCorsConfiguration("/**", config);

        // Devuelve la fuente de configuración CORS
        return source;
    }
    
    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter(){
        // Crea un filtro de CORS, pasando la configuración definida anteriormente
        FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(configurationSource()));       
        // Establece que este filtro debe ejecutarse con la mayor prioridad posible
        // Esto garantiza que el filtro de CORS se ejecute antes que otros filtros en la cadena
        corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE);    
        // Devuelve el bean del filtro de CORS registrado
        return corsBean;
    }
}