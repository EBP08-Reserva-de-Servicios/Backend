package com.reservas.autenticacion.controller;

import com.reservas.autenticacion.service.AuthService;
import com.reservas.autenticacion.service.ResultadoLogin;
import com.reservas.autenticacion.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Textos del prototipo / contrato 14.2
    private static final String MSG_CREDENCIALES =
            "Correo o contraseña incorrectos. Verifica tus datos e intenta de nuevo.";
    private static final String MSG_BLOQUEADA =
            "Tu cuenta está bloqueada temporalmente. Intenta de nuevo en %d minutos.";

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    /** HU-02 / HU-Seg-02: 200 éxito, 400 incompleto, 401 credenciales/rol, 423 bloqueada. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginSolicitud solicitud) {
        ResultadoLogin r = authService.login(solicitud.correo(), solicitud.clave(), solicitud.rol());

        return switch (r.tipo()) {
            case EXITOSO -> ResponseEntity.ok(
                    new LoginRespuesta(r.cuenta(), tokenService.generar(r.cuenta())));
            case INCOMPLETO -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errores", r.errores()));
            case CREDENCIALES_INVALIDAS -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errores", List.of(MSG_CREDENCIALES)));
            case BLOQUEADA -> ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("errores", List.of(MSG_BLOQUEADA.formatted(r.minutosRestantes()))));
        };
    }
}