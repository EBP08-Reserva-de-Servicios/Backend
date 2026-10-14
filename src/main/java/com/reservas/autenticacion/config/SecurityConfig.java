package com.reservas.autenticacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Seguridad de la aplicación.
 * A2:  el login entrega el token JWT.
 * A3a: el backend VERIFICA el token si llega (firma con JWT_SECRET, vencimiento y emisor "agendaya").
 * A3b: reglas por rol (HU-Seg-03), según la matriz 14.3 del contexto del equipo:
 *      - sin token en una ruta protegida -> 401; con token del rol equivocado (u otro NIT) -> 403;
 *      - lo que usa el front del Sprint 1 (lecturas y POST /api/reservas sin token) sigue ABIERTO
 *        (expandir y contraer: exigir token al agendar se hace cuando se despliegue el front nuevo).
 * Las reglas se evalúan en orden: de lo más específico a lo más general.
 */
@Configuration
public class SecurityConfig {

    private static final String ROL_CLIENTE = "ROLE_CLIENTE";
    private static final String ROL_PROVEEDOR = "ROLE_PROVEEDOR";

    @Bean
    public SecurityFilterChain filtrosDeSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                                  // API REST con token: no usa CSRF
            .cors(Customizer.withDefaults())                               // respeta el CorsConfig que ya existe
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // sin sesión: cada petición trae su token
            .authorizeHttpRequests(auth -> auth
                // 1) Registro y login: cualquiera
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/login",
                        "/api/clientes/registro",
                        "/api/proveedores/registro").permitAll()

                // 2) Gestión del proveedor (HU-19, HU-29, HU-32): solo el PROVEEDOR dueño de ese NIT
                .requestMatchers(HttpMethod.POST,
                        "/api/proveedores/{nit}/servicios",
                        "/api/proveedores/{nit}/profesionales",
                        "/api/proveedores/{nit}/profesionales/{profesionalId}/horario").access(proveedorDelNit())
                .requestMatchers(HttpMethod.DELETE,
                        "/api/proveedores/{nit}/profesionales/{profesionalId}/horario/{franjaId}").access(proveedorDelNit())

                // 3) Agendar (HU-24): sin token sigue abierto (front del Sprint 1); con token, solo CLIENTE
                .requestMatchers(HttpMethod.POST, "/api/reservas").access(sinTokenOCliente())

                // 4) Mis citas: el controller ya responde 401/403 con los textos del contrato (14.6)
                .requestMatchers(HttpMethod.GET, "/api/reservas/mias").permitAll()

                // 5) Escritura del catálogo general y de datos semilla: nadie (HU-Seg-03)
                .requestMatchers(HttpMethod.POST,
                        "/api/servicios",
                        "/api/proveedores",                                // registro legado del Sprint 1
                        "/api/servicio-proveedor",
                        "/api/disponibilidad",
                        "/api/departamentos",
                        "/api/municipios").denyAll()
                .requestMatchers(HttpMethod.PUT, "/api/servicios/**").denyAll()
                .requestMatchers(HttpMethod.DELETE, "/api/servicios/**").denyAll()

                // 6) Lista de profesionales de un proveedor: trae correo, teléfono y documento (datos personales),
                //    así que solo la ve el PROVEEDOR dueño. La foto y el horario siguen públicos (regla 7).
                //    El calendario del cliente no se afecta: usa ProfesionalResumen (solo el nombre).
                .requestMatchers(HttpMethod.GET, "/api/proveedores/{nit}/profesionales").access(proveedorDelNit())

                // 7) Lecturas públicas: buscar, disponibilidad, proveedores, servicios, ubicación, logo, foto, horario...
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // 8) Swagger, preflight de CORS y la página de error de Spring
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()

                // 9) Todo lo demás: cerrado
                .anyRequest().denyAll())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(rolesDesdeElToken())));
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

    /**
     * Por defecto Spring lee los permisos del claim "scope". Nuestro token trae el claim "rol"
     * ("CLIENTE" o "PROVEEDOR"): se convierte en ROLE_CLIENTE / ROLE_PROVEEDOR.
     */
    private static JwtAuthenticationConverter rolesDesdeElToken() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    /** Token de PROVEEDOR cuyo "sub" (su NIT) es igual al {nit} de la URL. Evita que un proveedor gestione a otro. */
    private static AuthorizationManager<RequestAuthorizationContext> proveedorDelNit() {
        return (autenticacion, contexto) -> {
            Authentication a = autenticacion.get();
            boolean permitido = a instanceof JwtAuthenticationToken jwt
                    && tieneRol(a, ROL_PROVEEDOR)
                    && jwt.getToken().getSubject() != null
                    && jwt.getToken().getSubject().equals(contexto.getVariables().get("nit"));
            return new AuthorizationDecision(permitido);
        };
    }

    /** Sin token (front del Sprint 1) o con token de CLIENTE. Un token de PROVEEDOR no puede agendar. */
    private static AuthorizationManager<RequestAuthorizationContext> sinTokenOCliente() {
        return (autenticacion, contexto) -> {
            Authentication a = autenticacion.get();
            boolean sinToken = !(a instanceof JwtAuthenticationToken);
            return new AuthorizationDecision(sinToken || tieneRol(a, ROL_CLIENTE));
        };
    }

    private static boolean tieneRol(Authentication a, String rol) {
        return a != null && a.getAuthorities().stream().anyMatch(g -> rol.equals(g.getAuthority()));
    }
}
