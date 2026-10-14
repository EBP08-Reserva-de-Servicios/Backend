package com.reservas.autenticacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Seguridad de la aplicación.
 * A2: todo abierto. A3 (paso 1): el backend VERIFICA el token JWT si llega
 * (firma con JWT_SECRET, vencimiento e emisor "agendaya"), pero todavía no exige token en ningún endpoint.
 * Las reglas por rol (HU-Seg-03) se agregan en el siguiente paso de A3.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtrosDeSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                                  // API REST con token: no usa CSRF
            .cors(Customizer.withDefaults())                               // respeta el CorsConfig que ya existe
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // sin sesión: cada petición trae su token
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())  // por ahora, todo abierto
            .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));  // lee y verifica "Authorization: Bearer ..."
        return http.build();
    }

    /** Verifica los tokens con la MISMA clave con la que TokenService los firma. */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secreto}") String secreto) {
        SecretKey clave = new SecretKeySpec(Base64.getDecoder().decode(secreto), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("agendaya"));  // vencimiento + emisor
        return decoder;
    }
}