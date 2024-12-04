package com.springboot.backend.userapp.users_backend.auth.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.backend.userapp.users_backend.auth.SimpleGrantedAuthorityJsonCreator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import static com.springboot.backend.userapp.users_backend.auth.TokenJwtConfig.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


//  Filtro personalizado para validar un token JWT en cada solicitud entrante.
//  Extiende BasicAuthenticationFilter de Spring Security. 
public class JwtValidationFilter extends BasicAuthenticationFilter {

    // Constructor que recibe el AuthenticationManager.
    // Este objeto se utiliza para la autenticación de usuarios en Spring Security.
    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    
    // Método principal para realizar la validación del token JWT.
    // Se ejecuta en cada solicitud antes de llegar al controlador.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Obtener el valor del encabezado de autorización.
        String header = request.getHeader(HEADER_AUTHORIZATION);

        // Si el encabezado es nulo o no comienza con el prefijo esperado, continuar con el siguiente filtro.
        if (header == null || !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }

        // Extraer el token eliminando el prefijo (por ejemplo, "Bearer ").
        String token = header.replace(PREFIX_TOKEN, "");

        try {
            // Parsear el token JWT para obtener los "claims" (información contenida en el token).
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY) // Validar la firma del token con la clave secreta.
                    .build()
                    .parseSignedClaims(token) // Analizar y verificar el token firmado.
                    .getPayload();

            // Obtener el nombre del usuario (subject) del token.
            String userName = claims.getSubject();

            // Extraer los roles o autoridades del token.
            Object authoritiesClaims = claims.get("authorities");

            // Convertir los roles a una colección de objetos GrantedAuthority.
            Collection<? extends GrantedAuthority> roles = Arrays.asList(new ObjectMapper()
                    .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class)
                    .readValue(authoritiesClaims.toString().getBytes(), SimpleGrantedAuthority[].class));

            // Crear un token de autenticación para Spring Security.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, null, roles);

            // Establecer el contexto de seguridad con el token de autenticación.
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Continuar con la cadena de filtros.
            chain.doFilter(request, response);

        } catch (JwtException e) {
            // Si hay un error al procesar el token, devolver un error 401 (no autorizado).
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage()); // Mensaje técnico del error.
            body.put("message", "El token es inválido!"); // Mensaje más amigable para el usuario.

            // Configurar la respuesta HTTP con el error.
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(401); // Código de estado HTTP 401 (no autorizado).
            response.setContentType(CONTENT_TYPE); // Establecer el tipo de contenido de la respuesta.
        }
    }
}
