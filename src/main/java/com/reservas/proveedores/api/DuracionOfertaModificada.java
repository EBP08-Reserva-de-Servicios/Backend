package com.reservas.proveedores.api;

/**
 * HU-26: evento que publica catalogo cuando un proveedor cambia la DURACIÓN de una oferta.
 * Lo escucha proveedores (HorarioService) para regenerar los bloques libres de los profesionales
 * que atienden esa oferta. Vive en proveedores.api para conservar la dirección de dependencias
 * (catalogo ya depende de proveedores.api; proveedores nunca importa catalogo).
 */
public record DuracionOfertaModificada(Long servicioProveedorId) {
}
