# AgendaYa — API REST (Backend)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-blue.svg)](https://supabase.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Backend transaccional para la plataforma **AgendaYa** (EBP08), encargada de la gestión de catálogos, proveedores, disponibilidad temporal y reservas de citas en línea sin choques de horario.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 4.1.1
* **Persistencia & ORM:** Spring Data JPA / Hibernate
* **Base de Datos:** PostgreSQL (Alojada en Supabase)
* **Documentación de API:** Swagger UI / OpenAPI 3
* **Gestor de Dependencias:** Maven

---

## 🏗️ Arquitectura del Sistema

El proyecto está diseñado bajo una **Arquitectura en Capas (Layered Architecture)** para garantizar la separación de responsabilidades:

```text
src/main/java/co/edu/udea/agendaya/
├── controller/     # Capa de Presentación (Endpoints REST, DTOs y manejo de HTTP)
├── service/        # Capa de Lógica de Negocio (Validaciones de disponibilidad y choques)
├── repository/     # Capa de Persistencia (Interfaces Spring Data JPA)
├── entity/         # Capa de Dominio (Entidades Mapeadas con JPA / PostgreSQL)
└── config/         # Configuraciones de CORS, Swagger y Beans del sistema
```

---

## 🗄️ Modelo Relacional y Entidades

* `Departamento` / `Municipio`: Ubicación geográfica de los proveedores.
* `Servicio`: Catálogo general de servicios ofrecidos en la plataforma.
* `Proveedor`: Información comercial de los negocios (NIT, razón social, dirección, teléfono).
* `ServicioProveedor`: Entidad intermedia comercial (Servicio asignado a un Proveedor con precio específico).
* `Disponibilidad`: Bloques de horarios libres (`fecha`, `horaInicio`, `horaFin`) asociados a un `ServicioProveedor`.
* `Reserva`: Registros de agendamiento con código único UUID, fecha, hora y estado (`PENDIENTE`).

---

## 🚀 Instalación y Ejecución Local

### Prerrequisitos
* Java JDK 21 instalado.
* Instancia local de PostgreSQL o credenciales de conexión a PostgreSQL.

### Pasos de Configuración

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/EBP08-Reserva-de-Servicios/Backend.git](https://github.com/EBP08-Reserva-de-Servicios/Backend.git)
   cd Backend
   ```

2. **Configurar variables de entorno o propiedades:**
   Crea un archivo `src/main/resources/application-local.properties` (asegúrate de que esté ignorado en Git):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/reservas
   spring.datasource.username=tu_usuario
   spring.datasource.password=tu_contraseña
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Ejecutar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Acceder a la Documentación Interactiva (Swagger):**
   Abre en tu navegador: `http://localhost:8080/swagger-ui.html`

---

## 📌 Principales Endpoints de la API

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/ubicaciones/departamentos` | Lista departamentos y sus municipios. |
| `GET` | `/api/proveedores/buscar` | Buscador combinado por servicio, municipio, fecha y hora. |
| `GET` | `/api/disponibilidad/servicio-proveedor/{id}` | Consulta franjas libres de un servicio/proveedor. |
| `POST` | `/api/reservas` | Crea una reserva validando choque de horarios (409 Conflict si está ocupado). |

---

## 🔍 Validación de Calidad y Pruebas

El flujo de reservas fue validado mediante casos de prueba en Swagger UI y Postman:
1. **Caso de Éxito:** Agendamiento en franja libre devuelve código `200 OK` y el objeto `Reserva` con estado `PENDIENTE`.
2. **Caso de Conflicto (Choque):** Intentar reservar exactamente la misma franja horaria para el mismo `ServicioProveedor` rechaza la transacción con código `409 Conflict`.

---

## ⚠️ Limitaciones Conocidas y Pendientes (Sprint 2)

* **Marca de Disponibilidad:** Al crearse una reserva, la entidad `Disponibilidad` no cambia automáticamente su estado a `RESERVADO` (la prevención de duplicados actúa en la consulta previa de reservas).
* **Consultas por Rango:** La API de disponibilidad consulta por fecha exacta; no existe aún endpoint por rango de días.
* **Estados Inactivos:** Los estados `CONFIRMADA` y `CANCELADA` en `Reserva` están modelados pero no expuestos para cambio de estado por parte del proveedor (programado para el módulo de administración en Sprint 2).
* **Despliegue Gratuito:** La instancia en Render puede entrar en modo de suspensión tras 15 minutos de inactividad, tardando cerca de 60 segundos en responder en la primera petición.

---

## 🔗 Enlaces Relacionados
* [Repositorio Frontend](https://github.com/EBP08-Reserva-de-Servicios/Frontend)
* [README General de la Organización](https://github.com/EBP08-Reserva-de-Servicios/.github)
