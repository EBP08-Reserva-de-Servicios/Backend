package com.reservas.autenticacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la aplicación (A2).
 * Por ahora TODO queda abierto para no romper el frontend en producción.
 * Las reglas por rol (HU-Seg-03) se agregan en A3.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtrosDeSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())              // API REST con token: no usa CSRF
            .cors(Customizer.withDefaults())           // respeta el CorsConfig que ya existe
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
