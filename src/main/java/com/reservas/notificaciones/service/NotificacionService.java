package com.reservas.notificaciones.service;

import com.reservas.agendamiento.api.ReservaAgendada;
import com.reservas.catalogo.api.ConsultaOferta;
import com.reservas.catalogo.api.OfertaDetalle;
import com.reservas.clientes.api.ClientePerfil;
import com.reservas.clientes.api.ConsultaClientes;
import com.reservas.notificaciones.model.Notificacion;
import com.reservas.notificaciones.repository.NotificacionRepository;
import com.reservas.proveedores.api.ConsultaProfesionales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * HU-06 Notificar cita agendada: cada vez que un cliente agenda, la EMPRESA recibe una notificación.
 *
 * Escucha el evento ReservaAgendada DESPUÉS de que la reserva quedó guardada (AFTER_COMMIT) y guarda
 * la notificación en una transacción NUEVA, abierta y cerrada dentro del try. Así:
 *  - si la reserva falla (409, 404...), no se crea ninguna notificación;
 *  - si la notificación fallara (incluso al hacer commit), la cita del cliente NO se pierde y el
 *    cliente no recibe un error: solo queda el registro en el log.
 * Solo usa los api de agendamiento, clientes, catalogo y proveedores.
 */
@Service
public class NotificacionService {

    public static final String TIPO_CITA_AGENDADA = "CITA_AGENDADA";

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    private final NotificacionRepository notificacionRepository;
    private final ConsultaClientes consultaClientes;
    private final ConsultaOferta consultaOferta;
    private final ConsultaProfesionales consultaProfesionales;
    private final TransactionTemplate transaccionNueva;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               ConsultaClientes consultaClientes,
                               ConsultaOferta consultaOferta,
                               ConsultaProfesionales consultaProfesionales,
                               PlatformTransactionManager transacciones) {
        this.notificacionRepository = notificacionRepository;
        this.consultaClientes = consultaClientes;
        this.consultaOferta = consultaOferta;
        this.consultaProfesionales = consultaProfesionales;
        this.transaccionNueva = new TransactionTemplate(transacciones);
        this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alAgendarReserva(ReservaAgendada evento) {
        try {
            transaccionNueva.executeWithoutResult(estado -> crearNotificacion(evento));
        } catch (RuntimeException e) {
            // La cita ya quedó guardada: un error aquí no debe deshacerla ni llegarle al cliente; solo se registra.
            log.error("HU-06: no se pudo crear la notificación de la reserva {}", evento.codigo(), e);
        }
    }

    private void crearNotificacion(ReservaAgendada evento) {
        if (notificacionRepository.existsByReservaCodigoAndTipo(evento.codigo(), TIPO_CITA_AGENDADA)) {
            return;                                             // ya se avisó de esta reserva
        }
        Optional<OfertaDetalle> oferta = consultaOferta.obtenerOferta(evento.idServicioProveedor());
        if (oferta.isEmpty() || oferta.get().proveedor() == null) {
            log.warn("HU-06: la reserva {} no tiene oferta/empresa; no se crea notificación", evento.codigo());
            return;
        }
        Notificacion n = new Notificacion();
        n.setProveedorNit(oferta.get().proveedor().nit());
        n.setTipo(TIPO_CITA_AGENDADA);
        n.setMensaje(armarMensaje(evento, oferta.get()));
        n.setReservaCodigo(evento.codigo());
        n.setLeida(false);
        n.setFechaCreacion(LocalDateTime.now());
        notificacionRepository.save(n);
    }

    /**
     * "¡Nueva cita agendada! [cliente] identificado con [documento] reservó [servicio] con [profesional]
     *  para el [fecha] a las [hora]."  (texto exacto del contrato v1.2)
     * Si el bloque no tenía profesional (bloques antiguos), se omite " con [profesional]".
     */
    private String armarMensaje(ReservaAgendada evento, OfertaDetalle oferta) {
        Optional<ClientePerfil> cliente = consultaClientes.obtenerPerfil(evento.clienteId());
        String nombreCliente = cliente.map(c -> nombreCompleto(c.nombres(), c.apellidos())).orElse("Un cliente");
        String documento = cliente.map(ClientePerfil::numeroDocumento).orElse("documento no registrado");

        String servicio = (oferta.servicio() != null && oferta.servicio().nombre() != null)
                ? oferta.servicio().nombre() : "un servicio";

        String conProfesional = consultaProfesionales.obtenerProfesional(evento.profesionalId())
                .map(p -> " con " + nombreCompleto(p.nombres(), p.apellidos()))
                .orElse("");

        return "¡Nueva cita agendada! " + nombreCliente + " identificado con " + documento
                + " reservó " + servicio + conProfesional
                + " para el " + fechaEnTexto(evento.fecha()) + " a las " + horaEnTexto(evento.hora());   // "a. m." ya cierra la frase
    }

    private static String nombreCompleto(String nombres, String apellidos) {
        String n = (nombres == null) ? "" : nombres.trim();
        String a = (apellidos == null) ? "" : apellidos.trim();
        return (n + " " + a).trim();
    }

    /** 2026-11-12 -> "12 de noviembre de 2026" (sin depender del idioma del servidor). */
    static String fechaEnTexto(LocalDate fecha) {
        return fecha.getDayOfMonth() + " de " + MESES[fecha.getMonthValue() - 1] + " de " + fecha.getYear();
    }

    /** 09:00 -> "9:00 a. m."; 13:30 -> "1:30 p. m."; 12:00 -> "12:00 p. m."; 00:15 -> "12:15 a. m.". */
    static String horaEnTexto(LocalTime hora) {
        int h = hora.getHour() % 12;
        if (h == 0) {
            h = 12;
        }
        return h + ":" + String.format("%02d", hora.getMinute()) + (hora.getHour() < 12 ? " a. m." : " p. m.");
    }
}