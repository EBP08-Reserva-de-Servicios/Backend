package com.reservas.clientes.controller;

import com.reservas.clientes.api.ClienteResumen;
import com.reservas.clientes.model.Cliente;
import com.reservas.clientes.service.ClienteService;
import com.reservas.clientes.service.RegistroClienteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Cliente cliente = new Cliente();
        cliente.setNombres(solicitud.nombres());
        cliente.setApellidos(solicitud.apellidos());
        cliente.setTipoDocumento(solicitud.tipoDocumento());
        cliente.setNumeroDocumento(solicitud.numeroDocumento());
        cliente.setTelefono(solicitud.telefono());
        cliente.setCorreo(solicitud.correo());

        try {
            Cliente guardado = clienteService.registrarCliente(cliente, solicitud.clave());
            ClienteResumen resumen = new ClienteResumen(
                    guardado.getApellidos(), guardado.getId(), guardado.getNombres());
            return ResponseEntity.status(HttpStatus.CREATED).body(resumen);

        } catch (RegistroClienteException e) {
            HttpStatus estado = (e.getTipo() == RegistroClienteException.Tipo.DUPLICADO)
                    ? HttpStatus.CONFLICT        // 409: correo o documento ya registrados
                    : HttpStatus.BAD_REQUEST;    // 400: datos que no cumplen las reglas
            return ResponseEntity.status(estado).body(Map.of("errores", e.getErrores()));
        }
    }
}