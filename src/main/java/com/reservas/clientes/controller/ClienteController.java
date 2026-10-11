package com.reservas.clientes.controller;

import com.reservas.clientes.api.ClienteResumen;
import com.reservas.clientes.model.Cliente;
import com.reservas.clientes.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody ClienteRegistroSolicitud solicitud) {
        List<String> errores = new ArrayList<>();

        if (solicitud.nombres() == null || solicitud.nombres().isBlank()) errores.add("El nombre es obligatorio.");
        if (solicitud.apellidos() == null || solicitud.apellidos().isBlank()) errores.add("El apellido es obligatorio.");
        if (solicitud.correo() == null || solicitud.correo().isBlank()) errores.add("El correo es obligatorio.");
        if (solicitud.clave() == null || solicitud.clave().isBlank()) errores.add("La contraseña es obligatoria.");

        // Validaciones de las 5 reglas de contraseña
        String clave = solicitud.clave();
        if (clave.length() < 8) errores.add("La contraseña debe tener al menos 8 caracteres.");
        if (!clave.matches(".*[A-Z].*")) errores.add("La contraseña debe incluir al menos una letra mayúscula.");
        if (!clave.matches(".*[a-z].*")) errores.add("La contraseña debe incluir al menos una letra minúscula.");
        if (!clave.matches(".*\\d.*")) errores.add("La contraseña debe incluir al menos un número.");
        if (!clave.matches(".*[^A-Za-z0-9].*")) errores.add("La contraseña debe incluir al menos un carácter especial.");

        if (!errores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errores", errores));
        }

        // Validar duplicados de correo o documento
        if (clienteService.findByCorreo(solicitud.correo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errores", List.of("Este correo ya está registrado.")));
        }

        Cliente cliente = new Cliente();
        cliente.setNombres(solicitud.nombres());
        cliente.setApellidos(solicitud.apellidos());
        cliente.setTipoDocumento(solicitud.tipoDocumento());
        cliente.setNumeroDocumento(solicitud.numeroDocumento());
        cliente.setTelefono(solicitud.telefono());
        cliente.setCorreo(solicitud.correo());

        Cliente guardado = clienteService.registrarCliente(cliente, solicitud.clave());

        ClienteResumen resumen = new ClienteResumen(
            guardado.getApellidos(),
            guardado.getId(),
            guardado.getNombres()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
    }
}