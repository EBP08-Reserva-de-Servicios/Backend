package com.reservas.compartido.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Único PasswordEncoder de la aplicación.
 * Lo usan ClienteService y ProveedorService para cifrar y verificar contraseñas.
 * No declarar otro bean PasswordEncoder en ningún componente.
 */
@Configuration
public class CifradoConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}